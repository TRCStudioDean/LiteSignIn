package studio.trc.bukkit.litesignin.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.database.storage.MySQLStorage;
import studio.trc.bukkit.litesignin.database.storage.YamlStorage;
import studio.trc.bukkit.litesignin.database.storage.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.message.color.ColorUtils;
import studio.trc.bukkit.litesignin.event.Menu;
import studio.trc.bukkit.litesignin.thread.LiteSignInThread;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.util.woodsignscript.WoodSignUtil;

public class PluginControl
{
    public static void reload() {
        ConfigurationUtil.reloadConfig();
        MessageUtil.loadPlaceholders();
        YamlStorage.cache.clear();
        SQLiteStorage.cache.clear();
        MySQLStorage.cache.clear();
        if (useMySQLStorage()) {
            reloadMySQL();
        } else if (useSQLiteStorage()) {
            reloadSQLite();
        } else {
            SignInQueue.getInstance().loadQueue();
        }
        try {
            if (ConfigurationType.CONFIG.getRobustConfig().getBoolean("PlaceholderAPI.Enabled")) {
                if (!PlaceholderAPIImpl.getInstance().isRegistered()) {
                    PlaceholderAPIImpl.getInstance().register();
                }
                MessageUtil.setEnabledPAPI(true);
                LiteSignInProperties.sendOperationMessage("FindThePlaceholderAPI", MessageUtil.getDefaultPlaceholders());
            }
        } catch (Error ex) {
            MessageUtil.setEnabledPAPI(false);
            ConfigurationUtil.getConfig(ConfigurationType.CONFIG).set("PlaceholderAPI.Enabled", false);
            LiteSignInProperties.sendOperationMessage("PlaceholderAPINotFound", MessageUtil.getDefaultPlaceholders());
        }
        Bukkit.getOnlinePlayers().stream().filter(ps -> Menu.menuOpening.containsKey(ps.getUniqueId())).forEachOrdered(Player::closeInventory);
        LiteSignInThread.initialize();
        headCacheData.clear();
        if (enableSignScript()) {
            WoodSignUtil.loadScripts();
            WoodSignUtil.loadSigns();
            WoodSignUtil.scan();
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{scripts}", String.valueOf(WoodSignUtil.getWoodSignScripts().size()));
            placeholders.put("{signs}", String.valueOf(WoodSignUtil.getAllScriptedSign().size()));
            LiteSignInProperties.sendOperationMessage("WoodSignScriptLoaded", placeholders);
        }
    }
    
