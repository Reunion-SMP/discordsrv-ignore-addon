package com.github.jenbroek.discordsrv_ignore_addon;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CmdToggle implements TabExecutor {

	private final DiscordsrvIgnoreAddon plugin;

	public CmdToggle(DiscordsrvIgnoreAddon plugin) {
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
			if (plugin.getUnsubscribed().add(sender)) {
				sender.sendMessage("All Discord messages are now hidden");
			} else {
				plugin.getUnsubscribed().remove(sender);
				sender.sendMessage("Non-ignored Discord messages are now shown");
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
