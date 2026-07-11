package studio.trc.bukkit.litesignin.reward.type;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInStatisticsTimeReward
    extends SignInRewardColumn
{
    @Getter
    private final SignInGroup group;
    @Getter
    private final int time;
    
    public SignInStatisticsTimeReward(SignInGroup group, int time) {
        this.group = group;
        this.time = time;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.STATISTICS_TIMES;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Override-default-rewards")) {
            return config.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Broadcast-Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Broadcast-Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times." + time + ".Play-Sounds");
    }
}
