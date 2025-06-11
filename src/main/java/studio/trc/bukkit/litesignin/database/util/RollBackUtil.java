package studio.trc.bukkit.litesignin.database.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.database.DatabaseTable;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.database.storage.MySQLStorage;
import studio.trc.bukkit.litesignin.database.storage.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.storage.YamlStorage;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;
import studio.trc.bukkit.litesignin.event.Menu;
import studio.trc.bukkit.litesignin.util.PluginControl;

public class RollBackUtil
{
    @Getter
    private static boolean rollingback = false;
    
    /**
     * Start rollback method.
     * @param rollBackFile Target backup file
     * @param backup Whether to back up the current data before performing a rollback
     * @param users State information recipient
     * @return 
     */
    public static Thread startRollBack(File rollBackFile, boolean backup, CommandSender... users) {
        Thread thread = new Thread(new RollBackMethod(rollBackFile, users).rollBack(backup), "LiteSignIn-RollBack");
        thread.start();
        return thread;
    }
    
    /**
     * Roll back method.
     */
    public static class RollBackMethod {
        
        private final File rollBackFile;
        private final CommandSender[] rollBackUsers;
        
        public RollBackMethod(File rollBackFile, CommandSender... rollBackUsers) {
            this.rollBackFile = rollBackFile;
            this.rollBackUsers = rollBackUsers;
        }
        
        public Runnable rollBack(boolean backup) {
            return () -> {
                if (backup) {
                    for (CommandSender sender : rollBackUsers) {
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Database-Management.Backup.Auto-Backup");
                    }
                    BackupUtil.startSyncBackup(rollBackUsers);
                }
                Bukkit.getOnlinePlayers().stream().filter(ps -> Menu.menuOpening.containsKey(ps.getUniqueId())).forEachOrdered(Player::closeInventory);
                if (rollBackFile.exists()) {
                    rollingback = true;
                    try (Connection sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + rollBackFile.getPath())) {
                        ResultSet rs = sqlConnection.prepareStatement("SELECT * FROM PlayerData").executeQuery();
                        if (PluginControl.useMySQLStorage()) {
                            MySQLEngine mysql = MySQLEngine.getInstance();
                            mysql.executeUpdate("DROP TABLE IF EXISTS " + mysql.getTableSyntax(DatabaseTable.PLAYER_DATA));
                            mysql.initialize();
                        } else if (PluginControl.useSQLiteStorage()) {
                            SQLiteEngine sqlite = SQLiteEngine.getInstance();
                            sqlite.executeUpdate("DROP TABLE IF EXISTS " + sqlite.getTableSyntax(DatabaseTable.PLAYER_DATA));
                            sqlite.initialize();
                        } else {
                            File playerFolder = new File("plugins/LiteSignIn/Players/");
                            if (!playerFolder.exists()) {
                                playerFolder.mkdirs();
                            } else {
                                for (File playerFile : playerFolder.listFiles()) {
                                    playerFile.delete();
                                }
                            }
                        }
                        while (rs.next()) {
                            String uuid = rs.getString("UUID");
                            String name = rs.getString("Name");
                            int year = rs.getInt("Year");
                            int month = rs.getInt("Month");
                            int day = rs.getInt("Day");
                            int hour = rs.getInt("Hour");
                            int minute = rs.getInt("Minute");
                            int second = rs.getInt("Second");
                            int continuous = rs.getInt("Continuous");
                            int retroactivecard = rs.getInt("RetroactiveCard");
                            String history = rs.getString("History");
                            if (name == null) {
                                name = "null";
                            }
                            if (history == null) {
                                history = "";
                            }
                            if (PluginControl.useMySQLStorage()) {
                                MySQLEngine mysql = MySQLEngine.getInstance();
                                mysql.executeUpdate("INSERT INTO " + mysql.getTableSyntax(DatabaseTable.PLAYER_DATA)
                                        + "(UUID, Name, Year, Month, Day, Hour, Minute, Second, Continuous, RetroactiveCard, History)"
                                        + " VALUES(?, ?, " + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", " + second + ", " + continuous + ", " + retroactivecard + ", ?)", uuid, name, history);
                            } else if (PluginControl.useSQLiteStorage()) {
                                SQLiteEngine sqlite = SQLiteEngine.getInstance();
                                sqlite.executeUpdate("INSERT INTO " + sqlite.getTableSyntax(DatabaseTable.PLAYER_DATA)
                                        + "(UUID, Name, Year, Month, Day, Hour, Minute, Second, Continuous, RetroactiveCard, History)"
                                        + " VALUES(?, ?, " + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", " + second + ", " + continuous + ", " + retroactivecard + ", ?)", uuid, name, history);
                            } else {
                                File file = new File("plugins/LiteSignIn/Players/" + uuid + ".yml");
                                file.createNewFile();
                                FileConfiguration data = new YamlConfiguration();
                                data.set("Name", name);
                                data.set("Last-time-SignIn.Year", year);
                                data.set("Last-time-SignIn.Month", month);
                                data.set("Last-time-SignIn.Day", day);
                                data.set("Last-time-SignIn.Hour", hour);
                                data.set("Last-time-SignIn.Minute", minute);
                                data.set("Last-time-SignIn.Second", second);
                                data.set("Continuous-SignIn", continuous);
                                data.set("RetroactiveCard", retroactivecard);
                                List<String> dates = Arrays.asList(history.split(", "));
                                data.set("History", dates);
                                data.save(file);
                            }
                        }
                        if (PluginControl.useMySQLStorage()) {
                            MySQLStorage.cache.clear();
                            PluginControl.reloadMySQL();
                        } else if (PluginControl.useSQLiteStorage()) {
                            SQLiteStorage.cache.clear();
                            PluginControl.reloadSQLite();
                        } else {
                            YamlStorage.cache.clear();
                        }
                        for (CommandSender sender : rollBackUsers) {
                            if (sender != null) {
                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                placeholders.put("{file}", rollBackFile.getName());
                                MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Database-Management.RollBack.Successfully", placeholders);
                            }
                        }
                    } catch (Throwable t) {
                        for (CommandSender sender : rollBackUsers) {
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                            placeholders.put("{error}", t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "null");
                            MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Database-Management.RollBack.Failed", placeholders);
                        }
                        if (PluginControl.useMySQLStorage()) {
                            MySQLStorage.cache.clear();
                            PluginControl.reloadMySQL();
                        } else if (PluginControl.useSQLiteStorage()) {
                            SQLiteStorage.cache.clear();
                            PluginControl.reloadSQLite();
                        } else {
                            YamlStorage.cache.clear();
                        }
                        t.printStackTrace();
                    }
                    rollingback = false;
                }
            };
        }
    }
}
