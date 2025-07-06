package me.natesoftware.lastManStanding.util;

import me.natesoftware.lastManStanding.LastManStanding;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class MessageUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            Component component = LEGACY_SERIALIZER.deserialize(getPrefix() + message);
            sender.sendMessage(component);
        } else {
            sender.sendMessage(stripColor(getPrefix() + message));
        }
    }

    public static void sendSuccessMessage(CommandSender sender, String message) {
        sendMessage(sender, "&a" + message);
    }

    public static void sendErrorMessage(CommandSender sender, String message) {
        sendMessage(sender, "&c" + message);
    }

    public static void sendWarningMessage(CommandSender sender, String message) {
        sendMessage(sender, "&e" + message);
    }

    public static void sendInfoMessage(CommandSender sender, String message) {
        sendMessage(sender, "&b" + message);
    }

    public static void broadcast(String message) {
        Component component = LEGACY_SERIALIZER.deserialize(getPrefix() + message);
        Bukkit.broadcast(component);
    }

    public static void broadcastSuccess(String message) {
        broadcast("&a" + message);
    }

    public static void broadcastError(String message) {
        broadcast("&c" + message);
    }

    public static void broadcastWarning(String message) {
        broadcast("&e" + message);
    }

    public static void broadcastInfo(String message) {
        broadcast("&b" + message);
    }

    // Console logging methods
    public static void logInfo(String message) {
        LastManStanding.getInstance().getLogger().info(message);
    }

    public static void logWarning(String message) {
        LastManStanding.getInstance().getLogger().warning(message);
    }

    public static void logError(String message) {
        LastManStanding.getInstance().getLogger().severe(message);
    }

    public static void logError(String message, Throwable throwable) {
        LastManStanding.getInstance().getLogger().log(Level.SEVERE, message, throwable);
    }

    // Utility methods
    private static String getPrefix() {
        try {
            return LastManStanding.getInstance().getConfigManager().getPrefix();
        } catch (Exception e) {
            return "&6[LMS] &r";
        }
    }

    private static String stripColor(String message) {
        return message.replaceAll("&[0-9a-fk-or]", "");
    }

    // Format time in seconds to MM:SS
    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    // Create action bar message
    public static Component createActionBar(String message) {
        return LEGACY_SERIALIZER.deserialize(message);
    }

    // Create title message
    public static Component createTitle(String message) {
        return LEGACY_SERIALIZER.deserialize(message)
                .decoration(TextDecoration.BOLD, true);
    }

    // Create subtitle message
    public static Component createSubtitle(String message) {
        return LEGACY_SERIALIZER.deserialize(message)
                .color(NamedTextColor.GRAY);
    }
}