package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommandType;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInSpecialDateReward
    extends SignInRewardColumn
{
    private final SignInGroup group;
    private final SignInDate date;
    
    public SignInSpecialDateReward(SignInGroup group, SignInDate date) {
        this.group = group;
        this.date = date;
    }
    
    @Override
    public SignInGroup getGroup() {
        return group;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.SPECIALDATE;
    }
    
    public SignInDate getDate() {
        return date;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Override-default-rewards")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        List<SignInRewardCommand> list = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Commands")) {
            for (String commands : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Commands")) {
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
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Play-Sounds");
    }
}
