package me.natesoftware.lastManStanding.command.commands;

import me.natesoftware.lastManStanding.LastManStanding;
import me.natesoftware.lastManStanding.command.SubCommand;
import me.natesoftware.lastManStanding.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsSubCommand extends SubCommand {

    private final LastManStanding plugin;

    public SettingsSubCommand(LastManStanding plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String getDescription() {
        return "Manage game settings";
    }

    @Override
    public String getUsage() {
        return "/lms settings <setting> [value]";
    }

    @Override
    public String getPermission() {
        return "lms.host";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Show all settings
            showAllSettings(sender);
            return;
        }

        if (args.length == 2) {
            // Show specific setting
            showSetting(sender, args[1]);
            return;
        }

        if (args.length == 3) {
            // Set a setting
            setSetting(sender, args[1], args[2]);
            return;
        }

        MessageUtil.sendErrorMessage(sender, "Usage: " + getUsage());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            // Complete setting names
            List<String> settings = Arrays.asList("maxplayers", "minplayers", "countdown", "modifier");
            for (String setting : settings) {
                if (setting.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(setting);
                }
            }
        } else if (args.length == 3) {
            // Complete values based on setting
            String setting = args[1].toLowerCase();
            switch (setting) {
                case "maxplayers":
                    completions.addAll(Arrays.asList("8", "10", "12", "16", "20"));
                    break;
                case "minplayers":
                    completions.addAll(Arrays.asList("2", "3", "4", "5"));
                    break;
                case "countdown":
                    completions.addAll(Arrays.asList("5", "10", "15", "20", "30"));
                    break;
                case "modifier":
                    completions.addAll(Arrays.asList("none", "speed", "strength", "resistance"));
                    break;
            }
        }

        return completions;
    }

    private void showAllSettings(CommandSender sender) {
        MessageUtil.sendInfoMessage(sender, "=== Game Settings ===");
        MessageUtil.sendMessage(sender, "&7Max Players: &f" + plugin.getConfigManager().getMaxPlayers());
        MessageUtil.sendMessage(sender, "&7Min Players: &f" + plugin.getConfigManager().getMinPlayers());
        MessageUtil.sendMessage(sender, "&7Countdown: &f" + plugin.getConfigManager().getCountdownTime() + "s");
        MessageUtil.sendMessage(sender, "&7Modifier: &f" + plugin.getConfigManager().getDefaultModifier());
        MessageUtil.sendMessage(sender, "&7Use &f/lms settings <setting> <value> &7to change");
    }

    private void showSetting(CommandSender sender, String setting) {
        switch (setting.toLowerCase()) {
            case "maxplayers":
                MessageUtil.sendMessage(sender, "&7Max Players: &f" + plugin.getConfigManager().getMaxPlayers());
                break;
            case "minplayers":
                MessageUtil.sendMessage(sender, "&7Min Players: &f" + plugin.getConfigManager().getMinPlayers());
                break;
            case "countdown":
                MessageUtil.sendMessage(sender, "&7Countdown: &f" + plugin.getConfigManager().getCountdownTime() + "s");
                break;
            case "modifier":
                MessageUtil.sendMessage(sender, "&7Modifier: &f" + plugin.getConfigManager().getDefaultModifier());
                break;
            default:
                MessageUtil.sendErrorMessage(sender, "Unknown setting: " + setting);
                MessageUtil.sendMessage(sender, "&7Available: maxplayers, minplayers, countdown, modifier");
        }
    }

    private void setSetting(CommandSender sender, String setting, String value) {
        switch (setting.toLowerCase()) {
            case "maxplayers":
                try {
                    int maxPlayers = Integer.parseInt(value);
                    if (maxPlayers < 2 || maxPlayers > 50) {
                        MessageUtil.sendErrorMessage(sender, "Max players must be between 2 and 50!");
                        return;
                    }
                    if (maxPlayers < plugin.getConfigManager().getMinPlayers()) {
                        MessageUtil.sendErrorMessage(sender, "Max players cannot be less than min players!");
                        return;
                    }
                    plugin.getConfigManager().setMaxPlayers(maxPlayers);
                    MessageUtil.sendSuccessMessage(sender, "Max players set to " + maxPlayers);
                } catch (NumberFormatException e) {
                    MessageUtil.sendErrorMessage(sender, "Invalid number: " + value);
                }
                break;

            case "minplayers":
                try {
                    int minPlayers = Integer.parseInt(value);
                    if (minPlayers < 2 || minPlayers > 20) {
                        MessageUtil.sendErrorMessage(sender, "Min players must be between 2 and 20!");
                        return;
                    }
                    if (minPlayers > plugin.getConfigManager().getMaxPlayers()) {
                        MessageUtil.sendErrorMessage(sender, "Min players cannot be more than max players!");
                        return;
                    }
                    plugin.getConfigManager().setMinPlayers(minPlayers);
                    MessageUtil.sendSuccessMessage(sender, "Min players set to " + minPlayers);
                } catch (NumberFormatException e) {
                    MessageUtil.sendErrorMessage(sender, "Invalid number: " + value);
                }
                break;

            case "countdown":
                try {
                    int countdown = Integer.parseInt(value);
                    if (countdown < 3 || countdown > 60) {
                        MessageUtil.sendErrorMessage(sender, "Countdown must be between 3 and 60 seconds!");
                        return;
                    }
                    plugin.getConfigManager().setCountdownTime(countdown);
                    MessageUtil.sendSuccessMessage(sender, "Countdown set to " + countdown + " seconds");
                } catch (NumberFormatException e) {
                    MessageUtil.sendErrorMessage(sender, "Invalid number: " + value);
                }
                break;

            case "modifier":
                // Validate modifier (you can expand this list)
                List<String> validModifiers = Arrays.asList("none", "speed", "strength", "resistance");
                if (!validModifiers.contains(value.toLowerCase())) {
                    MessageUtil.sendErrorMessage(sender, "Invalid modifier! Valid options: " +
                            String.join(", ", validModifiers));
                    return;
                }
                plugin.getConfigManager().setDefaultModifier(value.toLowerCase());
                MessageUtil.sendSuccessMessage(sender, "Modifier set to " + value.toLowerCase());
                break;

            default:
                MessageUtil.sendErrorMessage(sender, "Unknown setting: " + setting);
                MessageUtil.sendMessage(sender, "&7Available: maxplayers, minplayers, countdown, modifier");
        }
    }
}