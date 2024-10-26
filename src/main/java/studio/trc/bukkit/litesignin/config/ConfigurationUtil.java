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

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;
import studio.trc.bukkit.litesignin.util.MessageUtil;

public class ConfigurationUtil
{
    private final static Map<ConfigurationType, PreparedConfiguration> cache = new HashMap();
    
    public static PreparedConfiguration getConfig(ConfigurationType type) {
        if (cache.get(type) == null) {
            cache.put(type, new PreparedConfiguration(new YamlConfiguration(), type));
        }
        return cache.get(type);
    }
    
    public static FileConfiguration getFileConfiguration(ConfigurationType fileType) {
        return getConfig(fileType).getRawConfig();
    }
    
    public static void reloadConfig(ConfigurationType fileType) {
        saveResource(fileType);
        try (InputStreamReader Config = new InputStreamReader(new FileInputStream("plugins/LiteSignIn/" + fileType.getFileName()), "UTF-8")) {
            getFileConfiguration(fileType).load(Config);
        } catch (IOException | InvalidConfigurationException ex) {
            File oldFile = new File("plugins/LiteSignIn/" + fileType.getFileName() + ".old");
            File file = new File("plugins/LiteSignIn/" + fileType.getFileName());
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{file}", fileType.getFileName());
            SignInPluginProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
            if (oldFile.exists()) {
                oldFile.delete();
            }
            file.renameTo(oldFile);
            saveResource(fileType);
            try (InputStreamReader newConfig = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                getFileConfiguration(fileType).load(newConfig);
                SignInPluginProperties.sendOperationMessage("ConfigurationRepair", new HashMap());
            } catch (IOException | InvalidConfigurationException ex1) {
                ex1.printStackTrace();
            }
        }
    }
    
    public static void reloadConfig() {
        for (ConfigurationType type : ConfigurationType.values()) {
            if (type.equals(ConfigurationType.WOOD_SIGN_SETTINGS)) {
                continue; //Because has a dedicated loading function
            }
            reloadConfig(type);
        }
    }
    
    public static void saveResource(ConfigurationType fileType) {
        if (!new File("plugins/LiteSignIn").exists()) {
            new File("plugins/LiteSignIn").mkdir();
        }
        try {
            File configFile = new File("plugins/LiteSignIn/" + fileType.getFileName());
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream is;
                if (fileType.equals(ConfigurationType.GUI_SETTINGS) || fileType.equals(ConfigurationType.REWARD_SETTINGS)) {
                    String version = Bukkit.getBukkitVersion();
                    if (version.startsWith("1.7") || version.startsWith("1.8") || version.startsWith("1.9") || version.startsWith("1.10") || version.startsWith("1.11") || version.startsWith("1.12")) {
                        is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/" + fileType.getFileName().replace(".yml", "") + "-OLDVERSION.yml");
                    } else {
                        is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/" + fileType.getFileName().replace(".yml", "") + "-NEWVERSION.yml");
                    }
                } else {
                    is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/" + fileType.getFileName());
                }
                byte[] bytes = new byte[is.available()];
                for (int len = 0; len != bytes.length; len += is.read(bytes, len, bytes.length - len));
                try (OutputStream out = new FileOutputStream(configFile)) {
                    out.write(bytes);
                }
            }
        } catch (IOException ex) {}
    }

    public static void saveConfig() {
        for (ConfigurationType type : ConfigurationType.values()) {
            if (type.equals(ConfigurationType.WOOD_SIGN_SETTINGS)) {
                continue; //Because has a dedicated saving function
            }
            saveConfig(type);
        }
    }
    
    public static void saveConfig(ConfigurationType fileType) {
        try {
            cache.get(fileType).getRawConfig().save("plugins/LiteSignIn/" + fileType.getFileName());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}