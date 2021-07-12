package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.SignInRewardUtil;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommandType;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

public class SignInNormalReward
    extends SignInRewardUtil
{
    private final SignInGroup group;
    
    public SignInNormalReward(SignInGroup group) {
        this.group = group;
    }
    
    @Override
    public SignInGroup getGroup() {
        return group;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.NORMAL;
    }
    
    @Override
    public List<String> getMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Normal-Time.Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Normal-Time.Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        List<SignInRewardCommand> list = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Normal-Time.Commands")) {
            for (String commands : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Normal-Time.Commands")) {
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
        return super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Normal-Time.Reward-Items");
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Normal-Time.Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Normal-Time.Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Normal-Time.Play-Sounds");
    }
}
