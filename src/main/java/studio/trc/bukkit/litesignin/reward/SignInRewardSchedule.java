package studio.trc.bukkit.litesignin.reward;

import java.util.LinkedList;
import java.util.List;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.reward.type.SignInNormalReward;

/**
 * Sign in reward queue
 * @author Dean
 */
public class SignInRewardSchedule
{
    private final List<SignInReward> queue = new LinkedList();
    private final Storage playerData;
    
    public SignInRewardSchedule(Storage playerData) {
        this.playerData = playerData;
    }
    
    public List<SignInReward> getRewards() {
        return queue;
    }
    
    public void addReward(SignInReward reward) {
        queue.add(reward);
    }
    
    public void clearQueue() {
        queue.clear();
    }
    
    public void run(boolean retroactive) {
        if (retroactive) {
            SignInRewardRetroactive retroactiveTime = null;
            for (SignInReward reward : queue) {
                if (reward instanceof SignInRewardRetroactive) {
                    retroactiveTime = (SignInRewardRetroactive) reward;
                    reward.giveReward(playerData);
                    break;
                }
            }
            for (SignInReward reward : queue) {
                if (!retroactiveTime.isDisable(reward.getModule()) && !reward.getModule().equals(SignInRewardModule.RETROACTIVE_TIME)) {
                    reward.giveReward(playerData);
                }
            }
        } else {
            boolean overrideDefaultReward = false;
            for (SignInReward reward : queue) {
                if (reward instanceof SignInRewardColumn) {
                    if (((SignInRewardColumn) reward).overrideDefaultRewards()) {
                        overrideDefaultReward = true;
                        break;
                    }
                }
            }
            for (SignInReward reward : queue) {
                if (reward instanceof SignInNormalReward) {
                    if (!overrideDefaultReward) {
                        reward.giveReward(playerData);
                    }
                } else {
                    reward.giveReward(playerData);
                }
            }
        }
    }
}
