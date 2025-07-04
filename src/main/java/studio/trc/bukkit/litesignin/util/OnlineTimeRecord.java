package studio.trc.bukkit.litesignin.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;

import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;

public class OnlineTimeRecord 
{
    @Getter
    private static final Map<UUID, Long> joinTimeRecord = new HashMap();
    @Getter
    private static final Map<UUID, OnlineTimeRecord > onlineTimeRecords = new HashMap();
    
    @Getter
    private final long timeInMillis;
    @Getter
    private final SignInDate recordTime;

    public OnlineTimeRecord(long timeInMillis, SignInDate recordTime) {
        this.timeInMillis = timeInMillis;
        this.recordTime = recordTime;
    }
    
    public static void savePlayerOnlineTime(Player player) {
        onlineTimeRecords.put(player.getUniqueId(), new OnlineTimeRecord(getTodayOnlineTime(player), SignInDate.getInstance(new Date())));
    }
    
    public static long getTodayOnlineTime(Player player) {
        return getTodayOnlineTime(player.getUniqueId());
    }
    
    public static long getTodayOnlineTime(UUID uuid) {
        if (!joinTimeRecord.containsKey(uuid)) return 0;
        SignInDate lastPlayed = SignInDate.getInstance(new Date(joinTimeRecord.get(uuid)));
        SignInDate now = SignInDate.getInstance(new Date());
        if (onlineTimeRecords.containsKey(uuid) && ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Online-Duration-Condition.Statistics")) {
            if (lastPlayed.getYear() == now.getYear() && lastPlayed.getMonth() == now.getMonth() && lastPlayed.getDay() == now.getDay()) {
                OnlineTimeRecord  record = onlineTimeRecords.get(uuid);
                if (record.getRecordTime().getYear() == now.getYear() && record.getRecordTime().getMonth() == now.getMonth() && record.getRecordTime().getDay() == now.getDay()) {
                    return record.getTimeInMillis() + now.getMillisecond() - lastPlayed.getMillisecond();
                } else {
                    return now.getMillisecond() - lastPlayed.getMillisecond();
                }
            } else {
                lastPlayed = SignInDate.getInstance(now.getYear(), now.getMonth(), now.getDay(), 0, 0, 0);
                return now.getMillisecond() - lastPlayed.getMillisecond();
            }
        } else {
            if (lastPlayed.getYear() != now.getYear() || lastPlayed.getMonth() != now.getMonth() || lastPlayed.getDay() != now.getDay()) {
                lastPlayed = SignInDate.getInstance(now.getYear(), now.getMonth(), now.getDay(), 0, 0, 0);
            }
            return now.getMillisecond() - lastPlayed.getMillisecond();
        }
    }
    
    public static long getSignInRequirement(Player player) {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.CONFIG);
        if (config.getBoolean("Online-Duration-Condition.Enabled")) {
            String[] time = config.getString("Online-Duration-Condition.Time").split(":");
            if (time.length == 3 && LiteSignInUtils.isInteger(time[0]) && LiteSignInUtils.isInteger(time[1]) && LiteSignInUtils.isInteger(time[2])) {
                long requirement = Long.valueOf(time[0]) * 1000 * 60 * 60 + Long.valueOf(time[1]) * 1000 * 60 + Long.valueOf(time[2]) * 1000;
                return getTodayOnlineTime(player) >= requirement ? -1 : requirement - getTodayOnlineTime(player);
            }
        }
        return -1;
    }
}
