package me.natesoftware.lastManStanding.listeners;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final LastManStanding plugin;

    public PlayerJoinListener(LastManStanding plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Add player to manager
        plugin.getPlayerManager().addPlayer(event.getPlayer());

        // Add to queue
        plugin.getQueueManager().addToQueue(event.getPlayer());

        // Show current queue status
        int queueSize = plugin.getQueueManager().getQueueSize();
        int maxPlayers = plugin.getConfigManager().getMaxPlayers();

        MessageUtil.sendInfoMessage(event.getPlayer(),
                "Queue: " + queueSize + "/" + maxPlayers + " players");

        // Check if we can start the game
        if (plugin.getGameManager().canStartGame()) {
            MessageUtil.broadcast("&aEnough players are online! A host can start the game with &f/lms start");
        }
    }
}