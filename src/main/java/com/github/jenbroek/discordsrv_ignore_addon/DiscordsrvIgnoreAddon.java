package com.github.jenbroek.discordsrv_ignore_addon;

import com.github.jenbroek.discordsrv_ignore_addon.cmd.CmdIgnore;
import com.github.jenbroek.discordsrv_ignore_addon.cmd.CmdIgnorelist;
import com.github.jenbroek.discordsrv_ignore_addon.cmd.CmdToggle;
import com.github.jenbroek.discordsrv_ignore_addon.data.JedisSimpleMap;
import com.github.jenbroek.discordsrv_ignore_addon.data.JedisSimpleSet;
import com.github.jenbroek.discordsrv_ignore_addon.data.SimpleMap;
import com.github.jenbroek.discordsrv_ignore_addon.data.SimpleSet;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreBroadcastEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisAccessControlException;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

public final class DiscordsrvIgnoreAddon extends JavaPlugin implements Listener {

	private UnifiedJedis jedis;
	private boolean redisReady = true;
	private final Object redisReadyLock = new Object();

	private JedisSimpleSet<UUID> unsubscribed;
	private JedisSimpleMap<UUID, Set<String>> hasIgnored;

	// XXX workaround for lack of author in DiscordGuildMessagePreBroadcastEvent (see below)
	private final IdentityHashMap<Component, String> cachedAuthors = new IdentityHashMap<>();

	@Override
	public void onEnable() {
		DiscordSRV.api.subscribe(this);

		Objects.requireNonNull(getCommand("discordtoggle")).setExecutor(new CmdToggle(this));
		Objects.requireNonNull(getCommand("discordignore")).setExecutor(new CmdIgnore(this));
		Objects.requireNonNull(getCommand("discordignorelist")).setExecutor(new CmdIgnorelist(this));

		saveDefaultConfig();

		try {
			jedis = initializeRedis(getConfig());
			jedis.ping();
		} catch (JedisConnectionException e) {
			getLogger().severe("Failed to connect to Redis during setup: " + e.getMessage());
			getLogger().warning("Disabling plugin...");
			redisReady = false;

			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		unsubscribed = new JedisSimpleSet<>(new HashSet<>(), jedis, "unsubscribed", UUID::toString, UUID::fromString);
		hasIgnored = new JedisSimpleMap<>(
			new HashMap<>(),
			jedis,
			"ignored",
			UUID::toString,
			UUID::fromString,
			s -> Strings.join(s, ';'),
			s -> Arrays.stream(s.split(";")).collect(Collectors.toSet())
		);

		var syncInterval = getConfig().getInt("redis.sync-interval");
		Bukkit.getAsyncScheduler().runAtFixedRate(
			this, t -> {
				if (!saveData()) {
					t.cancel();
					getLogger().warning("Disabling plugin...");
					Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().disablePlugin(this));
				}
			}, syncInterval, syncInterval, TimeUnit.MINUTES
		);
	}

	@Override
	public void onDisable() {
		DiscordSRV.api.unsubscribe(this);
		saveData();
		jedis.close();
	}

	public SimpleSet<UUID> getUnsubscribed() {
		return unsubscribed;
	}

	public SimpleMap<UUID, Set<String>> getHasIgnored() {
		return hasIgnored;
	}

	@Subscribe
	public void onDiscordMessagePreBroadcast(DiscordGuildMessagePreBroadcastEvent e) {
		// TODO use `e.getAuthor()` once https://github.com/DiscordSRV/DiscordSRV/pull/1789 is available
		var author = cachedAuthors.remove(e.getMessage());

		e.getRecipients().removeIf(r -> {
			if (!(r instanceof Player p)) return false;
			if (unsubscribed.contains(p.getUniqueId())) return true;

			if (author == null) return false;

			var ignoring = hasIgnored.get(p.getUniqueId());
			return ignoring != null && ignoring.contains(author);
		});
	}

	@Subscribe
	public void onDiscordMessagePostProcess(DiscordGuildMessagePostProcessEvent e) {
		// XXX workaround for lack of author in DiscordGuildMessagePreBroadcastEvent (see above)
		//
		// We use a cache with a Component as a key because the author isn't included in
		// DiscordGuildMessagePreBroadcastEvent. To prevent two users with identical
		// messages from being mistaken for each other, we must rely on referential
		// instead of object equality, hence IdentityHashMap (see above).
		cachedAuthors.put(e.getMinecraftMessage(), e.getAuthor().getId());
	}

	private boolean saveData() {
		synchronized (redisReadyLock) {
			if (redisReady) {
				try {
					unsubscribed.sync();
					hasIgnored.sync();
				} catch (JedisConnectionException e) {
					getLogger().warning("Failed to connect to Redis: " + e.getMessage());
					getLogger().warning("Retrying later...");
				} catch (JedisAccessControlException e) {
					getLogger().warning("Error while authenticating to Redis: " + e.getMessage());
					getLogger().warning("Retrying later...");
				} catch (JedisException e) {
					getLogger().severe("Failed to sync to Redis: " + e.getMessage());
					redisReady = false;
					return false;
				}
				return true;
			} else {
				return false;
			}
		}
	}

	private static UnifiedJedis initializeRedis(FileConfiguration config) {
		var host = config.getString("redis.host", "localhost");
		var port = config.getInt("redis.port", 6379);
		var user = config.getString("redis.user", null);
		var password = config.getString("redis.password", null);
		var maxTotal = config.getInt("redis.max-total-connections", JedisPoolConfig.DEFAULT_MAX_TOTAL);
		var maxIdleDuration = config.getLong("redis.max-idle-duration", 10L);

		var jedisCfg = DefaultJedisClientConfig.builder()
		                                       .user(user)
		                                       .password(password)
		                                       .build();

		var jedisPooled = new JedisPooled(new HostAndPort(host, port), jedisCfg);
		jedisPooled.getPool().setMaxTotal(maxTotal);
		jedisPooled.getPool().setMaxIdle(maxTotal);
		jedisPooled.getPool().setDurationBetweenEvictionRuns(Duration.of(maxIdleDuration, ChronoUnit.MINUTES));

		return jedisPooled;
	}

}
