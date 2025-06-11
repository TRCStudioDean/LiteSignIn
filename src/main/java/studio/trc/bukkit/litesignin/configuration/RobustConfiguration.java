package studio.trc.bukkit.litesignin.configuration;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.message.MessageUtil;

/**
 * Used to manage configuration files.
 * @author Dean
 */
public class RobustConfiguration
{
    @Getter
    private final ConfigurationType type;
    @Getter
    private final YamlConfiguration config;

    public RobustConfiguration(ConfigurationType type) {
        this.type = type;
        this.config = type.getConfig();
    }
    
    public void repairConfigurationSection(String path) {
        YamlConfiguration defaultFile = DefaultConfigurationFile.getDefaultConfig(type);
        config.set(path, defaultFile.get(path) != null ? defaultFile.get(path) : "null");
        type.saveConfig();
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{config}", type.getFileName());
        placeholders.put("{path}", path);
        MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationType.MESSAGES.getRobustConfig(), "Repaired-Config-Section", placeholders);
    }
    
    public Object get(String path) {
        return config.get(path);
    }

    public int getInt(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getInt(path);
        } else {
            return config.getInt(path);
        }
    }

    public int getInt(String path, int def) {
        return config.get(path) != null ? getInt(path) : def;
    }

    public double getDouble(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getDouble(path);
        } else {
            return config.getDouble(path);
        }
    }

    public double getDouble(String path, double def) {
        return config.get(path) != null ? getDouble(path) : def;
    }

    public long getLong(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getLong(path);
        } else {
            return config.getLong(path);
        }
    }

    public long getLong(String path, long def) {
        return config.get(path) != null ? getLong(path) : def;
    }

    public boolean getBoolean(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getBoolean(path);
        } else {
            return config.getBoolean(path);
        }
    }

    public boolean getBoolean(String path, boolean def) {
        return config.get(path) != null ? getBoolean(path) : def;
    }

    public String getString(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getString(path);
        } else {
            return config.getString(path);
        }
    }

    public String getString(String path, String def) {
        return config.get(path) != null ? getString(path) : def;
    }

    public List getList(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getList(path);
        } else {
            return config.getList(path);
        }
    }

    public List<String> getStringList(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getStringList(path);
        } else {
            return config.getStringList(path);
        }
    }

    public List<Integer> getIntegerList(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getIntegerList(path);
        } else {
            return config.getIntegerList(path);
        }
    }

    public ItemStack getItemStack(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getItemStack(path);
        } else {
            return config.getItemStack(path);
        }
    }

    public ItemStack getItemStack(String path, ItemStack def) {
        return config.get(path) != null ? getItemStack(path) : def;
    }

    public ConfigurationSection getConfigurationSection(String path) {
        if (config.get(path) == null) {
            repairConfigurationSection(path);
            return config.getConfigurationSection(path);
        } else {
            return config.getConfigurationSection(path);
        }
    }
    
    public boolean contains(String path) {
        return config.contains(path);
    }

    public void load(Reader reader) throws IOException, InvalidConfigurationException {
        config.load(reader);
    }
    
    public void set(String path, Object value) {
        config.set(path, value);
    }

    public void save(File file) throws IOException {
        config.save(file);
    }
}
