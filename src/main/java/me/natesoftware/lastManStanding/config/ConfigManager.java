package me.natesoftware.lastManStanding.config;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final LastManStanding plugin;
    private FileConfiguration config;

    public ConfigManager(LastManStanding plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();

        // Reload config from file
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        // Validate config
        validateConfig();

        MessageUtil.logInfo("Configuration loaded successfully!");
    }

    private void validateConfig() {
        // Check for required config values and set defaults if missing
        if (!config.contains("game.max-players")) {
            config.set("game.max-players", 10);
        }

        if (!config.contains("game.min-players")) {
            config.set("game.min-players", 2);
        }

        if (!config.contains("game.countdown-time")) {
            config.set("game.countdown-time", 10);
        }

        if (!config.contains("game.default-modifier")) {
            config.set("game.default-modifier", "none");
        }

        if (!config.contains("messages.prefix")) {
            config.set("messages.prefix", "&6&l[LMS] &r");
        }

        plugin.saveConfig();
    }

    public void reloadConfig() {
        loadConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Game settings getters
    public int getMaxPlayers() {
        return config.getInt("game.max-players", 10);
    }

    public int getMinPlayers() {
        return config.getInt("game.min-players", 2);
    }

    public int getCountdownTime() {
        return config.getInt("game.countdown-time", 10);
    }

    public String getDefaultModifier() {
        return config.getString("game.default-modifier", "none");
    }

    // Message settings
    public String getPrefix() {
        return config.getString("messages.prefix", "&6[LMS] &r");
    }

    // Game settings setters (for host commands)
    public void setMaxPlayers(int maxPlayers) {
        config.set("game.max-players", maxPlayers);
        plugin.saveConfig();
    }

    public void setMinPlayers(int minPlayers) {
        config.set("game.min-players", minPlayers);
        plugin.saveConfig();
    }

    public void setCountdownTime(int seconds) {
        config.set("game.countdown-time", seconds);
        plugin.saveConfig();
    }

    public void setDefaultModifier(String modifier) {
        config.set("game.default-modifier", modifier);
        plugin.saveConfig();
    }
}