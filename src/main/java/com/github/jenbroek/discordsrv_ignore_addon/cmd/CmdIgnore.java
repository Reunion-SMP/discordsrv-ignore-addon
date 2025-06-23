package com.github.jenbroek.discordsrv_ignore_addon.cmd;

import github.scarsz.discordsrv.DiscordSRV;
import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

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
			return true;
		}
		if (args.length == 0) return false;

		UUID mcUid = player.getUniqueId();
		String selfDiscordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(mcUid);

		for (String arg : args) {
			tryGetDiscordUid(arg).thenAcceptAsync(discordUid -> {
				if (discordUid == null) return;
				if (discordUid.equals(selfDiscordId)) return;

				CompletableFuture.runAsync(() -> {
					try {
						var ignored = plugin.getIgnoreStorage().getIgnoredDiscordUserIds(mcUid);
						if (!ignored.contains(discordUid)) {
							plugin.getIgnoreStorage().addIgnore(mcUid, discordUid);
						}
					} catch (SQLException e) {
						e.printStackTrace(); // Optionally log internally
					}
				});
			});
		}
		return true;
	}

	private CompletableFuture<String> tryGetDiscordUid(String input) {
		if (DISCORD_UID.matcher(input).matches()) {
			return CompletableFuture.completedFuture(input);
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(input);
		if (target != null && target.hasPlayedBefore()) {
			UUID uuid = target.getUniqueId();
			return CompletableFuture.supplyAsync(() ->
				DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(uuid)
			).exceptionally(t -> null);
		}

		return CompletableFuture.completedFuture(null);
	}
}