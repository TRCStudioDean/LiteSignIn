package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommandType;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInSpecialWeekReward
    extends SignInRewardColumn
{
    private final SignInGroup group;
    private final int week;
    
    public SignInSpecialWeekReward(SignInGroup group, int week) {
        this.group = group;
        this.week = week;
    }

    @Override
    public boolean overrideDefaultRewards() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Override-default-rewards")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Override-default-rewards");
        }
        return false;
    }
    
    public int getWeek() {
        return week;
    }
    
    @Override
    public SignInGroup getGroup() {
        return group;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.SPECIALWEEK;
    }

    @Override
    public List<String> getMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        List<SignInRewardCommand> list = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Commands")) {
            for (String commands : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Commands")) {
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
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Weeks." + week + ".Play-Sounds");
    }
}
