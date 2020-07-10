package studio.trc.bukkit.litesignin.nms;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.util.CustomItem;
import studio.trc.bukkit.litesignin.util.PluginControl;

/**
 * Used by /signin itemcollection.
 * Allows the player to display the item’s parameters while moving the mouse pointer over the text.
 * @author Dean
 */
public class JsonItemStack
{
    public static Class<?> craftItemStack;
    public static Class<?> nbtTagCompound;
    public static Class<?> itemStack;
    public static boolean nmsFound;
    
    public static void reloadNMS() {
        try {
            craftItemStack = Class.forName("org.bukkit.craftbukkit." + PluginControl.nmsVersion + ".inventory.CraftItemStack");
            nbtTagCompound = Class.forName("net.minecraft.server." + PluginControl.nmsVersion + ".NBTTagCompound");
            itemStack = Class.forName("net.minecraft.server." + PluginControl.nmsVersion + ".ItemStack");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JsonItemStack.class.getName()).log(Level.SEVERE, null, ex);
            nmsFound = false;
        }
    }
    
    public static BaseComponent[] getJsonItemStackArray(List<CustomItem> itemStackList) {
        List<BaseComponent> text = new ArrayList();
        for (int i = 0;i < itemStackList.size();i++) {
            if (itemStackList.get(i) != null && !itemStackList.get(i).getItemStack().getType().equals(Material.AIR)) {
                text.add(getJsonItemStack(itemStackList.get(i).getItemStack(), itemStackList.get(i).getName()));
                if (i < itemStackList.size() - 1) {
                    text.add(new TextComponent(", "));
                }
            }
        }
        return text.toArray(new BaseComponent[] {});
    }
    
    public static BaseComponent getJsonItemStack(ItemStack is) {
        String text;
        try {
            text = is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : (String) is.getClass().getMethod("getI18NDisplayName").invoke(is);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            text = is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : is.getType().toString().toLowerCase().replace("_", " ");
        }
        if (is != null && !is.getType().equals(Material.AIR)) {
            BaseComponent tc = new TextComponent(text);
            ComponentBuilder cb = new ComponentBuilder(getJsonAsNBTTagCompound(is));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cb.create()));
            return tc;
        }
        return null;
    }
    
    public static BaseComponent getJsonItemStack(ItemStack is, String text) {
        if (is != null && !is.getType().equals(Material.AIR)) {
            BaseComponent tc = new TextComponent(text);
            ComponentBuilder cb = new ComponentBuilder(getJsonAsNBTTagCompound(is));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cb.create()));
            return tc;
        }
        return null;
    }
    
    public static String getJsonAsNBTTagCompound(ItemStack is) {
        try {
            Object mcStack = craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, is);
            Object NBTTagCompound = nbtTagCompound.newInstance();
            itemStack.getDeclaredMethod("save", nbtTagCompound).invoke(mcStack, NBTTagCompound);
            return NBTTagCompound.toString();
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            Logger.getLogger(JsonItemStack.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
