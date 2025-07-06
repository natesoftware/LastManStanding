package me.natesoftware.lastManStanding.game;

public enum GameState {
    WAITING,        // Waiting for players, no game active
    STARTING,       // Countdown phase, game about to start
    ACTIVE,         // Game is actively running
    ENDING,         // Game is ending, cleanup phase
    MAINTENANCE     // Server maintenance mode
}