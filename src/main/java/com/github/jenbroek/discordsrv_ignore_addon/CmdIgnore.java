package com.github.jenbroek.discordsrv_ignore_addon;

import github.scarsz.discordsrv.DiscordSRV;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdIgnore implements CommandExecutor {

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
				String user = null;
				var player = Bukkit.getPlayerExact(arg);
				if (player != null) {
					user = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
				}

				if (user == null) {
					sender.sendMessage("No Discord user found for '%s'".formatted(arg));
				} else {
					var ignoring = plugin.getHasIgnored().getOrDefault(sender, new HashSet<>());

					if (ignoring.add(user)) {
						sender.sendMessage("Ignoring Discord messages from '%s'".formatted(player.getName()));
					} else {
						ignoring.remove(user);
						sender.sendMessage("No longer ignoring Discord messages from '%s'".formatted(player.getName()));
					}

					plugin.getHasIgnored().put(sender, ignoring);
				}
			}
		}

		return true;
	}

}
