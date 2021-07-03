package studio.trc.bukkit.litesignin.reward.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

import org.bukkit.Material;
import org.bukkit.Sound;
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
import studio.trc.bukkit.litesignin.reward.SignInRewardColumn;
import studio.trc.bukkit.litesignin.reward.SignInRewardModule;
import studio.trc.bukkit.litesignin.reward.SignInRewardTask;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommandType;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;

public class SignInSpecialRankingReward
    implements SignInRewardColumn
{
    private final SignInGroup group;
    private final int ranking;
    
    public SignInSpecialRankingReward(SignInGroup group, int ranking) {
        this.group = group;
        this.ranking = ranking;
    }
    
    public int getRanking() {
        return ranking;
    }
    
    @Override
    public SignInGroup getGroup() {
        return group;
    }

    @Override
    public SignInRewardModule getModule() {
        return SignInRewardModule.SPECIALRANKING;
    }
    
    @Override
    public boolean overrideDefaultRewards() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Override-default-rewards")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getBoolean("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Override-default-rewards");
        }
        return false;
    }

    @Override
    public List<String> getMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInRewardCommand> getCommands() {
        List<SignInRewardCommand> list = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Commands")) {
            for (String commands : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Commands")) {
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
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Reward-Items")) {
            for (String item : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Reward-Items")) {
                String[] itemdata = item.split(":");
                try {
                    ItemStack is = new ItemStack(Material.valueOf(itemdata[0].toUpperCase()));
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
                } catch (IllegalArgumentException e) {
                    if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).contains("Manual-Settings." + itemdata[0] + ".Item")) {
                        ItemStack is;
                        try {
                            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).contains("Manual-Settings." + itemdata[0] + ".Data")) {
                                is = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemdata[0] + ".Item").toUpperCase()), 1, (short) ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getInt("Reward-Items." + itemdata[0] + ".Data"));
                            } else {
                                is = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemdata[0] + ".Item").toUpperCase()), 1);
                            }
                        } catch (IllegalArgumentException ex2) {
                            continue;
                        }
                        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemdata[0] + ".Head-Owner") != null) {
                            PluginControl.setHead(is, MessageUtil.toPlaceholderAPIResult(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString("Manual-Settings." + itemdata[0] + ".Head-Owner"), player).replace("{player}", player.getName()));
                        }
                        ItemMeta im = is.getItemMeta();
                        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).contains("Manual-Settings." + itemdata[0] + ".Lore")) {
                            List<String> lore = new ArrayList();
                            for (String lores : ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getStringList("Manual-Settings." + itemdata[0] + ".Lore")) {
                                lore.add(MessageUtil.toPlaceholderAPIResult(lores.replace("&", "§"), player));
                            }
                            im.setLore(lore);
                        }
                        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).contains("Manual-Settings." + itemdata[0] + ".Enchantment")) {
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
                        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemdata[0] + ".Hide-Enchants") != null) PluginControl.hideEnchants(im);
                        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).contains("Manual-Settings." + itemdata[0] + ".Display-Name")) im.setDisplayName(MessageUtil.toPlaceholderAPIResult(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemdata[0] + ".Display-Name").replace("&", "§"), player));
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
                    } else if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).contains("Item-Collection." + itemdata[0])) {
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
    public List<String> getBroadcastMessages() {
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Broadcast-Messages")) {
            return ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Broadcast-Messages");
        }
        return new ArrayList();
    }

    @Override
    public List<SignInSound> getSounds() {
        List<SignInSound> sounds = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).contains("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Play-Sounds")) {
            for (String value : ConfigurationUtil.getConfig(ConfigurationType.REWARDSETTINGS).getStringList("Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Play-Sounds")) {
                String[] args = value.split("-");
                try {
                    Sound sound = Sound.valueOf(args[0].toUpperCase());
                    float volume = Float.valueOf(args[1]);
                    float pitch = Float.valueOf(args[2]);
                    boolean broadcast = Boolean.valueOf(args[3]);
                    sounds.add(new SignInSound(sound, volume, pitch, broadcast));
                } catch (IllegalArgumentException ex) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{sound}", args[0]);
                    SignInPluginProperties.sendOperationMessage("InvalidSound", placeholders);
                } catch (Exception ex) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{path}", "Reward-Settings.Permission-Groups." + group.getGroupName() + ".Special-Ranking." + ranking + ".Play-Sounds." + value);
                    SignInPluginProperties.sendOperationMessage("InvalidSoundSetting", placeholders);
                }
            } 
        }
        return sounds;
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
                        case BROADCAST_MESSAGES_SENDING: {
                            getBroadcastMessages().stream().forEach(messages -> {
                                Bukkit.getOnlinePlayers().stream().forEach(players -> {
                                    players.sendMessage(MessageUtil.toPlaceholderAPIResult(messages.replace("{continuous}", String.valueOf(playerData.getContinuousSignIn())).replace("{queue}", queue).replace("{total-number}", String.valueOf(playerData.getCumulativeNumber())).replace("{player}", player.getName()).replace("{prefix}", ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Prefix")).replace("&", "§"), player));
                                });
                            });
                            break;
                        }
                        case PLAYSOUNDS: {
                            getSounds().stream().forEach(sounds -> {sounds.playSound(player);});
                            break;
                        }
                    }
                } catch (Exception ex) {}
            }
        }
    }
}
