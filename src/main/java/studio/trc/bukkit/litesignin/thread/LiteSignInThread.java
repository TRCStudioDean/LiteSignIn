package studio.trc.bukkit.litesignin.thread;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;

import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.database.DatabaseTable;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.database.util.RollBackUtil;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;

public class LiteSignInThread
    extends Thread
{
    @Getter
    private static LiteSignInThread taskThread = null;
    
    @Getter
    @Setter
    private boolean running = false;
    @Getter
    private final List<LiteSignInTask> tasks = new LinkedList();
    
    @Getter
    private final double delay;
    
    public LiteSignInThread(double delay) {
        super("LiteSignIn-Pool");
        this.delay = delay;
    }

    @Override
    public void run() {
        running = true;
        List<LiteSignInTask> cache = new LinkedList();
        while (running) {
            try {
                long usedTime = System.currentTimeMillis();
                if (!BackupUtil.isBackingUp() && !RollBackUtil.isRollingback()) {
                    synchronized (tasks) {
                        cache.clear();
                        cache.addAll(tasks);
                        cache.stream().filter(task -> {
                            if (task.getTotalExecuteTimes() != -1 && task.getExecuteTimes() >= task.getTotalExecuteTimes()) {
                                tasks.remove(task);
                                return false;
                            }
                            return !(task.isOnlyPlayersOnline() && Bukkit.getOnlinePlayers().isEmpty());
                        }).forEach(LiteSignInTask::run);
                    }
                }
                long speed = ((long) (delay * 1000)) - (System.currentTimeMillis() - usedTime);
                if (speed >= 0) sleep(speed);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void initialize() {
        if (taskThread != null && taskThread.running) {
            taskThread.running = false;
        }
        taskThread = new LiteSignInThread(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("Async-Thread-Settings.Task-Thread-Delay"));
        
        if (PluginControl.useMySQLStorage()) {
            taskThread.tasks.add(new LiteSignInTask(() -> {
                MySQLEngine.getInstance().executeQuery("SELECT COUNT(*) FROM " + MySQLEngine.getInstance().getTableSyntax(DatabaseTable.PLAYER_DATA));
            }, -1, ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getLong("MySQL-Storage.Wait-Timeout"), false));
        }
        
        SignInPluginProperties.sendOperationMessage("AsyncThreadStarted", MessageUtil.getDefaultPlaceholders());
        taskThread.start();
    }
    
    public static void runTask(Runnable task) {
        synchronized (taskThread.tasks) {
            taskThread.tasks.add(new LiteSignInTask(task, 1, 0));
        }
    }
    
    public static void runTask(Runnable task, double second) {
        synchronized (taskThread.tasks) {
            taskThread.tasks.add(new LiteSignInTask(task, 1, (long) (1D / ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("Async-Thread-Settings.Task-Thread-Delay") * second)));
        }
    }
}
