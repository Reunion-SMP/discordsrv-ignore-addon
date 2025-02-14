package com.github.jenbroek.discordsrv_ignore_addon.cmd;

import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.jenbroek.discordsrv_ignore_addon.data.Message.CHAT_HIDDEN;
import static com.github.jenbroek.discordsrv_ignore_addon.data.Message.CHAT_SHOWN;

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
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You must be a player to use this command!");
		} else {
			var mcUid = player.getUniqueId();

			if (plugin.getUnsubscribed().contains(mcUid)) {
				plugin.getUnsubscribed().remove(mcUid);
				player.sendMessage(CHAT_SHOWN.asComponent(plugin.getConfig()));
			} else {
				plugin.getUnsubscribed().add(mcUid);
				player.sendMessage(CHAT_HIDDEN.asComponent(plugin.getConfig()));
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
