package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.trc.bukkit.litesignin.config.Configuration;

import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInStatisticsTimeOfMonthReward 
    extends SignInRewardColumn
{
    @Getter
    private final SignInGroup group;
    @Getter
    private final int time;
    @Getter
    private final int month;
    private final String settings;
    
    public SignInStatisticsTimeOfMonthReward(SignInGroup group, int month, int time) {
        this.group = group;
        this.month = month;
        this.time = time;
        settings = getSettings();
    }
    
    public boolean isAvailable() {
        return settings != null;
    }
    
    public String getSettings() {
        Configuration config = ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + time + ".Valid-Months")) {
            if (config.getIntegerList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + time + ".Valid-Months").contains(month)) {
                return String.valueOf(time);
            }
        } else {
            return String.valueOf(time);
        }
        return null;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.STATISTICS_TIME_OF_MONTH;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        if (!isAvailable()) return false;
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Override-default-rewards")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        if (!isAvailable()) return new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        if (!isAvailable()) return new ArrayList();
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        if (!isAvailable()) return new ArrayList();
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (!isAvailable()) return new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        if (!isAvailable()) return new ArrayList();
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Of-Month." + settings + ".Play-Sounds");
    }
}
