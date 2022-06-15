package studio.trc.bukkit.litesignin.database.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;

import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.database.MySQLStorage;
import studio.trc.bukkit.litesignin.database.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.YamlStorage;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;
import studio.trc.bukkit.litesignin.event.Menu;
import studio.trc.bukkit.litesignin.util.PluginControl;

public class RollBackUtil
{
    /**
     * Start rollback method.
     * @param rollBackFile Target backup file
     * @param backup Whether to back up the current data before performing a rollback
     * @param rollBackUsers State information recipient
     */
    public static void startRollBack(File rollBackFile, boolean backup, CommandSender... rollBackUsers) {
        new RollBackMethod(rollBackFile, rollBackUsers).rollBack(backup);
    }
    
    /**
     * Reset the database
     * @param databaseConnection DB connection
     * @param databasePath 
     * @throws java.sql.SQLException 
     */
    public static void resetDatabase(Connection databaseConnection, String databasePath) throws SQLException {
        databaseConnection.prepareStatement("DROP TABLE IF EXISTS " + databasePath).executeUpdate();
        databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS " + databasePath + "("
            + "UUID VARCHAR(36) NOT NULL,"
            + " Name VARCHAR(16),"
            + " Year INT,"
            + " Month INT,"
            + " Day INT,"
            + " Hour INT,"
            + " Minute INT,"
            + " Second INT,"
            + " Continuous INT,"
            + " RetroactiveCard INT,"
            + " History LONGTEXT,"
            + " PRIMARY KEY (UUID))").executeUpdate();
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
        
        //SQLite mode only.
        private Connection tempConnection;
        
        public void run() {
            Bukkit.getOnlinePlayers().stream().filter(ps -> Menu.menuOpening.containsKey(ps.getUniqueId())).forEachOrdered(Player::closeInventory);
            if (rollBackFile.exists()) {
                try (Connection sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + rollBackFile.getPath())) {
                    ResultSet rs = sqlConnection.prepareStatement("SELECT * FROM PlayerData").executeQuery();
                    if (PluginControl.useMySQLStorage()) {
                        resetDatabase(MySQLEngine.getConnection(), MySQLEngine.getDatabase() + "." + MySQLEngine.getTable());
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteEngine.getConnection().close();
                        tempConnection = DriverManager.getConnection("jdbc:sqlite:" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName());
                        resetDatabase(tempConnection, SQLiteEngine.getTable());
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
                            PreparedStatement statement = MySQLEngine.getConnection().prepareStatement("INSERT INTO " + MySQLEngine.getDatabase() + "." + MySQLEngine.getTable() + "(UUID, Name, Year, Month, Day, Hour, Minute, Second, Continuous, RetroactiveCard, History)"
                                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            statement.setString(1, uuid);
                            statement.setString(2, name);
                            statement.setInt(3, year);
                            statement.setInt(4, month);
                            statement.setInt(5, day);
                            statement.setInt(6, hour);
                            statement.setInt(7, minute);
                            statement.setInt(8, second);
                            statement.setInt(9, continuous);
                            statement.setInt(10, retroactivecard);
                            statement.setString(11, history);
                            statement.executeUpdate();
                        } else if (PluginControl.useSQLiteStorage()) {
                            PreparedStatement statement = tempConnection.prepareStatement("INSERT INTO " + SQLiteEngine.getTable() + "(UUID, Name, Year, Month, Day, Hour, Minute, Second, Continuous, RetroactiveCard, History)"
                                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            statement.setString(1, uuid);
                            statement.setString(2, name);
                            statement.setInt(3, year);
                            statement.setInt(4, month);
                            statement.setInt(5, day);
                            statement.setInt(6, hour);
                            statement.setInt(7, minute);
                            statement.setInt(8, second);
                            statement.setInt(9, continuous);
                            statement.setInt(10, retroactivecard);
                            statement.setString(11, history);
                            statement.executeUpdate();
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
                        MySQLEngine.reloadConnectionParameters();
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteStorage.cache.clear();
                        SQLiteEngine.reloadConnectionParameters();
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
                        MySQLEngine.reloadConnectionParameters();
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteStorage.cache.clear();
                        SQLiteEngine.reloadConnectionParameters();
                    } else {
                        YamlStorage.cache.clear();
                    }
                    t.printStackTrace();
                }
            }
        }
        
        public void rollBack(boolean backup) {
            if (backup) {
                for (CommandSender sender : rollBackUsers) {
                    MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Database-Management.Backup.Auto-Backup");
                }
                BackupUtil.startBackup(rollBackUsers);
            }
            run();
        }
    }
}
