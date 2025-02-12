package com.github.jenbroek.discordsrv_ignore_addon.cmd;

import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;
import com.github.jenbroek.discordsrv_ignore_addon.data.Message;
import github.scarsz.discordsrv.DiscordSRV;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdIgnore implements CommandExecutor {

	private static final Pattern DISCORD_UID = Pattern.compile("[0-9]{18}");

	private final DiscordsrvIgnoreAddon plugin;

	public CmdIgnore(DiscordsrvIgnoreAddon plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(
		@NotNull CommandSender sender,
		@NotNull Command command,
		@NotNull String label,
		@NotNull String[] args
	) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You must be a player to use this command!");
		} else {
			if (args.length == 0) {
				return false;
			}

			for (var arg : args) {
				tryGetDiscordUid(arg).thenAcceptAsync(user -> {
					if (user == null) {
						player.sendMessage(Message.UNKNOWN_USER.asComponent(plugin.getConfig(), arg));
					} else {
						var ignoring = plugin.getIgnoring()
						                     .getOrDefault(player.getUniqueId(), ConcurrentHashMap.newKeySet());

						if (ignoring.add(user)) {
							player.sendMessage(Message.USER_IGNORED.asComponent(plugin.getConfig(), arg));
						} else {
							ignoring.remove(user);
							player.sendMessage(Message.USER_UNIGNORED.asComponent(plugin.getConfig(), arg));
						}

						plugin.getIgnoring().put(player.getUniqueId(), ignoring);
					}
				}, Bukkit.getScheduler().getMainThreadExecutor(plugin)); // Ensure running on Bukkit's main thread
			}
		}

		return true;
	}

	private CompletableFuture<String> tryGetDiscordUid(String playerNameOrDiscordId) {
		if (!DISCORD_UID.matcher(playerNameOrDiscordId).matches()) {
			var player = Bukkit.getPlayerExact(playerNameOrDiscordId);

			if (player != null) {
				var mcUid = player.getUniqueId();
				return CompletableFuture.supplyAsync(
					() -> DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(mcUid)
				).exceptionally(t -> playerNameOrDiscordId);
			}
		}

		// Already a Discord ID
		return CompletableFuture.completedFuture(playerNameOrDiscordId);
	}

}
