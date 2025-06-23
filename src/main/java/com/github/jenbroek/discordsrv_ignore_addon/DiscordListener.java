package com.github.jenbroek.discordsrv_ignore_addon;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreBroadcastEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Set;

import org.bukkit.entity.Player;

public class DiscordListener {

	private final DiscordsrvIgnoreAddon plugin;

	// XXX workaround for lack of author in DiscordGuildMessagePreBroadcastEvent (see below)
	private final IdentityHashMap<Component, String> cachedAuthors = new IdentityHashMap<>();

	public DiscordListener(DiscordsrvIgnoreAddon plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void onDiscordMessagePreBroadcast(DiscordGuildMessagePreBroadcastEvent e) {
		// TODO use `e.getAuthor()` once https://github.com/DiscordSRV/DiscordSRV/pull/1789 is available
		var author = cachedAuthors.remove(e.getMessage());

		e.getRecipients().removeIf(r -> {
			if (!(r instanceof Player p)) return false;
			if (plugin.getUnsubscribed().contains(p.getUniqueId())) return true;

			if (author == null) return false;

			Set<String> ignoring = null;
			try {
   				 ignoring = plugin.getIgnoreStorage().getIgnoredDiscordUserIds(p.getUniqueId());
			} catch (SQLException ex) {
    			ex.printStackTrace();
			}
			return ignoring != null && ignoring.contains(author);
		});
	}

	@Subscribe
	public void onDiscordMessagePostProcess(DiscordGuildMessagePostProcessEvent e) {
		// XXX workaround for lack of author in DiscordGuildMessagePreBroadcastEvent (see above)
		//
		// We use a cache with a Component as a key because the author isn't included in
		// DiscordGuildMessagePreBroadcastEvent. To prevent two users with identical
		// messages from being mistaken for each other, we must rely on referential
		// instead of object equality, hence IdentityHashMap (see above).
		cachedAuthors.put(e.getMinecraftMessage(), e.getAuthor().getId());
	}

}
