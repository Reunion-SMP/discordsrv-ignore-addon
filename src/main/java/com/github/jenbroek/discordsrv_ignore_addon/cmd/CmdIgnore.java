package com.github.jenbroek.discordsrv_ignore_addon.cmd;

import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;
import com.github.jenbroek.discordsrv_ignore_addon.data.Message;
import github.scarsz.discordsrv.DiscordSRV;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
				var user = getDiscordUid(arg);
				if (user == null) {
					player.sendMessage(Message.UNKNOWN_USER.asComponent(plugin.getConfig(), arg));
				} else {
					var ignoring = plugin.getHasIgnored().getOrDefault(player.getUniqueId(), new HashSet<>());

					if (ignoring.add(user)) {
						player.sendMessage(Message.USER_IGNORED.asComponent(plugin.getConfig(), arg));
					} else {
						ignoring.remove(user);
						player.sendMessage(Message.USER_UNIGNORED.asComponent(plugin.getConfig(), arg));
					}

					plugin.getHasIgnored().put(player.getUniqueId(), ignoring);
				}
			}
		}

		return true;
	}

	private static @Nullable String getDiscordUid(String playerNameOrDiscordId) {
		String user = null;
		if (DISCORD_UID.matcher(playerNameOrDiscordId).matches()) {
			user = playerNameOrDiscordId;
		} else {
			var player = Bukkit.getPlayerExact(playerNameOrDiscordId);

			if (player != null) {
				var uuid = player.getUniqueId();
				try {
					user = CompletableFuture
						.supplyAsync(() -> DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(uuid))
						.get();
				} catch (InterruptedException | ExecutionException ignored) {
					// Ignored
				}
			}
		}
		return user;
	}

}
