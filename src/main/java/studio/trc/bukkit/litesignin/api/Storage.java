package studio.trc.bukkit.litesignin.api;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import studio.trc.bukkit.litesignin.database.MySQLStorage;
import studio.trc.bukkit.litesignin.database.YamlStorage;
import studio.trc.bukkit.litesignin.database.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Data Storage for Users
 * @author Dean
 */
public interface Storage
    extends Statistics
{
    /**
     * Give user rewards.
     * @param retroactive
     */
    public void giveReward(boolean retroactive);
    
    /**
     * Get the year of last sign in.
     * @return 
     */
    public int getYear();
    
    /**
     * Get the month of last sign in.
     * @return 
     */
    public int getMonth();
    
    /**
     * Get the date of last sign in.
     * @return 
     */
    public int getDay();
    
    /**
     * Get the hour of last sign in.
     * @return 
     */
    public int getHour();
    
    /**
     * Get the minute of last sign in.
     * @return 
     */
    public int getMinute();
    
    /**
     * Get the second of last sign in.
     * @return 
     */
    public int getSecond();
    
    /**
     * Get the number of consecutive sign in for users.
     * @return 
     */
    public int getContinuousSignIn();
    
    /**
     * Get the number of retroactive cards.
     * @return 
     */
    public int getRetroactiveCard();
    
    /**
     * Get the Player's instance.
     * @return 
     */
    public Player getPlayer();
    
    /**
     * Get the player's name in database.
     * @return 
     */
    public String getName();
    
    /**
     * Get the player's group.
     * @return 
     */
    public SignInGroup getGroup();
    
    /**
     * Get the player's all matched groups.
     * @return 
     */
    public List<SignInGroup> getAllGroup();
    
    /**
     * Obtaining historical records.
     * @return 
     */
    public List<SignInDate> getHistory();
    
    /**
     * Setting the user's sign in history.
     * @param history 
     * @param saveData 
     */
    public void setHistory(List<SignInDate> history, boolean saveData);
    
    /**
     * Set the specified time to the user's sign in time.
     */
    public void signIn();
    
    /**
     * Set the current time to the user's sign as historicalDate.
     * @param historicalDate 
     */
    public void signIn(SignInDate historicalDate);
    
    /**
     * Give player a specified number of cards.
     * @param amount 
     */
    public void giveRetroactiveCard(int amount);
    
    /**
     * Remove the specified number of cards from the player.
     * @param amount 
     */
    public void takeRetroactiveCard(int amount);
    
    /**
     * Set the specified number of cards from the player.
     * Only the virtual prop mode is vaild.
     * @param amount 
     * @param saveData 
     */
    public void setRetroactiveCard(int amount, boolean saveData);
    
    /**
     * Set the specified time to the user's sign in time.
     * @param date
     * @param saveData 
     */
    public void setSignInTime(SignInDate date, boolean saveData);
    
    /**
     * Set the number of consecutive sign in.
     * @param number
     * @param saveData 
     */
    public void setContinuousSignIn(int number, boolean saveData);
    
    /**
     * Save user data.
     */
    public void saveData();
    
    public static Storage getPlayer(Player player) {
        if (PluginControl.useMySQLStorage()) {
            return MySQLStorage.getPlayerData(player);
        } else if (PluginControl.useSQLiteStorage()) {
            return SQLiteStorage.getPlayerData(player);
        } else {
            return YamlStorage.getPlayerData(player);
        }
    }
    
    public static Storage getPlayer(String playername) {
        if (PluginControl.useMySQLStorage()) {
            for (MySQLStorage data : MySQLStorage.cache.values()) {
                if (data.getName().equalsIgnoreCase(playername)) {
                    return data;
                }
            }
            UUID uuid = null;
            try {
                PreparedStatement statement = MySQLEngine.getConnection().prepareStatement("SELECT UUID FROM " + MySQLEngine.getDatabase() + "." + MySQLEngine.getTable() + " WHERE Name = ?");
                statement.setString(1, playername);
                ResultSet rs = MySQLEngine.executeQuery(statement);
                if (rs.next()) {
                    uuid = UUID.fromString(rs.getString("UUID"));
                }
            } catch (SQLException ex) {}
            return uuid != null ? MySQLStorage.getPlayerData(uuid) : null;
        } else if (PluginControl.useSQLiteStorage()) {
            for (SQLiteStorage data : SQLiteStorage.cache.values()) {
                if (data.getName().equalsIgnoreCase(playername)) {
                    return data;
                }
            }
            UUID uuid = null;
            try {
                PreparedStatement statement = SQLiteEngine.getConnection().prepareStatement("SELECT UUID FROM " + SQLiteEngine.getTable() + " WHERE Name = ?");
                statement.setString(1, playername);
                ResultSet rs = SQLiteEngine.executeQuery(statement);
                if (rs.next()) {
                    uuid = UUID.fromString(rs.getString("UUID"));
                }
            } catch (SQLException ex) {}
            return uuid != null ? SQLiteStorage.getPlayerData(uuid) : null;
        } else {
            return YamlStorage.getPlayerData(Bukkit.getOfflinePlayer(playername).getUniqueId());
        }
    }
    
    public static Storage getPlayer(UUID uuid) {
        if (PluginControl.useMySQLStorage()) {
            return MySQLStorage.getPlayerData(uuid);
        } else if (PluginControl.useSQLiteStorage()) {
            return SQLiteStorage.getPlayerData(uuid);
        } else {
            return YamlStorage.getPlayerData(uuid);
        }
    }
}
