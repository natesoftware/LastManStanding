package me.natesoftware.lastManStanding.queue;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.player.LMSPlayer;
import me.natesoftware.lastManStanding.player.PlayerState;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueueManager {

    private final LastManStanding plugin;
    private final List<UUID> queue;

    public QueueManager(LastManStanding plugin) {
        this.plugin = plugin;
        this.queue = new ArrayList<>();
    }

    public void addToQueue(Player player) {
        UUID uuid = player.getUniqueId();

        // Don't add if already in queue
        if (queue.contains(uuid)) {
            return;
        }

        // Add to queue
        queue.add(uuid);

        // Update player state
        LMSPlayer lmsPlayer = plugin.getPlayerManager().getPlayer(player);
        lmsPlayer.setPlayerState(PlayerState.WAITING);

        MessageUtil.logInfo(player.getName() + " added to queue. Queue size: " + queue.size());
    }

    public void removeFromQueue(Player player) {
        UUID uuid = player.getUniqueId();

        if (queue.remove(uuid)) {
            MessageUtil.logInfo(player.getName() + " removed from queue. Queue size: " + queue.size());
        }
    }

    public boolean isInQueue(Player player) {
        return queue.contains(player.getUniqueId());
    }

    public int getQueueSize() {
        return queue.size();
    }

    public List<Player> getQueuedPlayers() {
        List<Player> players = new ArrayList<>();

        for (UUID uuid : queue) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }

        return players;
    }

    public List<Player> getGamePlayers() {
        List<Player> queuedPlayers = getQueuedPlayers();
        int maxPlayers = plugin.getConfigManager().getMaxPlayers();

        // Return up to max players
        return queuedPlayers.subList(0, Math.min(queuedPlayers.size(), maxPlayers));
    }

    public List<Player> getSpectatorPlayers() {
        List<Player> queuedPlayers = getQueuedPlayers();
        int maxPlayers = plugin.getConfigManager().getMaxPlayers();

        // Return players beyond the cap
        if (queuedPlayers.size() > maxPlayers) {
            return queuedPlayers.subList(maxPlayers, queuedPlayers.size());
        }

        return new ArrayList<>();
    }

    public void clearQueue() {
        queue.clear();
        MessageUtil.logInfo("Queue cleared.");
    }

    public void cleanupOfflinePlayers() {
        // Remove players who are no longer online
        queue.removeIf(uuid -> {
            Player player = plugin.getServer().getPlayer(uuid);
            return player == null || !player.isOnline();
        });
    }

    public String getQueueStatus() {
        int current = getQueueSize();
        int max = plugin.getConfigManager().getMaxPlayers();
        int min = plugin.getConfigManager().getMinPlayers();

        return String.format("Queue: %d/%d (Min: %d)", current, max, min);
    }

    public boolean hasEnoughPlayers() {
        return getQueueSize() >= plugin.getConfigManager().getMinPlayers();
    }
}