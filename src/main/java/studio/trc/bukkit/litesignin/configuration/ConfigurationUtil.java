package studio.trc.bukkit.litesignin.configuration;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationUtil
{
    private final static Map<ConfigurationType, RobustConfiguration> cacheConfig = new HashMap<>();
    
    public static RobustConfiguration getConfig(ConfigurationType fileType) {
        if (cacheConfig.containsKey(fileType)) {
            return cacheConfig.get(fileType);
        }
        RobustConfiguration config = new RobustConfiguration(fileType);
        cacheConfig.put(fileType, config);
        return config;
    }
    
    public static void reloadConfig() {
        for (ConfigurationType type : ConfigurationType.values()) {
            if (type.equals(ConfigurationType.WOOD_SIGN_SETTINGS)) {
                continue; //Because has a dedicated loading function
            }
            reloadConfig(type);
        }
    }
    
    public static boolean reloadConfig(ConfigurationType fileType) {
        fileType.saveResource(); 
        return fileType.reloadConfig();
    }
    
}