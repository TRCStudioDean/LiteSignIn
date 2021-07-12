package studio.trc.bukkit.litesignin.util;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.config.MessageUtil;

public class SignInPluginProperties
{
    /**
     * System Language
     */
    public static Properties propertiesFile = new Properties();
    
    public static void reloadProperties() {
        try {
            propertiesFile.load(Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + ".properties"));
        } catch (IOException ex) {}
        sendOperationMessage("LanguageLoaded");
    }
    
    public static void sendOperationMessage(String path) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            sender.sendMessage(MessageUtil.toColor(propertiesFile.getProperty(path)));
        }
    }
    
    public static void sendOperationMessage(String path, Map<String, String> placeholders) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            String message = propertiesFile.getProperty(path);
            for (String placeholder : placeholders.keySet()) {
                message = message.replace(placeholder, placeholders.get(placeholder));
            }
            sender.sendMessage(MessageUtil.toColor(MessageUtil.prefix(message)));
        }
    }
}
