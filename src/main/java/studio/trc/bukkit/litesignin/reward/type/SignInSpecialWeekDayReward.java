package studio.trc.bukkit.litesignin.reward.type;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInSpecialWeekDayReward
    extends SignInRewardColumn
{
    @Getter
    private final SignInGroup group;
    @Getter
    private final int week;
    
    public SignInSpecialWeekDayReward(SignInGroup group, int week) {
        this.group = group;
        this.week = week;
    }

    @Override
    public boolean overrideDefaultRewards() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Override-default-rewards")) {
            return config.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.SPECIAL_WEEK_DAYS;
    }

    @Override
    public List<String> getMessages() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) { 
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Broadcast-Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Broadcast-Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Week-Days." + week + ".Play-Sounds");
    }
}
