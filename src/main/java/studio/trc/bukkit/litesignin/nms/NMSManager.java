package studio.trc.bukkit.litesignin.nms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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

public class NMSManager
{
    public static Class<?> craftItemStack;
    public static Class<?> nbtTagCompound;
    public static Class<?> gameProfileSerializer;
    public static Class<?> itemStack;
    public static boolean nmsFound;
    
    public static String getPackageName() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
    
    public static void reloadNMS() {
        
        //craftbukkit
        try {
            craftItemStack = Class.forName("org.bukkit.craftbukkit." + getPackageName() + ".inventory.CraftItemStack");
        } catch (ArrayIndexOutOfBoundsException ex) {
            try {
                craftItemStack = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            } catch (ClassNotFoundException ex1) {
                nmsFound = false;
            }
        } catch (ClassNotFoundException ex) {
            nmsFound = false;
        }
        
        //net.minecraft.server
        try {
            if (Bukkit.getBukkitVersion().startsWith("1.17") || Bukkit.getBukkitVersion().startsWith("1.18") || Bukkit.getBukkitVersion().startsWith("1.19") || Bukkit.getBukkitVersion().startsWith("1.20") || Bukkit.getBukkitVersion().startsWith("1.21")) {
                nbtTagCompound = Class.forName("net.minecraft.nbt.NBTTagCompound");
                gameProfileSerializer = Class.forName("net.minecraft.nbt.GameProfileSerializer");
                itemStack = Class.forName("net.minecraft.world.item.ItemStack");
            } else {
                nbtTagCompound = Class.forName("net.minecraft.server." + getPackageName() + ".NBTTagCompound");
                gameProfileSerializer = Class.forName("net.minecraft.server." + getPackageName() + ".GameProfileSerializer");
                itemStack = Class.forName("net.minecraft.server." + getPackageName() + ".ItemStack"); 
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NMSManager.class.getName()).log(Level.SEVERE, null, ex);
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
            Method saveMethod = Arrays.stream(itemStack.getDeclaredMethods()).filter(method -> method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(nbtTagCompound) && method.getReturnType().equals(nbtTagCompound)).findFirst().orElse(null);
            if (saveMethod != null) {
                if (saveMethod.isAccessible()) {
                    saveMethod.invoke(mcStack, NBTTagCompound);
                } else {
                    saveMethod.setAccessible(true);
                    saveMethod.invoke(mcStack, NBTTagCompound);
                    saveMethod.setAccessible(false);
                }
            } else {
                nbtTagCompound.getMethod("putString", String.class, String.class).invoke(NBTTagCompound, "id", is.getType().getKey().getNamespace() + ":" + is.getType().getKey().getKey());
                nbtTagCompound.getMethod("putByte", String.class, byte.class).invoke(NBTTagCompound, "Count", (byte) is.getAmount());
            }
            return NBTTagCompound.toString();
        } catch (Exception ex) {
            return is.getType().name();
        }
    }
}
