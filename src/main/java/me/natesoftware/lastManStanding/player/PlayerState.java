package me.natesoftware.lastManStanding.player;

public enum PlayerState {
    OFFLINE,        // Player not on server
    WAITING,        // Player in queue, waiting for game
    PLAYING,        // Player actively in game
    SPECTATING,     // Player spectating (over player cap or eliminated)
    ELIMINATED      // Player eliminated from current game
}