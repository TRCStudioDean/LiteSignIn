package studio.trc.bukkit.litesignin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import me.clip.placeholderapi.PlaceholderAPI;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.config.PreparedConfiguration;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;

public class MessageUtil
{
    private static final Map<String, String> defaultPlaceholders = new HashMap();
    private static final Map<String, BaseComponent> defaultJsonComponents = new HashMap();
    private static final Pattern hexColorPattern = Pattern.compile("#[a-fA-F0-9]{6}");
    
    @Getter
    @Setter
    private static boolean enabledPAPI = false;
    
    public static void loadPlaceholders() {
        defaultPlaceholders.clear();
        defaultPlaceholders.put("{plugin_version}", Main.getInstance().getDescription().getVersion());
        defaultPlaceholders.put("{language}", getLanguage());
        defaultPlaceholders.put("{prefix}", getPrefix());
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param message Message text.
     */
    public static void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, message, defaultPlaceholders, defaultJsonComponents);
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param message Message text.
     * @param placeholders String placeholders.
     */
    public static void sendMessage(CommandSender sender, String message, Map<String, String> placeholders) {
        sendMessage(sender, message, placeholders, defaultJsonComponents);
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param message Message text.
     * @param placeholders String placeholders.
     * @param jsonComponents JSON Messages.
     */
    public static void sendMessage(CommandSender sender, String message, Map<String, String> placeholders, Map<String, BaseComponent> jsonComponents) {
        if (sender == null) return;
        message = replacePlaceholders(sender, message, placeholders);
        if (jsonComponents.isEmpty()) {
            sender.sendMessage(message);
        } else {
            sendJSONMessage(sender, createJsonMessage(sender, message, jsonComponents));
        }
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param messages String messages.
     */
    public static void sendMessage(CommandSender sender, List<String> messages) {
        messages.stream().forEach(rawMessage -> {
            sendMessage(sender, rawMessage);
        });
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param messages String messages.
     * @param placeholders String placeholders.
     */
    public static void sendMessage(CommandSender sender, List<String> messages, Map<String, String> placeholders) {
        messages.stream().forEach(rawMessage -> {
            sendMessage(sender, rawMessage, placeholders);
        });
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param messages String messages.
     * @param placeholders String placeholders.
     * @param jsonComponents JSON messages.
     */
    public static void sendMessage(CommandSender sender, List<String> messages, Map<String, String> placeholders, Map<String, BaseComponent> jsonComponents) {
        messages.stream().forEach(rawMessage -> {
            sendMessage(sender, rawMessage, placeholders, jsonComponents);
        });
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param configuration config name.
     * @param configPath config path.
     */
    public static void sendMessage(CommandSender sender, PreparedConfiguration configuration, String configPath) {
        sendMessage(sender, configuration, configPath, defaultPlaceholders, defaultJsonComponents);
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param configuration config name.
     * @param configPath config path.
     * @param placeholders String placeholders.
     */
    public static void sendMessage(CommandSender sender, PreparedConfiguration configuration, String configPath, Map<String, String> placeholders) {
        sendMessage(sender, configuration, configPath, placeholders, defaultJsonComponents);
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param configuration config name.
     * @param configPath config path.
     * @param placeholders String placeholders.
     * @param jsonComponents JSON messages.
     */
    public static void sendMessage(CommandSender sender, PreparedConfiguration configuration, String configPath, Map<String, String> placeholders, Map<String, BaseComponent> jsonComponents) {
        List<String> messages = configuration.getStringList(getLanguage() + "." + configPath);
        if (messages.isEmpty() && !ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + configPath).equals("[]")) {
            sendMessage(sender, configuration.getString(getLanguage() + "." + configPath), placeholders, jsonComponents);
        } else {
            sendMessage(sender, messages, placeholders, jsonComponents);
        }
    }
    
    /**
     * Send json message to command sender.
     * @param sender Command sender.
     * @param components JSON components
     */
    public static void sendJSONMessage(CommandSender sender, List<BaseComponent> components) {
        if (sender instanceof Player) {
            try {
                ((Player) sender).spigot().sendMessage(components.toArray(new BaseComponent[] {}));
            } catch (Exception ex) {
                StringBuilder builder = new StringBuilder();
                components.stream().map(component -> component.toPlainText()).forEach(builder::append);
                sender.sendMessage(builder.toString());
             }
        } else {
            StringBuilder builder = new StringBuilder();
            components.stream().map(component -> component.toPlainText()).forEach(message -> {
                builder.append(message);
            });
            sender.sendMessage(builder.toString());
        }
    }
    
    /**
     * Send command message to command sender.
     * @param sender Command sender.
     * @param configPath config path.
     */
    public static void sendCommandMessage(CommandSender sender, String configPath) {
        sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Command-Messages." + configPath, defaultPlaceholders, defaultJsonComponents);
    }
    
    /**
     * Send command message to command sender.
     * @param sender Command sender.
     * @param configPath config path.
     * @param placeholders String placeholders.
     */
    public static void sendCommandMessage(CommandSender sender, String configPath, Map<String, String> placeholders) {
        sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Command-Messages." + configPath, placeholders, defaultJsonComponents);
    }
    
    /**
     * Send command message to command sender.
     * @param sender Command sender.
     * @param configPath config path.
     * @param placeholders String placeholders.
     * @param jsonComponents JSON messages.
     */
    public static void sendCommandMessage(CommandSender sender, String configPath, Map<String, String> placeholders, Map<String, BaseComponent> jsonComponents) {
        sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Command-Messages." + configPath, placeholders, jsonComponents);
    }
    
    /**
     * Replace all placeholders to the corresponding text.
     * @param message Target text.
     * @param placeholders Placeholders.
     * @return 
     */
    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (message == null) return null;
        List<TextParagraph> splitedTexts = splitIntoParagraphs(message, placeholders);
        StringBuilder string = new StringBuilder();
        splitedTexts.stream().forEach(paragraph -> {
            if (paragraph.isPlaceholder()) {
                string.append(paragraph.getText());
            } else {
                string.append(message.substring(paragraph.startsWith, paragraph.endsWith).replace("/n", "\n"));
            }
        });
        return toColor(string.toString());
    }
    
    /**
     * Replace all placeholders to the corresponding text.
     * @param sender Use for hook PlaceholderAPI
     * @param message Target text.
     * @param placeholders Placeholders.
     * @return 
     */
    public static String replacePlaceholders(CommandSender sender, String message, Map<String, String> placeholders) {
        if (message == null) return null;
        List<TextParagraph> splitedTexts = splitIntoParagraphs(message, placeholders);
        StringBuilder string = new StringBuilder();
        splitedTexts.stream().forEach(paragraph -> {
            if (paragraph.isPlaceholder()) {
                string.append(paragraph.getText());
            } else {
                string.append(toPlaceholderAPIResult(message.substring(paragraph.startsWith, paragraph.endsWith), sender).replace("/n", "\n"));
            }
        });
        return toColor(string.toString());
    }
    
    /**
     * Create Json message
     * @param sender Use for hook PlaceholderAPI
     * @param message Target text.
     * @param baseComponents Json components placeholders.
     * @return 
     */
    public static List<BaseComponent> createJsonMessage(CommandSender sender, String message, Map<String, BaseComponent> baseComponents) {
        List<TextParagraph> splitedTexts = splitIntoComponentParagraphs(message, baseComponents);
        List<BaseComponent> components = new LinkedList();
        splitedTexts.stream().forEach(paragraph -> {
            if (paragraph.isPlaceholder()) {
                components.add(paragraph.getComponent());
            } else {
                components.add(new TextComponent(toColor(toPlaceholderAPIResult(message.substring(paragraph.startsWith, paragraph.endsWith), sender).replace("/n", "\n"))));
            }
        });
        return components;
    }
    
    public static List<TextParagraph> splitIntoParagraphs(String message, Map<String, String> placeholders) {
        List<TextParagraph> splitedTexts = new LinkedList();
        splitedTexts.add(new TextParagraph(0, message.length(), message));
        placeholders.keySet().stream().filter(placeholder -> placeholder != null).map(placeholder -> {
            List<TextParagraph> newArray = new ArrayList();
            splitedTexts.stream().forEach(textParagraphs -> {
                String message_lowerCase = textParagraphs.getText().toLowerCase();
                String placeholder_lowerCase = placeholder.toLowerCase();
                if (message_lowerCase.contains(placeholder_lowerCase)) {
                    String[] splitText = message_lowerCase.split(escape(placeholder_lowerCase), -1);
                    int last = textParagraphs.startsWith;
                    for (String paragraph : splitText) {
                        int next = last + paragraph.length();
                        if (last != next) {
                            TextParagraph subParagraph = new TextParagraph(last, next, paragraph);
                            last = last + paragraph.length();
                            newArray.add(subParagraph);
                        }
                        if (last < textParagraphs.endsWith) {
                            TextParagraph insertPlaceholder = new TextParagraph(last, last + placeholder.length(), placeholders.get(placeholder), placeholder);
                            last = last + placeholder.length();
                            newArray.add(insertPlaceholder);
                        }
                    }
                } else {
                    newArray.add(textParagraphs);
                }
            });
            return newArray;
        }).forEach(newArray -> {
            splitedTexts.clear();
            splitedTexts.addAll(newArray);
        });
        return splitedTexts;
    }
    
    /**
     * Split placeholders at text into paragraphs.
     * 
     * Example: 
     *     Text: "This plugin was completed in {year}/{month}/{day}."
     *     Placeholders: "{year}"="2019", "{month}"="11", "{day}"="15"
     *     ------->
     *     "[This plugin was completed in ], [2019], [/], [11], [/], [15], [.]" as array instance.
     * 
     * @param message Target text.
     * @param baseComponents JSON components.
     * @return 
     */
    public static List<TextParagraph> splitIntoComponentParagraphs(String message, Map<String, BaseComponent> baseComponents) {
        List<TextParagraph> splitedTexts = new LinkedList();
        splitedTexts.add(new TextParagraph(0, message.length(), new TextComponent(message)));
        baseComponents.keySet().stream().filter(placeholder -> placeholder != null).map(placeholder -> {
            List<TextParagraph> newArray = new ArrayList();
            splitedTexts.stream().forEach(textParagraphs -> {
                String message_lowerCase = textParagraphs.getComponent().toPlainText().toLowerCase();
                String placeholder_lowerCase = placeholder.toLowerCase();
                if (message_lowerCase.contains(placeholder_lowerCase)) {
                    String[] splitText = message_lowerCase.split(escape(placeholder_lowerCase), -1);
                    int last = textParagraphs.startsWith;
                    for (String paragraph : splitText) {
                        int next = last + paragraph.length();
                        if (last != next) {
                            TextParagraph subParagraph = new TextParagraph(last, next, new TextComponent(paragraph));
                            last = last + paragraph.length();
                            newArray.add(subParagraph);
                        }
                        if (last < textParagraphs.endsWith) {
                            TextParagraph insertComponent = new TextParagraph(last, last + placeholder.length(), baseComponents.get(placeholder), placeholder);
                            last = last + placeholder.length();
                            newArray.add(insertComponent);
                        }
                    }
                } else {
                    newArray.add(textParagraphs);
                }
            });
            return newArray;
        }).forEach(newArray -> {
            splitedTexts.clear();
            splitedTexts.addAll(newArray);
        });
        return splitedTexts;
    }
    
    public static String escape(String text) {
        return text.replace("{", "\\{").replace("}", "\\}");
    }
    
    public static String toPlaceholderAPIResult(String text, CommandSender sender) {
        return text != null && PluginControl.usePlaceholderAPI() && sender instanceof Player ? PlaceholderAPI.setPlaceholders((Player) sender, text) : text;
    }
    
    public static String getMessage(String path) {
        return toColor(prefix(ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getString(getLanguage() + "." + path)));
    }
    
    public static String getLanguage() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Language");
    }

    public static String getPrefix() {
        return MessageUtil.toColor(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Prefix"));
    }
    
    public static String toColor(String text) {
        String version = Bukkit.getBukkitVersion();
        if (!version.startsWith("1.7") && !version.startsWith("1.8") && !version.startsWith("1.9") && !version.startsWith("1.10") &&
            !version.startsWith("1.11") && !version.startsWith("1.12") && !version.startsWith("1.13") && !version.startsWith("1.14") && !version.startsWith("1.15")) {
            try {
                Matcher matcher = hexColorPattern.matcher(text);
                while (matcher.find()) {
                    String color = text.substring(matcher.start(), matcher.end());
                    text = text.replace(color, net.md_5.bungee.api.ChatColor.of(color).toString());
                    matcher = hexColorPattern.matcher(text);
                }
            } catch (Throwable t) {}
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public static String prefix(String text) {
        return replacePlaceholders(Bukkit.getConsoleSender(), text, new HashMap());
    }
    
    public static List<String> getMessageList(String path) {
        return ConfigurationUtil.getConfig(ConfigurationType.MESSAGES).getStringList(getLanguage() + "." + path);
    }
    
    public static Map<String, String> getDefaultPlaceholders() {
        return new HashMap(defaultPlaceholders);
    }
    
    public static Map<String, BaseComponent> getDefaultComponents() {
        return new HashMap(defaultJsonComponents);
    }
    
    /**
     * Plugin langauge
     */
    public static enum Language {
        
        /**
         * Simplified Chinese
         */
        SIMPLIFIED_CHINESE("Simplified-Chinese"),
        
        /**
         * Traditional Chinese
         */
        TRADITIONAL_CHINESE("Traditional-Chinese"),
        
        /**
         * Japanese
         */
        JAPANESE("Japanese"),
        
        /**
         * English
         */
        ENGLISH("English");
        
        public static Language getLocaleLanguage() {
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
        
        private final String folderName;
        
        private Language(String folderName) {
            this.folderName = folderName;
        }
        
        public String getFolderName() {
            return folderName;
        }
    }
    
    /**
     * Use for placeholders manager.
     */
    public static class TextParagraph {
        
        private final int startsWith;
        private final int endsWith;
        private final BaseComponent component;
        private final String text;
        private final String placeholder;
        
        public TextParagraph(int startsWith, int endsWith, BaseComponent component, String placeholder) {
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.component = component;
            this.placeholder = placeholder;
            this.text = component.toPlainText();
        }
        
        public TextParagraph(int startsWith, int endsWith, BaseComponent component) {
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.component = component;
            this.placeholder = null;
            this.text = component.toPlainText();
        }
        
        public TextParagraph(int startsWith, int endsWith, String text, String placeholder) {
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.component = null;
            this.placeholder = placeholder;
            this.text = text;
        }
        
        public TextParagraph(int startsWith, int endsWith, String text) {
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.component = null;
            this.placeholder = null;
            this.text = text;
        }
        
        public int start() {
            return startsWith;
        }
        
        public int end() {
            return endsWith;
        }
        
        public boolean isPlaceholder() {
            return placeholder != null;
        }
        
        public BaseComponent getComponent() {
            return component;
        }
        
        public String getText() {
            return text;
        }
        
        public String getPlaceholder() {
            return placeholder;
        }
    }
}
