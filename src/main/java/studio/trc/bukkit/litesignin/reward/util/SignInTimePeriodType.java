package studio.trc.bukkit.litesignin.reward.util;

public enum SignInTimePeriodType {
    
    /**
     * Now.
     */
    ON_TIME,

    /**
     * It will be triggered if the current time is later than this time.
     */
    AFTER_THIS_TIME,

    /**
     * It will be triggered if the current time is earlier than this time.
     */
    BEFORE_THIS_TIME;
}
