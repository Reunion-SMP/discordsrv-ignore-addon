package com.github.jenbroek.discordsrv_ignore_addon;

import java.util.HashSet;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CmdIgnorelist implements TabExecutor {

	private final DiscordsrvIgnoreAddon plugin;

	public CmdIgnorelist(DiscordsrvIgnoreAddon plugin) {
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
			var ignoring = plugin.getHasIgnored().getOrDefault(sender, new HashSet<>());
			var s = String.join(", ", ignoring);

			if (s.isEmpty()) {
				sender.sendMessage(Message.LIST_IGNORED_EMPTY.asComponent(plugin.getConfig()));
			} else {
				sender.sendMessage(Message.LIST_IGNORED_TEMPLATE.asComponent(plugin.getConfig(), s));
			}
		}

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(
		@NotNull CommandSender sender,
		@NotNull Command command,
		@NotNull String label,
		@NotNull String[] args
	) {
		// Inhibit default completions
		return List.of();
	}

}
