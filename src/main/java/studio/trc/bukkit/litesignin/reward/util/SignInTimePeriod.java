package studio.trc.bukkit.litesignin.reward.util;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.util.SignInDate;

public class SignInTimePeriod
{
    public static String getSetting(SignInGroup group, SignInDate time) {
        if (!ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods")) {
            return null;
        }
        for (String value : ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getConfigurationSection("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods").getKeys(false)) {
            SignInDate timePeriod = SignInDate.getInstanceAsTimePeriod(value);
            if (timePeriod == null) {
                continue;
            }
            SignInTimePeriodType timePeriodType = getFromName(ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getString("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + value + ".Option"));
            if (timePeriodType == null) {
                return null;
            }
            switch (timePeriodType) {
                case ON_TIME: {
                    String[] section = value.split(":");
                    switch (section.length) {
                        case 1: {
                            if (time.getHour() == timePeriod.getHour()) {
                                return value;
                            }
                            break;
                        }
                        case 2: {
                            if (time.getHour() == timePeriod.getHour() &&
                                time.getMinute() == timePeriod.getMinute()) {
                                return value;
                            }
                            break;
                        }
                        case 3: {
                            if (time.getHour() == timePeriod.getHour() &&
                                time.getMinute() == timePeriod.getMinute() &&
                                time.getSecond() == timePeriod.getSecond()) {
                                return value;
                            }
                            break;
                        }
                    }
                    break;
                }
                case AFTER_THIS_TIME: {
                    SignInDate limit;
                    if (!ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + value + ".Time-Limit")) {
                        limit = SignInDate.getInstanceAsTimePeriod("23:59:59");
                    } else {
                        limit = SignInDate.getInstanceAsTimePeriod(ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getString("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + value + ".Time-Limit"));
                    }
                    if (limit == null) {
                        continue;
                    }
                    if (time.compareTo(timePeriod) >= 0 && time.compareTo(limit) <= 0) {
                        return value;
                    }
                    break;
                }
                case BEFORE_THIS_TIME: {
                    SignInDate limit;
                    if (!ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + value + ".Time-Limit")) {
                        limit = SignInDate.getInstanceAsTimePeriod("00:00:00");
                    } else {
                        limit = SignInDate.getInstanceAsTimePeriod(ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getString("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + value + ".Time-Limit"));
                    }
                    if (limit == null) {
                        continue;
                    }
                    if (time.compareTo(timePeriod) <= 0 && time.compareTo(limit) >= 0) {
                        return value;
                    }
                    break;
                }
            }
        }
        return null;
    }
    
    public static SignInTimePeriodType getFromName(String name) {
        if (name == null) return null;
        for (SignInTimePeriodType timePeriod : SignInTimePeriodType.values()) {
            if (name.equalsIgnoreCase(timePeriod.name())) {
                return timePeriod;
            }
        }
        return null;
    }
}
