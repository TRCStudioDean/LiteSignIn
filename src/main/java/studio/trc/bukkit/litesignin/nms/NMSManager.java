package studio.trc.bukkit.litesignin.nms;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import studio.trc.bukkit.litesignin.util.AdventureUtils;

public class NMSManager
{
    public static Class<?> craftItemStack;
    public static Class<?> nbtTagCompound = null;
//    public static Class<?> gameProfileSerializer1 = null;
    public static Class<?> itemStack = null;
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
        if (!Bukkit.getBukkitVersion().startsWith("1.7") && !Bukkit.getBukkitVersion().startsWith("1.8") && !Bukkit.getBukkitVersion().startsWith("1.9") && !Bukkit.getBukkitVersion().startsWith("1.10")
            && !Bukkit.getBukkitVersion().startsWith("1.11") && !Bukkit.getBukkitVersion().startsWith("1.12") && !Bukkit.getBukkitVersion().startsWith("1.13") && !Bukkit.getBukkitVersion().startsWith("1.14")
            && !Bukkit.getBukkitVersion().startsWith("1.15") && !Bukkit.getBukkitVersion().startsWith("1.16")) {
            try {
                nbtTagCompound = Class.forName("net.minecraft.nbt.NBTTagCompound");
//                gameProfileSerializer = Class.forName("net.minecraft.nbt.GameProfileSerializer");
                itemStack = Class.forName("net.minecraft.world.item.ItemStack");
            } catch (ClassNotFoundException ex) {
                // 1.21.9+ No longer needed.
            }
        } else {
            try {
                nbtTagCompound = Class.forName("net.minecraft.server." + getPackageName() + ".NBTTagCompound");
//                gameProfileSerializer = Class.forName("net.minecraft.server." + getPackageName() + ".GameProfileSerializer");
                itemStack = Class.forName("net.minecraft.server." + getPackageName() + ".ItemStack"); 
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                nmsFound = false;
            }
        }
    }

    public static void setItemHover(ItemStack item, BaseComponent component) {
        if (nbtTagCompound == null) return;
        try {
            Item hoverItem = new Item(
                item.getType().getKey().toString(),
                item.getAmount(),
                ItemTag.ofNbt(item.getItemMeta() != null ? (String) ItemMeta.class.getMethod("getAsString").invoke(item.getItemMeta()) : "")
            );
            component.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM, hoverItem));
        } catch (Throwable t) {
            try {
                Object mcStack = craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item);
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
                    nbtTagCompound.getMethod("putString", String.class, String.class).invoke(NBTTagCompound, "id", item.getType().getKey().toString());
                    nbtTagCompound.getMethod("putByte", String.class, byte.class).invoke(NBTTagCompound, "Count", (byte) item.getAmount());
                }
                component.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(NBTTagCompound.toString()).create()));
            } catch (Throwable t1) {
                t1.printStackTrace();
            }
        }
    }

    public static Object setItemHover(ItemStack item, Object component) {
        return AdventureUtils.toComponent(component).hoverEvent(HoverEventSource.class.cast(item));
    }
    
    public static Object getAdventureJSONItemStack(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR)) {
            try {
                String translationKey = Material.class.getMethod("translationKey").invoke(item.getType()).toString();
                return setItemHover(item, Component.translatable(translationKey));
            } catch (Exception ex) {
                return setItemHover(item, AdventureUtils.serializeText(toDisplayName(item.getType().name())));
            }
        }
        return Component.text("");
    }
    
    public static TextComponent getBungeeJSONItemStack(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR)) {
            TextComponent component = new TextComponent(toDisplayName(item.getType().name()));
            setItemHover(item, component);
            return component;
        }
        return new TextComponent();
    }
    
    private static String toDisplayName(String text) {
        String[] words = text.split("_", -1);
        for (int i = 0;i < words.length;i++) {
            if (words[i].length() <= 1) continue;
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
        }
        return String.join(" ", words);
    }
}
