package studio.trc.bukkit.litesignin.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import studio.trc.bukkit.litesignin.Main;

public class SignInPluginProperties
{
    /**
     * System Language
     */
    public static Properties propertiesFile = new Properties();
    
    public static void reloadProperties() {
        try {
            propertiesFile.load(Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + ".properties"));
            sendOperationMessage("LanguageLoaded");
            List<String> authors = new ArrayList();
            switch (MessageUtil.Language.getLocaleLanguage()) {
                case SIMPLIFIED_CHINESE: {
                    authors.add("红色创意工作室 (TRC Studio)");
                    break;
                }
                case TRADITIONAL_CHINESE: {
                    authors.add("紅色創意工作室 (TRC Studio)");
                    break;
                }
                default: {
                    authors.add("The Red Creative Studio (TRC Studio)");
                    break;
                }
            }
            Field field = Main.getInstance().getDescription().getClass().getDeclaredField("authors");
            field.setAccessible(true);
            field.set(Main.getInstance().getDescription(), authors);
        } catch (Exception ex) {}
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
            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, message, placeholders)));
        }
    }
}
