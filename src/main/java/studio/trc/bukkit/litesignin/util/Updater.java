package studio.trc.bukkit.litesignin.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;

public class Updater
{
    private static boolean foundANewVersion = false;
    private static String newVersion;
    private static String link;
    private static String description;
    private static Thread checkUpdateThread;
    private static Date date = new Date();
    
    /**
     * Initialize programs.
     */
    public static void initialize() {
        checkUpdateThread = new Thread(() -> {
            try {
                URL url = new URL("https://trc.studio/resources/spigot/litesignin/update.yml");
                try (Reader reader = new InputStreamReader(url.openStream(), "UTF-8")) {
                    YamlConfiguration yaml = new YamlConfiguration();
                    yaml.load(reader);
                    String version = yaml.getString("latest-version");
                    String downloadLink = yaml.getString("link");
                    String description_ = "description.Default";
                    if (yaml.get("description." + MessageUtil.getLanguage()) != null) {
                        description_ = yaml.getString("description." + MessageUtil.getLanguage());
                    } else {
                        for (String languages : yaml.getConfigurationSection("description").getKeys(false)) {
                            if (MessageUtil.getLanguage().contains(languages)) {
                                description_ = yaml.getString("description." + MessageUtil.getLanguage());
                                break;
                            }
                        }
                    }
                    String nowVersion = Bukkit.getPluginManager().getPlugin("LiteSignIn").getDescription().getVersion();
                    if (!nowVersion.equalsIgnoreCase(version)) {
                        newVersion = version;
                        foundANewVersion = true;
                        link = downloadLink;
                        description = description_;
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{version}", version);
                        placeholders.put("%link%", downloadLink);
                        placeholders.put("{link}", downloadLink);
                        placeholders.put("{nowVersion}", nowVersion);
                        placeholders.put("{description}", description_);
                        MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Checked", placeholders);
                    }
                } catch (InvalidConfigurationException | IOException ex) {
                    MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Error");
                }
            } catch (MalformedURLException ex) {
                MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Error");
            }
            date = new Date();
        }, "LiteSignIn-Updater");
    }
    
    /**
     * Start check updater.
     */
    public static void checkUpdate() {
        initialize();
        checkUpdateThread.start();
    }
    
    /**
     * Return whether found a new version.
     * @return 
     */
    public static boolean isFoundANewVersion() {
        return foundANewVersion;
    }
    
    /**
     * Get new version.
     * @return 
     */
    public static String getNewVersion() {
        return newVersion;
    }
    
    /**
     * Get download link.
     * @return 
     */
    public static String getLink() {
        return link;
    }
    
    /**
     * Get new version's update description.
     * @return 
     */
    public static String getDescription() {
        return description;
    }
    
    /**
     * Get the time of last check update.
     * @return 
     */
    public static Date getTimeOfLastCheckUpdate() {
        return date;
    }
}
