package studio.trc.bukkit.litesignin.reward;

public interface SignInRewardRetroactive
    extends SignInReward
{
    /**
     * 
     * @param module SignInReward module.
     * @return 
     */
    public boolean isDisable(SignInRewardModule module);
}
