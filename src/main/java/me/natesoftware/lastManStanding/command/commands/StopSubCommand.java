package me.natesoftware.lastManStanding.command.commands;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.command.SubCommand;
import me.natesoftware.lastManStanding.game.GameState;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopSubCommand extends SubCommand {

    private final LastManStanding plugin;

    public StopSubCommand(LastManStanding plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Force stop the current game";
    }

    @Override
    public String getUsage() {
        return "/lms stop";
    }

    @Override
    public String getPermission() {
        return "lms.host";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendErrorMessage(sender, "This command can only be used by players!");
            return;
        }

        Player player = (Player) sender;
        GameState gameState = plugin.getGameManager().getGameState();

        // Check if there's a game to stop
        if (gameState == GameState.WAITING) {
            MessageUtil.sendErrorMessage(sender, "No game is currently running!");
            return;
        }

        // Check if player is host
        if (!plugin.getPlayerManager().isHost(player)) {
            MessageUtil.sendErrorMessage(sender, "You don't have permission to stop the game!");
            return;
        }

        // Force end the game
        plugin.getGameManager().forceEndGame();
        MessageUtil.sendSuccessMessage(sender, "Game stopped successfully!");
        MessageUtil.broadcast("&cGame was force-stopped by " + player.getName());
    }
}