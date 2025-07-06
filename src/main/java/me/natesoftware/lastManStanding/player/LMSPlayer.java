package me.natesoftware.lastManStanding.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LMSPlayer {

    private final UUID uuid;
    private PlayerState playerState;
    private boolean online;
    private long joinTime;

    // Game statistics (for future use)
    private int kills;
    private int deaths;
    private int wins;
    private int gamesPlayed;

    public LMSPlayer(UUID uuid) {
        this.uuid = uuid;
        this.playerState = PlayerState.WAITING;
        this.online = false;
        this.joinTime = System.currentTimeMillis();

        // Initialize stats
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
        this.gamesPlayed = 0;
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return online && getBukkitPlayer() != null && getBukkitPlayer().isOnline();
    }

    public void setOnline(boolean online) {
        this.online = online;
        if (online) {
            this.joinTime = System.currentTimeMillis();
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public long getJoinTime() {
        return joinTime;
    }

    // Game statistics methods
    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        this.wins++;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void addGamePlayed() {
        this.gamesPlayed++;
    }

    public void resetGameStats() {
        // Reset stats for new game
        this.kills = 0;
    }
}