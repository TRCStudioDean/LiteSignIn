package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommandType;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;
import studio.trc.bukkit.litesignin.reward.util.SignInTimePeriod;
import studio.trc.bukkit.litesignin.util.SignInDate;

public class SignInSpecialTimePeriodReward
    extends SignInRewardColumn
{
    private final SignInGroup group;
    private final SignInDate now;
    private final String setting;
    
    public SignInSpecialTimePeriodReward(SignInGroup group, SignInDate now) {
        this.group = group;
        this.now = now;
        setting = SignInTimePeriod.getSetting(group, now);
    }
    
    public SignInDate getTime() {
        return now;
    }
    
    public boolean isAvailable() {
        return setting != null;
    }
    
    @Override
    public SignInGroup getGroup() {
        return group;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.SPECIALTIMEPERIOD;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        if (!isAvailable()) return false;
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Override-default-rewards")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        if (!isAvailable()) return new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        if (!isAvailable()) return new ArrayList();
        List<SignInRewardCommand> list = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Commands")) {
            for (String commands : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Commands")) {
                if (commands.toLowerCase().startsWith("server:")) {
                    list.add(new SignInRewardCommand(SignInRewardCommandType.SERVER, commands.substring(7)));
                } else if (commands.toLowerCase().startsWith("op:")) {
                    list.add(new SignInRewardCommand(SignInRewardCommandType.OP, commands.substring(3)));
                } else {
                    list.add(new SignInRewardCommand(SignInRewardCommandType.PLAYER, commands));
                }
            }
        }
        return list;
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        return isAvailable() ? super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Reward-Items") : new ArrayList();
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Time-periods." + setting + ".Play-Sounds");
    }
}
