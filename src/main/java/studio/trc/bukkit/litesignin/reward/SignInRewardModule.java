package studio.trc.bukkit.litesignin.reward;

import lombok.Getter;

import studio.trc.bukkit.litesignin.message.MessageUtil;

public enum SignInRewardModule
{
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
     * Special week days
     */
    SPECIAL_WEEK_DAYS("Special-Week-Days"),

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
     * Statistics times
     */
    STATISTICS_TIMES("Statistics-Times"),

    /**
     * Special times of month
     */
    SPECIAL_TIMES_OF_MONTH("Special-Times-Of-Month"),

    /**
     * Statistics times of month
     */
    STATISTICS_TIMES_OF_MONTH("Statistics-Times-Of-Month"),

    /**
     * Special times cycle
     */
    SPECIAL_TIMES_CYCLE("Special-Times-Cycle"),

    /**
     * Statistics times cycle
     */
    STATISTICS_TIMES_CYCLE("Statistics-Times-Cycle"),

    /**
     * Custom date and time periods
     */
    CUSTOM_DATE_AND_TIME_PERIOD("Custom-Date-And-Time-Periods");

    @Getter
    private final String configPath;

    private SignInRewardModule(String configPath) {
        this.configPath = configPath;
    }
    
    public String getDisplayName() {
        return MessageUtil.getMessage("Reward-Types." + configPath);
    }
}
