package me.natesoftware.lastManStanding.command.commands;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.command.SubCommand;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartSubCommand extends SubCommand {

    private final LastManStanding plugin;

    public StartSubCommand(LastManStanding plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Start the LastManStanding game";
    }

    @Override
    public String getUsage() {
        return "/lms start";
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

        // Try to start the game
        if (plugin.getGameManager().startGame(player)) {
            MessageUtil.sendSuccessMessage(sender, "Game start initiated!");
        }
        // Error message is sent by GameManager.startGame() if it fails
    }
}