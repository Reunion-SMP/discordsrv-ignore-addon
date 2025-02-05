package com.github.jenbroek.discordsrv_ignore_addon;

import github.scarsz.discordsrv.DiscordSRV;
import java.util.HashSet;
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
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use this command!");
		} else {
			if (args.length == 0) {
				return false;
			}

			for (var arg : args) {
				var user = getDiscordUid(arg);
				if (user == null) {
					sender.sendMessage(Message.UNKNOWN_USER.asComponent(plugin.getConfig(), arg));
				} else {
					var ignoring = plugin.getHasIgnored().getOrDefault(sender, new HashSet<>());

					if (ignoring.add(user)) {
						sender.sendMessage(Message.USER_IGNORED.asComponent(plugin.getConfig(), arg));
					} else {
						ignoring.remove(user);
						sender.sendMessage(Message.USER_UNIGNORED.asComponent(plugin.getConfig(), arg));
					}

					plugin.getHasIgnored().put(sender, ignoring);
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
				user = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
			}
		}
		return user;
	}

}
