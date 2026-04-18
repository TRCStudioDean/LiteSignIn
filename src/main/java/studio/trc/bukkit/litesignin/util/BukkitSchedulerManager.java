package studio.trc.bukkit.litesignin.util;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import studio.trc.bukkit.litesignin.Main;

public class BukkitSchedulerManager 
{
    /**
     * Run bukkit task
     * @param task The task
     * @param delay Delayed ticks
     * @param foliaObject Task types recognized by Folia
     */
    public static void runBukkitTask(Runnable task, long delay, Object foliaObject) {
        try {
            if (delay == 0) {
                Bukkit.getScheduler().runTask(Main.getInstance(), task);
            } else {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), task, delay);
            }
        } catch (UnsupportedOperationException ex) {
            if (foliaObject instanceof Player) {
                runPlayerTask(task, delay, (Player) foliaObject);
            } else if (foliaObject instanceof Location) {
                runRegionTask(task, delay, (Location) foliaObject);
            } else {
                runGlobalTask(task, delay);
            }
        }
    }
    
    public static void runRegionTask(Runnable task, long delay, Location location) {
        try {
            Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            Consumer runnable = run -> task.run();
            if (delay == 0) {
                scheduler.getClass().getMethod("run", Plugin.class, Location.class, Consumer.class).invoke(scheduler, Main.getInstance(), location, runnable);
            } else {
                scheduler.getClass().getMethod("runDelayed", Plugin.class, Location.class, Consumer.class, long.class).invoke(scheduler, Main.getInstance(), location, runnable, delay);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            task.run();
        }
    }
    
    public static void runPlayerTask(Runnable task, long delay, Player player) {
        try {
            Object scheduler = Player.class.getMethod("getScheduler").invoke(player);
            Consumer runnable = run -> task.run();
            if (delay == 0) {
                scheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class).invoke(scheduler, Main.getInstance(), runnable, null);
            } else {
                scheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class).invoke(scheduler, Main.getInstance(), runnable, null, delay);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            task.run();
        }
    }
    
    public static void runGlobalTask(Runnable task, long delay) {
        try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Consumer runnable = run -> task.run();
            if (delay == 0) {
                scheduler.getClass().getMethod("run", Plugin.class, Consumer.class).invoke(scheduler, Main.getInstance(), runnable);
            } else {
                scheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class).invoke(scheduler, Main.getInstance(), runnable, delay);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            task.run();
        }
    }
    
    public static enum SchedulerType {
        DEFAULT, REGION, ENTITY, GLOBAL_REGION;
    }
}
