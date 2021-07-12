package studio.trc.bukkit.litesignin.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.MessageUtil;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.gui.SignInGUIColumn.KeyType;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.PluginControl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SignInGUI
{
    
    public static SignInInventory getGUI(Player player) {
        /**
         * Chest GUI Create
         */
        Inventory gui = Bukkit.createInventory(null, 54, MessageUtil.toColor(replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.GUI-Name"), "{date}", new SimpleDateFormat(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")).format(new Date()))));
        
        /**
         * elements
         */
        List<SignInGUIColumn> columns = new ArrayList();
        
        for (SignInGUIColumn items : getKey(player)) {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            columns.add(items);
        }
        
        for (SignInGUIColumn items : getOthers(player)) {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            columns.add(items);
        }
        
        return new SignInInventory(gui, columns);
    }
    
    public static SignInInventory getGUI(Player player, int month) {
        /**
         * Chest GUI Create
         */
        Inventory gui;
        
        /**
         * If month = specified month, return basic gui.
         */
        Date now = new Date();
        if (month == SignInDate.getInstance(now).getMonth()) {
            gui = Bukkit.createInventory(null, 54, MessageUtil.toColor(replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.GUI-Name"), "{date}", new SimpleDateFormat(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")).format(now))));
        } else {
            gui = Bukkit.createInventory(null, 54, MessageUtil.toColor(replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Specified-Month-GUI-Name"), "{month}", String.valueOf(month))));
        }
        
        /**
         * elements
         */
        List<SignInGUIColumn> columns = new ArrayList();
        
        for (SignInGUIColumn items : getKey(player, month)) {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            columns.add(items);
        }
        
        for (SignInGUIColumn items : getOthers(player, month)) {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            columns.add(items);
        }
        
        return new SignInInventory(gui, columns, month);
    }
    
    public static SignInInventory getGUI(Player player, int month, int year) {
        /**
         * Chest GUI Create
         */
        Inventory gui;
        SignInDate today = SignInDate.getInstance(new Date());
        
        /**
         * If month = specified month or year = specified year, return basic gui.
         */
        if (month == today.getMonth() && year == today.getYear()) {
            gui = Bukkit.createInventory(null, 54, MessageUtil.toColor(replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.GUI-Name"), "{date}", new SimpleDateFormat(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")).format(new Date()))));
        } else {
            Map<String, String> placeholders = new HashMap();
            placeholders.put("{month}", String.valueOf(month));
            placeholders.put("{year}", String.valueOf(year));
            gui = Bukkit.createInventory(null, 54, MessageUtil.toColor(MessageUtil.replacePlaceholders(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Specified-Year-GUI-Name"), placeholders)));
        }
        
        /**
         * elements
         */
        List<SignInGUIColumn> columns = new ArrayList();
        
        for (SignInGUIColumn items : getKey(player, month, year)) {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            columns.add(items);
        }
        
        for (SignInGUIColumn items : getOthers(player, month, year)) {
            gui.setItem(items.getKeyPostion(), items.getItemStack());
            columns.add(items);
        }
        
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
        if (specifiedDate.compareTo(today) <= -1) {
            List<ItemStack> items = new LinkedList();
            List<KeyType> keys = new LinkedList();
            for (int i = 0;i < days[month - 1];i++) {
                SignInDate historicalDate = SignInDate.getInstance(year, month, i + 1);
                ItemStack key;
                KeyType keyType;
                if (playerdata.alreadySignIn(historicalDate)) {
                    try {
                        if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Data") != null) {
                            key = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Item").toUpperCase()), i + 1, (short) ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getInt(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Data"));
                        } else {
                            key = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Item").toUpperCase()), i + 1);
                        }
                    } catch (IllegalArgumentException ex) {
                        key = new ItemStack(Material.BARRIER, i + 1);
                    }
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Head-Owner") != null) {
                        PluginControl.setHead(key, replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Head-Owner"), "{player}", player.getName()));
                    }
                    ItemMeta im = key.getItemMeta();
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Lore") != null) {
                        List<String> lore = new ArrayList();
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{day}", String.valueOf(i + 1));
                        placeholders.put("{date}", historicalDate.getName(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
                        placeholders.put("{continuous}", continuous);
                        placeholders.put("{queue}", queue);
                        placeholders.put("{total-number}", totalNumber);
                        placeholders.put("{cards}", cards);
                        placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
                        placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
                        placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
                        placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
                        for (String lores : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Lore")) {
                            lore.add(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, lores, placeholders)));
                        }
                        im.setLore(lore);
                    }
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Enchantment") != null) {
                        for (String name : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Enchantment")) {
                            String[] data = name.split(":");
                            for (Enchantment enchant : Enchantment.values()) {
                                if (enchant.getName().equalsIgnoreCase(data[0])) {
                                    try {
                                        im.addEnchant(enchant, Integer.valueOf(data[1]), true);
                                    } catch (Exception ex) {}
                                }
                            }
                        }
                    }
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Display-Name") != null) im.setDisplayName(MessageUtil.toColor(replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Already-SignIn.Display-Name"), "{day}", String.valueOf(i + 1))));
                    key.setItemMeta(im);
                    keyType = KeyType.Already_SignIn;
                } else {
                    try {
                        if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Data") != null) {
                            key = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Item").toUpperCase()), i + 1, (short) ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getInt(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Data"));
                        } else {
                            key = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Item").toUpperCase()), i + 1);
                        }
                    } catch (IllegalArgumentException ex) {
                        key = new ItemStack(Material.BARRIER, i + 1);
                    }
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Head-Owner") != null) {
                        PluginControl.setHead(key, replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Head-Owner"), "{player}", player.getName()));
                    }
                    ItemMeta im = key.getItemMeta();
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Lore") != null) {
                        List<String> lore = new ArrayList();
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{day}", String.valueOf(i + 1));
                        placeholders.put("{date}", historicalDate.getName(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
                        placeholders.put("{continuous}", continuous);
                        placeholders.put("{queue}", queue);
                        placeholders.put("{total-number}", totalNumber);
                        placeholders.put("{cards}", cards);
                        placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
                        placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
                        placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
                        placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
                        for (String lores : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Lore")) {
                            lore.add(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, lores, placeholders)));
                        }
                        im.setLore(lore);
                    }
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Enchantment") != null) {
                        for (String name : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Enchantment")) {
                            String[] data = name.split(":");
                            for (Enchantment enchant : Enchantment.values()) {
                                if (enchant.getName().equalsIgnoreCase(data[0])) {
                                    try {
                                        im.addEnchant(enchant, Integer.valueOf(data[1]), true);
                                    } catch (Exception ex) {}
                                }
                            }
                        }
                    }
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Display-Name") != null) im.setDisplayName(MessageUtil.toColor(replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Missed-SignIn.Display-Name"), "{day}", String.valueOf(i + 1))));
                    key.setItemMeta(im);
                    keyType = KeyType.Missed_SignIn;
                }
                items.add(key);
                keys.add(keyType);
            }
            if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Slots") != null) {
                int i = 0;
                for (String slots : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Slots")) {
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
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Data") != null) {
                        key = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Item").toUpperCase()), i + 1, (short) ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getInt(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Data"));
                    } else {
                        key = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Item").toUpperCase()), i + 1);
                    }
                } catch (IllegalArgumentException ex) {
                    key = new ItemStack(Material.BARRIER, i + 1);
                }
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Head-Owner") != null) {
                    PluginControl.setHead(key, replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Head-Owner"), "{player}", player.getName()));
                }
                ItemMeta im = key.getItemMeta();
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Lore") != null) {
                    List<String> lore = new ArrayList();
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{day}", String.valueOf(i + 1));
                    placeholders.put("{continuous}", continuous);
                    placeholders.put("{queue}", queue);
                    placeholders.put("{total-number}", totalNumber);
                    placeholders.put("{cards}", cards);
                    placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
                    placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
                    placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
                    placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
                    for (String lores : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Lore")) {
                        lore.add(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, lores, placeholders)));
                    }
                    im.setLore(lore);
                }
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Enchantment") != null) {
                    for (String name : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Enchantment")) {
                        String[] data = name.split(":");
                        for (Enchantment enchant : Enchantment.values()) {
                            if (enchant.getName().equalsIgnoreCase(data[0])) {
                                try {
                                    im.addEnchant(enchant, Integer.valueOf(data[1]), true);
                                } catch (Exception ex) {}
                            }
                        }
                    }
                }
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Hide-Enchants") != null) PluginControl.hideEnchants(im);
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Display-Name") != null) im.setDisplayName(MessageUtil.toColor(replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Comming-Soon.Display-Name"), "{day}", String.valueOf(i + 1))));
                key.setItemMeta(im);
                items.add(key);
                keys.add(KeyType.Comming_Soon);
            }
            if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Slots") != null) {
                int i = 0;
                for (String slots : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key.Slots")) {
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
        SignInDate now = SignInDate.getInstance(new Date());
        int year = now.getYear();
        return getKey(player, month, year);
    }
    
    /**
     * Return key buttons.
     * @param player 
     * @return 
     */
    public static Set<SignInGUIColumn> getKey(Player player) {
        SignInDate now = SignInDate.getInstance(new Date());
        int year = now.getYear();
        int month = now.getMonth();
        return getKey(player, month, year);
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
        int nextPageMonth = getNextPageMonth(month);
        int nextPageYear = getNextPageYear(month, year);
        int previousPageMonth = getPreviousPageMonth(month);
        int previousPageYear = getPreviousPageYear(month, year);
        if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others") != null) {
            for (String items : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others").getKeys(false)) {
                ItemStack other;
                try {
                    if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Data") != null) {
                        other = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Item").toUpperCase()), 1, (short) ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getInt(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Data"));
                    } else {
                        other = new ItemStack(Material.valueOf(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Item").toUpperCase()), 1);
                    }
                } catch (IllegalArgumentException ex) {
                    other = new ItemStack(Material.BARRIER);
                }
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Head-Owner") != null) {
                    PluginControl.setHead(other, replace(player, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Head-Owner"), "{player}", player.getName()));
                }
                ItemMeta im = other.getItemMeta();
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Lore") != null) {
                    List<String> lore = new ArrayList();
                    Map<String, String> placeholders = new HashMap();
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
                    for (String lores : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Lore")) {
                        lore.add(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, lores, placeholders)));
                    }
                    im.setLore(lore);
                }
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Enchantment") != null) {
                    for (String name : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Enchantment")) {
                        String[] data = name.split(":");
                        for (Enchantment enchant : Enchantment.values()) {
                            if (enchant.getName().equalsIgnoreCase(data[0])) {
                                try {
                                    im.addEnchant(enchant, Integer.valueOf(data[1]), true);
                                } catch (Exception ex) {}
                            }
                        }
                    }
                }
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Hide-Enchants") != null) PluginControl.hideEnchants(im);
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Display-Name") != null) im.setDisplayName(MessageUtil.toColor(MessageUtil.toPlaceholderAPIResult(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Display-Name"), player)));
                other.setItemMeta(im);
                other.setAmount(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Amount") != null ? ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getInt(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Amount") : 1);
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Slots") != null) {
                    for (int slot : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getIntegerList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Slots")) {
                        set.add(new SignInGUIColumn(other, slot, items));
                    }
                }
                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).get(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Slot") != null) {
                    set.add(new SignInGUIColumn(other, ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getInt(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Slot"), items));
                }
            }
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
    
    public static String replace(Player player, String text, String target, String replacement) {
        Map<String, String> placeholders = new HashMap();
        placeholders.put(target, replacement);
        return MessageUtil.replacePlaceholders(player, text, placeholders);
    }
}
