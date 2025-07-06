package me.natesoftware.lastManStanding.game.phases;

import me.natesoftware.lastManStanding.game.GameManager;
import me.natesoftware.lastManStanding.player.LMSPlayer;
import me.natesoftware.lastManStanding.player.PlayerState;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.Location;

/**
 * Waiting phase - players wait in lobby until game starts
 */
public class WaitingPhase extends BaseGamePhase {

    private static final String PHASE_NAME = "Waiting";
    private static final String PHASE_DESCRIPTION = "Players are waiting for the game to start";
    private static final int PHASE_DURATION = -1; // Unlimited duration

    public WaitingPhase(GameManager gameManager) {
        super(gameManager, PHASE_NAME, PHASE_DESCRIPTION, PHASE_DURATION);
    }

    @Override
    protected void onStart() {
        // Setup all players in waiting state
        setupWaitingPlayers();

        // Show waiting message
        MessageUtil.broadcast("&7Players are waiting for the game to start...");
        MessageUtil.broadcast("&7Host can use &f/lms start &7to begin the game!");
    }

    @Override
    protected void onStop() {
        // Nothing special needed when stopping waiting phase
        MessageUtil.logInfo("Waiting phase ended");
    }

    @Override
    protected void onUpdate() {
        // Check if we have enough players
        if (!gameManager.getPlugin().getQueueManager().hasEnoughPlayers()) {
            // Show waiting message occasionally
            if (getRemainingTime() % 30 == 0) { // Every 30 seconds
                int current = gameManager.getPlugin().getQueueManager().getQueueSize();
                int required = gameManager.getPlugin().getConfigManager().getMinPlayers();
                MessageUtil.broadcast("&7Waiting for players... &f" + current + "/" + required);
            }
        }
    }

    @Override
    protected void onTimeout() {
        // Waiting phase doesn't timeout
        MessageUtil.logWarning("Waiting phase received timeout event - this shouldn't happen!");
    }

    @Override
    protected void onPlayerJoin(Player player) {
        // Setup the joining player
        setupWaitingPlayer(player);

        // Announce to others
        MessageUtil.broadcast("&a" + player.getName() + " &7joined the game! &f(" +
                gameManager.getPlugin().getQueueManager().getQueueSize() + " players)");

        // Welcome the player
        MessageUtil.sendInfoMessage(player, "Welcome to Last Man Standing!");
        MessageUtil.sendInfoMessage(player, "Waiting for the game to start...");

        // Show game info
        showGameInfo(player);
    }

    @Override
    protected void onPlayerLeave(Player player) {
        // Announce to others
        MessageUtil.broadcast("&c" + player.getName() + " &7left the game! &f(" +
                gameManager.getPlugin().getQueueManager().getQueueSize() + " players)");
    }

    @Override
    protected void onPlayerDeath(Player player) {
        // Players shouldn't die in waiting phase, but handle it gracefully
        MessageUtil.logWarning("Player " + player.getName() + " died in waiting phase - respawning");

        // Respawn the player
        player.spigot().respawn();
        setupWaitingPlayer(player);
    }

    @Override
    public boolean canTransition(String toPhase) {
        switch (toPhase.toLowerCase()) {
            case "mining":
            case "pvp":
            case "endgame":
                // Can transition to any game phase if we have enough players
                return gameManager.getPlugin().getQueueManager().hasEnoughPlayers();
            default:
                return false;
        }
    }

    /**
     * Setup all players for waiting phase
     */
    private void setupWaitingPlayers() {
        for (Player player : gameManager.getPlugin().getQueueManager().getQueuedPlayers()) {
            setupWaitingPlayer(player);
        }
    }

    /**
     * Setup a single player for waiting phase
     */
    private void setupWaitingPlayer(Player player) {
        LMSPlayer lmsPlayer = gameManager.getPlugin().getPlayerManager().getPlayer(player);

        // Set player state
        lmsPlayer.setPlayerState(PlayerState.WAITING);

        // Setup player
        player.setGameMode(GameMode.ADVENTURE); // Adventure mode in waiting
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();

        // Teleport to waiting area
        Location waitingLocation = getWaitingLocation();
        player.teleport(waitingLocation);

        // Give waiting items (if any)
        giveWaitingItems(player);
    }

    /**
     * Get the waiting location from config
     */
    private Location getWaitingLocation() {
        try {
            String worldName = gameManager.getPlugin().getConfigManager().getConfig()
                    .getString("world.waiting-world", "world");
            double x = gameManager.getPlugin().getConfigManager().getConfig()
                    .getDouble("world.waiting.x", 0);
            double y = gameManager.getPlugin().getConfigManager().getConfig()
                    .getDouble("world.waiting.y", 64);
            double z = gameManager.getPlugin().getConfigManager().getConfig()
                    .getDouble("world.waiting.z", 0);

            return new Location(gameManager.getPlugin().getServer().getWorld(worldName), x, y, z);
        } catch (Exception e) {
            MessageUtil.logWarning("Failed to get waiting location from config, using world spawn");
            return gameManager.getPlugin().getServer().getWorlds().get(0).getSpawnLocation();
        }
    }

    /**
     * Give waiting items to a player (compass, info book, etc.)
     */
    private void giveWaitingItems(Player player) {
        // TODO: Add waiting items in future
        // For now, just clear inventory
        player.getInventory().clear();
    }

    /**
     * Show game information to a player
     */
    private void showGameInfo(Player player) {
        int current = gameManager.getPlugin().getQueueManager().getQueueSize();
        int required = gameManager.getPlugin().getConfigManager().getMinPlayers();
        int max = gameManager.getPlugin().getConfigManager().getMaxPlayers();

        MessageUtil.sendInfoMessage(player, "Game Info:");
        MessageUtil.sendInfoMessage(player, "  Players: " + current + "/" + max);
        MessageUtil.sendInfoMessage(player, "  Required: " + required);

        if (current >= required) {
            MessageUtil.sendSuccessMessage(player, "Ready to start! Waiting for host...");
        } else {
            MessageUtil.sendInfoMessage(player, "Need " + (required - current) + " more players");
        }
    }
}