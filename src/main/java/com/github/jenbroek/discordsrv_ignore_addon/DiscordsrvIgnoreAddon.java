package com.github.jenbroek.discordsrv_ignore_addon;

import com.github.jenbroek.discordsrv_ignore_addon.cmd.CmdIgnore;
import com.github.jenbroek.discordsrv_ignore_addon.cmd.CmdIgnorelist;
import com.github.jenbroek.discordsrv_ignore_addon.cmd.CmdUnignore;
import com.github.jenbroek.discordsrv_ignore_addon.storage.IgnoreStorage;
import com.github.jenbroek.discordsrv_ignore_addon.storage.SQLiteIgnoreStorage;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Set;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DiscordsrvIgnoreAddon extends JavaPlugin {

    private IgnoreStorage ignoreStorage;
    private final Set<UUID> unsubscribed = new java.util.HashSet<>(); 
    public Set<UUID> getUnsubscribed() {
      return unsubscribed;
        }

    private final ConcurrentMap<UUID, Set<String>> ignoring = new ConcurrentHashMap<>();

    public ConcurrentMap<UUID, Set<String>> getIgnoring() {
      return ignoring;
        }
    @Override
    public void onEnable() {
    if (!getDataFolder().exists()) {
        getDataFolder().mkdirs();
    }

    try {
        this.ignoreStorage = new SQLiteIgnoreStorage(new File(getDataFolder(), "ignore.db").getPath());
    } catch (SQLException e) {
        getLogger().severe("Could not initialize SQLite storage!");
        e.printStackTrace();
        getServer().getPluginManager().disablePlugin(this);
        return;
    }


        // Register listener
        DiscordSRV.api.subscribe(new DiscordListener(this));

        // Register commands
        getCommand("discordignore").setExecutor(new CmdIgnore(this));
        getCommand("discordunignore").setExecutor(new CmdUnignore(this));
        getCommand("discordignorelist").setExecutor(new CmdIgnorelist(this));

        getLogger().info("DiscordSRV Ignore Addon loaded with SQLite backend.");
    }

    @Override
    public void onDisable() {
        try {
            if (ignoreStorage instanceof SQLiteIgnoreStorage sqlite) {
                sqlite.close();
            }
        } catch (SQLException e) {
            getLogger().severe("Failed to close SQLite database.");
            e.printStackTrace();
        }

        getLogger().info("DiscordSRV Ignore Addon disabled.");
    }

    public IgnoreStorage getIgnoreStorage() {
        return ignoreStorage;
    }
}