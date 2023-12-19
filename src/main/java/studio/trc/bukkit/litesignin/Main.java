package studio.trc.bukkit.litesignin;

import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.command.SignInCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.thread.LiteSignInThread;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.database.storage.MySQLStorage;
import studio.trc.bukkit.litesignin.database.storage.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;
import studio.trc.bukkit.litesignin.event.Menu;
import studio.trc.bukkit.litesignin.event.Quit;
import studio.trc.bukkit.litesignin.event.Join;
import studio.trc.bukkit.litesignin.nms.NMSManager;
import studio.trc.bukkit.litesignin.util.Updater;
import studio.trc.bukkit.litesignin.util.metrics.Metrics;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;
import studio.trc.bukkit.litesignin.util.woodsignscript.WoodSignEvent;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Do not resell the source code of this plug-in.
 * @author TRCStudioDean
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
        NMSManager.reloadNMS();
        SignInPluginProperties.sendOperationMessage("PluginEnabledSuccessfully", MessageUtil.getDefaultPlaceholders());
        
        //It will run after the server is started.
        Bukkit.getScheduler().runTask(this, () -> {
            if (PluginControl.enableUpdater()) {
                Updater.checkUpdate();
            }
        });
        
        //Metrics
        if (PluginControl.enableMetrics()) {
            metrics = new Metrics(main, 11849);
        }
    }
    
    @Override
    public void onDisable() {
        LiteSignInThread.getTaskThread().setRunning(false);
        SignInPluginProperties.sendOperationMessage("AsyncThreadStopped", MessageUtil.getDefaultPlaceholders());
        if (PluginControl.useMySQLStorage()) {
            MySQLStorage.cache.values().stream().forEach(MySQLStorage::saveData);
        } else if (PluginControl.useSQLiteStorage()) {
            SQLiteStorage.cache.values().stream().forEach(SQLiteStorage::saveData);
        }
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Backup.Auto-Backup")) {
            MessageUtil.sendMessage(getServer().getConsoleSender(), "Database-Management.Backup.Auto-Backup");
            Thread thread = BackupUtil.startBackup(getServer().getConsoleSender());
            while (thread.isAlive()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (SQLiteEngine.getInstance() != null) {
            SQLiteEngine.getInstance().disconnect();
        }
        if (MySQLEngine.getInstance() != null) {
            MySQLEngine.getInstance().disconnect();
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
