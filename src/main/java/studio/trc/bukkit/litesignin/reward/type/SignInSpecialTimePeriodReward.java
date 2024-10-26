package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;
import studio.trc.bukkit.litesignin.reward.util.SignInTimePeriod;
import studio.trc.bukkit.litesignin.util.SignInDate;

public class SignInSpecialTimePeriodReward
    extends SignInRewardColumn
{
    @Getter
    private final SignInGroup group;
    @Getter
    private final SignInDate now;
    @Getter
    private final String setting;
    
    public SignInSpecialTimePeriodReward(SignInGroup group, SignInDate now) {
        this.group = group;
        this.now = now;
        setting = SignInTimePeriod.getSetting(group, now);
    }
    
    public boolean isAvailable() {
        return setting != null;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.SPECIAL_TIME_PERIOD;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        if (!isAvailable()) return false;
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Override-default-rewards")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        if (!isAvailable()) return new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        if (!isAvailable()) return new ArrayList();
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        return isAvailable() ? super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Reward-Items") : new ArrayList();
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (!isAvailable()) return new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        if (!isAvailable()) return new ArrayList();
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Play-Sounds");
    }
}
