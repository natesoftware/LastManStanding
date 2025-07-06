package me.natesoftware.lastManStanding.game;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.player.LMSPlayer;
import me.natesoftware.lastManStanding.player.PlayerState;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager {

    private final LastManStanding plugin;
    private GameState gameState;
    private BukkitTask countdownTask;
    private BukkitTask gameTask;
    private List<Player> activePlayers;
    private List<Player> spectators;
    private int countdownSeconds;
    private long gameStartTime;

    public GameManager(LastManStanding plugin) {
        this.plugin = plugin;
        this.gameState = GameState.WAITING;
        this.activePlayers = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.countdownSeconds = 0;
    }

    public LastManStanding getPlugin() {
        return plugin;
    }

    public boolean canStartGame() {
        return gameState == GameState.WAITING && plugin.getQueueManager().hasEnoughPlayers();
    }

    public boolean startGame(Player initiator) {
        if (!canStartGame()) {
            MessageUtil.sendErrorMessage(initiator, "Cannot start game! " +
                    getGameStateMessage());
            return false;
        }

        // Check if initiator is host
        if (!plugin.getPlayerManager().isHost(initiator)) {
            MessageUtil.sendErrorMessage(initiator, "You don't have permission to start the game!");
            return false;
        }

        // Start countdown
        startCountdown();
        return true;
    }

    private void startCountdown() {
        gameState = GameState.STARTING;
        countdownSeconds = plugin.getConfigManager().getCountdownTime();

        // Get game and spectator players
        activePlayers = plugin.getQueueManager().getGamePlayers();
        spectators = plugin.getQueueManager().getSpectatorPlayers();

        // Announce game start
        MessageUtil.broadcast("&aGame starting with " + activePlayers.size() + " players!");
        if (!spectators.isEmpty()) {
            MessageUtil.broadcast("&7" + spectators.size() + " players will spectate.");
        }

        // Start countdown task
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownSeconds <= 0) {
                    // Start the actual game
                    startActiveGame();
                    cancel();
                    return;
                }

                // Broadcast countdown
                if (countdownSeconds <= 5 || countdownSeconds % 5 == 0) {
                    MessageUtil.broadcast("&eGame starting in &f" + countdownSeconds + " &eseconds!");
                }

                countdownSeconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private void startActiveGame() {
        gameState = GameState.ACTIVE;
        gameStartTime = System.currentTimeMillis();

        // Setup all players
        setupGamePlayers();
        setupSpectators();

        // Announce game start
        MessageUtil.broadcast("&a&lGOOD LUCK!");

        // Start game monitoring task
        startGameMonitoring();

        MessageUtil.logInfo("Game started with " + activePlayers.size() + " players");
    }

    private void setupGamePlayers() {
        for (Player player : activePlayers) {
            LMSPlayer lmsPlayer = plugin.getPlayerManager().getPlayer(player);

            // Set player state
            lmsPlayer.setPlayerState(PlayerState.PLAYING);
            lmsPlayer.resetGameStats();

            Location spawnLocation = getSpawnLocation();

            // Setup player
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20.0);
            player.setFoodLevel(20);
//            player.getInventory().clear();
            player.teleport(spawnLocation);

            // Send player message
            MessageUtil.sendSuccessMessage(player, "You are now playing! Good luck!");
        }
    }

    private void setupSpectators() {
        Location spawnLocation = getSpawnLocation();

        for (Player player : spectators) {
            LMSPlayer lmsPlayer = plugin.getPlayerManager().getPlayer(player);

            // Set player state
            lmsPlayer.setPlayerState(PlayerState.SPECTATING);

            // Setup spectator
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(spawnLocation);

            // Send spectator message
            MessageUtil.sendInfoMessage(player, "You are spectating this game!");
        }
    }

    private void startGameMonitoring() {
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check win condition
                if (checkWinCondition()) {
                    cancel();
                    return;
                }

                // Check for game timeout (if configured)
                int maxDuration = plugin.getConfigManager().getConfig().getInt("game.max-duration", 0);
                if (maxDuration > 0) {
                    long elapsedMinutes = (System.currentTimeMillis() - gameStartTime) / 60000;
                    if (elapsedMinutes >= maxDuration) {
                        MessageUtil.broadcast("&cGame has reached maximum duration! Ending...");
                        endGame(null);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    public boolean checkWinCondition() {
        // Count alive players
        List<Player> alivePlayers = getAlivePlayers();

        if (alivePlayers.size() <= 1) {
            // Game over
            Player winner = alivePlayers.isEmpty() ? null : alivePlayers.get(0);
            endGame(winner);
            return true;
        }

        return false;
    }

    private List<Player> getAlivePlayers() {
        List<Player> alive = new ArrayList<>();

        for (Player player : activePlayers) {
            if (player.isOnline()) {
                LMSPlayer lmsPlayer = plugin.getPlayerManager().getPlayer(player);
                if (lmsPlayer.getPlayerState() == PlayerState.PLAYING) {
                    alive.add(player);
                }
            }
        }

        return alive;
    }

    public void handlePlayerQuit(Player player) {
        if (gameState != GameState.ACTIVE) {
            return;
        }

        // Remove from active players
        activePlayers.remove(player);

        // Update player state
        LMSPlayer lmsPlayer = plugin.getPlayerManager().getPlayer(player);
        lmsPlayer.setPlayerState(PlayerState.ELIMINATED);

        MessageUtil.logInfo(player.getName() + " quit during active game");
    }

    public void handlePlayerDeath(Player player) {
        if (gameState != GameState.ACTIVE) {
            return;
        }

        // Update player state
        LMSPlayer lmsPlayer = plugin.getPlayerManager().getPlayer(player);
        lmsPlayer.setPlayerState(PlayerState.ELIMINATED);
        lmsPlayer.addDeath();

        // Make them spectator
        player.setGameMode(GameMode.SPECTATOR);
        spectators.add(player);

        MessageUtil.broadcast("&c" + player.getName() + " has been eliminated!");
        MessageUtil.sendErrorMessage(player, "You have been eliminated! You are now spectating.");

        MessageUtil.logInfo(player.getName() + " was eliminated");
    }

    private void endGame(Player winner) {
        gameState = GameState.ENDING;

        // Cancel any running tasks
        if (gameTask != null && !gameTask.isCancelled()) {
            gameTask.cancel();
        }

        // Announce winner
        if (winner != null) {
            MessageUtil.broadcast("&6&l" + winner.getName() + " &6is the Last Man Standing!");

            // Update winner stats
            LMSPlayer winnerData = plugin.getPlayerManager().getPlayer(winner);
            winnerData.addWin();

            MessageUtil.sendSuccessMessage(winner, "Congratulations! You won the game!");
        } else {
            MessageUtil.broadcast("&7The game has ended with no winner.");
        }

        // Reset all players
        resetAllPlayers();

        // Cleanup and reset
        new BukkitRunnable() {
            @Override
            public void run() {
                resetGame();
            }
        }.runTaskLater(plugin, 100L); // Wait 5 seconds before reset
    }

    private void resetAllPlayers() {
        Location spawnLocation = getSpawnLocation();

        // Reset all players (active + spectators)
        List<Player> allPlayers = new ArrayList<>(activePlayers);
        allPlayers.addAll(spectators);

        for (Player player : allPlayers) {
            if (player.isOnline()) {
                LMSPlayer lmsPlayer = plugin.getPlayerManager().getPlayer(player);

                // Reset player state
                lmsPlayer.setPlayerState(PlayerState.WAITING);
                lmsPlayer.addGamePlayed();

                // Reset game mode and stats
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.getInventory().clear();

                // Teleport if configured
                if (plugin.getConfigManager().getConfig().getBoolean("game.teleport-after-game", true)) {
                    player.teleport(spawnLocation);
                }

                MessageUtil.sendInfoMessage(player, "Game ended! You're back in the queue.");
            }
        }
    }

    private void resetGame() {
        gameState = GameState.WAITING;
        activePlayers.clear();
        spectators.clear();
        gameStartTime = 0;

        // Clean up offline players
        plugin.getQueueManager().cleanupOfflinePlayers();
        plugin.getPlayerManager().cleanup();

        MessageUtil.logInfo("Game reset complete. Ready for next game.");
    }

    public void forceEndGame() {
        if (gameState == GameState.WAITING) {
            return;
        }

        // Cancel countdown if in progress
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel();
        }

        // End active game
        if (gameState == GameState.ACTIVE || gameState == GameState.STARTING) {
            MessageUtil.broadcast("&cGame force-ended by administrator.");
            endGame(null);
        }
    }

    private Location getSpawnLocation() {
        try {
            String worldName = plugin.getConfigManager().getConfig().getString("world.arena-world", "world");
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) throw new IllegalStateException("World not found: " + worldName);

            // Get bounds
            int minX = plugin.getConfigManager().getConfig().getInt("world.spawn.min.x");
            int maxX = plugin.getConfigManager().getConfig().getInt("world.spawn.max.x");
            int minZ = plugin.getConfigManager().getConfig().getInt("world.spawn.min.z");
            int maxZ = plugin.getConfigManager().getConfig().getInt("world.spawn.max.z");
            int y = plugin.getConfigManager().getConfig().getInt("world.spawn.y", 64); // Optional fallback

            // Random coordinates within bounds
            int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);

            // Use the highest Y at that location, or your custom Y if you're spawning underground
            Location loc = new Location(world, x + 0.5, y, z + 0.5); // +0.5 to center player
            loc.setY(world.getHighestBlockYAt(loc)); // Comment this out if you want fixed Y (like in caves)

            return loc;
        } catch (Exception e) {
            MessageUtil.logWarning("Failed to get random spawn location from config, using world spawn");
            return plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
    }

    private String getGameStateMessage() {
        switch (gameState) {
            case WAITING:
                if (!plugin.getQueueManager().hasEnoughPlayers()) {
                    return "Not enough players! " + plugin.getQueueManager().getQueueStatus();
                }
                return "Ready to start!";
            case STARTING:
                return "Game is already starting!";
            case ACTIVE:
                return "Game is already active!";
            case ENDING:
                return "Game is ending!";
            default:
                return "Unknown game state.";
        }
    }

    // Getters
    public GameState getGameState() {
        return gameState;
    }

    public List<Player> getActivePlayers() {
        return new ArrayList<>(activePlayers);
    }

    public List<Player> getSpectators() {
        return new ArrayList<>(spectators);
    }

    public int getAlivePlayerCount() {
        return getAlivePlayers().size();
    }

    public long getGameDuration() {
        if (gameStartTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - gameStartTime;
    }
}