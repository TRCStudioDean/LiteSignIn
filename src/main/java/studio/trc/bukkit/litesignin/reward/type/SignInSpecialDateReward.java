package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInSpecialDateReward
    extends SignInRewardColumn
{
    @Getter
    private final SignInGroup group;
    @Getter
    private final SignInDate date;
    
    public SignInSpecialDateReward(SignInGroup group, SignInDate date) {
        this.group = group;
        this.date = date;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.SPECIAL_DATE;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Override-default-rewards")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Dates." + date.getMonthAsString() + "-" + date.getDayAsString() + ".Play-Sounds");
    }
}
