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
        Map<SignInRewardModule, Boolean> map = new HashMap();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules")) {
            map.put(SignInRewardModule.SPECIAL_DATE, ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Dates"));
            map.put(SignInRewardModule.SPECIAL_WEEK, ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Weeks"));
            map.put(SignInRewardModule.STATISTICS_TIME, ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Statistics-Times"));
            map.put(SignInRewardModule.STATISTICS_TIME_OF_MONTH, ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Statistics-Times-Of-Month"));
        }
        collection = map;
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
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Messages");
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
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Play-Sounds");
    }
}
