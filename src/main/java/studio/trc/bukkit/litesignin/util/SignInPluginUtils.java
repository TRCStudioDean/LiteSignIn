package studio.trc.bukkit.litesignin.util;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.config.Configuration;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;

public class SignInPluginUtils
{
    /*
     * Special prompts
     */
    public static void playerOnly() {
        MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Player-Only");
    }
    
    public static void noPermission(CommandSender sender) {
        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "No-Permission");
    }
    
    public static void playerNotExist(CommandSender sender, String playerName) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{player}", playerName);
        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Player-Not-Exist", placeholders);
    }
    /*
     *
     */
    
    public static boolean isInteger(String value) {
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
    
    public static boolean isPlayer(CommandSender sender, boolean report) {
        boolean value = sender instanceof Player;
        if (report && !value) {
            playerOnly();
        }
        return value;
    }
    
    public static boolean hasPermission(CommandSender sender, String configPath) {
        Configuration config = ConfigurationUtil.getConfig(ConfigurationType.CONFIG);
        if (config.getBoolean("Permissions." + configPath + ".Default")) return true;
        return sender.hasPermission(config.getString("Permissions." + configPath + ".Permission"));
    }
    
    public static boolean hasCommandPermission(CommandSender sender, String configPath, boolean report) {
        if (configPath == null) return true;
        boolean value = hasPermission(sender, "Commands." + configPath);
        if (report && !value) {
            noPermission(sender);
        }
        return value;
    }
}
