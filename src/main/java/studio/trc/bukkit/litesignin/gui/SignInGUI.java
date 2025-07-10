package studio.trc.bukkit.litesignin.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.gui.SignInGUIColumn.KeyType;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;
import studio.trc.bukkit.litesignin.nms.NMSManager;

import studio.trc.bukkit.litesignin.message.color.ColorUtils;

public class SignInGUI
{
    public static SignInInventory getGUI(Player player) {
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings");
        /**
         * Create chest GUI
         */
        Inventory gui = Bukkit.createInventory(null, 54, ColorUtils.toColor(replace(player, section.getString("GUI-Name"), "{date}", new SimpleDateFormat(section.getString("Date-Format")).format(new Date()))));
        
        /**
         * Elements
         */
        List<SignInGUIColumn> columns = new ArrayList();
        
        getKey(player).stream().map(items -> {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            return items;
        }).forEach(items -> columns.add(items));
        
        getOthers(player).stream().map(items -> {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            return items;
        }).forEach(items -> columns.add(items));
        
        return new SignInInventory(gui, columns);
    }
    
    public static SignInInventory getGUI(Player player, int month) {
        /**
         * Chest GUI
         */
        Inventory gui;
        
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings");
        
        /**
         * If month = specified month, return basic gui.
         */
        Date now = new Date();
        if (month == SignInDate.getInstance(now).getMonth()) {
            gui = Bukkit.createInventory(null, 54, ColorUtils.toColor(replace(player, section.getString("GUI-Name"), "{date}", new SimpleDateFormat(section.getString("Date-Format")).format(now))));
        } else {
            gui = Bukkit.createInventory(null, 54, ColorUtils.toColor(replace(player, section.getString("Specified-Month-GUI-Name"), "{month}", String.valueOf(month))));
        }
        
        /**
         * Elements
         */
        List<SignInGUIColumn> columns = new ArrayList();
        
        getKey(player, month).stream().map(items -> {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            return items;
        }).forEach(items -> columns.add(items));
        
        getOthers(player, month).stream().map(items -> {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            return items;
        }).forEach((items) -> columns.add(items));
        
        return new SignInInventory(gui, columns, month);
    }
    
    public static SignInInventory getGUI(Player player, int month, int year) {
        /**
         * Chest GUI
         */
        Inventory gui;
        SignInDate today = SignInDate.getInstance(new Date());
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings");
        
        /**
         * If month = specified month and year = specified year, return basic gui.
         */
        if (year == today.getYear()) {
            if (month == today.getMonth()) {
                gui = Bukkit.createInventory(null, 54, ColorUtils.toColor(replace(player, section.getString("GUI-Name"), "{date}", new SimpleDateFormat(section.getString("Date-Format")).format(new Date()))));
            } else {
                gui = Bukkit.createInventory(null, 54, ColorUtils.toColor(replace(player, section.getString("Specified-Month-GUI-Name"), "{month}", String.valueOf(month))));
            }
        } else {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{month}", String.valueOf(month));
            placeholders.put("{year}", String.valueOf(year));
            gui = Bukkit.createInventory(null, 54, MessageUtil.replacePlaceholders(player, section.getString("Specified-Year-GUI-Name"), placeholders));
        }
        
        /**
         * Elements
         */
        List<SignInGUIColumn> columns = new ArrayList();
        
        getKey(player, month, year).stream().map(items -> {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            return items;
        }).forEach(items -> columns.add(items));
        
        getOthers(player, month, year).stream().map(items -> {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            return items;
        }).forEach(items -> columns.add(items));
        
        return new SignInInventory(gui, columns, month, year);
    }
    
