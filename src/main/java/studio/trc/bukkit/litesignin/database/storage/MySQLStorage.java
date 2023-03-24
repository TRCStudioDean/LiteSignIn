package studio.trc.bukkit.litesignin.database.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.Configuration;
import studio.trc.bukkit.litesignin.event.custom.PlayerSignInEvent;
import studio.trc.bukkit.litesignin.event.custom.SignInRewardEvent;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.reward.SignInRewardSchedule;
import studio.trc.bukkit.litesignin.reward.type.*;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.trc.bukkit.litesignin.database.DatabaseTable;

public final class MySQLStorage
    implements Storage
{
    public static final Map<UUID, MySQLStorage> cache = new HashMap();
    
    @Getter
    private int continuous = 0;
    @Getter
    private int year = 1970;
    @Getter
    private int month = 1;
    @Getter
    private int day = 1;
    @Getter
    private int hour = 0;
    @Getter
    private int minute = 0;
    @Getter
    private int second = 0;
    @Getter
    private String name = null;
    @Getter
    private List<SignInDate> history = new ArrayList();
    private final UUID uuid;
    private int retroactiveCard = 0; 
    
    public MySQLStorage(Player player) {
        uuid = player.getUniqueId();
        reloadData();
        cache.put(uuid, MySQLStorage.this);
    }
    
    public MySQLStorage(UUID uuid) {
        this.uuid = uuid;
        reloadData();
        cache.put(uuid, MySQLStorage.this);
    }
    
    public void reloadData() {
        try {
            MySQLEngine mysql = MySQLEngine.getInstance();
            mysql.checkConnection();
            ResultSet rs = mysql.executeQuery("SELECT * FROM " + mysql.getTableSyntax(DatabaseTable.PLAYER_DATA) + " WHERE UUID = ?", uuid.toString());
            if (rs.next()) {
                continuous = rs.getObject("Continuous") != null ? rs.getInt("Continuous") : 0;
                name = rs.getObject("Name") != null ? rs.getString("Name") : null;
                year = rs.getObject("Year") != null ? rs.getInt("Year") : 1970;
                month = rs.getObject("Month") != null ? rs.getInt("Month") : 1;
                day = rs.getObject("Day") != null ? rs.getInt("Day") : 1;
                hour = rs.getObject("Hour") != null ? rs.getInt("Hour") : 0;
                minute = rs.getObject("Minute") != null ? rs.getInt("Minute") : 0;
                second = rs.getObject("Second") != null ? rs.getInt("Second") : 0;
                retroactiveCard = rs.getObject("RetroactiveCard") != null ? rs.getInt("RetroactiveCard") : 0;
                if (rs.getObject("History") != null && !rs.getString("History").equals("")) {
                    List<SignInDate> list = new ArrayList();
                    for (String data : Arrays.asList(rs.getString("History").split(", "))) {
                        list.add(SignInDate.getInstance(data));
                    }
                    history = list;
                } else {
                    history = new ArrayList();
                }
            } else {
                String playerName = Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid) != null ? Bukkit.getOfflinePlayer(uuid).getName() : "null";
                mysql.executeUpdate("INSERT INTO " + mysql.getTableSyntax(DatabaseTable.PLAYER_DATA)
                        + "(UUID, Name, Year, Month, Day, Hour, Minute, Second, Continuous)"
                        + " VALUES(?, ?, 1970, 1, 1, 0, 0, 0, 0)", uuid.toString(), playerName);
            }
            checkContinuousSignIn();
        } catch (SQLException ex) {
            MySQLEngine.getInstance().throwSQLException(ex, "ExecuteQueryFailed", true);
        }
    }
    
    @Override
    public void checkContinuousSignIn() {
        String[] date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()).split("-");
        int y = getYear();
        int m = getMonth();
        int d = getDay();
        if (Integer.valueOf(date[0]) == year && Integer.valueOf(date[1]) == month && Integer.valueOf(date[2]) == day) return;
        boolean breakSign = true;
        if (y == Integer.valueOf(date[0])) {
            int[] ds = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            if ((y % 4 == 0 && y % 100 != 0) || y % 400 == 0) {
                ds[1] = 29;
            }
            if (ds[m - 1] == d && m + 1 == Integer.valueOf(date[1])) {
                breakSign = false;
            } else if (d + 1 == Integer.valueOf(date[2])) {
                breakSign = false;
            }
        } else if (y + 1 == Integer.valueOf(date[0])) {
            if (m == 12 && Integer.valueOf(date[1]) == 1 && d == 31 && Integer.valueOf(date[2]) == 1) {
                breakSign = false;
            }
        }
        if (breakSign) {
            setContinuousSignIn(0, true);
        }
    }
    
    @Override
    public void giveReward(boolean retroactive) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Enable-Multi-Group-Reward")) {
            getAllGroup().stream().forEach(group -> {
                int queue = SignInQueue.getInstance().getRank(uuid);
                int continuousSignIn = getContinuousSignIn();
                int totalNumber = getCumulativeNumber();
                SignInDate today = SignInDate.getInstance(new Date());
                int week = today.getWeek();
                SignInRewardSchedule rewardQueue = new SignInRewardSchedule(this);
                rewardQueue.addReward(new SignInSpecialTimeReward(group, continuousSignIn));
                rewardQueue.addReward(new SignInSpecialDateReward(group, today));
                rewardQueue.addReward(new SignInStatisticsTimeReward(group, totalNumber));
                rewardQueue.addReward(new SignInSpecialWeekReward(group, week));
                if (retroactive) rewardQueue.addReward(new SignInRetroactiveTimeReward(group));
                else {
                    rewardQueue.addReward(new SignInSpecialTimePeriodReward(group, today));
                    rewardQueue.addReward(new SignInSpecialRankingReward(group, queue));
                    rewardQueue.addReward(new SignInNormalReward(group));
                }
                SignInRewardEvent event = new SignInRewardEvent(player, rewardQueue);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) rewardQueue.run(retroactive);
            });
        } else {
            SignInGroup group = getGroup();
            if (group == null) return;
            int queue = SignInQueue.getInstance().getRank(uuid);
            int continuousSignIn = getContinuousSignIn();
            int totalNumber = getCumulativeNumber();
            SignInDate today = SignInDate.getInstance(new Date());
            int week = today.getWeek();
            SignInRewardSchedule rewardQueue = new SignInRewardSchedule(this);
            rewardQueue.addReward(new SignInSpecialTimeReward(group, continuousSignIn));
            rewardQueue.addReward(new SignInSpecialDateReward(group, today));
            rewardQueue.addReward(new SignInStatisticsTimeReward(group, totalNumber));
            rewardQueue.addReward(new SignInSpecialWeekReward(group, week));
            if (retroactive) rewardQueue.addReward(new SignInRetroactiveTimeReward(group));
            else {
                rewardQueue.addReward(new SignInSpecialTimePeriodReward(group, today));
                rewardQueue.addReward(new SignInSpecialRankingReward(group, queue));
                rewardQueue.addReward(new SignInNormalReward(group));
            }
            SignInRewardEvent event = new SignInRewardEvent(player, rewardQueue);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            rewardQueue.run(retroactive);
        }
    }
    
    @Override
    public SignInGroup getGroup() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        SignInGroup group = null;
        Configuration config = ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS);
        for (String groups : config.getStringList("Reward-Settings.Groups-Priority")) {
            if (config.get("Reward-Settings.Permission-Groups." + groups + ".Permission") != null) {
                if (player.hasPermission(config.getString("Reward-Settings.Permission-Groups." + groups + ".Permission"))) {
                    group = new SignInGroup(groups);
                    break;
                }
            }
        }
        if (group == null && config.get("Reward-Settings.Permission-Groups.Default") != null) {
            group = new SignInGroup("Default");
        }
        return group;
    }

    @Override
    public List<SignInGroup> getAllGroup() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        List<SignInGroup> groups = new ArrayList();
        Configuration config = ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS);
        config.getStringList("Reward-Settings.Groups-Priority").stream()
            .filter(group -> config.get("Reward-Settings.Permission-Groups." + group + ".Permission") != null && player.hasPermission(config.getString("Reward-Settings.Permission-Groups." + group + ".Permission")))
            .forEach(group -> groups.add(new SignInGroup(group)));
        if (groups.isEmpty() && config.get("Reward-Settings.Permission-Groups.Default") != null) {
            groups.add(new SignInGroup("Default"));
        }
        return groups;
    }
    
    @Override
    public int getContinuousSignIn() {
        return continuous;
    }
    
    @Override
    public int getCumulativeNumber() {
        return clearUselessData(getHistory()).size();
    }
    
    @Override
    public int getRetroactiveCard() {
        if (PluginControl.enableRetroactiveCardRequiredItem()) {
            int amount = 0;
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return 0;
            ItemStack card = PluginControl.getRetroactiveCardRequiredItem(player);
            if (card == null) return 0;
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && !is.getType().equals(Material.AIR)) {
                    ItemMeta im = is.getItemMeta();
                    if (im.equals(card.getItemMeta())) {
                        amount += is.getAmount();
                    }
                }
            }
            return amount;
        }
        return retroactiveCard;
    }
    
    @Override
    public UUID getUserUUID() {
        return uuid;
    }
    
    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
    
    @Override
    public boolean alreadySignIn() {
        return getHistory().stream().anyMatch(date -> date.equals(SignInDate.getInstance(new Date())));
    }

    @Override
    public boolean alreadySignIn(SignInDate date) {
        return getHistory().stream().anyMatch(dates -> dates.equals(date));
    }

    @Override
    public List<SignInDate> clearUselessData(List<SignInDate> dates) {
        if (dates.size() == 1) return dates;
        List<SignInDate> result = new ArrayList();
        List<String> record = new ArrayList();
        dates.stream().filter(date -> !record.contains(date.getYear() + "-" + date.getMonth() + "-" + date.getDay())).map(date -> {
            result.add(date);
            return date;
        }).forEach(date -> {
            record.add(date.getYear() + "-" + date.getMonth() + "-" + date.getDay());
        });
        return result;
    }
    
    @Override
    public void setHistory(List<SignInDate> history, boolean saveData) {
        this.history = history;
        if (saveData) saveData();
    }
    
    @Override
    public void signIn() {
        SignInDate today = SignInDate.getInstance(new Date());
        PlayerSignInEvent event = new PlayerSignInEvent(uuid, today, false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        setSignInTime(today, false);
        List<SignInDate> historys = getHistory();
        historys.add(today);
        setHistory(clearUselessData(historys), false);
        setContinuousSignIn(SignInDate.getContinuous(historys), true);
        SignInQueue.getInstance().loadQueue();
        giveReward(false);
    }
    
    @Override
    public void signIn(SignInDate historicalDate) {
        historicalDate = SignInDate.getInstance(historicalDate.getYear(), historicalDate.getMonth(), historicalDate.getDay());
        if (PluginControl.getRetroactiveCardMinimumDate() != null && historicalDate.compareTo(PluginControl.getRetroactiveCardMinimumDate()) < 0) {
            return;
        }
        PlayerSignInEvent event = new PlayerSignInEvent(uuid, historicalDate, true);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        List<SignInDate> historys = new ArrayList();
        boolean added = false;
        if (!getHistory().isEmpty()) {
            for (SignInDate records : getHistory()) {
                if (historicalDate.compareTo(records) > 0) {
                    historys.add(records);
                } else if (historicalDate.compareTo(records) == 0) {
                    historys.add(historicalDate);
                    added = true;
                } else if (historicalDate.compareTo(records) < 0) {
                    if (!added) {
                        historys.add(historicalDate);
                        added = true;
                    }
                    historys.add(records);
                }
            }
        }
        if (!added) {
            historys.add(historicalDate);
        }
        setHistory(clearUselessData(historys), false);
        setContinuousSignIn(SignInDate.getContinuous(historys), true);
        giveReward(true);
        lastSignInTime.put(uuid, System.currentTimeMillis());
    }
    
    @Override
    public void setSignInTime(SignInDate date, boolean saveData) {
        year = date.getYear();
        month = date.getMonth();
        day = date.getDay();
        hour = date.getHour();
        minute = date.getMinute();
        second = date.getSecond();
        if (saveData) saveData();
    }
    
    @Override
    public void setContinuousSignIn(int number, boolean saveData) {
        continuous = number;
        if (saveData) saveData();
    }
    
    @Override
    public void giveRetroactiveCard(int amount) {
        if (amount < 1) return;
        if (PluginControl.enableRetroactiveCardRequiredItem()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            ItemStack card = PluginControl.getRetroactiveCardRequiredItem(player);
            if (card == null) return;
            card.setAmount(amount);
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(card);
            } else {
                player.getWorld().dropItem(player.getLocation(), card);
            }
        } else {
            setRetroactiveCard(getRetroactiveCard() + amount, true);
        }
    }

    @Override
    public void takeRetroactiveCard(int amount) {
        if (amount < 1) return;
        if (PluginControl.enableRetroactiveCardRequiredItem()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            ItemStack card = PluginControl.getRetroactiveCardRequiredItem(player);
            if (card == null) return;
            List<ItemStack> itemOnInv = new ArrayList();
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && !is.getType().equals(Material.AIR)) {
                    ItemMeta im = is.getItemMeta();
                    if (im.equals(card.getItemMeta())) {
                        itemOnInv.add(is);
                    }
                }
            }
            for (ItemStack is : itemOnInv) {
                if (is.getAmount() <= amount) {
                    amount -= is.getAmount();
                    is.setAmount(0);
                    is.setType(Material.AIR);
                } else {
                    is.setAmount(is.getAmount() - amount);
                    break;
                }
            }
        } else {
            setRetroactiveCard(getRetroactiveCard() - amount, true);
        }
    }
    
    @Override
    public void setRetroactiveCard(int amount, boolean saveData) {
        if (PluginControl.enableRetroactiveCardRequiredItem()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            ItemStack card = PluginControl.getRetroactiveCardRequiredItem(player);
            if (card == null) return;
            for (ItemStack items : player.getInventory().getContents()) {
                if (items != null && !items.getType().equals(Material.AIR)) {
                    if (items.getItemMeta().equals(card.getItemMeta())) {
                        items.setAmount(0);
                        items.setType(Material.AIR);
                    }
                }
            }
            card.setAmount(amount);
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(card);
            } else {
                player.getWorld().dropItem(player.getLocation(), card);
            }
            if (saveData) saveData();
        } else {
            retroactiveCard = amount >= 0 ? amount : 0;
            if (saveData) saveData();
        }
    }
    
    @Override
    public void saveData() {
        try {
            MySQLEngine mysql = MySQLEngine.getInstance();
            mysql.checkConnection();
            String playerName = Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid) != null ? Bukkit.getOfflinePlayer(uuid).getName() : "null";
            mysql.executeUpdate("UPDATE " + mysql.getTableSyntax(DatabaseTable.PLAYER_DATA) + " SET Name = ?,"
                + "Year = " + year + ", "
                + "Month = " + month + ", "
                + "Day = " + day + ", "
                + "Hour = " + hour + ", "
                + "Minute = " + minute + ", "
                + "Second = " + second + ", "
                + "Continuous = " + continuous + ", "
                + "RetroactiveCard = " + retroactiveCard + ", History = ? WHERE UUID = ?",
                playerName, history.toString().substring(1, history.toString().length() - 1), uuid.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static long lastUpdateTime = System.currentTimeMillis();
    
    public static MySQLStorage getPlayerData(Player player) {
        if (PluginControl.getMySQLRefreshInterval() <= 0) {
            return new MySQLStorage(player.getUniqueId());
        }
        MySQLStorage data = cache.get(player.getUniqueId());
        if (data != null && (PluginControl.getMySQLRefreshInterval() == 0 || System.currentTimeMillis() - lastUpdateTime >= PluginControl.getMySQLRefreshInterval() * 1000)) {
            return data;
        }
        data = new MySQLStorage(player);
        cache.clear();
        cache.put(player.getUniqueId(), data);
        lastUpdateTime = System.currentTimeMillis();
        return data;
    }
    
    public static MySQLStorage getPlayerData(UUID uuid) {
        if (PluginControl.getMySQLRefreshInterval() <= 0) {
            return new MySQLStorage(uuid);
        }
        MySQLStorage data = cache.get(uuid);
        if (data != null && (PluginControl.getMySQLRefreshInterval() == 0 || System.currentTimeMillis() - lastUpdateTime >= PluginControl.getMySQLRefreshInterval() * 1000)) {
            return data;
        }
        data = new MySQLStorage(uuid);
        cache.clear();
        cache.put(uuid, data);
        lastUpdateTime = System.currentTimeMillis();
        return data;
    }
    
    /**
     * Back up all player data.
     * @param filePath Backup file path. 
     * @throws java.sql.SQLException 
     */
    public static void backup(String filePath) throws SQLException {
        try (Connection sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + filePath)) {
            sqlConnection.prepareStatement(DatabaseTable.PLAYER_DATA.getDefaultCreateTableSyntax()).executeUpdate();
            MySQLEngine mysql = MySQLEngine.getInstance();
            ResultSet rs = mysql.executeQuery("SELECT * FROM " + mysql.getTableSyntax(DatabaseTable.PLAYER_DATA));
            PreparedStatement statement = sqlConnection.prepareStatement("INSERT INTO PlayerData(UUID, Name, Year, Month, Day, Hour, Minute, Second, Continuous, RetroactiveCard, History)  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
