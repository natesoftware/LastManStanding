package me.natesoftware.lastManStanding.listeners;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.game.GameState;
import me.natesoftware.lastManStanding.player.LMSPlayer;
import me.natesoftware.lastManStanding.player.PlayerState;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final LastManStanding plugin;

    public PlayerDeathListener(LastManStanding plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        LMSPlayer lmsPlayer = plugin.getPlayerManager().getPlayer(player);

        // Handle death based on game state
        GameState gameState = plugin.getGameManager().getGameState();

        switch (gameState) {
            case WAITING:
                // Players shouldn't die in waiting, but handle gracefully
                handleWaitingDeath(event, player, lmsPlayer);
                break;

            case STARTING:
                // Cancel death during countdown
                handleStartingDeath(event, player, lmsPlayer);
                break;

            case ACTIVE:
                // Handle elimination
                handleActiveDeath(event, player, lmsPlayer);
                break;

            case ENDING:
                // Game is ending, just respawn
                handleEndingDeath(event, player, lmsPlayer);
                break;
        }
    }

    private void handleWaitingDeath(PlayerDeathEvent event, Player player, LMSPlayer lmsPlayer) {
        // Cancel death, clear drops
        event.setCancelled(true);
        event.getDrops().clear();

        // Log warning
        MessageUtil.logWarning("Player " + player.getName() + " died while waiting - respawning");

        // Respawn player
        player.spigot().respawn();
        player.setHealth(20.0);
        player.setFoodLevel(20);
    }

    private void handleStartingDeath(PlayerDeathEvent event, Player player, LMSPlayer lmsPlayer) {
        // Cancel death during countdown
        event.setCancelled(true);
        event.getDrops().clear();

        // Respawn and heal
        player.spigot().respawn();
        player.setHealth(20.0);
        player.setFoodLevel(20);

        MessageUtil.sendWarningMessage(player, "You cannot die during countdown!");
    }

    private void handleActiveDeath(PlayerDeathEvent event, Player player, LMSPlayer lmsPlayer) {
        // Don't cancel death - it's part of the game

        // Handle killer credit
        Player killer = player.getKiller();
        if (killer != null) {
            LMSPlayer killerLMS = plugin.getPlayerManager().getPlayer(killer);
            killerLMS.addKill();

            // Custom death message
            String deathMessage = "&c" + player.getName() + " &7was eliminated by &a" + killer.getName() + "&7!";
            event.setDeathMessage(null); // Clear vanilla message
            MessageUtil.broadcast(deathMessage);
        } else {
            // Natural death or other cause
            String deathMessage = "&c" + player.getName() + " &7was eliminated!";
            event.setDeathMessage(null);
            MessageUtil.broadcast(deathMessage);
        }

        // Handle game elimination
        plugin.getGameManager().handlePlayerDeath(player);

        // Clear drops to prevent item farming
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    private void handleEndingDeath(PlayerDeathEvent event, Player player, LMSPlayer lmsPlayer) {
        // Game is ending, just cancel death
        event.setCancelled(true);
        event.getDrops().clear();

        // Respawn player
        player.spigot().respawn();
        player.setHealth(20.0);
        player.setFoodLevel(20);
    }
}