package studio.trc.bukkit.litesignin.reward;

import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;

public interface SignInReward
{
    /**
     * Get SignInReward permission group.
     * @return 
     */
    public SignInGroup getGroup();
    
    /**
     * Get SignInReward module.
     * It is used to indicate the reward form of SignInReward.
     * @return 
     */
    public SignInRewardModule getModule();
    
    /**
     * Get messages.
     * @return 
     */
    public List<String> getMessages();
    
    /**
     * Get commands.
     * @return 
     */
    public List<SignInRewardCommand> getCommands();
    
    /**
     * Get Reward items.
     * @param player Use for PlaceholderAPI request.
     * @return 
     */
    public List<ItemStack> getRewardItems(Player player);
    
    /**
     * Give reward.
     * @param playerData 
     */
    public void giveReward(Storage playerData);
}
