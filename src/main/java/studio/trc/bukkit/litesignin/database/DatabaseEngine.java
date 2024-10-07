package studio.trc.bukkit.litesignin.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import studio.trc.bukkit.litesignin.database.engine.SQLQuery;

public interface DatabaseEngine
{
    /**
     * Connect to database.
     */
    public void connect();
    
    /**
     * Disconnect from database.
     */
    public void disconnect();
    
    /**
     * Check connection status.
     * @throws SQLException 
     */
    public void checkConnection() throws SQLException;
    
    /**
     * Execute update syntax.
     * @param sqlSyntax
     * @param values 
     * @return How much rows affected.
     */
    public int executeUpdate(String sqlSyntax, String... values);
    
    /**
     * Execute multi queries.
     * @param sqlSyntax
     * @param parameters 
     * @return How much rows affected.
     */
    public int[] executeMultiQueries(String sqlSyntax, List<Map<Integer, String>> parameters);
    
    /**
     * Execute query syntax.
     * @param sqlSyntax
     * @param values
     * @return The results.
     */
    public SQLQuery executeQuery(String sqlSyntax, String... values);
    
    /**
     * Get database connection intance.
     * @return 
     */
    public Connection getConnection();
    
    /**
     * Throw SQL exception.
     * @param exception
     * @param path
     * @param reconnect 
     */
    public void throwSQLException(Exception exception, String path, boolean reconnect);
    
    /**
     * Initialization method.
     */
    public void initialize();
}
