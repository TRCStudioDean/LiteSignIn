package studio.trc.bukkit.litesignin.database.engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.database.DatabaseEngine;
import studio.trc.bukkit.litesignin.database.DatabaseTable;
import studio.trc.bukkit.litesignin.database.DatabaseType;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;

public class MySQLEngine
    implements DatabaseEngine
{
    @Getter
    @Setter
    private static MySQLEngine instance = null;
    @Getter
    private static Connection mysqlConnection = null;
    
    @Getter
    private final int port;
    @Getter
    private final String hostname;
    @Getter
    private final String userName;
    @Getter
    private final String password;
    @Getter
    private final Map<String, String> jdbcOptions;
    
    public MySQLEngine(String hostname, int port, String userName, String password, Map<String, String> jdbcOptions) {
        this.hostname = hostname;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.jdbcOptions = jdbcOptions;
    }
    
    @Override
    public void connect() {
        try {
            if (mysqlConnection != null && !mysqlConnection.isClosed()) {
                disconnect();
            } else {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException ex) {
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                    } catch (ClassNotFoundException ex1) {
                        throwSQLException(ex1, "NoDriverFound", true);
                        ConfigurationUtil.getConfig(ConfigurationType.CONFIG).set("MySQL-Storage.Enabled", false);
                    }
                }
            }
            mysqlConnection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + getConnectionURI(), userName, password);
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{database}", "MySQL");
            LiteSignInProperties.sendOperationMessage("SuccessfullyConnected", placeholders);
            initialize();
        } catch (SQLException ex) {
            throwSQLException(ex, "ConnectionFailed", false);
        }
    }
    
    @Override
    public void disconnect() {
        if (mysqlConnection != null) {
            try {
                if (!mysqlConnection.isClosed()) {
                    mysqlConnection.close();
                }
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{database}", "MySQL");
                LiteSignInProperties.sendOperationMessage("Disconnected", placeholders);
            } catch (SQLException ex) {
                throwSQLException(ex, "ConnectionError", false);
            }
        }
    }
    
    @Override
    public void checkConnection() throws SQLException {
        if (Main.getInstance().isEnabled()) {
            while (mysqlConnection == null || mysqlConnection.isClosed()) {
                connect();
            }
        }
    }
    
    @Override
    public int executeUpdate(String sqlSyntax, String... values) {
        try {
            checkConnection();
            try (PreparedStatement statement = mysqlConnection.prepareStatement(sqlSyntax)) {
                int number = 0;
                for (String value : values) {
                    number++;
                    statement.setString(number, value);
                }
                return statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throwSQLException(ex, "ExecuteUpdateFailed", true);
            return 0;
        }
    }
    
    @Override
    public int[] executeMultiQueries(String sqlSyntax, List<Map<Integer, String>> parameters) {
        try {
            checkConnection();
            try (PreparedStatement statement = mysqlConnection.prepareStatement(sqlSyntax)) {
                for (Map<Integer, String> parameter : parameters) {
                    for (int id : parameter.keySet()) {
                        statement.setString(id, parameter.get(id));
                    }
                    statement.addBatch();
                }
                return statement.executeBatch();
            }
        } catch (SQLException ex) {
            throwSQLException(ex, "ExecuteUpdateFailed", true);
            return new int[0];
        }
    }
    
    @Override
    public SQLQuery executeQuery(String sqlSyntax, String... values) {
        try {
            checkConnection();
            PreparedStatement statement = mysqlConnection.prepareStatement(sqlSyntax);
            int number = 0;
            for (String value : values) {
                number++;
                statement.setString(number, value);
            }
            return new SQLQuery(statement.executeQuery(), statement);
        } catch (SQLException ex) {
            throwSQLException(ex, "ExecuteQueryFailed", true);
            return null;
        }
    }

    @Override
    public Connection getConnection() {
        return mysqlConnection;
    }
    
    @Override
    public void throwSQLException(Exception exception, String path, boolean reconnect) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{database}", "MySQL");
        placeholders.put("{error}", exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : "null");
        LiteSignInProperties.sendOperationMessage(path, placeholders);
        try {
            if (reconnect && mysqlConnection.isClosed()) {
                connect();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void initialize() {
        if (PluginControl.useMySQLStorage()) {
            try {
                checkConnection();
                Statement statement = mysqlConnection.createStatement();
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + getDatabaseName());
                statement.close();
                statement = mysqlConnection.createStatement();
                for (DatabaseTable table : DatabaseTable.values()) {
                    statement.addBatch(table.getCreateTableSyntax(DatabaseType.MYSQL));
                }
                statement.executeBatch();
                statement.close();
            } catch (SQLException ex) {
                throwSQLException(ex, "InitializationFailed", true);
            }
        }
    }
    
    public String getDatabaseName() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("MySQL-Storage.Database");
    }
    
    public String getTableSyntax(DatabaseTable table) {
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("MySQL-Storage.Automatic-Deploy-Mode")) {
            return getDatabaseName() + "." + table.getDisplayName();
        } else {
            return table.getDisplayName();
        }
    }
    
    private StringBuilder getConnectionURI() {
        StringBuilder builder = new StringBuilder();
        if (jdbcOptions.isEmpty()) return builder;
        if (!ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("MySQL-Storage.Automatic-Deploy-Mode")) {
            builder.append(getDatabaseName());
        }
        builder.append("?");
        int length = 0;
        for (String option : jdbcOptions.keySet()) {
            length++;
            builder.append(option).append("=").append(jdbcOptions.get(option));
            if (length < jdbcOptions.size()) {
                builder.append("&");
            }
        }
        return builder;
    }
}
