package com.github.jenbroek.discordsrv_ignore_addon;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreBroadcastEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DiscordsrvIgnoreAddon extends JavaPlugin implements Listener {

	// TODO persist
	private final Set<CommandSender> unsubscribed = new HashSet<>();

	@Override
	public void onEnable() {
		DiscordSRV.api.subscribe(this);
	}

	@Override
	public void onDisable() {
		DiscordSRV.api.unsubscribe(this);
	}

	@Subscribe
	public void onDiscordMessagePreBroadcast(DiscordGuildMessagePreBroadcastEvent e) {
		e.getRecipients().removeIf(unsubscribed::contains);
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
			if (unsubscribed.add(sender)) {
				sender.sendMessage("Discord messages are now hidden");
			} else {
				unsubscribed.remove(sender);
				sender.sendMessage("Discord messages are now shown");
			}
		}
		return true;
	}

	@Override
	public @NotNull List<String> onTabComplete(
		@NotNull CommandSender sender,
		@NotNull Command command,
		@NotNull String alias,
		@NotNull String[] args
	) {
		// Inhibit default completions
		return List.of();
	}

}
