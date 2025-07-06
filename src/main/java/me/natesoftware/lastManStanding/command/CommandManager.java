package me.natesoftware.lastManStanding.command;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.command.commands.SettingsSubCommand;
import me.natesoftware.lastManStanding.command.commands.StartSubCommand;
import me.natesoftware.lastManStanding.command.commands.StatusSubCommand;
import me.natesoftware.lastManStanding.command.commands.StopSubCommand;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final LastManStanding plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CommandManager(LastManStanding plugin) {
        this.plugin = plugin;
        registerSubCommands();
    }

    private void registerSubCommands() {
        registerSubCommand(new StartSubCommand(plugin));
        registerSubCommand(new StopSubCommand(plugin));
        registerSubCommand(new SettingsSubCommand(plugin));
        registerSubCommand(new StatusSubCommand(plugin));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            // Check permission
            if (!sub.hasPermission(sender)) {
                MessageUtil.sendErrorMessage(sender, "You don't have permission to use this command!");
                return true;
            }

            // Execute command
            try {
                sub.execute(sender, args);
            } catch (Exception e) {
                MessageUtil.sendErrorMessage(sender, "An error occurred while executing the command.");
                MessageUtil.logError("Error executing command: " + args[0], e);
            }
        } else {
            MessageUtil.sendErrorMessage(sender, "Unknown subcommand: " + args[0]);
            sendHelpMessage(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete subcommand names
            for (SubCommand subCommand : subCommands.values()) {
                if (subCommand.hasPermission(sender)) {
                    String name = subCommand.getName();
                    if (name.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(name);
                    }
                }
            }
        } else if (args.length > 1) {
            // Complete subcommand arguments
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && subCommand.hasPermission(sender)) {
                List<String> subCompletions = subCommand.tabComplete(sender, args);
                if (subCompletions != null) {
                    completions.addAll(subCompletions);
                }
            }
        }

        return completions;
    }

    private void sendHelpMessage(CommandSender sender) {
        MessageUtil.sendInfoMessage(sender, "Available commands:");

        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand.hasPermission(sender)) {
                MessageUtil.sendMessage(sender, "&7- &f" + subCommand.getUsage() + " &7- " + subCommand.getDescription());
            }
        }
    }
}