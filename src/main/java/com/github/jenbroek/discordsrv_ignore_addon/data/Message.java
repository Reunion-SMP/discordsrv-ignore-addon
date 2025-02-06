package com.github.jenbroek.discordsrv_ignore_addon.data;

import java.util.Objects;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

public enum Message {

	UNKNOWN_USER("unknown-user"),
	USER_IGNORED("user-ignored"),
	USER_UNIGNORED("user-unignored"),
	LIST_IGNORED_EMPTY("list-ignored-empty"),
	LIST_IGNORED_TEMPLATE("list-ignored-template"),
	CHAT_HIDDEN("chat-hidden"),
	CHAT_SHOWN("chat-shown");

	private final String key;

	Message(String key) {
		this.key = key;
	}

	public TextComponent asComponent(FileConfiguration config, Object... placeholders) {
		var msg = Objects.requireNonNull(
			config.getString("messages." + this.key),
			"Missing message in config for " + this.key
		).formatted(placeholders);

		return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
	}

}
