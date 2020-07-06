package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.MessageUtil;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.SignInRewardRetroactive;
import studio.trc.bukkit.litesignin.reward.SignInRewardTask;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommandType;
import studio.trc.bukkit.litesignin.util.PluginControl;

public class SignInRetroactiveTimeReward
    implements SignInRewardRetroactive
{
    private final SignInGroup group;
    private final Map<SignInRewardModule, Boolean> collection;
    
    public SignInRetroactiveTimeReward(SignInGroup group) {
        this.group = group;
        Map<SignInRewardModule, Boolean> map = new HashMap();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).get("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules") != null) {
            map.put(SignInRewardModule.SPECIALDATE, ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Dates"));
            map.put(SignInRewardModule.SPECIALWEEK, ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Weeks"));
            map.put(SignInRewardModule.SPECIALTIME, ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Special-Times"));
            map.put(SignInRewardModule.STATISTICSTIME, ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Disabled-Modules.Statistics-Times"));
        }
        collection = map;
    }
    
    @Override
    public SignInGroup getGroup() {
        return group;
    }

    @Override
    public boolean isDisable(SignInRewardModule module) {
        return collection.containsKey(module) ? collection.get(module) : false;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.RETROACTIVETIME;
    }

    @Override
    public List<String> getMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).get("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Messages") != null) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        List<SignInRewardCommand> list = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).get("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Commands") != null) {
            for (String commands : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Commands")) {
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
        List<ItemStack> list = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).get("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Reward-Items") != null) {
            for (String item : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Retroactive-Time.Reward-Items")) {
                String[] itemdata = item.split(":");
                try {
                    ItemStack is = new ItemStack(Material.getMaterial(itemdata[0].toUpperCase()));
                    try {
                        if (itemdata[1].contains("-")) {
                            is.setAmount(PluginControl.getRandom(itemdata[1]));
                        } else {
                            is.setAmount(Integer.valueOf(itemdata[1]));
                        }
                    } catch (NumberFormatException ex) {}
                    list.add(is);
                } catch (IllegalArgumentException e) {
                    if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemdata[0] + ".Item") != null) {
                        ItemStack is;
                        try {
                            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemdata[0] + ".Data") != null) {
                                is = new ItemStack(Material.getMaterial(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemdata[0] + ".Item").toUpperCase()), 1, (short) ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getInt("Reward-Items." + itemdata[0] + ".Data"));
                            } else {
                                is = new ItemStack(Material.getMaterial(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemdata[0] + ".Item").toUpperCase()), 1);
                            }
                        } catch (IllegalArgumentException ex2) {
                            continue;
                        }
                        ItemMeta im = is.getItemMeta();
                        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemdata[0] + ".Lore") != null) {
                            List<String> lore = new ArrayList();
                            for (String lores : ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getStringList("Manual-Settings." + itemdata[0] + ".Lore")) {
                                lore.add(MessageUtil.toPlaceholderAPIResult(lores.replace("&", "§"), player));
                            }
                            im.setLore(lore);
                        }
                        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemdata[0] + ".Enchantment") != null) {
                            for (String name : ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getStringList("Manual-Settings." + itemdata[0] + ".Enchantment")) {
                                String[] data = name.split(":");
                                for (Enchantment enchant : Enchantment.values()) {
                                    if (enchant.getName().equalsIgnoreCase(data[0])) {
                                        try {
                                            im.addEnchant(enchant, Integer.valueOf(data[1]), true);
                                        } catch (NumberFormatException ex) {}
                                    }
                                }
                            }
                        }
                        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemdata[0] + ".Display-Name") != null) im.setDisplayName(MessageUtil.toPlaceholderAPIResult(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemdata[0] + ".Display-Name").replace("&", "§"), player));
                        is.setItemMeta(im);
                        try {
                            if (itemdata[1].contains("-")) {
                                is.setAmount(PluginControl.getRandom(itemdata[1]));
                            } else {
                                is.setAmount(Integer.valueOf(itemdata[1]));
                            }
                        } catch (NumberFormatException ex) {
                            is.setAmount(1);
                        }
                        list.add(is);
                    } else if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Item-Collection." + itemdata[0]) != null) {
                        ItemStack is = ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getItemStack("Item-Collection." + itemdata[0]);
                        if (is != null) {
                            try {
                                if (itemdata[1].contains("-")) {
                                    is.setAmount(PluginControl.getRandom(itemdata[1]));
                                } else {
                                    is.setAmount(Integer.valueOf(itemdata[1]));
                                }
                            } catch (NumberFormatException ex) {
                                is.setAmount(1);
                            }
                            list.add(is);
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void giveReward(Storage playerData) {
        String queue = String.valueOf(SignInQueue.getInstance().getRank(playerData.getUserUUID()));
        if (playerData.getPlayer() != null) {
            Player player = playerData.getPlayer();
            for (String taskName : ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getStringList("Reward-Task-Sequence")) {
                try {
                    switch (SignInRewardTask.valueOf(taskName.toUpperCase())) {
                        case ITEMS_REWARD: {
                            player.getInventory().addItem(getRewardItems(player).toArray(new ItemStack[0]));
                            break;
                        }
                        case COMMANDS_EXECUTION: {
                            getCommands().stream().forEach(commands -> {commands.runWithThePlayer(player);});
                            break;
                        }
                        case MESSAGES_SENDING: {
                            getMessages().stream().forEach(messages -> {player.sendMessage(MessageUtil.toPlaceholderAPIResult(messages.replace("{continuous}", String.valueOf(playerData.getContinuousSignIn())).replace("{queue}", queue).replace("{total-number}", String.valueOf(playerData.getCumulativeNumber())).replace("{player}", player.getName()).replace("{prefix}", ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Prefix")).replace("&", "§"), player));});
                            break;
                        }
                    }
                } catch (Exception ex) {}
            }
        }
    }
}
