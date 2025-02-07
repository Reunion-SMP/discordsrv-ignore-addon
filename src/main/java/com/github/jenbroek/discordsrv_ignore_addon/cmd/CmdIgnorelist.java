package com.github.jenbroek.discordsrv_ignore_addon.cmd;

import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;
import com.github.jenbroek.discordsrv_ignore_addon.data.Message;
import github.scarsz.discordsrv.DiscordSRV;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.bukkit.Bukkit;
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
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You must be a player to use this command!");
		} else {
			var ignoring = plugin.getIgnoring().getOrDefault(player.getUniqueId(), new HashSet<>());
			var s = String.join(", ", ignoring.stream().map(CmdIgnorelist::getMinecraftName).toList());

			if (s.isEmpty()) {
				player.sendMessage(Message.LIST_IGNORED_EMPTY.asComponent(plugin.getConfig()));
			} else {
				player.sendMessage(Message.LIST_IGNORED_TEMPLATE.asComponent(plugin.getConfig(), s));
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

	private static String getMinecraftName(String discordUid) {
		UUID uid = null;

		try {
			uid = CompletableFuture.supplyAsync(
				() -> DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUid)
			).get();
		} catch (InterruptedException | ExecutionException ignored) {
			// Ignored
		}

		if (uid != null) {
			var n = Bukkit.getOfflinePlayer(uid).getName();
			if (n != null) {
				return n;
			}
		}

		return discordUid;
	}

}
