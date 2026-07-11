package studio.trc.bukkit.litesignin.reward.type;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInSpecialTimeOfMonthReward 
    extends SignInRewardColumn
{
    @Getter
    private final SignInGroup group;
    @Getter
    private final int month;
    @Getter
    private final int time;
    private final String settings;
    
    public SignInSpecialTimeOfMonthReward (SignInGroup group, int month, int time) {
        this.group = group;
        this.month = month;
        this.time = time;
        settings = getSettings();
    }
    
    public boolean isAvailable() {
        return settings != null;
    }
    
    public String getSettings() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + time + ".Valid-Months")) {
            if (config.getIntegerList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + time + ".Valid-Months").contains(month)) {
                return String.valueOf(time);
            }
        } else {
            return String.valueOf(time);
        }
        return null;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.SPECIAL_TIMES_OF_MONTH;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        if (!isAvailable()) return false;
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Override-default-rewards")) {
            return config.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Override-default-rewards");
        }
        return false;
    }
    
    @Override
    public List<String> getMessages() {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Broadcast-Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Broadcast-Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInSound> getSounds() {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Times-Of-Month." + settings + ".Play-Sounds");
    }
}
