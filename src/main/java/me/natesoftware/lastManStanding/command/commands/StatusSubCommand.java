package me.natesoftware.lastManStanding.command.commands;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.command.SubCommand;
import me.natesoftware.lastManStanding.game.GameState;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.command.CommandSender;

public class StatusSubCommand extends SubCommand {

    private final LastManStanding plugin;

    public StatusSubCommand(LastManStanding plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Show current game status";
    }

    @Override
    public String getUsage() {
        return "/lms status";
    }

    @Override
    public String getPermission() {
        return "lms.use";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        GameState gameState = plugin.getGameManager().getGameState();

        MessageUtil.sendInfoMessage(sender, "=== LastManStanding Status ===");

        // Game state
        MessageUtil.sendMessage(sender, "&7Game State: &f" + formatGameState(gameState));

        // Queue information
        String queueStatus = plugin.getQueueManager().getQueueStatus();
        MessageUtil.sendMessage(sender, "&7" + queueStatus);

        // Game-specific information
        switch (gameState) {
            case WAITING:
                int needed = plugin.getConfigManager().getMinPlayers() - plugin.getQueueManager().getQueueSize();
                if (needed > 0) {
                    MessageUtil.sendMessage(sender, "&7Need &f" + needed + " &7more players to start");
                } else {
                    MessageUtil.sendMessage(sender, "&aReady to start! A host can use &f/lms start");
                }
                break;

            case STARTING:
                MessageUtil.sendMessage(sender, "&eGame starting soon...");
                break;

            case ACTIVE:
                int alive = plugin.getGameManager().getAlivePlayerCount();
                int spectators = plugin.getGameManager().getSpectators().size();
                MessageUtil.sendMessage(sender, "&7Players alive: &f" + alive);
                MessageUtil.sendMessage(sender, "&7Spectators: &f" + spectators);

                // Game duration
                long duration = plugin.getGameManager().getGameDuration();
                String durationStr = MessageUtil.formatTime((int) (duration / 1000));
                MessageUtil.sendMessage(sender, "&7Game duration: &f" + durationStr);
                break;

            case ENDING:
                MessageUtil.sendMessage(sender, "&eGame is ending...");
                break;
        }

        // Server settings
        MessageUtil.sendMessage(sender, "&7Settings:");
        MessageUtil.sendMessage(sender, "&7  Max Players: &f" + plugin.getConfigManager().getMaxPlayers());
        MessageUtil.sendMessage(sender, "&7  Min Players: &f" + plugin.getConfigManager().getMinPlayers());
        MessageUtil.sendMessage(sender, "&7  Countdown: &f" + plugin.getConfigManager().getCountdownTime() + "s");
        MessageUtil.sendMessage(sender, "&7  Modifier: &f" + plugin.getConfigManager().getDefaultModifier());
    }

    private String formatGameState(GameState state) {
        switch (state) {
            case WAITING:
                return "&aWaiting for players";
            case STARTING:
                return "&eStarting";
            case ACTIVE:
                return "&cActive";
            case ENDING:
                return "&7Ending";
            default:
                return "&7Unknown";
        }
    }
}