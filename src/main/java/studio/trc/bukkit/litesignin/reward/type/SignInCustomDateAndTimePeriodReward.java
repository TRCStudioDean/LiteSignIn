package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;
import studio.trc.bukkit.litesignin.util.SignInDate;

public class SignInCustomDateAndTimePeriodReward
    extends SignInRewardColumn
{
    @Getter
    private final SignInDate now;
    @Getter
    private final SignInGroup group;
    @Getter
    private final List<String> settings = new ArrayList<>();
    
    private String currentSetting;
    private int currentIndex = 0;

    public SignInCustomDateAndTimePeriodReward(SignInGroup group, SignInDate now) {
        this.group = group;
        this.now = now;
        YamlConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getConfig();
        ConfigurationSection customs = config.getConfigurationSection("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods");
        if (customs != null) {
            customs.getKeys(false).stream().forEach(custom -> {
                if (custom.contains("~")) {
                    SignInDate[] range = SignInDate.getTimeRange(custom);
                    if (range != null && now.compareTo(range[0]) >= 0 && now.compareTo(range[1]) <= 0) {
                        settings.add(custom);
                    }
                } else {
                    SignInDate specfic = SignInDate.getInstanceByFormat(custom);
                    if (specfic != null) {
                        if (!specfic.hasTimePeriod()) {
                            if (specfic.getYear() == now.getYear() && specfic.getMonth() == now.getMonth() && specfic.getDay() == now.getDay()) {
                                settings.add(custom);
                            }
                        } else {
                            if (specfic.getMillisecond() / 1000 == now.getMillisecond() / 1000) {
                                settings.add(custom);
                            }
                        }
                    }
                }
            });
        }
    }
    
    public boolean isAvailable() {
        return !settings.isEmpty();
    }
    
    private void next() {
        currentSetting = settings.get(currentIndex);
        currentIndex++;
    }
    
    private boolean hasNext() {
        return settings.size() != currentIndex;
    }

    @Override
    public void giveReward(Storage playerData) {
        while (hasNext()) {
            next();
            super.giveReward(playerData);
        }
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.CUSTOM_DATE_AND_TIME_PERIOD;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        if (!isAvailable()) return false;
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Override-default-rewards")) {
            return config.getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        return super.getCommands("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Commands");
    }

    @Override
    public List<ItemStack> getRewardItems(Player player) {
        return isAvailable() ? super.getRewardItems(player, "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Reward-Items") : Collections.EMPTY_LIST;
    }

    @Override
    public List<String> getBroadcastMessages() {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Broadcast-Messages")) {
            return config.getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Broadcast-Messages");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SignInSound> getSounds() {
        if (!isAvailable()) return Collections.EMPTY_LIST;
        return super.getSounds("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Custom-Date-And-Time-Periods." + currentSetting + ".Play-Sounds");
    }
}