    /**
     * Return key buttons.
     * @param player 
     * @param month specified month.
     * @param year specified year.
     * @return 
     */
    public static Set<SignInGUIColumn> getKey(Player player, int month, int year) {
        Set<SignInGUIColumn> set = new LinkedHashSet();
        SignInDate today = SignInDate.getInstance(new Date());
        if (today.getMonth() == month && today.getYear() == year) return getKey(player);
        Storage playerdata = Storage.getPlayer(player);
        String queue = String.valueOf(SignInQueue.getInstance().getRank(playerdata.getUserUUID()));
        String continuous = String.valueOf(playerdata.getContinuousSignIn());
        String totalNumber = String.valueOf(playerdata.getCumulativeNumber());
        String cards = String.valueOf(playerdata.getRetroactiveCard());
        int nextPageMonth = getNextPageMonth(month);
        int nextPageYear = getNextPageYear(month, year);
        int previousPageMonth = getPreviousPageMonth(month);
        int previousPageYear = getPreviousPageYear(month, year);
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        SignInDate specifiedDate = SignInDate.getInstance(year, month, 1);
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key");
        if (specifiedDate.compareTo(today) == -1) {
            List<ItemStack> items = new LinkedList();
            List<KeyType> keys = new LinkedList();
            for (int i = 0;i < days[month - 1];i++) {
                SignInDate historicalDate = SignInDate.getInstance(year, month, i + 1);
                ItemStack key;
                KeyType keyType;
                if (playerdata.alreadySignIn(historicalDate)) {
                    try {
                        if (section.get("Already-SignIn.Data") != null) {
                            key = new ItemStack(Material.valueOf(section.getString("Already-SignIn.Item").toUpperCase()), i + 1, (short) section.getInt("Already-SignIn.Data"));
                        } else {
                            key = new ItemStack(Material.valueOf(section.getString("Already-SignIn.Item").toUpperCase()), i + 1);
                        }
                    } catch (IllegalArgumentException ex) {
                        key = new ItemStack(Material.BARRIER, i + 1);
                    }
                    if (section.get("Already-SignIn.Head-Owner") != null) {
                        PluginControl.setHead(key, replace(player, section.getString("Already-SignIn.Head-Owner"), "{player}", player.getName()));
                    }
                    if (section.get("Already-SignIn.Amount") != null) {
                        key.setAmount(section.getInt("Already-SignIn.Amount"));
                    }
                    if (section.get("Already-SignIn.Head-Textures") != null) {
                        setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Head-Textures", key);
                    }
                    if (section.get("Already-SignIn.Custom-Model-Data") != null) {
                        setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Custom-Model-Data", key);
                    }
                    ItemMeta im = key.getItemMeta();
                    if (section.get("Already-SignIn.Lore") != null) {
                        List<String> lore = new ArrayList();
                        Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                        section.getStringList("Already-SignIn.Lore").stream().forEach(lores -> {
                            lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                        });
                        im.setLore(lore);
                    }
                    if (section.get("Already-SignIn.Enchantment") != null) {
                        setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Enchantment", im);
                    }
                    if (section.get("Already-SignIn.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                    if (section.get("Already-SignIn.Display-Name") != null) im.setDisplayName(ColorUtils.toColor(replace(player, section.getString("Already-SignIn.Display-Name"), "{day}", String.valueOf(i + 1))));
                    key.setItemMeta(im);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    try {
                        if (section.get("Missed-SignIn.Data") != null) {
                            key = new ItemStack(Material.valueOf(section.getString("Missed-SignIn.Item").toUpperCase()), i + 1, (short) section.getInt("Missed-SignIn.Data"));
                        } else {
                            key = new ItemStack(Material.valueOf(section.getString("Missed-SignIn.Item").toUpperCase()), i + 1);
                        }
                    } catch (IllegalArgumentException ex) {
                        key = new ItemStack(Material.BARRIER, i + 1);
                    }
                    if (section.get("Missed-SignIn.Head-Owner") != null) {
                        PluginControl.setHead(key, replace(player, section.getString("Missed-SignIn.Head-Owner"), "{player}", player.getName()));
                    }
                    if (section.get("Missed-SignIn.Amount") != null) {
                        key.setAmount(section.getInt("Missed-SignIn.Amount"));
                    }
                    if (section.get("Missed-SignIn.Head-Textures") != null) {
                        setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Head-Textures", key);
                    }
                    if (section.get("Missed-SignIn.Custom-Model-Data") != null) {
                        setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Custom-Model-Data", key);
                    }
                    ItemMeta im = key.getItemMeta();
                    if (section.get("Missed-SignIn.Lore") != null) {
                        List<String> lore = new ArrayList();
                        Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                        section.getStringList("Missed-SignIn.Lore").stream().forEach(lores -> {
                            lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                        });
                        im.setLore(lore);
                    }
                    if (section.get("Missed-SignIn.Enchantment") != null) {
                        setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Enchantment", im);
                    }
                    if (section.get("Missed-SignIn.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                    if (section.get("Missed-SignIn.Display-Name") != null) im.setDisplayName(ColorUtils.toColor(replace(player, section.getString("Missed-SignIn.Display-Name"), "{day}", String.valueOf(i + 1))));
                    key.setItemMeta(im);
                    keyType = KeyType.MISSED_SIGNIN;
                }
                items.add(key);
                keys.add(keyType);
            }
            if (section.get("Slots") != null) {
                int i = 0;
                for (String slots : section.getStringList("Slots")) {
                    try {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month - 1, i + 1);
                        set.add(new SignInGUIColumn(items.get(i), Integer.valueOf(slots), SignInDate.getInstance(cal.getTime()), keys.get(i)));
                    } catch (IndexOutOfBoundsException ex) {}
                    i++;
                }
            }
            return set;
        } else {
            List<ItemStack> items = new LinkedList();
            List<KeyType> keys = new LinkedList();
            for (int i = 0;i < days[month - 1];i++) {
                ItemStack key;
                try {
                    if (section.get("Comming-Soon.Data") != null) {
                        key = new ItemStack(Material.valueOf(section.getString("Comming-Soon.Item").toUpperCase()), i + 1, (short) section.getInt("Comming-Soon.Data"));
                    } else {
                        key = new ItemStack(Material.valueOf(section.getString("Comming-Soon.Item").toUpperCase()), i + 1);
                    }
                } catch (IllegalArgumentException ex) {
                    key = new ItemStack(Material.BARRIER, i + 1);
                }
                if (section.get("Comming-Soon.Head-Owner") != null) {
                    PluginControl.setHead(key, replace(player, section.getString("Comming-Soon.Head-Owner"), "{player}", player.getName()));
                }
                if (section.get("Comming-Soon.Amount") != null) {
                    key.setAmount(section.getInt("Comming-Soon.Amount"));
                }
                if (section.get("Comming-Soon.Head-Textures") != null) {
                    setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Head-Textures", key);
                }
                if (section.get("Comming-Soon.Custom-Model-Data") != null) {
                    setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Custom-Model-Data", key);
                }
                ItemMeta im = key.getItemMeta();
                if (section.get("Comming-Soon.Lore") != null) {
                    List<String> lore = new ArrayList();
                    Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, null);
                    section.getStringList("Comming-Soon.Lore").stream().forEach(lores -> {
                        lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                    });
                    im.setLore(lore);
                }
                if (section.get("Comming-Soon.Enchantment") != null) {
                    setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Enchantment", im);
                }
                if (section.get("Comming-Soon.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                if (section.get("Comming-Soon.Display-Name") != null) im.setDisplayName(ColorUtils.toColor(replace(player, section.getString("Comming-Soon.Display-Name"), "{day}", String.valueOf(i + 1))));
                key.setItemMeta(im);
                items.add(key);
                keys.add(KeyType.COMMING_SOON);
            }
            if (section.get("Slots") != null) {
                int i = 0;
                for (String slots : section.getStringList("Slots")) {
                    try {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month - 1, i + 1);
                        set.add(new SignInGUIColumn(items.get(i), Integer.valueOf(slots), SignInDate.getInstance(cal.getTime()), keys.get(i)));
                    } catch (IndexOutOfBoundsException ex) {}
                    i++;
                }
            }
            return set;
        }
    }
    
    /**
     * Return key buttons.
     * @param player 
     * @param month specified month.
     * @return 
     */
    public static Set<SignInGUIColumn> getKey(Player player, int month) {
        SignInDate today = SignInDate.getInstance(new Date());
        if (today.getMonth() == month) return getKey(player);
        else return getKey(player, month, today.getYear());
    }
    
    /**
     * Return key buttons.
     * @param player 
     * @return 
     */
    public static Set<SignInGUIColumn> getKey(Player player) {
        Set<SignInGUIColumn> set = new LinkedHashSet();
        Storage playerdata = Storage.getPlayer(player);
        String queue = String.valueOf(SignInQueue.getInstance().getRank(playerdata.getUserUUID()));
        String continuous = String.valueOf(playerdata.getContinuousSignIn());
        String totalNumber = String.valueOf(playerdata.getCumulativeNumber());
        String cards = String.valueOf(playerdata.getRetroactiveCard());
        String[] times = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).split("-");
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key");
        int year = Integer.valueOf(times[0]);
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        int month = Integer.valueOf(times[1]);
        int day = Integer.valueOf(times[2]);
        int nextPageMonth = getNextPageMonth(month);
        int nextPageYear = getNextPageYear(month, year);
        int previousPageMonth = getPreviousPageMonth(month);
        int previousPageYear = getPreviousPageYear(month, year);
        List<ItemStack> items = new LinkedList();
        List<KeyType> keys = new LinkedList();
        for (int i = 0;i < days[month - 1];i++) {
            SignInDate historicalDate = SignInDate.getInstance(year, month, i + 1);
            if (i + 1 < day) {
                ItemStack key;
                KeyType keyType;
                if (playerdata.alreadySignIn(historicalDate)) {
                    try {
                        if (section.get("Already-SignIn.Data") != null) {
                            key = new ItemStack(Material.valueOf(section.getString("Already-SignIn.Item").toUpperCase()), i + 1, (short) section.getInt("Already-SignIn.Data"));
                        } else {
                            key = new ItemStack(Material.valueOf(section.getString("Already-SignIn.Item").toUpperCase()), i + 1);
                        }
                    } catch (IllegalArgumentException ex) {
                        key = new ItemStack(Material.BARRIER, i + 1);
                    }
                    if (section.get("Already-SignIn.Head-Owner") != null) {
                        PluginControl.setHead(key, replace(player, section.getString("Already-SignIn.Head-Owner"), "{player}", player.getName()));
                    }
                    if (section.get("Already-SignIn.Amount") != null) {
                        key.setAmount(section.getInt("Already-SignIn.Amount"));
                    }
                    if (section.get("Already-SignIn.Head-Textures") != null) {
                        setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Head-Textures", key);
                    }
                    if (section.get("Already-SignIn.Custom-Model-Data") != null) {
                        setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Custom-Model-Data", key);
                    }
                    ItemMeta im = key.getItemMeta();
                    if (section.get("Already-SignIn.Lore") != null) {
                        List<String> lore = new ArrayList();
                        Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                        section.getStringList("Already-SignIn.Lore").stream().forEach(lores -> {
                            lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                        });
                        im.setLore(lore);
                    }
                    if (section.get("Already-SignIn.Enchantment") != null) {
                        setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Enchantment", im);
                    }
                    if (section.get("Already-SignIn.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                    if (section.get("Already-SignIn.Display-Name") != null) im.setDisplayName(ColorUtils.toColor(replace(player, section.getString("Already-SignIn.Display-Name"), "{day}", String.valueOf(i + 1))));
                    key.setItemMeta(im);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    if (section.get("Missed-SignIn.Data") != null) {
                        key = new ItemStack(Material.valueOf(section.getString("Missed-SignIn.Item").toUpperCase()), i + 1, (short) section.getInt("Missed-SignIn.Data"));
                    } else {
                        key = new ItemStack(Material.valueOf(section.getString("Missed-SignIn.Item").toUpperCase()), i + 1);
                    }
                    if (section.get("Missed-SignIn.Amount") != null) {
                        key.setAmount(section.getInt("Missed-SignIn.Amount"));
                    }
                    if (section.get("Missed-SignIn.Head-Owner") != null) {
                        PluginControl.setHead(key, replace(player, section.getString("Missed-SignIn.Head-Owner"), "{player}", player.getName()));
                    }
                    if (section.get("Missed-SignIn.Head-Textures") != null) {
                        setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Head-Textures", key);
                    }
                    if (section.get("Missed-SignIn.Custom-Model-Data") != null) {
                        setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Custom-Model-Data", key);
                    }
                    ItemMeta im = key.getItemMeta();
                    if (section.get("Missed-SignIn.Lore") != null) {
                        List<String> lore = new ArrayList();
                        Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                        section.getStringList("Missed-SignIn.Lore").stream().forEach(lores -> {
                            lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                        });
                        im.setLore(lore);
                    }
                    if (section.get("Missed-SignIn.Enchantment") != null) {
                        setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Enchantment", im);
                    }
                    if (section.get("Missed-SignIn.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                    if (section.get("Missed-SignIn.Display-Name") != null) im.setDisplayName(ColorUtils.toColor(replace(player, section.getString("Missed-SignIn.Display-Name"), "{day}", String.valueOf(i + 1))));
                    key.setItemMeta(im);
                    keyType = KeyType.MISSED_SIGNIN;
                }
                items.add(key);
                keys.add(keyType);
            } else if (i + 1 == day) {
                ItemStack key;
                KeyType keyType;
                if (playerdata.alreadySignIn(historicalDate)) {
                    try {
                        if (section.get("Already-SignIn.Data") != null) {
                            key = new ItemStack(Material.valueOf(section.getString("Already-SignIn.Item").toUpperCase()), i + 1, (short) section.getInt("Already-SignIn.Data"));
                        } else {
                            key = new ItemStack(Material.valueOf(section.getString("Already-SignIn.Item").toUpperCase()), i + 1);
                        }
                    } catch (IllegalArgumentException ex) {
                        key = new ItemStack(Material.BARRIER, i + 1);
                    }
                    if (section.get("Already-SignIn.Head-Owner") != null) {
                        PluginControl.setHead(key, replace(player, section.getString("Already-SignIn.Head-Owner"), "{player}", player.getName()));
                    }
                    if (section.get("Already-SignIn.Amount") != null) {
                        key.setAmount(section.getInt("Already-SignIn.Amount"));
                    }
                    if (section.get("Already-SignIn.Head-Textures") != null) {
                        setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Head-Textures", key);
                    }
                    if (section.get("Already-SignIn.Custom-Model-Data") != null) {
                        setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Custom-Model-Data", key);
                    }
                    ItemMeta im = key.getItemMeta();
                    if (section.get("Already-SignIn.Lore") != null)  {
                        List<String> lore = new ArrayList();
                        Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                        section.getStringList("Already-SignIn.Lore").stream().forEach(lores -> {
                            lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                        });
                        im.setLore(lore);
                    }
                    if (section.get("Already-SignIn.Enchantment") != null) {
                        setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Enchantment", im);
                    }
                    if (section.get("Already-SignIn.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                    if (section.get("Already-SignIn.Display-Name") != null) im.setDisplayName(ColorUtils.toColor(replace(player, section.getString("Already-SignIn.Display-Name"), "{day}", String.valueOf(i + 1))));
                    key.setItemMeta(im);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    if (section.get("Nothing-SignIn.Data") != null) {
                        key = new ItemStack(Material.valueOf(section.getString("Nothing-SignIn.Item").toUpperCase()), i + 1, (short) section.getInt("Nothing-SignIn.Data"));
                    } else {
                        key = new ItemStack(Material.valueOf(section.getString("Nothing-SignIn.Item").toUpperCase()), i + 1);
                    }
                    if (section.get("Nothing-SignIn.Amount") != null) {
                        key.setAmount(section.getInt("Nothing-SignIn.Amount"));
                    }
                    if (section.get("Nothing-SignIn.Head-Owner") != null) {
                        PluginControl.setHead(key, replace(player, section.getString("Nothing-SignIn.Head-Owner"), "{player}", player.getName()));
                    }
                    if (section.get("Nothing-SignIn.Head-Textures") != null) {
                        setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Nothing-SignIn.Head-Textures", key);
                    }
                    if (section.get("Nothing-SignIn.Custom-Model-Data") != null) {
                        setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Nothing-SignIn.Custom-Model-Data", key);
                    }
                    ItemMeta im = key.getItemMeta();
                    if (section.get("Nothing-SignIn.Lore") != null) {
                        List<String> lore = new ArrayList();
                        Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                        section.getStringList("Nothing-SignIn.Lore").stream().forEach(lores -> {
                            lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                        });
                        im.setLore(lore);
                    }
                    if (section.get("Nothing-SignIn.Enchantment") != null) {
                        setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Nothing-SignIn.Enchantment", im);
                    }
                    if (section.get("Nothing-SignIn.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                    if (section.get("Nothing-SignIn.Display-Name") != null) im.setDisplayName(ColorUtils.toColor(replace(player, section.getString("Nothing-SignIn.Display-Name"), "{day}", String.valueOf(i + 1))));
                    key.setItemMeta(im);
                    keyType = KeyType.NOTHING_SIGNIN;
                }
                items.add(key);
                keys.add(keyType);
            } else if (i + 1 > day) {
                ItemStack key;
                try {
                    if (section.get("Comming-Soon.Data") != null) {
                        key = new ItemStack(Material.valueOf(section.getString("Comming-Soon.Item").toUpperCase()), i + 1, (short) section.getInt("Comming-Soon.Data"));
                    } else {
                        key = new ItemStack(Material.valueOf(section.getString("Comming-Soon.Item").toUpperCase()), i + 1);
                    }
                } catch (IllegalArgumentException ex) {
                    key = new ItemStack(Material.BARRIER, i + 1);
                }
                if (section.get("Comming-Soon.Amount") != null) {
                    key.setAmount(section.getInt("Comming-Soon.Amount"));
                }
                if (section.get("Comming-Soon.Head-Owner") != null) {
                    PluginControl.setHead(key, replace(player, section.getString("Comming-Soon.Head-Owner"), "{player}", player.getName()));
                }
                if (section.get("Comming-Soon.Head-Textures") != null) {
                    setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Head-Textures", key);
                }
                if (section.get("Comming-Soon.Custom-Model-Data") != null) {
                    setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Custom-Model-Data", key);
                }
                ItemMeta im = key.getItemMeta();
                if (section.get("Comming-Soon.Lore") != null) {
                    List<String> lore = new ArrayList();
                    Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                    section.getStringList("Comming-Soon.Lore").stream().forEach(lores -> {
                        lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                    });
                    im.setLore(lore);
                }
                if (section.get("Comming-Soon.Enchantment") != null) {
                    setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Enchantment", im);
                }
                if (section.get("Comming-Soon.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                if (section.get("Comming-Soon.Display-Name") != null) im.setDisplayName(ColorUtils.toColor(replace(player, section.getString("Comming-Soon.Display-Name"), "{day}", String.valueOf(i + 1))));
                key.setItemMeta(im);
                items.add(key);
                keys.add(KeyType.COMMING_SOON);
            }
        }
        if (section.get("Slots") != null) {
            int i = 0;
            for (String slots : section.getStringList("Slots")) {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month - 1, i + 1);
                    set.add(new SignInGUIColumn(items.get(i), Integer.valueOf(slots), SignInDate.getInstance(cal.getTime()), keys.get(i)));
                } catch (IndexOutOfBoundsException ex) {}
                i++;
            }
        }
        return set;
    }
    
    /**
     * Return other buttons.
     * @param player 
     * @return 
     */
    public static Set<SignInGUIColumn> getOthers(Player player) {
        SignInDate today = SignInDate.getInstance(new Date());
        return getOthers(player, today.getMonth(), today.getYear());
    }
    
    public static Set<SignInGUIColumn> getOthers(Player player, int month) {
        SignInDate today = SignInDate.getInstance(new Date());
        return getOthers(player, month, today.getYear());
    }
    
    public static Set<SignInGUIColumn> getOthers(Player player, int month, int year) {
        Set<SignInGUIColumn> set = new HashSet();
        Storage playerdata = Storage.getPlayer(player);
        String queue = String.valueOf(SignInQueue.getInstance().getRank(playerdata.getUserUUID()));
        String continuous = String.valueOf(playerdata.getContinuousSignIn());
        String totalNumber = String.valueOf(playerdata.getCumulativeNumber());
        String cards = String.valueOf(playerdata.getRetroactiveCard());
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others");
        int nextPageMonth = getNextPageMonth(month);
        int nextPageYear = getNextPageYear(month, year);
        int previousPageMonth = getPreviousPageMonth(month);
        int previousPageYear = getPreviousPageYear(month, year);
        if (section != null) {
            section.getKeys(false).stream().forEach(items -> {
                ItemStack other;
                try {
                    if (section.get(items + ".Data") != null) {
                        other = new ItemStack(Material.valueOf(section.getString(items + ".Item").toUpperCase()), 1, (short) section.getInt(items + ".Data"));
                    } else {
                        other = new ItemStack(Material.valueOf(section.getString(items + ".Item").toUpperCase()), 1);
                    }
                } catch (IllegalArgumentException ex) {
                    other = new ItemStack(Material.BARRIER);
                }
                if (section.get(items + ".Head-Owner") != null) {
                    PluginControl.setHead(other, replace(player, section.getString(items + ".Head-Owner"), "{player}", player.getName()));
                }
                if (section.get(items + ".Head-Textures") != null) {
                    setHeadTextures(player, MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Head-Textures", other);
                }
                if (section.get(items + ".Custom-Model-Data") != null) {
                    setCustomModelData(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Custom-Model-Data", other);
                }
                ItemMeta im = other.getItemMeta();
                if (section.get(items + ".Lore") != null) {
                    List<String> lore = new ArrayList();
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{continuous}", continuous);
                    placeholders.put("{queue}", queue);
                    placeholders.put("{total-number}", totalNumber);
                    placeholders.put("{cards}", cards);
                    placeholders.put("{month}", String.valueOf(month));
                    placeholders.put("{year}", String.valueOf(year));
                    placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
                    placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
                    placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
                    placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
                    section.getStringList(items + ".Lore").stream().forEach(lores -> {
                        lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders));
                    });
                    im.setLore(lore);
                }
                if (section.get(items + ".Enchantment") != null) {
                    setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Enchantment", im);
                }
                if (section.get(items + ".Hide-Enchants") != null) PluginControl.hideEnchants(im);
                if (section.get(items + ".Display-Name") != null) im.setDisplayName(ColorUtils.toColor(MessageUtil.toPlaceholderAPIResult(player, section.getString(items + ".Display-Name"))));
                other.setItemMeta(im);
                other.setAmount(section.get(items + ".Amount") != null ? section.getInt(items + ".Amount") : 1);
                if (section.get(items + ".Slots") != null) {
                    for (int slot : section.getIntegerList(items + ".Slots")) {
                        set.add(new SignInGUIColumn(other, slot, items));
                    }
                }
                if (section.get(items + ".Slot") != null) {
                    set.add(new SignInGUIColumn(other, section.getInt(items + ".Slot"), items));
                }
            });
        }
        return set;
    }
    
    public static int getNextPageMonth(int month) {
        if (month == 12) {
            return 1;
        } else {
            return month + 1;
        }
    }
    
    public static int getNextPageYear(int month, int year) {
        if (month != 12) {
            return year;
        }
        return year + 1;
    }
    
    public static int getPreviousPageMonth(int month) {
        if (month == 1) {
            return 12;
        } else {
            return month - 1;
        }
    }
    
    public static int getPreviousPageYear(int month, int year) {
        if (month != 1) {
            return year;
        }
        return year - 1;
    }
    
    private static Map<String, String> getPlaceholdersOfItemLore(
            int day,
            String continuous,
            String queue,
            String totalNumber,
            String cards,
            int nextPageMonth,
            int nextPageYear,
            int previousPageMonth,
            int previousPageYear,
            SignInDate historicalDate) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (historicalDate != null) placeholders.put("{date}", historicalDate.getName(ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
        placeholders.put("{day}", String.valueOf(day + 1));
        placeholders.put("{continuous}", continuous);
        placeholders.put("{queue}", queue);
        placeholders.put("{total-number}", totalNumber);
        placeholders.put("{cards}", cards);
        placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
        placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
        placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
        placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
        return placeholders;
    }
    
    private static String replace(Player player, String text, String target, String replacement) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put(target, replacement);
        return MessageUtil.replacePlaceholders(player, text, placeholders);
    }
    
    private static void setEnchantments(String configPath, ItemMeta im) {
        for (String name : ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getStringList(configPath)) {
            try {
                String[] data = name.split(":");
                boolean invalid = true;
                for (Enchantment enchant : Enchantment.values()) {
                    if (enchant.getName().equalsIgnoreCase(data[0])) {
                        try {
                            im.addEnchant(enchant, Integer.valueOf(data[1]), true);
                            invalid = false;
                            break;
                        } catch (Exception ex) {
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                            placeholders.put("{path}", configPath + "." + name);
                            LiteSignInProperties.sendOperationMessage("InvalidEnchantmentSetting", placeholders);
                        }
                    }
                }
                if (invalid) {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{enchantment}", data[0]);
                    placeholders.put("{path}", configPath + "." + name);
                    LiteSignInProperties.sendOperationMessage("InvalidEnchantment", placeholders);
                }
            } catch (Exception ex) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{path}", configPath + "." + name);
                LiteSignInProperties.sendOperationMessage("InvalidEnchantmentSetting", placeholders);
            }
        }
    }
    
    private static void setCustomModelData(String configPath, ItemStack is) {
        String version = Bukkit.getBukkitVersion();
        if (version.startsWith("1.7") ||
            version.startsWith("1.8") ||
            version.startsWith("1.9") ||
            version.startsWith("1.10") ||
            version.startsWith("1.11") ||
            version.startsWith("1.12") || 
            version.startsWith("1.13")) return;
        if (is.getItemMeta() == null) return;
        ItemMeta im = is.getItemMeta();
        String name = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getString(configPath);
        if (im == null || name == null) return;
        try {
            im.setCustomModelData(Integer.valueOf(name));
        } catch (Exception ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{data}", name);
            placeholders.put("{path}", configPath + "." + name);
            LiteSignInProperties.sendOperationMessage("InvalidCustomModelData", placeholders);
        }
        is.setItemMeta(im);
    }
    
    private static void setHeadTextures(Player player, String configPath, ItemStack is) {
        String version = Bukkit.getBukkitVersion();
        if (version == null || version.startsWith("1.7")) return;
        ItemMeta im = is.getItemMeta();
        String textures = MessageUtil.toPlaceholderAPIResult(player, ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getString(configPath));
        if (im == null || textures == null) return;
        if (is.getItemMeta() instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) im;
            GameProfile profile = new GameProfile(UUID.randomUUID(), "Skull");
            profile.getProperties().put("textures", new Property("textures", textures));
            try {
                Field profileField = skull.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                try {
                    profileField.set(skull, profile);
                } catch (IllegalArgumentException ex) {
                    Object resolvableProfile = Class.forName("net.minecraft.world.item.component.ResolvableProfile").getConstructor(GameProfile.class).newInstance(profile);
                    profileField.set(skull, resolvableProfile);
                }
                profileField.setAccessible(false);
                if (version.startsWith("1.20")) {
                    Field serializedProfileField = skull.getClass().getDeclaredField("serializedProfile");
                    Method writeGameProfile = Arrays.stream(NMSManager.gameProfileSerializer.getMethods()).filter(method -> method.getParameterTypes().length == 2 && method.getParameterTypes()[0].equals(NMSManager.nbtTagCompound) && method.getParameterTypes()[1].equals(profile.getClass()) && method.getReturnType().equals(NMSManager.nbtTagCompound)).findFirst().orElse(null);
                    if (writeGameProfile != null) {
                        serializedProfileField.setAccessible(true);
                        serializedProfileField.set(skull, writeGameProfile.invoke(null, NMSManager.nbtTagCompound.getConstructor().newInstance(), profile));
                        serializedProfileField.setAccessible(false);
                    }
                }
            } catch (Exception e) {}
            is.setItemMeta(skull);
        }
    }
}
