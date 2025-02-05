package com.github.jenbroek.discordsrv_ignore_addon;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreBroadcastEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiscordsrvIgnoreAddon extends JavaPlugin implements Listener {

	// TODO persist
	private final Set<CommandSender> unsubscribed = new HashSet<>();
	private final Map<CommandSender, Set<String>> hasIgnored = new HashMap<>();

	// XXX workaround for lack of author in DiscordGuildMessagePreBroadcastEvent (see below)
	private final IdentityHashMap<Component, String> cachedAuthors = new IdentityHashMap<>();

	@Override
	public void onEnable() {
		DiscordSRV.api.subscribe(this);

		Objects.requireNonNull(getCommand("discordtoggle")).setExecutor(new CmdToggle(this));
		Objects.requireNonNull(getCommand("discordignore")).setExecutor(new CmdIgnore(this));
		Objects.requireNonNull(getCommand("discordignorelist")).setExecutor(new CmdIgnorelist(this));

		saveDefaultConfig();
	}

	@Override
	public void onDisable() {
		DiscordSRV.api.unsubscribe(this);
	}

	public Set<CommandSender> getUnsubscribed() {
		return unsubscribed;
	}

	public Map<CommandSender, Set<String>> getHasIgnored() {
		return hasIgnored;
	}

	@Subscribe
	public void onDiscordMessagePreBroadcast(DiscordGuildMessagePreBroadcastEvent e) {
		// TODO use `e.getAuthor()` once https://github.com/DiscordSRV/DiscordSRV/pull/1789 is available
		var author = cachedAuthors.remove(e.getMessage());

		e.getRecipients().removeIf(r -> {
			if (unsubscribed.contains(r)) return true;

			if (author == null) return false;

			var ignoring = hasIgnored.get(r);
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

}
