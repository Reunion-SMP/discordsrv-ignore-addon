package com.github.jenbroek.discordsrv_ignore_addon.storage;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SQLiteIgnoreStorage implements IgnoreStorage {

    private final Connection connection;

    public SQLiteIgnoreStorage(String dbPath) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        setupTable();
    }

    private void setupTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ignored (
                    uuid TEXT NOT NULL,
                    ignored_discord_id TEXT NOT NULL,
                    PRIMARY KEY (uuid, ignored_discord_id)
                )
            """);
        }
    }

    @Override
    public void addIgnore(UUID playerUuid, String discordUserId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO ignored (uuid, ignored_discord_id) VALUES (?, ?)")) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, discordUserId);
            ps.executeUpdate();
        }
    }

    @Override
    public void removeIgnore(UUID playerUuid, String discordUserId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM ignored WHERE uuid = ? AND ignored_discord_id = ?")) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, discordUserId);
            ps.executeUpdate();
        }
    }

    @Override
    public Set<String> getIgnoredDiscordUserIds(UUID playerUuid) throws SQLException {
        Set<String> ignored = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT ignored_discord_id FROM ignored WHERE uuid = ?")) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ignored.add(rs.getString("ignored_discord_id"));
                }
            }
        }
        return ignored;
    }

    public void close() throws SQLException {
        if (connection != null) connection.close();
    }
}