package studio.trc.bukkit.litesignin.async;

import org.bukkit.Bukkit;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.MessageUtil;
import studio.trc.bukkit.litesignin.database.MySQLStorage;
import studio.trc.bukkit.litesignin.database.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.YamlStorage;
import studio.trc.bukkit.litesignin.util.PluginControl;

public class AutoSave
    extends Thread
{
    public static boolean inOperation = false;
    public static Thread thread;
  
    @Override
    public void run() {
        while (inOperation) {
            try {
                if (PluginControl.dataAutoSave()) {
                    if (!ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Auto-Save.Only-MySQL")) {
                        YamlStorage.cache.values().stream().forEach(YamlStorage::saveData);
                        SQLiteStorage.cache.values().stream().forEach(SQLiteStorage::saveData);
                    }
                    MySQLStorage.cache.values().stream().forEach(MySQLStorage::saveData);
                    MessageUtil.sendMessage(Bukkit.getConsoleSender(), "Auto-Save");
                }
            } catch (Exception e) {}
            try {
                sleep(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("Auto-Save.Delay") < 1 ? 60000 : (long) (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("Auto-Save.Delay") * 60000));
            } catch (InterruptedException ex) {}
        }
    }
  
    public static void startThread() {
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Auto-Save.Enabled")) {
            thread = new AutoSave();
            if (!inOperation) {
                inOperation = true;
                thread.start();
            }
        }
    }
  
    public static void stopThread() {
        if (inOperation) {
            inOperation = false;
            thread.stop();
        }
    }
}
