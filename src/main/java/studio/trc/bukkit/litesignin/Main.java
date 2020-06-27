package studio.trc.bukkit.litesignin;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import studio.trc.bukkit.litesignin.command.SignInCommand;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.MessageUtil;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.database.MySQLStorage;
import studio.trc.bukkit.litesignin.database.SQLiteStorage;
import studio.trc.bukkit.litesignin.event.Menu;
import studio.trc.bukkit.litesignin.event.Quit;
import studio.trc.bukkit.litesignin.event.Join;
import studio.trc.bukkit.litesignin.nms.JsonItemStack;
import studio.trc.bukkit.litesignin.util.PluginControl;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Do not resell the source code of this plug-in.
 * @author TRCRedstoner
 */
public class Main
    extends JavaPlugin
{
    
    /**
     * System Language
     */
    public static Properties language = new Properties();
    public static String lang = Locale.getDefault().toString();
    
    /**
     * Main instance
     */
    private static Main main;
    
    @Override
    public void onEnable() {
        main = this;
        
        if (lang.equalsIgnoreCase("zh_cn")) {
            try {
                language.load(getClass().getResourceAsStream("/Languages/Chinese.properties"));
            } catch (IOException ex) {}
        } else {
            try {
                language.load(getClass().getResourceAsStream("/Languages/English.properties"));
            } catch (IOException ex) {}
        }
        if (language.get("LanguageLoaded") != null) getServer().getConsoleSender().sendMessage(language.getProperty("LanguageLoaded").replace("&", "§"));
        
        if (!getDescription().getName().equals("LiteSignIn")) {
            if (language.get("PluginNameChange") != null) getServer().getConsoleSender().sendMessage(language.getProperty("PluginNameChange").replace("&", "§"));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        PluginControl.reload();
        getCommand("signin").setExecutor(new SignInCommand());
        getCommand("signin").setTabCompleter(new SignInCommand());
        getCommand("litesignin").setExecutor(new SignInCommand());
        getCommand("litesignin").setTabCompleter(new SignInCommand());
        registerEvent();
        JsonItemStack.reloadNMS();
        if (language.get("PluginEnabledSuccessfully") != null) getServer().getConsoleSender().sendMessage(language.getProperty("PluginEnabledSuccessfully").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
    }
    
    @Override
    public void onDisable() {
        if (PluginControl.useMySQLStorage()) {
            for (MySQLStorage data : MySQLStorage.cache.values()) {
                data.saveData();
            }
            if (language.get("DatabaseSave") != null) getServer().getConsoleSender().sendMessage(language.getProperty("DatabaseSave").replace("{database}", "MySQL").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
        } else if (PluginControl.useSQLiteStorage()) {
            for (SQLiteStorage data : SQLiteStorage.cache.values()) {
                data.saveData();
            }
            if (language.get("DatabaseSave") != null) getServer().getConsoleSender().sendMessage(language.getProperty("DatabaseSave").replace("{database}", "SQLite").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
        }
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Backup.Auto-Backup")) {
            MessageUtil.sendMessage(getServer().getConsoleSender(), "Database-Management.Backup.Auto-Backup");
            Thread thread = BackupUtil.startBackup(getServer().getConsoleSender());
            while (thread.isAlive()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static Main getInstance() {
        return main;
    }
    
    public static void registerEvent() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Join(), Main.getInstance());
        pm.registerEvents(new Menu(), Main.getInstance());
        pm.registerEvents(new Quit(), Main.getInstance());
    }
}
