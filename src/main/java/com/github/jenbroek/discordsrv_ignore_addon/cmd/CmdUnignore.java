package com.github.jenbroek.discordsrv_ignore_addon.cmd;

import github.scarsz.discordsrv.DiscordSRV;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.github.jenbroek.discordsrv_ignore_addon.DiscordsrvIgnoreAddon;

public class CmdUnignore implements CommandExecutor {

	private static final Pattern DISCORD_UID = Pattern.compile("[0-9]{18}");
	private final DiscordsrvIgnoreAddon plugin;

	public CmdUnignore(DiscordsrvIgnoreAddon plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(
		@NotNull CommandSender sender,
		@NotNull Command command,
		@NotNull String label,
		@NotNull String[] args
	) {
		if (!(sender instanceof Player player)) return true;
		if (args.length == 0) return false;

		UUID mcUid = player.getUniqueId();
		String selfDiscordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(mcUid);

		for (String arg : args) {
			tryGetDiscordUid(arg).thenAcceptAsync(discordUid -> {
				if (discordUid == null || discordUid.equals(selfDiscordId)) return;

				CompletableFuture
					.supplyAsync(() -> {
						try {
							return plugin.getIgnoreStorage().getIgnoredDiscordUserIds(mcUid);
						} catch (SQLException e) {
							e.printStackTrace();
							return null;
						}
					})
					.thenAcceptAsync(currentIgnored -> {
						if (currentIgnored == null || !currentIgnored.contains(discordUid)) return;

						try {
							plugin.getIgnoreStorage().removeIgnore(mcUid, discordUid);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}, Bukkit.getScheduler().getMainThreadExecutor(plugin));
			});
		}

		return true;
	}

	private CompletableFuture<String> tryGetDiscordUid(String input) {
		// If it's already a Discord UID
		if (DISCORD_UID.matcher(input).matches()) {
			return CompletableFuture.completedFuture(input);
		}

		// Otherwise, try resolving offline player to UUID → Discord ID
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
		if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
			UUID mcUid = offlinePlayer.getUniqueId();
			return CompletableFuture.supplyAsync(() ->
				DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(mcUid)
			).exceptionally(t -> null);
		}

		return CompletableFuture.completedFuture(null);
	}
}