package studio.trc.bukkit.litesignin.database.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;

public class SQLiteEngine
{
    public static boolean SQLReloading = false;
    
    private static Connection connection = null;
    private static String table = "playerdata";
    private static String filePath = "plugins/LiteSignIn/";
    private static String fileName = "Database.db";
    
    public static String getTable() {
        return table;
    }
   
    public static void reloadConnectionParameters() {
        table = ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("SQLite-Storage.Table-Name");
        fileName = ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("SQLite-Storage.Database-File");
        filePath = ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("SQLite-Storage.Database-Path");
        File databaseName = new File(filePath, fileName);
        if (!databaseName.exists()) {
            try {
                databaseName.createNewFile();
            } catch (IOException ex) {}
        }
        if (connection == null) {
            connectToDatabase();
        } else try {
            SQLReloading = true;
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            SignInPluginProperties.sendOperationMessage("Reconnect", placeholders);
            Thread closing = new Thread(() -> {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException ex) {}
            });
            closing.start();
            long time = System.currentTimeMillis();
            while (closing.isAlive()) {
                if (System.currentTimeMillis() - time > 10000) {
                    closing.stop();
                    break;
                }
                Thread.sleep(50);
            }
            SQLReloading = false;
            connectToDatabase();
        } catch (Exception ex) {}
    }
    
    public static void connectToDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + filePath + fileName);
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            SignInPluginProperties.sendOperationMessage("SuccessfulConnection", placeholders);
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + "("
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
        } catch (ClassNotFoundException ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            SignInPluginProperties.sendOperationMessage("NoDriverFound", placeholders);
            ConfigurationUtil.getConfig(ConfigurationType.CONFIG).set("SQLite-Storage.Enabled", false);
        } catch (SQLException ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            placeholders.put("{error}", ex.getLocalizedMessage());
            SignInPluginProperties.sendOperationMessage("ConnectionError", placeholders);
            ConfigurationUtil.getConfig(ConfigurationType.CONFIG).set("SQLite-Storage.Enabled", false);
        }
    }
  
    public static void repairConnection() {
        new Thread(() -> {
            int number = 0;
            while (true) {
                try {
                    connection = DriverManager.getConnection("jdbc:sqlite:" + filePath + fileName);
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{database}", "SQLite");
                    SignInPluginProperties.sendOperationMessage("ConnectionRepair", placeholders);
                    break;
                } catch (SQLException ex) {
                    number++;
                    if (number == ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getInt("SQLite-Storage.Automatic-Repair")) {
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{database}", "SQLite");
                        placeholders.put("{number}", String.valueOf(number));
                        SignInPluginProperties.sendOperationMessage("ConnectionRepairFailure", placeholders);
                    } else {
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{database}", "SQLite");
                        SignInPluginProperties.sendOperationMessage("BeyondRepair", placeholders);
                        break;
                    }
                }
            }
        }, "SQLiteRepairConnectionThread").start();
    }
  
    public static Connection getConnection() {
        return connection;
    }
  
    public static void executeUpdate(PreparedStatement statement) {
        while (SQLReloading) {} 
        try {
            statement.executeUpdate();
        }  catch (SQLException ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            placeholders.put("{error}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
            SignInPluginProperties.sendOperationMessage("DataSavingError", placeholders);
            try {
                if (getConnection().isClosed()) repairConnection();
            } catch (SQLException ex1) {}
        }
    }
  
    public static ResultSet executeQuery(PreparedStatement statement) {
        while (SQLReloading) {} 
        try {
            return statement.executeQuery();
        } catch (SQLException ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            placeholders.put("{error}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
            SignInPluginProperties.sendOperationMessage("DataReadingError", placeholders);
            try {
                if (getConnection().isClosed()) repairConnection();
            } catch (SQLException ex1) {}
        }
        return null;
    }
  
    @Deprecated
    public static void executeUpdate(String sql) {
        while (SQLReloading) {} 
        try {
            connection.createStatement().executeUpdate(sql);
        } catch (SQLException ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            placeholders.put("{error}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
            SignInPluginProperties.sendOperationMessage("DataSavingError", placeholders);
            try {
                if (getConnection().isClosed()) repairConnection();
            } catch (SQLException ex1) {}
        }
    }
  
    @Deprecated
    public static ResultSet executeQuery(String sql) {
        while (SQLReloading) {} 
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            placeholders.put("{error}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
            SignInPluginProperties.sendOperationMessage("DataReadingError", placeholders);
            try {
                if (getConnection().isClosed()) repairConnection();
            } catch (SQLException ex1) {}
        }
        return null;
    }
    
    public static String getFileName() {
        return fileName;
    }
    
    public static String getFilePath() {
        return filePath;
    }
    
    /**
     * Back up all player data.
     * @param filePath Backup file path. 
     * @throws java.sql.SQLException 
     */
    public static void backup(String filePath) throws SQLException {
        try (Connection sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + filePath)) {
            sqlConnection.prepareStatement("CREATE TABLE IF NOT EXISTS PlayerData("
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
            if (connection.isClosed()) {
                connectToDatabase();
            }
            ResultSet rs = executeQuery(connection.prepareStatement("SELECT * FROM " + table));
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
                PreparedStatement statement = sqlConnection.prepareStatement("INSERT INTO PlayerData(UUID, Name, Year, Month, Day, Hour, Minute, Second, Continuous, RetroactiveCard, History)"
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
            }
        }
    }
}
