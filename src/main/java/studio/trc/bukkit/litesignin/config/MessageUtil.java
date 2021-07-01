package studio.trc.bukkit.litesignin.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.clip.placeholderapi.PlaceholderAPI;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.util.PluginControl;

public class MessageUtil
{
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param path Messages.yml's path
     */
    public static void sendMessage(CommandSender sender, String path) {
        if (sender == null) return;
        List<String> messages = ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getStringList(getLanguage() + "." + path);
        if (messages.isEmpty() && !ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path).equals("[]")) {
            String message = ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n");
            if (PluginControl.usePlaceholderAPI() && sender instanceof Player) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders((Player) sender, message.replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n")));
            }
        } else {
            for (String message : messages) {
                if (PluginControl.usePlaceholderAPI() && sender instanceof Player) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders((Player) sender, message.replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n")));
                }
            }
        }
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param path Messages.yml's path
     * @param placeholders If the text contains a placeholder,
     *                      The placeholder will be replaced with the specified text.
     */
    public static void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        if (sender == null) return;
        List<String> messages = ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getStringList(getLanguage() + "." + path);
        if (messages.isEmpty() && !ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path).equals("[]")) {
            String message = ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n");
            for (String keySet : placeholders.keySet()) {
                message = message.replace(keySet, placeholders.get(keySet));
            }
            if (PluginControl.usePlaceholderAPI() && sender instanceof Player) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders((Player) sender, message)));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        } else {
            for (String message : messages) {
                for (String keySet : placeholders.keySet()) {
                    message = message.replace(keySet, placeholders.get(keySet));
                }
                if (PluginControl.usePlaceholderAPI() && sender instanceof Player) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders((Player) sender, message.replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n")));
                }
            }
        }
    }
    
    public static void sendJsonMessage(CommandSender sender, String message, Map<String, BaseComponent> baseComponents) {
        if (sender instanceof Player) {
            BaseComponent[] rawBCMessage = new BaseComponent[] {new TextComponent(message)};
            for (String placeholder : baseComponents.keySet()) {
                List<BaseComponent> newArray = new ArrayList();
                for (BaseComponent bc : rawBCMessage) {
                    String plainText = bc.toPlainText();
                    if (plainText.contains(placeholder)) {
                        String[] split = plainText.split(placeholder);
                        int end = 0;
                        for (String splitText : split) {
                            end++;
                            newArray.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', toPlaceholderAPIResult(splitText, sender).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
                            if (end < split.length || plainText.endsWith(placeholder)) {
                                newArray.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', toPlaceholderAPIResult(placeholder, sender).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
                            }
                        }
                    } else {
                        newArray.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', toPlaceholderAPIResult(bc.toPlainText(), sender).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
                    }
                }
                rawBCMessage = newArray.toArray(new BaseComponent[0]);
            }
            List<BaseComponent> bcMessage = new ArrayList();
            for (BaseComponent bc : rawBCMessage) {
                boolean non = true;
                for (String placeholder : baseComponents.keySet()) {
                    if (bc.toPlainText().equals(placeholder)) {
                        bcMessage.add(baseComponents.get(placeholder));
                        non = false;
                        break;
                    }
                }
                if (non) bcMessage.add(bc);
            }
            ((Player) sender).spigot().sendMessage(bcMessage.toArray(new BaseComponent[0]));
        } else {
            for (String placeholder : baseComponents.keySet()) {
                message = message.replace(placeholder, baseComponents.get(placeholder).toPlainText());
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    public static void sendJsonMessage(CommandSender sender, String message, Map<String, BaseComponent> baseComponents, Map<String, String> placeholders) {
        for (String keySet : placeholders.keySet()) {
            message = message.replace(keySet, placeholders.get(keySet));
        }
        if (sender instanceof Player) {
            BaseComponent[] rawBCMessage = new BaseComponent[] {new TextComponent(message)};
            for (String placeholder : baseComponents.keySet()) {
                List<BaseComponent> newArray = new ArrayList();
                for (BaseComponent bc : rawBCMessage) {
                    String plainText = bc.toPlainText();
                    if (plainText.contains(placeholder)) {
                        String[] split = plainText.split(placeholder);
                        int end = 0;
                        for (String splitText : split) {
                            end++;
                            newArray.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', toPlaceholderAPIResult(splitText, sender).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
                            if (end < split.length || plainText.endsWith(placeholder)) {
                                newArray.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', toPlaceholderAPIResult(placeholder, sender).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
                            }
                        }
                    } else {
                        newArray.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', toPlaceholderAPIResult(bc.toPlainText(), sender).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"))));
                    }
                }
                rawBCMessage = newArray.toArray(new BaseComponent[0]);
            }
            List<BaseComponent> bcMessage = new ArrayList();
            for (BaseComponent bc : rawBCMessage) {
                boolean non = true;
                for (String placeholder : baseComponents.keySet()) {
                    if (bc.toPlainText().equals(placeholder)) {
                        bcMessage.add(baseComponents.get(placeholder));
                        non = false;
                        break;
                    }
                }
                if (non) bcMessage.add(bc);
            }
            ((Player) sender).spigot().sendMessage(bcMessage.toArray(new BaseComponent[0]));
        } else {
            for (String placeholder : baseComponents.keySet()) {
                message = message.replace(placeholder, baseComponents.get(placeholder).toPlainText());
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    public static String toPlaceholderAPIResult(String text, CommandSender sender) {
        return PluginControl.usePlaceholderAPI() && sender instanceof Player ? PlaceholderAPI.setPlaceholders((Player) sender, text) : text;
    }
    
    public static String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path).replace("{prefix}", PluginControl.getPrefix()));
    }
    
    public static List<String> getMessageList(String path) {
        return ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getStringList(getLanguage() + "." + path);
    }
    
    public static String getLanguage() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Language");
    }
    
    public static enum Languages {
        
        SIMPLIFIED_CHINESE("Simplified-Chinese"),
        
        TRADITIONAL_CHINESE("Traditional-Chinese"),
        
        JAPANESE("Japanese"),
        
        ENGLISH("English");
        
        public static Languages getLocaleLanguage() {
            Locale lang = Locale.getDefault();
            if (lang.equals(Locale.SIMPLIFIED_CHINESE)) {
                return SIMPLIFIED_CHINESE;
            } else if (lang.equals(Locale.TRADITIONAL_CHINESE)) {
                return TRADITIONAL_CHINESE;
            } else if (lang.equals(Locale.JAPANESE) || lang.equals(Locale.JAPAN)) {
                return JAPANESE;
            } if (lang.equals(Locale.CHINA) || lang.equals(Locale.CHINESE)) {
                return SIMPLIFIED_CHINESE;
            } else {
                return ENGLISH;
            }
        }
        
        private final String fileName;
        
        private Languages(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFileName() {
            return fileName;
        }
    }
}
