package studio.trc.bukkit.litesignin.util;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import studio.trc.bukkit.litesignin.Main;

public class SignInPluginProperties
{
    /**
     * System Language
     */
    public static Properties propertiesFile = new Properties();
    public static Locale lang = Locale.getDefault();
    
    public static void reloadProperties() {
        if (lang.equals(Locale.SIMPLIFIED_CHINESE) || lang.equals(Locale.CHINESE)) {
            try {
                propertiesFile.load(Main.class.getResourceAsStream("/Languages/Chinese.properties"));
            } catch (IOException ex) {}
        } else {
            try {
                propertiesFile.load(Main.class.getResourceAsStream("/Languages/English.properties"));
            } catch (IOException ex) {}
        }
        sendOperationMessage("LanguageLoaded");
    }
    
    public static void sendOperationMessage(String path) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', propertiesFile.getProperty(path)));
        }
    }
    
    public static void sendOperationMessage(String path, boolean replacePrefix) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            if (replacePrefix) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', propertiesFile.getProperty(path).replace("{prefix}", PluginControl.getPrefix())));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', propertiesFile.getProperty(path)));
            }
        }
    }
    
    public static void sendOperationMessage(String path, Map<String, String> placeholders) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            String message = propertiesFile.getProperty(path);
            for (String placeholder : placeholders.keySet()) {
                message = message.replace(placeholder, placeholders.get(placeholder));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", PluginControl.getPrefix())));
        }
    }
}
