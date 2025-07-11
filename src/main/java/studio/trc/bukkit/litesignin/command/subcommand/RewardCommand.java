package studio.trc.bukkit.litesignin.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

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
            RewardType rewardType;
            try {
                rewardType = RewardType.valueOf(args[3].split(":")[0].toUpperCase());
            } catch (Exception ex) {
                placeholders.put("{rewardType}", args[3].split(":")[0].toUpperCase());
                MessageUtil.sendCommandMessage(sender, "Reward.Unknown-Type", placeholders);
                return;
            }
            //Check whether reward type is valid.
            if (config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName()) == null) {
                placeholders.put("{group}", group.getGroupName());
                placeholders.put("{rewardType}", rewardType.getConfigName());
                MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Reward", placeholders);
                return;
            }
            Storage playerdata = Storage.getPlayer(player);
            switch (rewardType) {
                case NORMAL_TIME: {
                    SignInReward reward = new SignInNormalReward(group);
                    reward.giveReward(playerdata);
                    placeholders.put("{group}", group.getGroupName());
                    placeholders.put("{rewardType}", rewardType.getConfigName());
                    placeholders.put("{value}", MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                    MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    break;
                }
                case RETROACTIVE_TIME: {
                    SignInReward reward = new SignInRetroactiveTimeReward(group);
                    reward.giveReward(playerdata);
                    placeholders.put("{group}", group.getGroupName());
                    placeholders.put("{rewardType}", rewardType.getConfigName());
                    placeholders.put("{value}", MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                    MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    break;
                }
                case SPECIAL_DATES: {
                    try {
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        SignInDate date = SignInDate.getInstance(new Date());
                        String[] monthAndDay = value.split("-");
                        date.setMonth(Integer.valueOf(monthAndDay[0]));
                        date.setDay(Integer.valueOf(monthAndDay[1]));
                        placeholders.put("{value}", date.getMonthAsString() + "-" + date.getDayAsString());
                        if (config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + date.getMonthAsString() + "-" + date.getDayAsString()) == null) {
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
                case SPECIAL_WEEKS: {
                    try {
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_WEEKS", placeholders);
                            return;
                        }
                        int week = Integer.valueOf(value);
                        if (week < 1 || week > 7 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + week) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.SPECIAL_WEEKS", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInSpecialWeekReward(group, week);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_WEEKS", placeholders);
                    }
                    break;
                }
                case SPECIAL_TIMES: {
                    try {
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES", placeholders);
                            return;
                        }
                        int time = Integer.valueOf(value);
                        if (time < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + time) == null) {
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
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_RANKING", placeholders);
                            return;
                        }
                        int ranking = Integer.valueOf(value);
                        if (ranking < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + ranking) == null) {
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
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        SignInDate date = SignInDate.getInstance(new Date());
                        String[] timePeriod = value.split(":");
                        date.setHour(Integer.valueOf(timePeriod[0]));
                        date.setMinute(Integer.valueOf(timePeriod[1]));
                        date.setSecond(Integer.valueOf(timePeriod[2]));
                        String settings = SignInTimePeriod.getSetting(group, date);
                        placeholders.put("{value}", settings);
                        if (config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + settings) == null) {
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
                case STATISTICS: {
                    try {
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS", placeholders);
                            return;
                        }
                        int number = Integer.valueOf(value);
                        if (number < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + number) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.STATISTICS", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInStatisticsTimeReward(group, number);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS", placeholders);
                    }
                    break;
                }
                case SPECIAL_TIMES_OF_MONTH: {
                    try {
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        String[] elements = value.split(":");
                        if (!LiteSignInUtils.isInteger(elements[0]) || !LiteSignInUtils.isInteger(elements[1])) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES_OF_MONTH", placeholders);
                            return;
                        }
                        int month = Integer.valueOf(elements[1]);
                        int time = Integer.valueOf(elements[0]);
                        if (time < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + time) == null) {
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
                case STATISTICS_OF_MONTH: {
                    try {
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        String[] elements = value.split(":");
                        if (!LiteSignInUtils.isInteger(elements[0]) || !LiteSignInUtils.isInteger(elements[1])) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_OF_MONTH", placeholders);
                            return;
                        }
                        int month = Integer.valueOf(elements[1]);
                        int number = Integer.valueOf(elements[0]);
                        if (number < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + number) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.STATISTICS_OF_MONTH", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInStatisticsTimeOfMonthReward(group, month, number);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_OF_MONTH", placeholders);
                    }
                    break;
                }
                case SPECIAL_TIME_CYCLE: {
                    try {
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.SPECIAL_TIMES_CYCLE", placeholders);
                            return;
                        }
                        int time = Integer.valueOf(value);
                        if (time < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + time) == null) {
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
                case STATISTICS_CYCLE: {
                    try {
                        placeholders.put("{group}", group.getGroupName());
                        placeholders.put("{rewardType}", rewardType.getConfigName());
                        String value = args[3].substring(args[3].indexOf(":") + 1);
                        placeholders.put("{value}", value);
                        if (!LiteSignInUtils.isInteger(value)) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_CYCLE", placeholders);
                            return;
                        }
                        int number = Integer.valueOf(value);
                        if (number < 1 || config.get("Reward-Settings.Permission-Groups." + group.getGroupName() + "." + rewardType.getConfigName() + "." + number) == null) {
                            MessageUtil.sendCommandMessage(sender, "Reward.Invalid-Parameters.STATISTICS_CYCLE", placeholders);
                            return;
                        }
                        SignInReward reward = new SignInStatisticsTimeCycleReward(group, number);
                        reward.giveReward(playerdata);
                        MessageUtil.sendCommandMessage(sender, "Reward.Successfully-Reward", placeholders);
                    } catch (Exception ex) {
                        placeholders.put("{value}", args.length >= 3 ? args[3].substring(args[3].indexOf(":") + 1) : MessageUtil.getMessage("Command-Messages.Reward.Nothing"));
                        MessageUtil.sendCommandMessage(sender, "Reward.Wrong-Parameters.STATISTICS_CYCLE", placeholders);
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
                return new ArrayList();
            }
            if (!args[3].contains(":")) {
                List<String> types = Arrays.stream(RewardType.values())
                        .map(type -> type.name())
                        .collect(Collectors.toList());
                return getTabElements(args, args.length, types);
            } else if (args[3].toUpperCase().startsWith(RewardType.SPECIAL_DATES.name())) {
                if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Special-Dates") == null) {
                    return new ArrayList();
                }
                return getTabElements(args, args.length, config.getConfigurationSection("Reward-Settings.Permission-Groups." + args[2] + ".Special-Dates").getKeys(false).stream().map(value -> RewardType.SPECIAL_DATES.name() + ":" + value).collect(Collectors.toList()));
            } else if (args[3].toUpperCase().startsWith(RewardType.SPECIAL_RANKING.name())) {
                if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Special-Ranking") == null) {
                    return new ArrayList();
                }
                return getTabElements(args, args.length, config.getConfigurationSection("Reward-Settings.Permission-Groups." + args[2] + ".Special-Ranking").getKeys(false).stream().map(value -> RewardType.SPECIAL_RANKING.name() + ":" + value).collect(Collectors.toList()));
            } else if (args[3].toUpperCase().startsWith(RewardType.SPECIAL_TIMES_OF_MONTH.name())) {
                if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Special-Times-Of-Month") == null) {
                    return new ArrayList();
                }
                List<String> values = new ArrayList();
                config.getConfigurationSection("Reward-Settings.Permission-Groups." + args[2] + ".Special-Times-Of-Month").getKeys(false).stream().forEach(value -> {
                    if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Special-Times-Of-Month." + value + ".Valid-Months") != null) {
                        config.getIntegerList("Reward-Settings.Permission-Groups." + args[2] + ".Special-Times-Of-Month." + value + ".Valid-Months").stream().forEach(month -> {
                            values.add(RewardType.SPECIAL_TIMES_OF_MONTH.name() + ":" + value + ":" + month);
                        });
                    } else {
                        values.add(RewardType.SPECIAL_TIMES_OF_MONTH.name() + ":" + value);
                    }
                });
                return getTabElements(args, args.length, values);
            } else if (args[3].toUpperCase().startsWith(RewardType.STATISTICS_OF_MONTH.name())) {
                if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Statistics-Times-Of-Month") == null) {
                    return new ArrayList();
                }
                List<String> values = new ArrayList();
                config.getConfigurationSection("Reward-Settings.Permission-Groups." + args[2] + ".Statistics-Times-Of-Month").getKeys(false).stream().forEach(value -> {
                    if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Statistics-Times-Of-Month." + value + ".Valid-Months") != null) {
                        config.getIntegerList("Reward-Settings.Permission-Groups." + args[2] + ".Statistics-Times-Of-Month." + value + ".Valid-Months").stream().forEach(month -> {
                            values.add(RewardType.STATISTICS_OF_MONTH.name() + ":" + value + ":" + month);
                        });
                    } else {
                        values.add(RewardType.STATISTICS_OF_MONTH.name() + ":" + value);
                    }
                });
                return getTabElements(args, args.length, values);
            } else if (args[3].toUpperCase().startsWith(RewardType.SPECIAL_TIMES.name())) {
                if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Special-Times") == null) {
                    return new ArrayList();
                }
                return getTabElements(args, args.length, config.getConfigurationSection("Reward-Settings.Permission-Groups." + args[2] + ".Special-Times").getKeys(false).stream().map(value -> RewardType.SPECIAL_TIMES.name() + ":" + value).collect(Collectors.toList()));
            } else if (args[3].toUpperCase().startsWith(RewardType.SPECIAL_TIME_PERIODS.name())) {
                if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Special-Time-periods") == null) {
                    return new ArrayList();
                }
                return getTabElements(args, args.length, config.getConfigurationSection("Reward-Settings.Permission-Groups." + args[2] + ".Special-Time-periods").getKeys(false).stream().map(value -> RewardType.SPECIAL_TIME_PERIODS.name() + ":" + value).collect(Collectors.toList()));
            } else if (args[3].toUpperCase().startsWith(RewardType.SPECIAL_WEEKS.name())) {
                if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Special-Weeks") == null) {
                    return new ArrayList();
                }
                return getTabElements(args, args.length, config.getConfigurationSection("Reward-Settings.Permission-Groups." + args[2] + ".Special-Weeks").getKeys(false).stream().map(value -> RewardType.SPECIAL_WEEKS.name() + ":" + value).collect(Collectors.toList()));
            } else if (args[3].toUpperCase().startsWith(RewardType.STATISTICS.name())) {
                if (config.get("Reward-Settings.Permission-Groups." + args[2] + ".Statistics-Times") == null) {
                    return new ArrayList();
                }
                return getTabElements(args, args.length, config.getConfigurationSection("Reward-Settings.Permission-Groups." + args[2] + ".Statistics-Times").getKeys(false).stream().map(value -> RewardType.STATISTICS.name() + ":" + value).collect(Collectors.toList()));
            }
        }
        return new ArrayList();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.REWARD;
    }
    
    public enum RewardType {
        /**
         * Normal time
         */
        NORMAL_TIME("Normal-Time"),
        
        /**
         * Retroactive time
         */
        RETROACTIVE_TIME("Retroactive-Time"),
        
        /**
         * Special times
         */
        SPECIAL_TIMES("Special-Times"),
        
        /**
         * Special weeks
         */
        SPECIAL_WEEKS("Special-Weeks"),
        
        /**
         * Special dates
         */
        SPECIAL_DATES("Special-Dates"),
        
        /**
         * Special ranking
         */
        SPECIAL_RANKING("Special-Ranking"),
        
        /**
         * Special time periods
         */
        SPECIAL_TIME_PERIODS("Special-Time-periods"),
        
        /**
         * Statistics
         */
        STATISTICS("Statistics-Times"),
        
        /**
         * Special times of month
         */
        SPECIAL_TIMES_OF_MONTH("Special-Times-Of-Month"),
        
        /**
         * Statistics of month
         */
        STATISTICS_OF_MONTH("Statistics-Times-Of-Month"),
        
        /**
         * Special time cycle
         */
        SPECIAL_TIME_CYCLE("Special-Times-Cycle"),
        
        /**
         * Statistics cycle
         */
        STATISTICS_CYCLE("Statistics-Times-Cycle");
        
        @Getter
        private final String configName;
        
        private RewardType(String configName) {
            this.configName = configName;
        }
    }
}
