package me.natesoftware.lastManStanding.listeners;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.game.GameState;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final LastManStanding plugin;

    public PlayerQuitListener(LastManStanding plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove from manager
        plugin.getPlayerManager().removePlayer(event.getPlayer());

        // Remove from queue
        plugin.getQueueManager().removeFromQueue(event.getPlayer());

        // Handle game-specific quit logic
        if (plugin.getGameManager().getGameState() == GameState.ACTIVE) {
            // Player quit during active game
            plugin.getGameManager().handlePlayerQuit(event.getPlayer());

            MessageUtil.broadcast("&e" + event.getPlayer().getName() + " left the game!");

            // Check if game should end
            if (plugin.getGameManager().checkWinCondition()) {
                // Game will end automatically
                return;
            }
        }

        // Broadcast queue update if in waiting state
        if (plugin.getGameManager().getGameState() == GameState.WAITING) {
            int queueSize = plugin.getQueueManager().getQueueSize();
            int maxPlayers = plugin.getConfigManager().getMaxPlayers();

            MessageUtil.broadcast("&7Queue: " + queueSize + "/" + maxPlayers + " players");
        }
    }
}