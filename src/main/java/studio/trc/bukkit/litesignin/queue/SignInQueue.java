package studio.trc.bukkit.litesignin.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;
import studio.trc.bukkit.litesignin.util.PluginControl;

/**
 * Sign-in ranking.
 * @author Dean
 */
public class SignInQueue
    extends ArrayList<SignInQueueElement>
{
    private static final Map<SignInDate, SignInQueue> cache = new HashMap();
    private static final Map<SignInDate, Long> lastUpdateTime = new HashMap();
    
    public static SignInQueue getInstance() {
        SignInDate date = SignInDate.getInstance(new Date());
        for (SignInDate dates : cache.keySet()) {
            if (dates.equals(date)) {
                SignInQueue queue = cache.get(dates);
                queue.checkUpdate();
                return queue;
            }
        }
        SignInQueue queue = new SignInQueue(date);
        cache.put(date, queue);
        queue.checkUpdate();
        return queue;
    }
    
    public static SignInQueue getInstance(SignInDate date) {
        for (SignInDate dates : cache.keySet()) {
            if (dates.equals(date)) {
                SignInQueue queue = cache.get(dates);
                queue.checkUpdate();
                return queue;
            }
        }
        SignInQueue queue = new SignInQueue(date);
        cache.put(date, queue);
        queue.checkUpdate();
        return queue;
    }
    
    private final FileConfiguration yamlFile = new YamlConfiguration();
    private final SignInDate date;
    
    public SignInQueue(SignInDate date) {
        this.date = date;
    }
    
    public SignInDate getDate() {
        return date;
    }
    
    public void loadQueue() {
        if (!PluginControl.enableSignInRanking()) {
            return;
        }
        try {
            if (PluginControl.useMySQLStorage() && !MySQLEngine.getConnection().isClosed()) {
                ResultSet rs = MySQLEngine.executeQuery(MySQLEngine.getConnection().prepareStatement("SELECT * FROM " + MySQLEngine.getDatabase() + "." + MySQLEngine.getTable() + " WHERE History LIKE '%" + date.getDataText(false) + "%'"));
                clear();
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("UUID"));
                        SignInDate time = null;
                        if (date.equals(SignInDate.getInstance(new Date()))) {
                            time = SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay(), rs.getInt("Hour"), rs.getInt("Minute"), rs.getInt("Second"));
                        } else {
                            Integer hour = null, minute = null, second = null;
                            for (String data : Arrays.asList(rs.getString("History").split(", "))) {
                                SignInDate targetDate = SignInDate.getInstance(data);
                                if (date.equals(targetDate)) {
                                    if (targetDate.hasTimePeriod()) {
                                        hour = targetDate.getHour();
                                        minute = targetDate.getMinute();
                                        second = targetDate.getSecond();
                                    }
                                    if (hour != null && minute != null && second != null) {
                                        time = SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay(), hour, minute, second);
                                    } else {
                                        time = SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay());
                                    }
                                    break;
                                }
                            }
                        }
                        if (time != null) add(new SignInQueueElement(uuid, time, rs.getString("Name")));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                lastUpdateTime.put(date, System.currentTimeMillis());
            } else if (PluginControl.useSQLiteStorage() && !SQLiteEngine.getConnection().isClosed()) {
                ResultSet rs = SQLiteEngine.executeQuery(SQLiteEngine.getConnection().prepareStatement("SELECT * FROM " + SQLiteEngine.getTable() + " WHERE History LIKE '%" + date.getDataText(false) + "%'"));
                clear();
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("UUID"));
                        SignInDate time = null;
                        if (date.equals(SignInDate.getInstance(new Date()))) {
                            time = SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay(), rs.getInt("Hour"), rs.getInt("Minute"), rs.getInt("Second"));
                        } else {
                            Integer hour = null, minute = null, second = null;
                            for (String data : Arrays.asList(rs.getString("History").split(", "))) {
                                SignInDate targetDate = SignInDate.getInstance(data);
                                if (date.equals(targetDate)) {
                                    if (targetDate.hasTimePeriod()) {
                                        hour = targetDate.getHour();
                                        minute = targetDate.getMinute();
                                        second = targetDate.getSecond();
                                    }
                                    if (hour != null && minute != null && second != null) {
                                        time = SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay(), hour, minute, second);
                                    } else {
                                        time = SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay());
                                    }
                                    break;
                                }
                            }
                        }
                        if (time != null) add(new SignInQueueElement(uuid, time, rs.getString("Name")));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                lastUpdateTime.put(date, System.currentTimeMillis());
            } else {
                clear();
                File cacheFile = new File("plugins/LiteSignIn/Cache.dat");
                if (!cacheFile.exists()) {
                    cacheFile.createNewFile();
                }
                if (!date.equals(SignInDate.getInstance(new Date()))) return;
                try (Reader reader = new InputStreamReader(new FileInputStream(cacheFile), "UTF-8")) {
                    yamlFile.load(reader);
                } catch (InvalidConfigurationException ex) {
                    Logger.getLogger(SignInQueue.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!yamlFile.contains("Date") || !SignInDate.getInstance(yamlFile.getString("Date")).equals(date)) {
                    yamlFile.set("Date", SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay()).getDataText(true));
                    yamlFile.set("Record", null);
                    saveData();
                    return;
                }
                if (yamlFile.contains("Record")) {
                    for (String datatext : yamlFile.getStringList("Record")) {
                        try {
                            String[] data = datatext.split(":");
                            UUID uuid = UUID.fromString(data[0]);
                            SignInDate historicalDate = SignInDate.getInstance(data[1]);
                            if (data.length > 2) {
                                add(new SignInQueueElement(uuid, historicalDate, data[2]));
                            } else {
                                add(new SignInQueueElement(uuid, historicalDate));
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void addRecord(UUID uuid, SignInDate date) {
        SignInDate today = SignInDate.getInstance(new Date());
        if (!yamlFile.contains("Date") || !SignInDate.getInstance(yamlFile.getString("Date")).equals(today)) {
            yamlFile.set("Date", SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay()).getDataText(true));
            yamlFile.set("Record", null);
            clear();
            saveData();
        }
        add(new SignInQueueElement(uuid, date, Bukkit.getOfflinePlayer(uuid).getName()));
        saveData();
    }
    
    public SignInQueueElement getElement(UUID uuid) {
        checkUpdate();
        for (SignInQueueElement element : this) {
            UUID queueUUID = element.getUUID();
            if (queueUUID.equals(uuid)) {
                return element;
            }
        }
        return null;
    }
    
    public int getRank(UUID uuid) {
        if (!PluginControl.enableSignInRanking()) {
            return 0;
        }
        checkUpdate();
        SignInQueueElement user = getElement(uuid);
        if (user == null) {
            return 0;
        }
        int rank = 1;
        if (user.getSignInDate().hasTimePeriod()) {
            for (SignInQueueElement element : this) {
                if (user.getSignInDate().compareTo(element.getSignInDate()) > 0 && element.getSignInDate().hasTimePeriod()) {
                    rank++;
                }
            }
        } else {
            for (SignInQueueElement element : this) {
                if (element.getSignInDate().hasTimePeriod()) {
                    rank++;
                }
            }
            for (SignInQueueElement element : getUnknownTimesElement()) {
                if (!element.getUUID().equals(uuid)) {
                    rank++;
                } else {
                    return rank;
                }
            }
        }
        return rank;
    }
    
    public List<SignInQueueElement> getUnknownTimesElement() {
        List<SignInQueueElement> list = new ArrayList();
        for (SignInQueueElement element : this) {
            if (!element.getSignInDate().hasTimePeriod()) {
                list.add(element);
            }
        }
        return list;
    }
    
    public void checkUpdate() {
        if (PluginControl.useMySQLStorage()) {
            if (!lastUpdateTime.containsKey(date)) {
                loadQueue();
            }
            if (PluginControl.getMySQLRefreshInterval() == 0 || System.currentTimeMillis() - lastUpdateTime.get(date) >= PluginControl.getMySQLRefreshInterval() * 1000) {
                loadQueue();
            }
        } else if (PluginControl.useSQLiteStorage()) {
            if (!lastUpdateTime.containsKey(date)) {
                loadQueue();
            }
            if (PluginControl.getSQLiteRefreshInterval() == 0 || System.currentTimeMillis() - lastUpdateTime.get(date) >= PluginControl.getSQLiteRefreshInterval() * 1000) {
                loadQueue();
            }
        }
    }
    
    public List<SignInQueueElement> getRankingUser(int ranking) {
        checkUpdate();
        List<SignInQueueElement> result = new ArrayList();
        for (SignInQueueElement element : this) {
            if (getRank(element.getUUID()) == ranking) {
                result.add(element);
            }
        }
        return result;
    }
    
    public void saveData() {
        if (!PluginControl.enableSignInRanking()) {
            return;
        }
        try {
            if (!PluginControl.useMySQLStorage() && !PluginControl.useSQLiteStorage()) {
                File file = new File("plugins/LiteSignIn/Cache.dat");
                if (!file.exists()) {
                    file.createNewFile();
                }
                List<String> array = new ArrayList();
                for (SignInQueueElement element : this) {
                    array.add(element.toString());
                }
                yamlFile.set("Record", array);
                yamlFile.save(file);
            }
        } catch (IOException ex) {
            Logger.getLogger(SignInQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
