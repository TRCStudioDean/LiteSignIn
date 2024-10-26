package studio.trc.bukkit.litesignin.util;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;

public class CustomItem
{
    @Getter
    private final ItemStack itemStack;
    @Getter
    private final String name;
    
    public CustomItem(ItemStack itemStack, String name) {
        this.itemStack = itemStack;
        this.name = name;
    }
    
    public void delete() {
        ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).set("Item-Collection." + name, null);
        ConfigurationUtil.saveConfig(ConfigurationType.CUSTOM_ITEMS);
    }
    
    public void give(Player player) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(itemStack);
        } else {
            player.getWorld().dropItem(player.getLocation(), itemStack);
        }
    }
    
    public static List<CustomItem> getItemStackCollection() {
        List<CustomItem> itemList = new ArrayList();
        for (String name : ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getConfigurationSection("Item-Collection").getKeys(false)) {
            ItemStack itemStack = ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getItemStack("Item-Collection." + name);
            if (itemStack != null) {
                itemList.add(new CustomItem(itemStack, name));
            }
        }
        return itemList;
    }
    
    public static CustomItem getCustomItem(String name) {
        ItemStack is = ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getItemStack("Item-Collection." + name);
        if (is == null) {
            return null;
        }
        return new CustomItem(is, name);
    }
    
    public static boolean addItemAsCollection(ItemStack is, String name) {
        for (CustomItem ci : getItemStackCollection()) {
            if (ci.getName().equals(name)) {
                return false;
            }
        }
        ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).set("Item-Collection." + name, is);
        ConfigurationUtil.saveConfig(ConfigurationType.CUSTOM_ITEMS);
        return true;
    }
    
    public static boolean deleteItemAsCollection(String name) {
        for (CustomItem ci : getItemStackCollection()) {
            if (ci.getName().equals(name)) {
                ci.delete();
                return true;
            }
        }
        return false;
    }
}
