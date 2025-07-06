package me.natesoftware.lastManStanding.game.phases;

import me.natesoftware.lastManStanding.game.GameManager;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Base implementation for all game phases
 */
public abstract class BaseGamePhase implements GamePhase {

    protected final GameManager gameManager;
    protected final String name;
    protected final String description;
    protected final int duration; // Duration in seconds, -1 for unlimited

    protected boolean active = false;
    protected long startTime = 0;
    protected BukkitTask updateTask;

    public BaseGamePhase(GameManager gameManager, String name, String description, int duration) {
        this.gameManager = gameManager;
        this.name = name;
        this.description = description;
        this.duration = duration;
    }

    @Override
    public void start() {
        if (active) {
            MessageUtil.logWarning("Phase " + name + " is already active!");
            return;
        }

        active = true;
        startTime = System.currentTimeMillis();

        MessageUtil.logInfo("Starting phase: " + name);

        // Call the phase-specific start logic
        onStart();

        // Start the update task
        startUpdateTask();

        // Announce phase start
        announcePhaseStart();
    }

    @Override
    public void stop() {
        if (!active) {
            MessageUtil.logWarning("Phase " + name + " is not active!");
            return;
        }

        active = false;

        // Stop the update task
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
            updateTask = null;
        }

        MessageUtil.logInfo("Stopping phase: " + name);

        // Call the phase-specific stop logic
        onStop();

        // Announce phase end
        announcePhaseEnd();
    }

    @Override
    public void update() {
        if (!active) {
            return;
        }

        // Check if phase has timed out
        if (duration > 0 && getRemainingTime() <= 0) {
            MessageUtil.logInfo("Phase " + name + " has timed out");
            onTimeout();
            return;
        }

        // Call phase-specific update logic
        onUpdate();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public int getRemainingTime() {
        if (duration <= 0) {
            return -1; // Unlimited
        }

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        return Math.max(0, duration - (int) elapsed);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public void handlePlayerJoin(Player player) {
        if (!active) {
            return;
        }

        MessageUtil.logInfo("Player " + player.getName() + " joined during " + name + " phase");
        onPlayerJoin(player);
    }

    @Override
    public void handlePlayerLeave(Player player) {
        if (!active) {
            return;
        }

        MessageUtil.logInfo("Player " + player.getName() + " left during " + name + " phase");
        onPlayerLeave(player);
    }

    @Override
    public void handlePlayerDeath(Player player) {
        if (!active) {
            return;
        }

        MessageUtil.logInfo("Player " + player.getName() + " died during " + name + " phase");
        onPlayerDeath(player);
    }

    /**
     * Start the update task that runs every second
     */
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(gameManager.getPlugin(), 0L, 20L); // Run every second
    }

    /**
     * Announce the start of this phase to all players
     */
    protected void announcePhaseStart() {
        String message = "&a&l" + name.toUpperCase() + " PHASE STARTED!";
        if (!description.isEmpty()) {
            message += " &7" + description;
        }
        MessageUtil.broadcast(message);
    }

    /**
     * Announce the end of this phase to all players
     */
    protected void announcePhaseEnd() {
        MessageUtil.broadcast("&c&l" + name.toUpperCase() + " PHASE ENDED!");
    }

    /**
     * Announce time remaining if duration is set
     */
    protected void announceTimeRemaining() {
        if (duration > 0) {
            int remaining = getRemainingTime();
            if (remaining == 60 || remaining == 30 || remaining == 10 || remaining <= 5) {
                MessageUtil.broadcast("&e" + name + " phase ends in &f" + remaining + " &eseconds!");
            }
        }
    }

    // Abstract methods that must be implemented by subclasses

    /**
     * Called when the phase starts - implement phase-specific logic here
     */
    protected abstract void onStart();

    /**
     * Called when the phase stops - implement cleanup logic here
     */
    protected abstract void onStop();

    /**
     * Called every second while the phase is active
     */
    protected abstract void onUpdate();

    /**
     * Called when the phase times out (if duration is set)
     */
    protected abstract void onTimeout();

    /**
     * Called when a player joins during this phase
     * @param player The player who joined
     */
    protected abstract void onPlayerJoin(Player player);

    /**
     * Called when a player leaves during this phase
     * @param player The player who left
     */
    protected abstract void onPlayerLeave(Player player);

    /**
     * Called when a player dies during this phase
     * @param player The player who died
     */
    protected abstract void onPlayerDeath(Player player);
}