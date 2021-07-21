package studio.trc.bukkit.litesignin.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import studio.trc.bukkit.litesignin.async.AutoSave;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.MessageUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.database.MySQLStorage;
import studio.trc.bukkit.litesignin.database.YamlStorage;
import studio.trc.bukkit.litesignin.database.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.event.Menu;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.util.woodsignscript.WoodSignUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class PluginControl
{
    public static String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    
    public static void reload() {
        ConfigurationUtil.reloadConfig();
        YamlStorage.cache.clear();
        SQLiteStorage.cache.clear();
        MySQLStorage.cache.clear();
        if (useMySQLStorage()) {
            MySQLEngine.reloadConnectionParameters();
        } else if (useSQLiteStorage()) {
            SQLiteEngine.reloadConnectionParameters();
        } else {
            SignInQueue.getInstance().loadQueue();
        }
        try {
            if (!Placeholders.getInstance().isRegistered()) {
                Placeholders.getInstance().register();
            }
            SignInPluginProperties.sendOperationMessage("FindThePlaceholderAPI", new HashMap());
        } catch (Error ex) {
            ConfigurationUtil.getConfig(ConfigurationType.CONFIG).set("Use-PlaceholderAPI", false);
            SignInPluginProperties.sendOperationMessage("PlaceholderAPINotFound", new HashMap());
        }
        Bukkit.getOnlinePlayers().stream().filter(ps -> Menu.menuOpening.containsKey(ps.getUniqueId())).forEachOrdered(Player::closeInventory);
        AutoSave.stopThread();
        AutoSave.startThread();
        headCacheData.clear();
        if (enableSignScript()) {
            WoodSignUtil.loadScripts();
            WoodSignUtil.loadSigns();
            WoodSignUtil.scan();
            Map<String, String> placeholders = new HashMap();
            placeholders.put("{scripts}", String.valueOf(WoodSignUtil.getWoodSignScripts().size()));
            placeholders.put("{signs}", String.valueOf(WoodSignUtil.getAllScriptedSign().size()));
            SignInPluginProperties.sendOperationMessage("WoodSignScriptLoaded", placeholders);
        }
    }
    
    public static void savePlayerData() {
        YamlStorage.cache.values().stream().forEach(YamlStorage::saveData);
        MySQLStorage.cache.values().stream().forEach(MySQLStorage::saveData);
        SQLiteStorage.cache.values().stream().forEach(SQLiteStorage::saveData);
    }
    
    public static void hideEnchants(ItemMeta im) {
        if (getNMSVersion().startsWith("v1_7")) {
            return;
        }
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }
    
    /**
     * Heads' cache data.
     */
    private static final Map<String, ItemMeta> headCacheData = new HashMap();
    
    public static void setHead(ItemStack is, String name) {
        if (getNMSVersion().startsWith("v1_7") || getNMSVersion().startsWith("v1_8") || getNMSVersion().startsWith("v1_9") || getNMSVersion().startsWith("v1_10") || getNMSVersion().startsWith("v1_11") || getNMSVersion().startsWith("v1_12")) {
            if (is.getType().equals(Material.valueOf("SKULL_ITEM")) && is.getData().getData() == 3) {
                if (headCacheData.containsKey(name)) {
                    is.setItemMeta(headCacheData.get(name));
                } else {
                    SkullMeta sm = (SkullMeta) is.getItemMeta();
                    sm.setOwner(name);
                    headCacheData.put(name, sm);
                    is.setItemMeta(sm);
                }
            }
        } else {
            if (is.getType().equals(Material.PLAYER_HEAD)) {
                if (headCacheData.containsKey(name)) {
                    is.setItemMeta(headCacheData.get(name));
                } else {
                    SkullMeta sm = (SkullMeta) is.getItemMeta();
                    sm.setOwningPlayer(Bukkit.getOfflinePlayer(name));
                    headCacheData.put(name, sm);
                    is.setItemMeta(sm);
                }
            }
        }
    }
    
    public static int getRetroactiveCardQuantityRequired() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getInt("Retroactive-Card.Quantity-Required");
    }
    
    public static double getRetroactiveCardIntervals() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("Retroactive-Card.Intervals");
    }
    
    public static double getMySQLRefreshInterval() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("MySQL-Storage.Refresh-Interval");
    }
    
    public static double getSQLiteRefreshInterval() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("SQLite-Storage.Refresh-Interval");
    }
    
    public static boolean usePlaceholderAPI() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Use-PlaceholderAPI");
    }
    
    public static boolean useMySQLStorage() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("MySQL-Storage.Enabled");
    }
    
    public static boolean useSQLiteStorage() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("SQLite-Storage.Enabled");
    }
    
    public static boolean dataAutoSave() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Auto-Save.Enabled");
    }
    
    public static boolean enableSignInRanking() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Enable-Sign-In-Ranking");
    }
     
    public static boolean enableRetroactiveCard() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Retroactive-Card.Enabled");
    }
     
    public static boolean enableRetroactiveCardRequiredItem() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Retroactive-Card.Required-Item.Enabled");
    }
    
    public static boolean enableJoinEvent() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Join-Event.Enabled");
    }
    
    public static boolean autoSignIn() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Join-Event.Auto-SignIn");
    }
    
    public static boolean enableUpdater() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Updater");
    }
    
    public static boolean enableMetrics() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Metrics");
    }
    
    public static boolean enableSignScript() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Wood-Signs-Script");
    }
    
    public static SignInDate getRetroactiveCardMinimumDate() {
        return SignInDate.getInstance(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Retroactive-Card.Minimum-Date"));
    }
    
    public static String getPrefix() {
        return MessageUtil.toColor(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Prefix"));
    }
    
    public static String getNMSVersion() {
        return PluginControl.nmsVersion;
    }
    
    public static ItemStack getRetroactiveCardRequiredItem(Player player) {
        String itemName = ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Retroactive-Card.Required-Item.CustomItem");
        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemName + ".Item") != null) {
            ItemStack is;
            try {
                if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemName + ".Data") != null) {
                    is = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemName + ".Item").toUpperCase()), 1, (short) ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getInt("Manual-Settings." + itemName + ".Data"));
                } else {
                    is = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemName + ".Item").toUpperCase()), 1);
                }
            } catch (IllegalArgumentException ex2) {
                return null;
            }
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemName + ".Head-Owner") != null) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("{player}", player.getName());
                PluginControl.setHead(is, MessageUtil.replacePlaceholders(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString("Manual-Settings." + itemName + ".Head-Owner"), placeholders));
            }
            ItemMeta im = is.getItemMeta();
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemName + ".Lore") != null) {
                List<String> lore = new ArrayList();
                for (String lores : ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getStringList("Manual-Settings." + itemName + ".Lore")) {
                    lore.add(MessageUtil.toPlaceholderAPIResult(MessageUtil.toColor(lores), player));
                }
                im.setLore(lore);
            }
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemName + ".Enchantment") != null) {
                for (String name : ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getStringList("Manual-Settings." + itemName + ".Enchantment")) {
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
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemName + ".Hide-Enchants") != null) PluginControl.hideEnchants(im);
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Manual-Settings." + itemName + ".Display-Name") != null) im.setDisplayName(MessageUtil.toColor(MessageUtil.toPlaceholderAPIResult(ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getString("Manual-Settings." + itemName + ".Display-Name"), player)));
            is.setItemMeta(im);
            return is;
        } else if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).get("Item-Collection." + itemName) != null) {
            ItemStack is = ConfigurationUtil.getConfig(ConfigurationType.CUSTOMITEMS).getItemStack("Item-Collection." + itemName);
            if (is != null) {
                return is;    
            }
        }
        return null;
    }
    
    public static int getRandom(int number1, int number2) {
        if (number1 == number2) {
            return number1;
        } else if (number1 > number2) {
            return new Random().nextInt(number1 - number2 + 1) + number2;
        } else if (number2 > number1) {
            return new Random().nextInt(number2 - number1 + 1) + number1;
        }
        return 0;
    }
    
    public static int getRandom(String placeholder) {
        String[] random = placeholder.split("-");
        try {
            int number1 = Integer.valueOf(random[0]);
            int number2 = Integer.valueOf(random[1]);
            if (number1 == number2) {
                return number1;
            } else if (number1 > number2) {
                return new Random().nextInt(number1 - number2 + 1) + number2;
            } else if (number2 > number1) {
                return new Random().nextInt(number2 - number1 + 1) + number1;
            }
        } catch (NumberFormatException ex) {}
        return 0;
    }
    
    public static boolean hasPermission(CommandSender sender, String path) {
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean(path + ".Default")) return true;
        return sender.hasPermission(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString(path + ".Permission"));
    }

    private static long backupFilesAcquisitionTime = 0;
    private static List<String> backupFiles = new ArrayList();
    
    public static List<String> getBackupFiles() {
        if (System.currentTimeMillis() - backupFilesAcquisitionTime <= 5000) {
            return backupFiles;
        }
        List<String> list = new ArrayList();
        File folder = new File(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Database-Management.Rollback.Backup-Folder-Path"));
        if (!folder.exists()) return list;
        File[] files = folder.listFiles();
        for (File f : files) {
            list.add(f.getName());
        }
        backupFiles = list;
        backupFilesAcquisitionTime = System.currentTimeMillis();
        return list;
    }
}
