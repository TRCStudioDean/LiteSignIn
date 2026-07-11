package studio.trc.bukkit.litesignin.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.reward.SignInReward;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.type.*;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.util.SignInTimePeriod;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.LiteSignInUtils;

public class RewardCommand
    implements SignInSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (args.length <= 3) {
            MessageUtil.sendCommandMessage(sender, "Reward.Help", placeholders);
        } else {
            RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
            //Check player.
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                LiteSignInUtils.playerNotExist(sender, args[1]);
            }
            placeholders.put("{player}", player.getName());
            //Check sign-in group.
            SignInGroup group;
            if (config.get("Reward-Settings.Permission-Groups." + args[2]) != null) {
                group = new SignInGroup(args[2]);
            } else {
                placeholders.put("{group}", args[2]);
                MessageUtil.sendCommandMessage(sender, "Reward.Unknown-Group", placeholders);
                return;
            }
            //Check reward type name.
            SignInRewardModule rewardType;
            try {
                rewardType = SignInRewardModule.valueOf(args[3].split(":")[0].toUpperCase());
            } catch (Exception ex) {
                placeholders.put("{rewardType}", args[3].split(":")[0].toUpperCase());
                MessageUtil.sendCommandMessage(sender, "Reward.Unknown-Type", placeholders);
                return;
            }
            placeholders.put("{rewardType}", rewardType.name());
            placeholders.put("{configPath}", rewardType.getConfigPath());
            placeholders.put("{displayName}", rewardType.getDisplayName());
            //Check whether reward type is valid.
            if (config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath()) == null) {
                placeholders.put("{group}", group.getGroupName());
                MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Reward", placeholders);
                return;
            }
            placeholders.put("{group}", group.getGroupName());
            Storage playerdata = Storage.getPlayer(player);
            switch (rewardType) {
                case NORMAL_TIME: {
                    SignInReward reward = new SignInNormalReward(group);
                    reward.giveReward(playerdata);
                    placeholders.put("{value}", MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                    MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    break;
                }
                case RETROACTIVE_TIME: {
                    SignInReward reward = new SignInRetroactiveTimeReward(group);
                    reward.giveReward(playerdata);
                    placeholders.put("{value}", MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                    MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    break;
                }
                case SPECIAL_DATES: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        SignInDate date = SignInDate.getInstance(new Date());
                        String[] monthAndDay = value.split("-");
                        date.setMonth(Integer.valueOf(monthAndDay[0]));
                        date.setDay(Integer.valueOf(monthAndDay[1]));
                        placeholders.put("{value}", date.getMonthAsString() + "-" + date.getDayAsString());
                        if (config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + date.getMonthAsString() + "-" + date.getDayAsString()) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.SPECIAL_DATES", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInSpecialDateReward(group, date);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_DATES", placeholders);
                    }
                    break;
                }
                case SPECIAL_WEEK_DAYS: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_WEEK_DAYS", placeholders);
                            return;
                        }
                        int week = Integer.valueOf(value);
                        if (week < 1 || week > 7 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + week) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.SPECIAL_WEEK_DAYS", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInSpecialWeekDayReward(group, week);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_WEEK_DAYS", placeholders);
                    }
                    break;
                }
                case SPECIAL_TIMES: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES", placeholders);
                            return;
                        }
                        int time = Integer.valueOf(value);
                        if (time < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + time) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.SPECIAL_TIMES", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInSpecialTimeReward(group, time);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES", placeholders);
                    }
                    break;
                }
                case SPECIAL_RANKING: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_RANKING", placeholders);
                            return;
                        }
                        int ranking = Integer.valueOf(value);
                        if (ranking < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + ranking) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.SPECIAL_RANKING", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInSpecialRankingReward(group, ranking);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_RANKING", placeholders);
                    }
                    break;
                }
                case SPECIAL_TIME_PERIODS: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        SignInDate date = SignInDate.getInstance(new Date());
                        String[] timePeriod = value.split(":");
                        date.setHour(Integer.valueOf(timePeriod[0]));
                        date.setMinute(Integer.valueOf(timePeriod[1]));
                        date.setSecond(Integer.valueOf(timePeriod[2]));
                        String settings = SignInTimePeriod.getSetting(group, date);
                        placeholders.put("{value}", settings);
                        if (config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + settings) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.SPECIAL_TIME_PERIODS", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInSpecialTimePeriodReward(group, date);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIME_PERIODS", placeholders);
                    }
                    break;
                }
                case STATISTICS_TIMES: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_TIMES", placeholders);
                            return;
                        }
                        int number = Integer.valueOf(value);
                        if (number < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + number) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.STATISTICS_TIMES", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInStatisticsTimeReward(group, number);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_TIMES", placeholders);
                    }
                    break;
                }
                case SPECIAL_TIMES_OF_MONTH: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        String[] elements = value.split(":");
                        if (!LiteSignInUtils.isInteger(elements[0]) || !LiteSignInUtils.isInteger(elements[1])) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES_OF_MONTH", placeholders);
                            return;
                        }
                        int month = Integer.valueOf(elements[1]);
                        int time = Integer.valueOf(elements[0]);
                        if (time < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + time) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.SPECIAL_TIMES_OF_MONTH", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInSpecialTimeOfMonthReward(group, month, time);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES_OF_MONTH", placeholders);
                    }
                    break;
                }
                case STATISTICS_TIMES_OF_MONTH: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        String[] elements = value.split(":");
                        if (!LiteSignInUtils.isInteger(elements[0]) || !LiteSignInUtils.isInteger(elements[1])) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_TIMES_OF_MONTH", placeholders);
                            return;
                        }
                        int month = Integer.valueOf(elements[1]);
                        int number = Integer.valueOf(elements[0]);
                        if (number < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + number) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.STATISTICS_TIMES_OF_MONTH", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInStatisticsTimeOfMonthReward(group, month, number);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_TIMES_OF_MONTH", placeholders);
                    }
                    break;
                }
                case SPECIAL_TIMES_CYCLE: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES_CYCLE", placeholders);
                            return;
                        }
                        int time = Integer.valueOf(value);
                        if (time < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + time) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.SPECIAL_TIMES_CYCLE", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInSpecialTimeCycleReward(group, time);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES_CYCLE", placeholders);
                    }
                    break;
                }
                case STATISTICS_TIMES_CYCLE: {
                    try {
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_TIMES_CYCLE", placeholders);
                            return;
                        }
                        int number = Integer.valueOf(value);
                        if (number < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigPath() + "." + number) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.STATISTICS_TIMES_CYCLE", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInStatisticsTimeCycleReward(group, number);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_TIMES_CYCLE", placeholders);
                    }
                    break;
                }
                case CUSTOM_DATE_AND_TIME_PERIOD: {
                    try {
                        String[] dateAndTime = args[3].split(":", 3);
                        if (dateAndTime.length == 3) {
                            SignInDate specfic = SignInDate.getInstanceByFormat(dateAndTime[1] + " " + dateAndTime[2]);
                            if (specfic != null) {
                                SignInCustomDateAndTimePeriodReward reward = new SignInCustomDateAndTimePeriodReward(group, specfic);
                                placeholders.put("{value}", dateAndTime[1] + " " + dateAndTime[2]);
                                if (reward.isAvailable()) {
                                    reward.giveReward(playerdata);
                                    MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                                } else {
                                    MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.CUSTOM_DATE_AND_TIME_PERIOD", placeholders);
                                }
                                return;
                            }
                        }
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.CUSTOM_DATE_AND_TIME_PERIOD", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.CUSTOM_DATE_AND_TIME_PERIOD", placeholders);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "reward";
    }

    private static final Map<SignInRewardModule, String> TYPE_CONFIG_MAP = new HashMap<>();
    private static final Map<SignInRewardModule, String> MONTH_TYPE_CONFIG_MAP = new HashMap<>();

    static {
        TYPE_CONFIG_MAP.put(SignInRewardModule.SPECIAL_DATES, "Special-Dates");
        TYPE_CONFIG_MAP.put(SignInRewardModule.SPECIAL_RANKING, "Special-Ranking");
        TYPE_CONFIG_MAP.put(SignInRewardModule.SPECIAL_TIMES, "Special-Times");
        TYPE_CONFIG_MAP.put(SignInRewardModule.SPECIAL_TIMES_CYCLE, "Special-Times-Cycle");
        TYPE_CONFIG_MAP.put(SignInRewardModule.SPECIAL_TIME_PERIODS, "Special-Time-periods");
        TYPE_CONFIG_MAP.put(SignInRewardModule.SPECIAL_WEEK_DAYS, "Special-Week-Days");
        TYPE_CONFIG_MAP.put(SignInRewardModule.STATISTICS_TIMES, "Statistics-Times");
        TYPE_CONFIG_MAP.put(SignInRewardModule.STATISTICS_TIMES_CYCLE, "Statistics-Times-Cycle");
        MONTH_TYPE_CONFIG_MAP.put(SignInRewardModule.SPECIAL_TIMES_OF_MONTH, "Special-Times-Of-Month");
        MONTH_TYPE_CONFIG_MAP.put(SignInRewardModule.STATISTICS_TIMES_OF_MONTH, "Statistics-Times-Of-Month");
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        if (args.length == 2) {
            return tabGetPlayersName(args, 2);
        }
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (args.length == 3) {
            return getTabElements(args, args.length, config.getStringList("Reward-Settings.Groups-Priority"));
        }
        if (args.length == 4) {
            if (config.get("Reward-Settings.Permission-Groups." + args[2]) == null) {
                return new ArrayList<>();
            }
            if (!args[3].contains(":")) {
                List<String> types = Arrays.stream(SignInRewardModule.values()).map(Enum::name).collect(Collectors.toList());
                return getTabElements(args, args.length, types);
            }
            for (Map.Entry<SignInRewardModule, String> entry : TYPE_CONFIG_MAP.entrySet()) {
                SignInRewardModule type = entry.getKey();
                String configSuffix = entry.getValue();
                if (args[3].split(":", 2)[0].equalsIgnoreCase(type.name())) {
                    String configPath = "Reward-Settings.Permission-Groups." + args[2] + "." + configSuffix;
                    if (config.get(configPath) == null) {
                        return new ArrayList<>();
                    }
                    return getTabElements(args, args.length, config.getConfigurationSection(configPath).getKeys(false).stream().map(value -> type.name() + ":" + value).collect(Collectors.toList()));
                }
            }
            for (Map.Entry<SignInRewardModule, String> entry : MONTH_TYPE_CONFIG_MAP.entrySet()) {
                SignInRewardModule type = entry.getKey();
                String configSuffix = entry.getValue();
                if (args[3].toUpperCase().startsWith(type.name())) {
                    String configPath = "Reward-Settings.Permission-Groups." + args[2] + "." + configSuffix;
                    if (config.get(configPath) == null) {
                        return new ArrayList<>();
                    }
                    List<String> values = new ArrayList<>();
                    config.getConfigurationSection(configPath).getKeys(false).forEach(value -> {
                        String monthConfigPath = configPath + "." + value + ".Valid-Months";
                        if (config.get(monthConfigPath) != null) {
                            config.getIntegerList(monthConfigPath).forEach(month -> values.add(type.name() + ":" + value + ":" + month));
                        } else {
                            values.add(type.name() + ":" + value);
                        }
                    });
                    return getTabElements(args, args.length, values);
                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.REWARD;
    }
}
