package me.natesoftware.lastManStanding.game.phases;

import me.natesoftware.lastManStanding.game.GameManager;
import org.bukkit.entity.Player;

/**
 * Interface for all game phases in LastManStanding
 */
public interface GamePhase {

    /**
     * Called when this phase starts
     */
    void start();

    /**
     * Called when this phase ends
     */
    void stop();

    /**
     * Called periodically while this phase is active
     * Used for phase-specific logic like countdowns, checks, etc.
     */
    void update();

    /**
     * Get the name of this phase
     * @return The phase name
     */
    String getName();

    /**
     * Get the description of this phase
     * @return The phase description
     */
    String getDescription();

    /**
     * Check if this phase can transition to another phase
     * @param toPhase The phase to transition to
     * @return true if transition is allowed, false otherwise
     */
    boolean canTransition(String toPhase);

    /**
     * Get the duration of this phase in seconds
     * @return Duration in seconds, or -1 for unlimited
     */
    int getDuration();

    /**
     * Get the remaining time in this phase
     * @return Remaining time in seconds, or -1 if unlimited
     */
    int getRemainingTime();

    /**
     * Check if this phase is currently active
     * @return true if active, false otherwise
     */
    boolean isActive();

    /**
     * Handle a player joining during this phase
     * @param player The player who joined
     */
    void handlePlayerJoin(Player player);

    /**
     * Handle a player leaving during this phase
     * @param player The player who left
     */
    void handlePlayerLeave(Player player);

    /**
     * Handle a player dying during this phase
     * @param player The player who died
     */
    void handlePlayerDeath(Player player);

    /**
     * Get the GameManager instance
     * @return The GameManager
     */
    GameManager getGameManager();
}