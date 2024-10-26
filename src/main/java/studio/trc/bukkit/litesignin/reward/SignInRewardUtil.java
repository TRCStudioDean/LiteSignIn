package studio.trc.bukkit.litesignin.reward;

import java.util.ArrayList;
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
import studio.trc.bukkit.litesignin.config.PreparedConfiguration;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommandType;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInPluginProperties;

public abstract class SignInRewardUtil
    implements SignInReward
{
    @Override
    public void giveReward(Storage playerData) {
        String queue = String.valueOf(SignInQueue.getInstance().getRank(playerData.getUserUUID()));
        if (playerData.getPlayer() != null) {
            Player player = playerData.getPlayer();
            for (String taskName : ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getStringList("Reward-Task-Sequence")) {
                try {
                    switch (SignInRewardTask.valueOf(taskName.toUpperCase())) {
                        case ITEMS_REWARD: {
                            getRewardItems(player).stream().forEach(item -> {
                                if (player.getInventory().firstEmpty() != -1) {
                                    player.getInventory().addItem(item);
                                } else {
                                    player.getWorld().dropItem(player.getLocation(), item);
                                }
                            });
                            break;
                        }
                        case COMMANDS_EXECUTION: {
                            getCommands().stream().forEach(commands -> commands.runWithThePlayer(player));
                            break;
                        }
                        case MESSAGES_SENDING: {
                            getMessages().stream().forEach(messages -> {
                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                placeholders.put("{continuous}", String.valueOf(playerData.getContinuousSignIn()));
                                placeholders.put("{queue}", queue);
                                placeholders.put("{total-number}", String.valueOf(playerData.getCumulativeNumber()));
                                placeholders.put("{player}", player.getName());
                                player.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, messages, placeholders)));
                            });
                            break;
                        }
                        case BROADCAST_MESSAGES_SENDING: {
                            getBroadcastMessages().stream().forEach(messages -> {
                                Bukkit.getOnlinePlayers().stream().forEach(players -> {
                                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                    placeholders.put("{continuous}", String.valueOf(playerData.getContinuousSignIn()));
                                    placeholders.put("{queue}", queue);
                                    placeholders.put("{total-number}", String.valueOf(playerData.getCumulativeNumber()));
                                    placeholders.put("{player}", player.getName());
                                    players.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, messages, placeholders)));
                                });
                            });
                            break;
                        }
                        case PLAYSOUNDS: {
                            getSounds().stream().forEach(sounds -> sounds.playSound(player));
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public List<ItemStack> getRewardItems(Player player, String configPath) {
        List<ItemStack> list = new ArrayList();
        PreparedConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains(configPath)) {
            config.getStringList(configPath).stream().map(itemData -> getItemFromItemData(player, itemData)).filter(item -> item != null).forEach(list::add);
        }
        return list;
    }
    
    public List<SignInRewardCommand> getCommands(String configPath) {
        List<SignInRewardCommand> list = new ArrayList();
        if (ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).contains(configPath)) {
            ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getStringList(configPath).stream().forEach(commands -> {
                if (commands.toLowerCase().startsWith("server:")) {
                    list.add(new SignInRewardCommand(SignInRewardCommandType.SERVER, commands.substring(7)));
                } else if (commands.toLowerCase().startsWith("op:")) {
                    list.add(new SignInRewardCommand(SignInRewardCommandType.OP, commands.substring(3)));
                } else {
                    list.add(new SignInRewardCommand(SignInRewardCommandType.PLAYER, commands));
                }
            });
        }
        return list;
    }
    
    public ItemStack getItemFromItemData(Player player, String item) {
        String[] itemdata = item.split(":");
        try {
            ItemStack is = new ItemStack(Material.valueOf(itemdata[0].toUpperCase()));
            try {
                if (itemdata[1].contains("-")) {
                    is.setAmount(PluginControl.getRandom(itemdata[1]));
                } else {
                    is.setAmount(Integer.valueOf(itemdata[1]));
                }
            } catch (NumberFormatException ex) {}
            return is;
        } catch (IllegalArgumentException e) {
            PreparedConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS);
            if (config.contains("Manual-Settings." + itemdata[0] + ".Item")) {
                ItemStack is;
                try {
                    if (config.contains("Manual-Settings." + itemdata[0] + ".Data")) {
                        is = new ItemStack(Material.valueOf(config.getString("Manual-Settings." + itemdata[0] + ".Item").toUpperCase()), 1, (short) ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS).getInt("Reward-Items." + itemdata[0] + ".Data"));
                    } else {
                        is = new ItemStack(Material.valueOf(config.getString("Manual-Settings." + itemdata[0] + ".Item").toUpperCase()), 1);
                    }
                } catch (IllegalArgumentException ex2) {
                    return null;
                }
                if (config.get("Manual-Settings." + itemdata[0] + ".Head-Owner") != null) {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{player}", player.getName());
                    PluginControl.setHead(is, MessageUtil.replacePlaceholders(player, ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getString("Manual-Settings." + itemdata[0] + ".Head-Owner"), placeholders));
                }
                ItemMeta im = is.getItemMeta();
                if (config.contains("Manual-Settings." + itemdata[0] + ".Lore")) {
                    List<String> lore = new ArrayList();
                    config.getStringList("Manual-Settings." + itemdata[0] + ".Lore").stream().forEach(lores -> lore.add(MessageUtil.toColor(MessageUtil.toPlaceholderAPIResult(lores, player))));
                    im.setLore(lore);
                }
                if (config.contains("Manual-Settings." + itemdata[0] + ".Enchantment")) {
                    setEnchantments("Manual-Settings." + itemdata[0] + ".Enchantment", im);
                }
                if (config.get("Manual-Settings." + itemdata[0] + ".Hide-Enchants") != null) PluginControl.hideEnchants(im);
                if (config.contains("Manual-Settings." + itemdata[0] + ".Display-Name")) im.setDisplayName(MessageUtil.toColor(MessageUtil.toPlaceholderAPIResult(config.getString("Manual-Settings." + itemdata[0] + ".Display-Name"), player)));
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
                return is;
            } else if (config.contains("Item-Collection." + itemdata[0])) {
                ItemStack is = config.getItemStack("Item-Collection." + itemdata[0]);
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
                    return is;
                }
            }
        }
        return null;
    }
    
    public List<SignInSound> getSounds(String configPath) {
        List<SignInSound> sounds = new ArrayList();
        PreparedConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.REWARD_SETTINGS);
        if (config.contains(configPath)) {
            config.getStringList(configPath).stream().forEach((value) -> {
                String[] args = value.split("-");
                try {
                    Sound sound = Sound.valueOf(args[0].toUpperCase());
                    float volume = Float.valueOf(args[1]);
                    float pitch = Float.valueOf(args[2]);
                    boolean broadcast = Boolean.valueOf(args[3]);
                    sounds.add(new SignInSound(sound, volume, pitch, broadcast));
                } catch (IllegalArgumentException ex) {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{sound}", args[0]);
                    placeholders.put("{path}", configPath + "." + value);
                    SignInPluginProperties.sendOperationMessage("InvalidSound", placeholders);
                } catch (Exception ex) {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{path}", configPath + "." + value);
                    SignInPluginProperties.sendOperationMessage("InvalidSoundSetting", placeholders);
                }
            }); 
        }
        return sounds;
    }
    
    private void setEnchantments(String configPath, ItemMeta im) {
        for (String name : ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getStringList(configPath)) {
            try {
                String[] data = name.split(":");
                boolean invalid = true;
                for (Enchantment enchant : Enchantment.values()) {
                    String enchantName;
                    try {
                        enchantName = enchant.getKey().getKey();
                    } catch (Throwable ex) {
                        enchantName = enchant.getName();
                    }
                    if (enchantName.equalsIgnoreCase(data[0])) {
                        try {
                            im.addEnchant(enchant, Integer.valueOf(data[1]), true);
                            invalid = false;
                            break;
                        } catch (Exception ex) {
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                            placeholders.put("{path}", configPath + "." + name);
                            SignInPluginProperties.sendOperationMessage("InvalidEnchantmentSetting", placeholders);
                        }
                    }
                }
                if (invalid) {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{enchantment}", data[0]);
                    placeholders.put("{path}", configPath + "." + name);
                    SignInPluginProperties.sendOperationMessage("InvalidEnchantment", placeholders);
                }
            } catch (Exception ex) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{path}", configPath + "." + name);
                SignInPluginProperties.sendOperationMessage("InvalidEnchantmentSetting", placeholders);
            }
        }
    }
}
