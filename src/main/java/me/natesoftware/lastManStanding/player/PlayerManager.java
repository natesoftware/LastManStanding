package me.natesoftware.lastManStanding.player;

import me.natesoftware.lastManStanding.LastManStanding;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final LastManStanding plugin;
    private final Map<UUID, LMSPlayer> players;

    public PlayerManager(LastManStanding plugin) {
        this.plugin = plugin;
        this.players = new HashMap<>();
    }

    public LMSPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    public LMSPlayer getPlayer(UUID uuid) {
        return players.computeIfAbsent(uuid, k -> new LMSPlayer(uuid));
    }

    public void addPlayer(Player player) {
        LMSPlayer lmsPlayer = getPlayer(player);
        lmsPlayer.setPlayerState(PlayerState.WAITING);
        lmsPlayer.setOnline(true);
    }

    public void removePlayer(Player player) {
        LMSPlayer lmsPlayer = getPlayer(player);
        lmsPlayer.setOnline(false);
        lmsPlayer.setPlayerState(PlayerState.OFFLINE);
    }

    public boolean isHost(Player player) {
        return player.hasPermission("lms.host") || player.isOp();
    }

    public void cleanup() {
        // Remove offline players after game ends
        players.entrySet().removeIf(entry -> !entry.getValue().isOnline());
    }

    public int getOnlinePlayerCount() {
        return (int) players.values().stream()
                .filter(LMSPlayer::isOnline)
                .count();
    }

    public int getPlayersInState(PlayerState state) {
        return (int) players.values().stream()
                .filter(LMSPlayer::isOnline)
                .filter(p -> p.getPlayerState() == state)
                .count();
    }
}