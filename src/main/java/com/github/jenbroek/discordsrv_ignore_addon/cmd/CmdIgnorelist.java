package com.github.jenbroek.discordsrv_ignore_addon.cmd;

import github.scarsz.discordsrv.DiscordSRV;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;

import static com.github.jenbroek.discordsrv_ignore_addon.data.Message.CHAT_HIDDEN_NOTICE;
import static com.github.jenbroek.discordsrv_ignore_addon.data.Message.LIST_IGNORED_EMPTY;
import static com.github.jenbroek.discordsrv_ignore_addon.data.Message.LIST_IGNORED_TEMPLATE;

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
			return true;
		}

		UUID mcUid = player.getUniqueId();

		// Run SQLite query async
		CompletableFuture
			.supplyAsync(() -> {
				try {
					return plugin.getIgnoreStorage().getIgnoredDiscordUserIds(mcUid);
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
			})
			.thenAcceptAsync(ignoring -> {
				if (ignoring == null) {
					return;
				}

				if (ignoring.isEmpty()) {
					player.sendMessage(LIST_IGNORED_EMPTY.asComponent(plugin.getConfig()));
				} else {
					CompletableFuture
						.supplyAsync(() -> String.join(
							", ",
							ignoring.stream()
								.map(this::tryGetMinecraftName)
								.toList()
								.stream()
								.map(CompletableFuture::join)
								.toList()
						))
						.thenAcceptAsync(s -> {
							player.sendMessage(LIST_IGNORED_TEMPLATE.asComponent(plugin.getConfig(), s));
							if (plugin.getUnsubscribed().contains(mcUid)) {
								player.sendMessage(CHAT_HIDDEN_NOTICE.asComponent(plugin.getConfig()));
							}
						}, Bukkit.getScheduler().getMainThreadExecutor(plugin));
				}
			});

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

	private CompletableFuture<String> tryGetMinecraftName(String discordUid) {
		return CompletableFuture
			.supplyAsync(() -> DiscordSRV.getPlugin().getAccountLinkManager().getUuid(discordUid))
			.exceptionally(t -> null)
			.thenApplyAsync(mcUid -> {
				if (mcUid == null) return discordUid;
				var name = Bukkit.getOfflinePlayer(mcUid).getName();
				return name != null ? name : discordUid;
			}, Bukkit.getScheduler().getMainThreadExecutor(plugin));
	}
}