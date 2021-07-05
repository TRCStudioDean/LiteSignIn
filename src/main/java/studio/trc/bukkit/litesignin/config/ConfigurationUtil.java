package studio.trc.bukkit.litesignin.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;

public class ConfigurationUtil
{
    private final static FileConfiguration config = new YamlConfiguration();
    private final static FileConfiguration messages = new YamlConfiguration();
    private final static FileConfiguration guisettings = new YamlConfiguration();
    private final static FileConfiguration rewardsettings = new YamlConfiguration();
    private final static FileConfiguration customitems = new YamlConfiguration();
    private final static FileConfiguration WOODSIGNSETTINGS = new YamlConfiguration();
    
    public static FileConfiguration getFileConfiguration(ConfigurationType fileType) {
        switch (fileType) {
            case CONFIG: return config;
            case CUSTOMITEMS: return customitems;
            case GUISETTINGS: return guisettings;
            case MESSAGES: return messages;
            case REWARDSETTINGS: return rewardsettings;
            case WOODSIGNSETTINGS: return WOODSIGNSETTINGS;
            default: return null;
        }
    }
    
    private final static Map<ConfigurationType, Configuration> cacheConfig = new HashMap();
    
    public static Configuration getConfig(ConfigurationType fileType) {
        if (cacheConfig.containsKey(fileType)) {
            return cacheConfig.get(fileType);
        }
        switch (fileType) {
            case CONFIG: {
                Configuration file = new Configuration(config, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case CUSTOMITEMS: {
                Configuration file = new Configuration(customitems, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case GUISETTINGS: {
                Configuration file = new Configuration(guisettings, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case MESSAGES: {
                Configuration file = new Configuration(messages, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case REWARDSETTINGS: {
                Configuration file = new Configuration(rewardsettings, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            case WOODSIGNSETTINGS: {
                Configuration file = new Configuration(WOODSIGNSETTINGS, fileType);
                cacheConfig.put(fileType, file);
                return file;
            }
            default: return null;
        }
    }
    
    public static void reloadConfig(ConfigurationType fileType) {
        saveResource(fileType);
        try (InputStreamReader Config = new InputStreamReader(new FileInputStream("plugins/LiteSignIn/" + fileType.getFileName()), "UTF-8")) {
            getFileConfiguration(fileType).load(Config);
        } catch (IOException | InvalidConfigurationException ex) {
            File oldFile = new File("plugins/LiteSignIn/" + fileType.getFileName() + ".old");
            File file = new File("plugins/LiteSignIn/" + fileType.getFileName());
            Map<String, String> placeholders = new HashMap();
            placeholders.put("{file}", fileType.getFileName());
            SignInPluginProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
            if (oldFile.exists()) {
                oldFile.delete();
            }
            file.renameTo(oldFile);
            saveResource(fileType);
            try (InputStreamReader newConfig = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                getFileConfiguration(fileType).load(newConfig);
                SignInPluginProperties.sendOperationMessage("ConfigurationRepair", true);
            } catch (IOException | InvalidConfigurationException ex1) {
                ex1.printStackTrace();
            }
        }
    }
    
    public static void reloadConfig() {
        for (ConfigurationType type : ConfigurationType.values()) {
            if (type.equals(ConfigurationType.WOODSIGNSETTINGS)) {
                continue;
            }
            reloadConfig(type);
        }
    }

    private static void saveResource(ConfigurationType file) {
        if (!new File("plugins/LiteSignIn").exists()) {
            new File("plugins/LiteSignIn").mkdir();
        }
        switch (file) {
            case CONFIG: {
                try {
                    File configFile = new File("plugins/LiteSignIn/Config.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Languages.getLocaleLanguage().getFileName() + "/Config.yml");
                        try (OutputStream out = new FileOutputStream(configFile)) {
                            int b;
                            while ((b = is.read()) != -1) {
                                out.write((char) b);
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
            case MESSAGES: {
                try {
                    File configFile = new File("plugins/LiteSignIn/Messages.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Languages.getLocaleLanguage().getFileName() + "/Messages.yml");
                        try (OutputStream out = new FileOutputStream(configFile)) {
                            int b;
                            while ((b = is.read()) != -1) {
                                out.write((char) b);
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
            case GUISETTINGS: {
                try {
                    File configFile = new File("plugins/LiteSignIn/GUISettings.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        String version = PluginControl.nmsVersion;
                        if (version.startsWith("v1_7") || version.startsWith("v1_8") || version.startsWith("v1_9") || version.startsWith("v1_10") || version.startsWith("v1_11") || version.startsWith("v1_12")) {
                            InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Languages.getLocaleLanguage().getFileName() + "/GUISettings-OLDVERSION.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
                            }
                        } else {
                            InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Languages.getLocaleLanguage().getFileName() + "/GUISettings-NEWVERSION.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
            case REWARDSETTINGS: {
                try {
                    File configFile = new File("plugins/LiteSignIn/RewardSettings.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        String version = PluginControl.nmsVersion;
                        if (version.startsWith("v1_7") || version.startsWith("v1_8")) {
                            InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Languages.getLocaleLanguage().getFileName() + "/RewardSettings-OLDVERSION.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
                            }
                        } else {
                            InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Languages.getLocaleLanguage().getFileName() + "/RewardSettings-NEWVERSION.yml");
                            try (OutputStream out = new FileOutputStream(configFile)) {
                                int b;
                                while ((b = is.read()) != -1) {
                                    out.write((char) b);
                                }
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
            case CUSTOMITEMS: {
                try {
                    File configFile = new File("plugins/LiteSignIn/CustomItems.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Languages.getLocaleLanguage().getFileName() + "/CustomItems.yml");
                        try (OutputStream out = new FileOutputStream(configFile)) {
                            int b;
                            while ((b = is.read()) != -1) {
                                out.write((char) b);
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
            case WOODSIGNSETTINGS: {
                try {
                    File configFile = new File("plugins/LiteSignIn/WoodSignSettings.yml");
                    if (!configFile.exists()) {
                        configFile.createNewFile();
                        InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Languages.getLocaleLanguage().getFileName() + "/WoodSignSettings.yml");
                        try (OutputStream out = new FileOutputStream(configFile)) {
                            int b;
                            while ((b = is.read()) != -1) {
                                out.write((char) b);
                            }
                        }
                    }
                } catch (IOException ex) {}
                break;
            }
        }
    }
    
    public static void saveConfig() {
        for (ConfigurationType type : ConfigurationType.values()) {
            if (type.equals(ConfigurationType.WOODSIGNSETTINGS)) {
                continue;
            }
            saveConfig(type);
        }
    }
    
    public static void saveConfig(ConfigurationType file) {
        try {
            switch (file) {
                case CONFIG: {
                    config.save("plugins/LiteSignIn/Config.yml");
                    break;
                }
                case CUSTOMITEMS: {
                    customitems.save("plugins/LiteSignIn/CustomItems.yml");
                    break;
                }
                case GUISETTINGS: {
                    guisettings.save("plugins/LiteSignIn/GUISettings.yml");
                    break;
                }
                case MESSAGES: {
                    messages.save("plugins/LiteSignIn/Messages.yml");
                    break;
                }
                case REWARDSETTINGS: {
                    rewardsettings.save("plugins/LiteSignIn/RewardSettings.yml");
                    break;
                }
                case WOODSIGNSETTINGS: {
                    rewardsettings.save("plugins/LiteSignIn/WoodSignSettings.yml");
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PluginControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
