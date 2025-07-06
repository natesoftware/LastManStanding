package me.natesoftware.lastManStanding;

import me.natesoftware.lastManStanding.command.CommandManager;
import me.natesoftware.lastManStanding.config.ConfigManager;
import me.natesoftware.lastManStanding.game.GameManager;
import me.natesoftware.lastManStanding.listeners.PlayerJoinListener;
import me.natesoftware.lastManStanding.listeners.PlayerQuitListener;
import me.natesoftware.lastManStanding.player.PlayerManager;
import me.natesoftware.lastManStanding.queue.QueueManager;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class LastManStanding extends JavaPlugin {

    // Plugin instance
    private static LastManStanding instance;

    // Core managers
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private QueueManager queueManager;
    private GameManager gameManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers in order
        initializeManagers();

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        // Plugin startup message
        MessageUtil.logInfo("LastManStanding plugin has been enabled!");
        MessageUtil.logInfo("Version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Clean shutdown
        if (gameManager != null) {
            gameManager.forceEndGame();
        }

        if (queueManager != null) {
            queueManager.clearQueue();
        }

        MessageUtil.logInfo("LastManStanding plugin has been disabled!");
    }

    private void initializeManagers() {
        // Order matters - some managers depend on others
        this.configManager = new ConfigManager(this);
        this.playerManager = new PlayerManager(this);
        this.queueManager = new QueueManager(this);
        this.gameManager = new GameManager(this);
        this.commandManager = new CommandManager(this);
    }

    private void registerCommands() {
        this.getCommand("lms").setExecutor(commandManager);
        this.getCommand("lms").setTabCompleter(commandManager);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
    }

    // Getters for managers
    public static LastManStanding getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}