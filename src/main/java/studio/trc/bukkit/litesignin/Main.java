package studio.trc.bukkit.litesignin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import studio.trc.bukkit.litesignin.util.Updater;
import studio.trc.bukkit.litesignin.util.metrics.Metrics;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;
import studio.trc.bukkit.litesignin.util.woodsignscript.WoodSignEvent;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import studio.trc.bukkit.litesignin.command.SignInCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;

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
    private static Metrics metrics;
    
    @Override
    public void onEnable() {
        main = this;
        
        SignInPluginProperties.reloadProperties();
        
        if (!getDescription().getName().equals("LiteSignIn")) {
            SignInPluginProperties.sendOperationMessage("PluginNameChange");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        registerCommandExecutor();
        registerEvent();
        PluginControl.reload();
        JsonItemStack.reloadNMS();
        SignInPluginProperties.sendOperationMessage("PluginEnabledSuccessfully", MessageUtil.getDefaultPlaceholders());
        
        //It will run after the server is started.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (PluginControl.enableUpdater()) {
                    Updater.checkUpdate();
                }
            }
        }.runTask(main);
        
        //Metrics
        if (PluginControl.enableMetrics()) {
            metrics = new Metrics(main, 11849);
        }
    }
    
    @Override
    public void onDisable() {
        if (PluginControl.useMySQLStorage()) {
            MySQLStorage.cache.values().stream().forEach(MySQLStorage::saveData);
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "MySQL");
            SignInPluginProperties.sendOperationMessage("DatabaseSave", placeholders);
        } else if (PluginControl.useSQLiteStorage()) {
            SQLiteStorage.cache.values().stream().forEach(SQLiteStorage::saveData);
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
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
    
    public static Metrics getMetrics() {
        return metrics;
    }
    
    private void registerEvent() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Join(), Main.getInstance());
        pm.registerEvents(new Menu(), Main.getInstance());
        pm.registerEvents(new Quit(), Main.getInstance());
        pm.registerEvents(new WoodSignEvent(), Main.getInstance());
        SignInPluginProperties.sendOperationMessage("PluginListenerRegistered");
    }
    
    private void registerCommandExecutor() {
        PluginCommand command = getCommand("signin");
        SignInCommand commandExecutor = new SignInCommand();
        command.setExecutor(commandExecutor);
        command.setTabCompleter(commandExecutor);
        for (SignInSubCommandType subCommandType : SignInSubCommandType.values()) {
            SignInCommand.getSubCommands().put(subCommandType.getSubCommandName(), subCommandType.getSubCommand());
        }
        SignInPluginProperties.sendOperationMessage("PluginCommandRegistered");
    }
}
