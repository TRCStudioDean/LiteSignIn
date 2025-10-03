package studio.trc.bukkit.litesignin.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullManager
{
    @Getter
    private static final Map<UUID, String> base64Meta = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
    
    public static void refreshTexture(UUID uuid, String name) {
        if (base64Meta.containsKey(uuid)) return;
        if (UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()).equals(uuid)) {
            return;
        }
        StringBuilder source = new StringBuilder();
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString());
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), LiteSignInProperties.getMessage("Charset")))) {
                while ((line = reader.readLine()) != null) {
                    source.append(line);
                    source.append('\n');
                }
            }
            JsonObject json = gson.fromJson(source.toString(), JsonObject.class);
            base64Meta.put(uuid, json.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString());
        } catch (Exception ex) {}
    }
    
    public static void refreshTextureByDefaultMethod(UUID uuid, String name) {
        if (base64Meta.containsKey(uuid)) return;
        try {
            ItemStack head = getDefaultHead();
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            if (!Bukkit.getBukkitVersion().startsWith("1.8") && !Bukkit.getBukkitVersion().startsWith("1.9") && !Bukkit.getBukkitVersion().startsWith("1.10") && !Bukkit.getBukkitVersion().startsWith("1.11") && !Bukkit.getBukkitVersion().startsWith("1.12")) {
                sm.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            } else {
                sm.setOwner(name);
            }
            head.setItemMeta(sm);
            String texture = getHeadTexturesFromHead(head);
            if (texture != null) {
                base64Meta.put(uuid, texture);
            } else {
                refreshTexture(uuid, name);
            }
        } catch (Exception ex) {}
    }
    
    private static ItemStack getDefaultHead() {
        if (!Bukkit.getBukkitVersion().startsWith("1.7") && !Bukkit.getBukkitVersion().startsWith("1.8") && !Bukkit.getBukkitVersion().startsWith("1.9") && !Bukkit.getBukkitVersion().startsWith("1.10") && !Bukkit.getBukkitVersion().startsWith("1.11") && !Bukkit.getBukkitVersion().startsWith("1.12")) {
            return new ItemStack(Material.PLAYER_HEAD);
        } else {
            return new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
        }
    }
    
    public static String getHeadTexturesFromHead(ItemStack headItem) {
        if (Bukkit.getBukkitVersion().startsWith("1.7") || headItem == null) return null;
        if (headItem.getItemMeta() instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) headItem.getItemMeta();
            Field profileField;
            try {
                profileField = skull.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                GameProfile profile;
                try {
                    profile = (GameProfile) profileField.get(skull);
                } catch (ClassCastException ex) {
                    Object resolvableProfile = profileField.get(skull);
                    try {
                        profile = (GameProfile) resolvableProfile.getClass().getMethod("gameProfile").invoke(resolvableProfile);
                    } catch (NoSuchMethodException ex1) {
                        Method method_ = Arrays.stream(resolvableProfile.getClass().getMethods()).filter(method -> method.getReturnType().getName().equals("com.mojang.authlib.GameProfile") && method.getParameterTypes().length == 0).findFirst().orElse(null);
                        profile = method_ == null ? null : (GameProfile) method_.invoke(resolvableProfile);
                    }
                }
                if (profile != null) {
                    Property property = getProperties(profile).get("textures").stream().findFirst().orElse(null);
                    if (property != null) {
                        try {
                            return property.getValue();
                        } catch (NoSuchMethodError error) {
                            try {
                                return property.getClass().getMethod("value").invoke(property).toString();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }
    
    public static ItemStack getHeadWithTextures(String textures) {
        ItemStack headItem = getDefaultHead();
        if (Bukkit.getBukkitVersion().startsWith("1.7") || textures == null) return headItem;
        SkullMeta skull = (SkullMeta) headItem.getItemMeta();
        GameProfile profile = generateGameProfile(textures);
        Field profileField;
        try {
            profileField = skull.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            try {
                profileField.set(skull, profile);
            } catch (IllegalArgumentException ex) { //1.21+
                try {
                    Object resolvableProfile = Class.forName("net.minecraft.world.item.component.ResolvableProfile").getConstructor(GameProfile.class).newInstance(profile);
                    profileField.set(skull, resolvableProfile);
                } catch (NoSuchMethodException ex1) { //1.21.9+
                    profileField.set(skull, Class.forName("net.minecraft.world.item.component.ResolvableProfile").getMethod("createResolved", GameProfile.class).invoke(null, profile));
                }
            }
//            if (Bukkit.getBukkitVersion().startsWith("1.20")) { // This is historical legacy code with unclear functionality, so it is temporarily commented out.
//                Field serializedProfileField = skull.getClass().getDeclaredField("serializedProfile");
//                Method writeGameProfile = Arrays.stream(NMSManager.gameProfileSerializer.getMethods()).filter(method -> method.getParameterTypes().length == 2 && method.getParameterTypes()[0].equals(NMSManager.nbtTagCompound) && method.getParameterTypes()[1].equals(profile.getClass()) && method.getReturnType().equals(NMSManager.nbtTagCompound)).findFirst().orElse(null);
//                if (writeGameProfile != null) {
//                    serializedProfileField.setAccessible(true);
//                    serializedProfileField.set(skull, writeGameProfile.invoke(null, NMSManager.nbtTagCompound.getConstructor().newInstance(), profile));
//                    serializedProfileField.setAccessible(false);
//                }
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        headItem.setItemMeta(skull);
        return headItem;
    }
    
    public static PropertyMap getProperties(GameProfile profile) {
        try {
            return profile.getProperties();
        } catch (NoSuchMethodError error) { // 1.21.9+
            try {
                return (PropertyMap) profile.getClass().getMethod("properties").invoke(profile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    public static GameProfile generateGameProfile(String textures) {
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "Skull");
            profile.getProperties().put("textures", new Property("textures", textures));
            return profile;
        } catch (NoSuchMethodError error) { // 1.21.9+
            try {
                Multimap map = ArrayListMultimap.create();
                map.put("textures", new Property("textures", textures));
                PropertyMap propertyMap = PropertyMap.class.getConstructor(com.google.common.collect.Multimap.class).newInstance(map);
                GameProfile profile = GameProfile.class.getConstructor(UUID.class, String.class, PropertyMap.class)
                .newInstance(
                    UUID.randomUUID(),
                    "Skull",
                    propertyMap
                );
                return profile;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
