package studio.trc.bukkit.litesignin;

import java.util.HashMap;
import java.util.Map;
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
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import studio.trc.bukkit.litesignin.updater.CheckUpdater;

/**
 * Do not resell the source code of this plug-in.
 * @author TRCRedstoner
 */
public class Main
    extends JavaPlugin
{
    /**
     * Main instance
     */
    private static Main main;
    
    @Override
    public void onEnable() {
        main = this;
        
        SignInPluginProperties.reloadProperties();
        
        if (!getDescription().getName().equals("LiteSignIn")) {
            SignInPluginProperties.sendOperationMessage("PluginNameChange");
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
        CheckUpdater.initialize();
        SignInPluginProperties.sendOperationMessage("PluginEnabledSuccessfully", true);
        
        //It will run after the server is started.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (PluginControl.enableUpdater()) {
                    CheckUpdater.checkUpdate();
                }
            }
        }.runTask(main);
    }
    
    @Override
    public void onDisable() {
        if (PluginControl.useMySQLStorage()) {
            MySQLStorage.cache.values().stream().forEach(MySQLStorage::saveData);
            Map<String, String> placeholders = new HashMap();
            placeholders.put("{database}", "MySQL");
            SignInPluginProperties.sendOperationMessage("DatabaseSave", placeholders);
        } else if (PluginControl.useSQLiteStorage()) {
            SQLiteStorage.cache.values().stream().forEach(SQLiteStorage::saveData);
            Map<String, String> placeholders = new HashMap();
            placeholders.put("{database}", "SQLite");
            SignInPluginProperties.sendOperationMessage("DatabaseSave", placeholders);
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
