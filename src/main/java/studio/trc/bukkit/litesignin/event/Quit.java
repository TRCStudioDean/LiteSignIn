package studio.trc.bukkit.litesignin.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.database.storage.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.storage.MySQLStorage;
import studio.trc.bukkit.litesignin.util.PluginControl;

public class Quit
    implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Storage.getPlayer(player).saveData();
        if (PluginControl.useMySQLStorage()) {
            MySQLStorage.cache.remove(player.getUniqueId());
        } else if (PluginControl.useSQLiteStorage()) {
            SQLiteStorage.cache.remove(player.getUniqueId());
        }
    }
}
