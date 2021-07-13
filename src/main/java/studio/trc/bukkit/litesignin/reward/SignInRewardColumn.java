package studio.trc.bukkit.litesignin.reward;

/**
 * If reward module has sub-settings,
 * You will need to implement it.
 * @author Dean
 */
public abstract class SignInRewardColumn
    extends SignInRewardUtil
{
    /**
     * Whether to override the default reward.
     * @return 
     */
    public abstract boolean overrideDefaultRewards();
}
