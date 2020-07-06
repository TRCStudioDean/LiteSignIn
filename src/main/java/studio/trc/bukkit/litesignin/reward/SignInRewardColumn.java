package studio.trc.bukkit.litesignin.reward;

/**
 * When a reward module has sub-settings,
 * You will need to implement it.
 * @author Dean
 */
public interface SignInRewardColumn
    extends SignInReward
{
    /**
     * Whether to override the default reward.
     * @return 
     */
    public boolean overrideDefaultRewards();
}
