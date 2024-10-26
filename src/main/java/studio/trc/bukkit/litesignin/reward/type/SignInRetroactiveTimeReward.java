package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.SignInRewardRetroactive;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInRetroactiveTimeReward
    extends SignInRewardRetroactive
{
    @Getter
    private final SignInGroup group;
    @Getter
    private final Map<SignInRewardModule, Boolean> collection;
    
    public SignInRetroactiveTimeReward(SignInGroup group) {
        this.group = group;
        collection = new HashMap<>();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules")) {
            collection.put(SignInRewardModule.SPECIAL_DATE, ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Dates"));
            collection.put(SignInRewardModule.SPECIAL_WEEK, ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Weeks"));
            collection.put(SignInRewardModule.STATISTICS_TIME, ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Statistics-Times"));
            collection.put(SignInRewardModule.STATISTICS_TIME_OF_MONTH, ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Statistics-Times-Of-Month"));
        }
    }

    @Override
    public boolean isDisable(SignInRewardModule module) {
        return collection.containsKey(module) ? collection.get(module) : false;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.RETROACTIVE_TIME;
    }

    @Override
    public List<String> getMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Play-Sounds");
    }
}
