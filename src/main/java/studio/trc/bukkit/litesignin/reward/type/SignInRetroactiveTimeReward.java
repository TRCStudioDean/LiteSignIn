package studio.trc.bukkit.litesignin.reward.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
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
        YamlConfiguration settings = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getConfig();
        if (settings.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules")) {
            collection.put(SignInRewardModule.SPECIAL_DATES, settings.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Dates"));
            collection.put(SignInRewardModule.SPECIAL_WEEK_DAYS, settings.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Week-Days"));
            collection.put(SignInRewardModule.STATISTICS_TIMES, settings.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Statistics-Times"));
            collection.put(SignInRewardModule.STATISTICS_TIMES_OF_MONTH, settings.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Statistics-Times-Of-Month"));
            collection.put(SignInRewardModule.STATISTICS_TIMES_CYCLE, settings.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Statistics-Times-Of-Cycle"));
            collection.put(SignInRewardModule.CUSTOM_DATE_AND_TIME_PERIOD, settings.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Custom-Date-And-Time-Periods"));
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
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Messages");
        }
        return Collections.EMPTY_LIST;
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
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Broadcast-Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Broadcast-Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Play-Sounds");
    }
}