    public static void reloadMySQL() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.CONFIG);
        Map<String, String> jdbcOptions = new HashMap();
        config.getConfigurationSection("MySQL-Storage.Options").getKeys(false).stream().forEach(option -> {
            jdbcOptions.put(option, config.getString("MySQL-Storage.Options." + option));
        });
        if (MySQLEngine.getInstance() != null) {
            MySQLEngine.getInstance().disconnect();
        }
        MySQLEngine.setInstance(new MySQLEngine(
            config.getString("MySQL-Storage.Hostname"),
            config.getInt("MySQL-Storage.Port"),
            config.getString("MySQL-Storage.Username"),
            config.getString("MySQL-Storage.Password"),
            jdbcOptions)
        );
        MySQLEngine.getInstance().connect();
    }
    
    public static void reloadSQLite() {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.CONFIG);
        if (SQLiteEngine.getInstance() != null) {
            SQLiteEngine.getInstance().disconnect();
        }
        SQLiteEngine.setInstance(new SQLiteEngine(config.getString("SQLite-Storage.Database-Path"), config.getString("SQLite-Storage.Database-File")));
        SQLiteEngine.getInstance().connect();
    }
    
    public static void savePlayerData() {
        YamlStorage.cache.values().stream().forEach(YamlStorage::saveData);
        MySQLStorage.cache.values().stream().forEach(MySQLStorage::saveData);
        SQLiteStorage.cache.values().stream().forEach(SQLiteStorage::saveData);
    }
    
    public static void hideEnchants(ItemMeta im) {
        if (Bukkit.getBukkitVersion().startsWith("1.7")) {
            return;
        }
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }
    
    /**
     * Heads' cache data.
     */
    private static final Map<String, ItemMeta> headCacheData = new HashMap();
    
    public static void setHead(ItemStack is, String name) {
        if (Bukkit.getBukkitVersion().startsWith("1.7") || Bukkit.getBukkitVersion().startsWith("1.8") || Bukkit.getBukkitVersion().startsWith("1.9") || Bukkit.getBukkitVersion().startsWith("1.10") || Bukkit.getBukkitVersion().startsWith("1.11") || Bukkit.getBukkitVersion().startsWith("1.12")) {
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
    
    public static int getGUILimitedDateYear() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getInt("GUI-Settings.Limit-Date.Minimum-Year");
    }
    
    public static int getGUILimitedDateMonth() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getInt("GUI-Settings.Limit-Date.Minimum-Month");
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
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("PlaceholderAPI.Enabled");
    }
    
    public static boolean useMySQLStorage() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("MySQL-Storage.Enabled");
    }
    
    public static boolean useSQLiteStorage() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("SQLite-Storage.Enabled");
    }
    
    public static boolean enableSignInRanking() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Enable-Sign-In-Ranking");
    }
    
    public static boolean enableSignInGUI() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("GUI-Settings.Enabled");
    }
    
    public static boolean enableGUILimitDate() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("GUI-Settings.Limit-Date.Enabled");
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
    
    public static SignInDate getGUILimitedDate() {
        return SignInDate.getInstance(getGUILimitedDateYear(), getGUILimitedDateMonth(), 1);
    }
    
    public static String getPrefix() {
        return ColorUtils.toColor(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Prefix"));
    }
    
    public static ItemStack getRetroactiveCardRequiredItem(Player player) {
        String itemName = ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Retroactive-Card.Required-Item.CustomItem");
        if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).get("Manual-Settings." + itemName + ".Item") != null) {
            ItemStack is;
            try {
                if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).get("Manual-Settings." + itemName + ".Data") != null) {
                    is = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getString("Manual-Settings." + itemName + ".Item").toUpperCase()), 1, (short) ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getInt("Manual-Settings." + itemName + ".Data"));
                } else {
                    is = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getString("Manual-Settings." + itemName + ".Item").toUpperCase()), 1);
                }
            } catch (IllegalArgumentException ex2) {
                return null;
            }
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).get("Manual-Settings." + itemName + ".Head-Owner") != null) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{player}", player.getName());
                PluginControl.setHead(is, MessageUtil.replacePlaceholders(player, ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getString("Manual-Settings." + itemName + ".Head-Owner"), placeholders));
            }
            ItemMeta im = is.getItemMeta();
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).get("Manual-Settings." + itemName + ".Lore") != null) {
                List<String> lore = new ArrayList();
                for (String lores : ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getStringList("Manual-Settings." + itemName + ".Lore")) {
                    lore.add(MessageUtil.toPlaceholderAPIResult(ColorUtils.toColor(lores), player));
                }
                im.setLore(lore);
            }
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).get("Manual-Settings." + itemName + ".Enchantment") != null) {
                for (String name : ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getStringList("Manual-Settings." + itemName + ".Enchantment")) {
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
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).get("Manual-Settings." + itemName + ".Hide-Enchants") != null) PluginControl.hideEnchants(im);
            if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).get("Manual-Settings." + itemName + ".Display-Name") != null) im.setDisplayName(ColorUtils.toColor(MessageUtil.toPlaceholderAPIResult(ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getString("Manual-Settings." + itemName + ".Display-Name"), player)));
            is.setItemMeta(im);
            return is;
        } else if (ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).get("Item-Collection." + itemName) != null) {
            ItemStack is = ConfigurationUtil.getConfig(ConfigurationType.CUSTOM_ITEMS).getItemStack("Item-Collection." + itemName);
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
    
    public static void runBukkitTask(Runnable task, long delay) {
        try {
            if (delay == 0) {
                Bukkit.getScheduler().runTask(Main.getInstance(), task);
            } else {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), task, delay);
            }
        } catch (UnsupportedOperationException ex) {
            //Folia suppport (test)
            Consumer runnable = run -> task.run();
            try {
                Object globalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                if (delay == 0) {
                    globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class).invoke(globalRegionScheduler, Main.getInstance(), runnable);
                } else {
                    globalRegionScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class).invoke(globalRegionScheduler, Main.getInstance(), runnable, delay);
                }
            } catch (Exception e) {
                e.printStackTrace();
                task.run();
            }
        }
    }
}
