package studio.trc.bukkit.litesignin.reward;

public abstract class SignInRewardRetroactive
    extends SignInRewardUtil
{
    /**
     * 
     * @param module SignInReward module.
     * @return 
     */
    public abstract boolean isDisable(SignInRewardModule module);
}
