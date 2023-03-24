package studio.trc.bukkit.litesignin.database.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.database.DatabaseEngine;
import studio.trc.bukkit.litesignin.database.DatabaseTable;
import studio.trc.bukkit.litesignin.database.DatabaseType;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;

public class SQLiteEngine 
    implements DatabaseEngine
{
    @Getter
    @Setter
    private static SQLiteEngine instance = null;
    @Getter
    private Connection sqliteConnection = null;
    
    @Getter
    private final String folderPath;
    @Getter
    private final String fileName;
    
    public SQLiteEngine(String folderPath, String fileName) {
        this.folderPath = folderPath;
        this.fileName = fileName;
    }

    @Override
    public void connect() {
        try {
            if (sqliteConnection != null && !sqliteConnection.isClosed()) {
                disconnect();
            } else {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException ex) {
                    throwSQLException(ex, "NoDriverFound", true);
                    ConfigurationUtil.getConfig(ConfigurationType.CONFIG).set("SQLite-Storage.Enabled", false);
                }
            }
            File databaseFolder = new File(folderPath);
            if (!databaseFolder.exists() || !databaseFolder.isDirectory()) {
                databaseFolder.mkdirs();
            }
            File databaseFile = new File(databaseFolder, fileName);
            if (databaseFile.exists()) {
                databaseFile.createNewFile();
            }
            sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + folderPath + "/" + fileName);
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "SQLite");
            SignInPluginProperties.sendOperationMessage("SuccessfullyConnected", placeholders);
            initialize();
        } catch (IOException | SQLException ex) {
            throwSQLException(ex, "ConnectionFailed", true);
        }
    }

    @Override
    public void disconnect() {
        if (sqliteConnection != null) {
            try {
                sqliteConnection.close();
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{database}", "SQLite");
                SignInPluginProperties.sendOperationMessage("Disconnected", placeholders);
            } catch (SQLException ex) {
                throwSQLException(ex, "ConnectionError", false);
            }
        }
    }

    @Override
    public void checkConnection() throws SQLException {
        if (Main.getInstance().isEnabled()) {
            while (sqliteConnection == null || sqliteConnection.isClosed()) {
                connect();
            }
        }
    }

    @Override
    public int executeUpdate(String sqlSyntax, String... values) {
        try {
            checkConnection();
            PreparedStatement statement = sqliteConnection.prepareStatement(sqlSyntax);
            int number = 0;
            for (String value : values) {
                number++;
                statement.setString(number, value);
            }
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throwSQLException(ex, "ExecuteUpdateFailed", true);
            return 0;
        }
    }

    @Override
    public int[] executeMultiQueries(String sqlSyntax, List<Map<Integer, String>> parameters) {
        try {
            checkConnection();
            PreparedStatement statement = sqliteConnection.prepareStatement(sqlSyntax);
            for (Map<Integer, String> parameter : parameters) {
                for (int id : parameter.keySet()) {
                    statement.setString(id, parameter.get(id));
                }
                statement.addBatch();
            }
            return statement.executeBatch();
        } catch (SQLException ex) {
            throwSQLException(ex, "ExecuteUpdateFailed", true);
            return new int[0];
        }
    }

    @Override
    public ResultSet executeQuery(String sqlSyntax, String... values) {
        try {
            checkConnection();
            PreparedStatement statement = sqliteConnection.prepareStatement(sqlSyntax);
            int number = 0;
            for (String value : values) {
                number++;
                statement.setString(number, value);
            }
            return statement.executeQuery();
        } catch (SQLException ex) {
            throwSQLException(ex, "ExecuteQueryFailed", true);
            return null;
        }
    }

    @Override
    public Connection getConnection() {
        return sqliteConnection;
    }

    @Override
    public void throwSQLException(Exception exception, String path, boolean reconnect) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{database}", "SQLite");
        placeholders.put("{error}", exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : "null");
        SignInPluginProperties.sendOperationMessage(path, placeholders);
        try {
            if (reconnect && sqliteConnection.isClosed()) {
                connect();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void initialize() {
        try {
            checkConnection();
            Statement statement = sqliteConnection.createStatement();
            for (DatabaseTable table : DatabaseTable.values()) {
                statement.addBatch(table.getCreateTableSyntax(DatabaseType.SQLITE));
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            throwSQLException(ex, "InitializationFailed", true);
        }
    }
    
    public String getTableSyntax(DatabaseTable table) {
        return table.getDisplayName();
    }
}
