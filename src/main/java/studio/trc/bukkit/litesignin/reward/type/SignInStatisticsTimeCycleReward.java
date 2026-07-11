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
import studio.trc.bukkit.litesignin.util.LiteSignInUtils;

public class SignInStatisticsTimeCycleReward 
    extends SignInRewardColumn
{
    @Getter
    private final SignInGroup group;
    @Getter
    private final int time;
    @Getter
    private final String setting;
    
    public SignInStatisticsTimeCycleReward(SignInGroup group, int time) {
        this.group = group;
        this.time = time;
        setting = getSettingPath();
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.STATISTICS_TIMES_CYCLE;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Override-default-rewards")) {
            return config.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Broadcast-Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Broadcast-Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle." + setting + ".Play-Sounds");
    }
    
    private String getSettingPath() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle")) {
            for (String number : config.getConfigurationSection("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Statistics-Times-Cycle").getKeys(false)) {
                if (LiteSignInUtils.isInteger(number) && time % Integer.valueOf(number) == 0) {
                    return number;
                }
            }
        }
        return "0";
    }
}
