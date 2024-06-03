package studio.trc.bukkit.litesignin.config;

import studio.trc.bukkit.litesignin.util.MessageUtil;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.litesignin.Main;

public class DefaultConfigurationFile
{
    private final static Map<ConfigurationType, FileConfiguration> cacheDefaultConfig = new HashMap();
    private final static Map<ConfigurationType, Boolean> isDefaultConfigLoaded = new HashMap();
    
    public static FileConfiguration getDefaultConfig(ConfigurationType type) {
        if (!isDefaultConfigLoaded.containsKey(type) || !isDefaultConfigLoaded.get(type)) {
            loadDefaultConfigurationFile(type);
            isDefaultConfigLoaded.put(type, true);
        }
        return cacheDefaultConfig.get(type);
    }
    
    public static void loadDefaultConfigurationFile(ConfigurationType type) {
        String jarPath = MessageUtil.Language.getLocaleLanguage().getFolderName();
        String fileName = type.getFileName();
        if (type.equals(ConfigurationType.GUISETTINGS)) {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            if (version.startsWith("1.7") || version.startsWith("1.8") || version.startsWith("1.9") || version.startsWith("1.10") || version.startsWith("1.11") || version.startsWith("1.12")) {
                fileName = "GUISettings-OLDVERSION.yml";
            } else {
                fileName = "GUISettings-NEWVERSION.yml";
            }
        }
        if (type.equals(ConfigurationType.REWARDSETTINGS)) {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            if (version.startsWith("1.7") || version.startsWith("1.8") || version.startsWith("1.9") || version.startsWith("1.10") || version.startsWith("1.11") || version.startsWith("1.12")) {
                fileName = "RewardSettings-OLDVERSION.yml";
            } else {
                fileName = "RewardSettings-NEWVERSION.yml";
            }
        }
        try (Reader config = new InputStreamReader(Main.getInstance().getClass().getResource("/Languages/" + jarPath + "/" + fileName).openStream(), "UTF-8")) {
            FileConfiguration configFile = new YamlConfiguration();
            configFile.load(config);
            cacheDefaultConfig.put(type, configFile);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }
}
