package com.github.jenbroek.discordsrv_ignore_addon.data;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

public enum Message {

	UNKNOWN_USER("unknown-user", "&#8094ddNo Discord user found for '&f%s&#8094dd'"),
	USER_IGNORED("user-ignored", "&#8094ddNow ignoring Discord messages from '&f%s&#8094dd'"),
	USER_UNIGNORED("user-unignored", "&#8094ddNo longer ignoring Discord messages from '&f%s&#8094dd'"),
	LIST_IGNORED_EMPTY("list-ignored-empty", "&#8094ddNo Discord users ignored"),
	LIST_IGNORED_TEMPLATE("list-ignored-template", "&#8094ddIgnoring Discord messages from: &f%s"),
	CHAT_HIDDEN_NOTICE("chat-hidden-notice", "&oNote: all Discord messages are currently &#8094ddhidden"),
	CHAT_HIDDEN("chat-hidden", "&#8094ddAll Discord messages are now &fhidden"),
	CHAT_SHOWN("chat-shown", "&#8094ddNon-ignored Discord messages are now &fshown");

	private final String key;
	private final String def;

	Message(String key, String def) {
		this.key = key;
		this.def = def;
	}

	public TextComponent asComponent(FileConfiguration cfg, Object... placeholders) {
		var msg = cfg.getString("messages." + this.key, this.def).formatted(placeholders);
		return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
	}

}
