package studio.trc.bukkit.litesignin.database.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.PreparedConfiguration;
import studio.trc.bukkit.litesignin.event.custom.PlayerSignInEvent;
import studio.trc.bukkit.litesignin.event.custom.SignInRewardEvent;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.SignInPluginUtils;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.reward.*;
import studio.trc.bukkit.litesignin.reward.type.*;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class YamlStorage
    implements Storage
{
    public static final Map<UUID, YamlStorage> cache = new HashMap();
    
    private final UUID uuid;
    private final FileConfiguration config = new YamlConfiguration();
    private boolean loaded = false;
    
    public YamlStorage(Player player) {
        uuid = player.getUniqueId();
        
        File dataFolder = new File("plugins/LiteSignIn/Players");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        
        File dataFile = new File("plugins/LiteSignIn/Players/" + uuid.toString() + ".yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {}
        }
        try (InputStreamReader Config = new InputStreamReader(new FileInputStream(dataFile), "UTF-8")) {
            config.load(Config);
        } catch (IOException | InvalidConfigurationException ex) {
            dataFileRepair();
        }
        config.set("Name", player.getName());
        
        checkContinuousSignIn();
        loaded = true;
    }
    
    public YamlStorage(UUID uuid) {
        this.uuid = uuid;
        
        File dataFolder = new File("plugins/LiteSignIn/Players");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        
        File dataFile = new File("plugins/LiteSignIn/Players/" + uuid.toString() + ".yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {}
        }
        try (InputStreamReader Config = new InputStreamReader(new FileInputStream(dataFile), "UTF-8")) {
            config.load(Config);
        } catch (IOException | InvalidConfigurationException ex) {
            dataFileRepair();
        }
        config.set("Name", Bukkit.getOfflinePlayer(uuid).getName());
        
        checkContinuousSignIn();
        loaded = true;
    }
    
    @Override
    public void checkContinuousSignIn() {
        String[] date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).split("-");
        int year = getYear();
        int month = getMonth();
        int day = getDay();
        if (Integer.valueOf(date[0]) == year && Integer.valueOf(date[1]) == month && Integer.valueOf(date[2]) == day) return;
        boolean breakSign = true;
        if (year == Integer.valueOf(date[0])) {
            int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                days[1] = 29;
            }
            if (days[month - 1] == day && month + 1 == Integer.valueOf(date[1])) {
                breakSign = false;
            } else if (day + 1 == Integer.valueOf(date[2])) {
                breakSign = false;
            }
        } else if (year + 1 == Integer.valueOf(date[0])) {
            if (month == 12 && Integer.valueOf(date[1]) == 1 && day == 31 && Integer.valueOf(date[2]) == 1) {
                breakSign = false;
            }
        }
        if (breakSign) {
            setContinuousSignIn(0, true);
        }
    }
    
    @Override
    public void giveReward(SignInDate retroactiveDate) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Enable-Multi-Group-Reward")) {
            getAllGroup().stream().forEach(group -> {
                int queue = SignInQueue.getInstance().getRank(uuid);
                int continuousSignIn = getContinuousSignIn();
                int totalNumber = getCumulativeNumber();
                SignInRewardSchedule rewardQueue = new SignInRewardSchedule(this);
                rewardQueue.addReward(new SignInStatisticsTimeReward(group, totalNumber));
                rewardQueue.addReward(new SignInStatisticsTimeCycleReward(group, totalNumber));
                if (retroactiveDate != null) {
                    int week = retroactiveDate.getWeek();
                    int retroactiveMonth = retroactiveDate.getMonth();
                    rewardQueue.addReward(new SignInSpecialWeekReward(group, week));
                    rewardQueue.addReward(new SignInStatisticsTimeOfMonthReward(group, retroactiveMonth, getCumulativeNumberOfMonth()));
                    rewardQueue.addReward(new SignInSpecialDateReward(group, retroactiveDate));
                    rewardQueue.addReward(new SignInRetroactiveTimeReward(group));
                } else {
                    SignInDate today = SignInDate.getInstance(new Date());
                    int week = today.getWeek();
                    int thisMonth = today.getMonth();
                    rewardQueue.addReward(new SignInSpecialWeekReward(group, week));
                    rewardQueue.addReward(new SignInStatisticsTimeOfMonthReward(group, thisMonth, getCumulativeNumberOfMonth()));
                    rewardQueue.addReward(new SignInSpecialDateReward(group, today));
                    rewardQueue.addReward(new SignInSpecialTimeReward(group, continuousSignIn));
                    rewardQueue.addReward(new SignInSpecialTimeCycleReward(group, continuousSignIn));
                    rewardQueue.addReward(new SignInSpecialTimeOfMonthReward(group, thisMonth, getContinuousSignInOfMonth()));
                    rewardQueue.addReward(new SignInSpecialTimePeriodReward(group, today));
                    rewardQueue.addReward(new SignInSpecialRankingReward(group, queue));
                    rewardQueue.addReward(new SignInNormalReward(group));
                }
                SignInRewardEvent event = new SignInRewardEvent(player, rewardQueue);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) rewardQueue.run(retroactiveDate != null);
            });
        } else {
            SignInGroup group = getGroup();
            if (group == null) return;
            int queue = SignInQueue.getInstance().getRank(uuid);
            int continuousSignIn = getContinuousSignIn();
            int totalNumber = getCumulativeNumber();
            SignInRewardSchedule rewardQueue = new SignInRewardSchedule(this);
            rewardQueue.addReward(new SignInStatisticsTimeReward(group, totalNumber));
            rewardQueue.addReward(new SignInStatisticsTimeCycleReward(group, totalNumber));
            if (retroactiveDate != null) {
                int week = retroactiveDate.getWeek();
                int retroactiveMonth = retroactiveDate.getMonth();
                rewardQueue.addReward(new SignInSpecialWeekReward(group, week));
                rewardQueue.addReward(new SignInStatisticsTimeOfMonthReward(group, retroactiveMonth, getCumulativeNumberOfMonth()));
                rewardQueue.addReward(new SignInSpecialDateReward(group, retroactiveDate));
                rewardQueue.addReward(new SignInRetroactiveTimeReward(group));
            } else {
                SignInDate today = SignInDate.getInstance(new Date());
                int week = today.getWeek();
                int thisMonth = today.getMonth();
                rewardQueue.addReward(new SignInSpecialWeekReward(group, week));
                rewardQueue.addReward(new SignInStatisticsTimeOfMonthReward(group, thisMonth, getCumulativeNumberOfMonth()));
                rewardQueue.addReward(new SignInSpecialDateReward(group, today));
                rewardQueue.addReward(new SignInSpecialTimeReward(group, continuousSignIn));
                rewardQueue.addReward(new SignInSpecialTimeCycleReward(group, continuousSignIn));
                rewardQueue.addReward(new SignInSpecialTimeOfMonthReward(group, thisMonth, getContinuousSignInOfMonth()));
                rewardQueue.addReward(new SignInSpecialTimePeriodReward(group, today));
                rewardQueue.addReward(new SignInSpecialRankingReward(group, queue));
                rewardQueue.addReward(new SignInNormalReward(group));
            }
            SignInRewardEvent event = new SignInRewardEvent(player, rewardQueue);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            rewardQueue.run(retroactiveDate != null);
        }
    }
    
    @Override
    public SignInGroup getGroup() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        SignInGroup group = null;
        PreparedConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
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
        PreparedConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        config.getStringList("Reward-Settings.Groups-Priority").stream()
            .filter(group -> config.get("Reward-Settings.Permission-Groups." + group + ".Permission") != null && player.hasPermission(config.getString("Reward-Settings.Permission-Groups." + group + ".Permission")))
            .forEach(group -> groups.add(new SignInGroup(group)));
        if (groups.isEmpty() && config.get("Reward-Settings.Permission-Groups.Default") != null) {
            groups.add(new SignInGroup("Default"));
        }
        return groups;
    }
    
    @Override
    public String getName() {
        return config.contains("Name") ? config.getString("Name") : Bukkit.getOfflinePlayer(uuid).getName();
    }
    
    @Override
    public int getYear() {
        return config.get("Last-time-SignIn.Year") != null ? config.getInt("Last-time-SignIn.Year") : 1970;
    }
    
    @Override
    public int getMonth() {
        return config.get("Last-time-SignIn.Month") != null ? config.getInt("Last-time-SignIn.Month") : 1;
    }
    
    @Override
    public int getDay() {
        return config.get("Last-time-SignIn.Day") != null ? config.getInt("Last-time-SignIn.Day") : 1;
    }
    
    @Override
    public int getHour() {
        return config.get("Last-time-SignIn.Hour") != null ? config.getInt("Last-time-SignIn.Hour") : 0;
    }
    
    @Override
    public int getMinute() {
        return config.get("Last-time-SignIn.Minute") != null ? config.getInt("Last-time-SignIn.Minute") : 0;
    }
    
    @Override
    public int getSecond() {
        return config.get("Last-time-SignIn.Second") != null ? config.getInt("Last-time-SignIn.Second") : 0;
    }
    
    @Override
    public int getContinuousSignIn() {
        return config.get("Continuous-SignIn") != null ? config.getInt("Continuous-SignIn") : 0;
    }
    
    @Override
    public int getRetroactiveCard() {
        if (PluginControl.enableRetroactiveCardRequiredItem()) {
            int amount = 0;
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return 0;
            ItemStack retroactiveCard = PluginControl.getRetroactiveCardRequiredItem(player);
            if (retroactiveCard == null) return 0;
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && !is.getType().equals(Material.AIR)) {
                    ItemMeta im = is.getItemMeta();
                    if (im.equals(retroactiveCard.getItemMeta())) {
                        amount += is.getAmount();
                    }
                }
            }
            return amount;
        }
        return config.get("RetroactiveCard") != null ? config.getInt("RetroactiveCard") : 0;
    }
    
    @Override
    public int getCumulativeNumber() {
        return clearUselessData(getHistory()).size();
    }
    
    @Override
    public int getContinuousSignInOfMonth() {
        return SignInDate.getContinuousOfMonth(getHistory());
    }
    
    @Override
    public int getCumulativeNumberOfMonth() {
        return SignInDate.getCumulativeNumberOfMonth(clearUselessData(getHistory()));
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
    public List<SignInDate> getHistory() {
        List<SignInDate> history = new ArrayList();
        if (config.get("History") != null) {
            config.getStringList("History").stream().forEach(data -> {
                history.add(SignInDate.getInstance(data));
            });
        }
        return history;
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
        List<String> data = new ArrayList();
        history.stream().forEach(dates -> {
            data.add(dates.getDataText(dates.hasTimePeriod()));
        });
        config.set("History", data);
        if (saveData) saveData();
    }
    
    @Override
    public void signIn() {
        if (SignInPluginUtils.checkInDisabledWorlds(uuid)) return;
        SignInDate today = SignInDate.getInstance(new Date());
        PlayerSignInEvent event = new PlayerSignInEvent(uuid, today, false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        setSignInTime(today, false);
        List<SignInDate> history = clearUselessData(getHistory());
        history.add(today);
        setHistory(history, false);
        setContinuousSignIn(SignInDate.getContinuous(history), true);
        giveReward(null);
    }
    
    @Override
    public void signIn(SignInDate historicalDate) {
        if (SignInPluginUtils.checkInDisabledWorlds(uuid)) return;
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
        giveReward(historicalDate);
        lastSignInTime.put(uuid, System.currentTimeMillis());
    }
    
    @Override
    public void giveRetroactiveCard(int amount) {
        if (amount < 1) return;
        if (PluginControl.enableRetroactiveCardRequiredItem()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            ItemStack retroactiveCard = PluginControl.getRetroactiveCardRequiredItem(player);
            if (retroactiveCard == null) return;
            retroactiveCard.setAmount(amount);
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(retroactiveCard);
            } else {
                player.getWorld().dropItem(player.getLocation(), retroactiveCard);
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
            ItemStack retroactiveCard = PluginControl.getRetroactiveCardRequiredItem(player);
            if (retroactiveCard == null) return;
            List<ItemStack> itemOnInv = new ArrayList();
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && !is.getType().equals(Material.AIR)) {
                    ItemMeta im = is.getItemMeta();
                    if (im.equals(retroactiveCard.getItemMeta())) {
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
            ItemStack retroactiveCard = PluginControl.getRetroactiveCardRequiredItem(player);
            if (retroactiveCard == null) return;
            for (ItemStack items : player.getInventory().getContents()) {
                if (items != null && !items.getType().equals(Material.AIR)) {
                    if (items.getItemMeta().equals(retroactiveCard.getItemMeta())) {
                        items.setAmount(0);
                        items.setType(Material.AIR);
                    }
                }
            }
            retroactiveCard.setAmount(amount);
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(retroactiveCard);
            } else {
                player.getWorld().dropItem(player.getLocation(), retroactiveCard);
            }
            if (saveData) saveData();
        } else {
            config.set("RetroactiveCard", amount >= 0 ? amount : 0);
            if (saveData) saveData();
        }
    }
    
    @Override
    public void setSignInTime(SignInDate date, boolean saveData) {
        config.set("Last-time-SignIn.Year", date.getYear());
        config.set("Last-time-SignIn.Month", date.getMonth());
        config.set("Last-time-SignIn.Day", date.getDay());
        config.set("Last-time-SignIn.Hour", date.getHour());
        config.set("Last-time-SignIn.Minute", date.getMinute());
        config.set("Last-time-SignIn.Second", date.getSecond());
        SignInQueue.getInstance().addRecord(uuid, date);
        if (saveData) saveData();
    }
    
    @Override
    public void setContinuousSignIn(int number, boolean saveData) {
        config.set("Continuous-SignIn", number);
        if (saveData) saveData();
    }
    
    @Override
    public void saveData() {
        if (loaded) {
            try {
                config.save("plugins/LiteSignIn/Players/" + uuid + ".yml");
            } catch (IOException ex) {}
        }
    }
    
    private void dataFileRepair() {
        File dataFolder = new File("plugins/LiteSignIn/Broken-Players");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        File dataFile = new File("plugins/LiteSignIn/Players/" + uuid.toString() + ".yml");
        dataFile.renameTo(new File("plugins/LiteSignIn/Broken-Players/" + uuid.toString() + ".yml"));
        try {
            dataFile.createNewFile();
        } catch (IOException ex) {}
        try (InputStreamReader Config = new InputStreamReader(new FileInputStream(dataFile), "UTF-8")) {
            config.load(Config);
        } catch (IOException | InvalidConfigurationException ex) {}
    }
    
    public static YamlStorage getPlayerData(Player player) {
        YamlStorage data = cache.get(player.getUniqueId());
        if (data != null) {
            return data;
        }
        data = new YamlStorage(player);
        cache.put(player.getUniqueId(), data);
        return data;
    }
    
    public static YamlStorage getPlayerData(UUID uuid) {
        YamlStorage data = cache.get(uuid);
        if (data != null) {
            return data;
        }
        data = new YamlStorage(uuid);
        cache.put(uuid, data);
        return data;
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
            File playerFolder = new File("plugins/LiteSignIn/Players");
            if (playerFolder.exists()) {
                File[] dataFiles = playerFolder.listFiles();
                for (File dataFile : dataFiles) {
                    if (dataFile.getName().endsWith(".yml")) {
                        YamlConfiguration yaml = new YamlConfiguration();
                        try {
                            yaml.load(dataFile);
                        } catch (IOException | InvalidConfigurationException ex) {
                            continue;
                        }
                        String uuid = dataFile.getName().replace(".yml", "");
                        String name = yaml.getString("Name");
                        int year = yaml.getInt("Last-time-SignIn.Year");
                        int month = yaml.getInt("Last-time-SignIn.Month");
                        int day = yaml.getInt("Last-time-SignIn.Day");
                        int hour = yaml.getInt("Last-time-SignIn.Hour");
                        int minute = yaml.getInt("Last-time-SignIn.Minute");
                        int second = yaml.getInt("Last-time-SignIn.Second");
                        int continuous = yaml.getInt("Continuous-SignIn");
                        int retroactivecard = yaml.getInt("RetroactiveCard");
                        String history = yaml.getStringList("History").toString().substring(1, yaml.getStringList("History").toString().length() - 1);
                        if (name == null) {
                            name = "null";
                        }
                        if (history == null) {
                            history = "";
                        }
                        try (PreparedStatement statement = sqlConnection.prepareStatement("INSERT INTO PlayerData(UUID, Name, Year, Month, Day, Hour, Minute, Second, Continuous, RetroactiveCard, History)"
                                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
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
        }
    }
}
