package com.github.jenbroek.discordsrv_ignore_addon.storage;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

public interface IgnoreStorage {
    void addIgnore(UUID playerUuid, String discordUserId) throws SQLException;
    void removeIgnore(UUID playerUuid, String discordUserId) throws SQLException;
    Set<String> getIgnoredDiscordUserIds(UUID playerUuid) throws SQLException;
}