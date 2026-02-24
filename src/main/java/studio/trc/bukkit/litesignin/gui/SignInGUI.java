package studio.trc.bukkit.litesignin.gui;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import studio.trc.bukkit.litesignin.message.color.ColorUtils;
import studio.trc.bukkit.litesignin.util.SkullManager;

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
        List<SignInGUIColumn> columns = new ArrayList<>();
        
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
        List<SignInGUIColumn> columns = new ArrayList<>();
        
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
        List<SignInGUIColumn> columns = new ArrayList<>();
        
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
    
    private static ItemStack getButtonItem(Player player, Map<String, String> placeholders, ConfigurationSection section, String path, int amount) {
        ItemStack result;
        try {
            if (section.get(path + ".Data") != null) {
                result = new ItemStack(Material.valueOf(section.getString(path + ".Item").toUpperCase()), amount, (short) section.getInt(path + ".Data"));
            } else {
                result = new ItemStack(Material.valueOf(section.getString(path + ".Item").toUpperCase()), amount);
            }
        } catch (IllegalArgumentException ex) {
            result = new ItemStack(Material.BARRIER, amount);
        }
        if (section.get(path + ".Head-Owner") != null) {
            PluginControl.setHead(result, replace(player, section.getString(path + ".Head-Owner"), "{player}", player.getName()));
        }
        if (section.get(path + ".Amount") != null) {
            result.setAmount(Integer.valueOf(MessageUtil.replacePlaceholders(player, section.getString(path + ".Amount"), placeholders)));
        }
        if (section.get(path + ".Head-Textures") != null) {
            setHeadTextures(player, placeholders, section, path, result);
        }
        if (section.get(path + ".Custom-Model-Data") != null) {
            setCustomModelData(player, placeholders, section, path, result);
        }
        if (section.get(path + ".Item-Model") != null) {
            setItemModel(player, placeholders, section, path, result);
        }
        if (section.get(path + ".Enchantment") != null) {
            setEnchantments(section, path, result);
        }
        ItemMeta im = result.getItemMeta();
        if (section.get(path + ".Lore") != null) {
            List<String> lore = new ArrayList<>();
            section.getStringList(path + ".Lore").stream().forEach(lores -> lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders)));
            im.setLore(lore);
        }
        if (section.get(path + ".Hide-Enchants") != null) {
            PluginControl.hideEnchants(im);
        }
        if (section.get(path + ".Display-Name") != null) {
            im.setDisplayName(MessageUtil.replacePlaceholders(player, section.getString(path + ".Display-Name"), placeholders));
        }
        result.setItemMeta(im);
        return result;
    }
    
    /**
     * Get calender's key buttons.
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
            List<ItemStack> items = new ArrayList<>();
            List<KeyType> keys = new ArrayList<>();
            for (int i = 0;i < days[month - 1];i++) {
                SignInDate historicalDate = SignInDate.getInstance(year, month, i + 1);
                Map<String, String> placeholders = getPlaceholdersOfKeyButton(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                ItemStack key;
                KeyType keyType;
                if (playerdata.alreadySignIn(historicalDate)) {
                    key = getButtonItem(player, placeholders, section, "Already-SignIn", i + 1);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    key = getButtonItem(player, placeholders, section, "Missed-SignIn", i + 1);
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
            List<ItemStack> items = new ArrayList<>();
            List<KeyType> keys = new ArrayList<>();
            for (int i = 0;i < days[month - 1];i++) {
                SignInDate historicalDate = SignInDate.getInstance(year, month, i + 1);
                Map<String, String> placeholders = getPlaceholdersOfKeyButton(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);
                items.add(getButtonItem(player, placeholders, section, "Comming-Soon", i + 1));
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
     * Get calender's key buttons.
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
        List<ItemStack> items = new ArrayList<>();
        List<KeyType> keys = new ArrayList<>();
        for (int i = 0;i < days[month - 1];i++) {
            SignInDate date = SignInDate.getInstance(year, month, i + 1);
            Map<String, String> placeholders = getPlaceholdersOfKeyButton(i, continuous, queue, totalNumber, cards, nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, date);
            if (i + 1 < day) {
                ItemStack key;
                KeyType keyType;
                if (playerdata.alreadySignIn(date)) {
                    key = getButtonItem(player, placeholders, section, "Already-SignIn", i + 1);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    key = getButtonItem(player, placeholders, section, "Missed-SignIn", i + 1);
                    keyType = KeyType.MISSED_SIGNIN;
                }
                items.add(key);
                keys.add(keyType);
            } else if (i + 1 == day) {
                ItemStack key;
                KeyType keyType;
                if (playerdata.alreadySignIn(date)) {
                    key = getButtonItem(player, placeholders, section, "Already-SignIn", i + 1);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    key = getButtonItem(player, placeholders, section, "Nothing-SignIn", i + 1);
                    keyType = KeyType.NOTHING_SIGNIN;
                }
                items.add(key);
                keys.add(keyType);
            } else if (i + 1 > day) {
                items.add(getButtonItem(player, placeholders, section, "Comming-Soon", i + 1));
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
                ItemStack other = getButtonItem(player, placeholders, section, items, 1);
                if (section.get(items + ".Slots") != null) {
                    section.getIntegerList(items + ".Slots").stream().forEach((slot) -> set.add(new SignInGUIColumn(other, slot, items)));
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
    
    private static Map<String, String> getPlaceholdersOfKeyButton(
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
    
    private static void setEnchantments(ConfigurationSection section, String configPath, ItemStack is) {
        ItemMeta im = is.getItemMeta();
        if (im == null) return;
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        for (String name : section.getStringList(configPath + ".Enchantments")) {
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
                            placeholders.put("{path}", configPath + "." + name);
                            LiteSignInProperties.sendOperationMessage("InvalidEnchantmentSetting", placeholders);
                        }
                    }
                }
                if (invalid) {
                    placeholders.put("{enchantment}", data[0]);
                    placeholders.put("{path}", configPath + "." + name);
                    LiteSignInProperties.sendOperationMessage("InvalidEnchantment", placeholders);
                }
            } catch (Exception ex) {
                placeholders.put("{path}", configPath + "." + name);
                LiteSignInProperties.sendOperationMessage("InvalidEnchantmentSetting", placeholders);
            }
        }
        is.setItemMeta(im);
    }
    
    private static void setCustomModelData(Player player, Map<String, String> placeholders, ConfigurationSection section, String configPath, ItemStack is) {
        String version = Bukkit.getBukkitVersion();
        if (version.startsWith("1.7") ||
            version.startsWith("1.8") ||
            version.startsWith("1.9") ||
            version.startsWith("1.10") ||
            version.startsWith("1.11") ||
            version.startsWith("1.12") || 
            version.startsWith("1.13")) return;
        ItemMeta im = is.getItemMeta();
        if (im == null) return;
        String name = MessageUtil.replacePlaceholders(player, section.getString(configPath + ".Custom-Model-Data"), placeholders);
        if (name == null) return;
        try {
            im.setCustomModelData(Integer.valueOf(name));
        } catch (Exception ex) {
            placeholders.put("{data}", name);
            placeholders.put("{path}", configPath + "." + name);
            LiteSignInProperties.sendOperationMessage("InvalidCustomModelData", placeholders);
        }
        is.setItemMeta(im);
    }
    
    private static void setItemModel(Player player, Map<String, String> placeholders, ConfigurationSection section, String configPath, ItemStack is) {
        String version = Bukkit.getBukkitVersion();
        if (version.startsWith("1.7") ||
            version.startsWith("1.8") ||
            version.startsWith("1.9") ||
            version.startsWith("1.10") ||
            version.startsWith("1.11") ||
            version.startsWith("1.12") || 
            version.startsWith("1.13") || 
            version.startsWith("1.14") || 
            version.startsWith("1.15") || 
            version.startsWith("1.16") || 
            version.startsWith("1.17") || 
            version.startsWith("1.18") || 
            version.startsWith("1.19")) {
            return;
        }
        ItemMeta im = is.getItemMeta();
        if (im == null) return;
        String name = MessageUtil.replacePlaceholders(player, section.getString(configPath + ".Item-Model"), placeholders);
        if (name == null) return;
        String[] modelInfo = name.split(":");
        try {
            Method method = im.getClass().getMethod("setItemModel", NamespacedKey.class);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (modelInfo.length == 2) {
                method.invoke(im, new NamespacedKey(modelInfo[0], modelInfo[1]));
            } else {
                method.invoke(im, NamespacedKey.minecraft(modelInfo[0]));
            }
        } catch (Exception ex) {
            placeholders.put("{data}", name);
            placeholders.put("{path}", configPath + "." + name);
            LiteSignInProperties.sendOperationMessage("InvalidItemModel", placeholders);
        }
        is.setItemMeta(im);
    }
    
    private static void setHeadTextures(Player player, Map<String, String> placeholders, ConfigurationSection section, String configPath, ItemStack is) {
        String version = Bukkit.getBukkitVersion();
        if (version == null || version.startsWith("1.7")) return;
        ItemMeta im = is.getItemMeta();
        if (im == null) return;
        String textures = MessageUtil.replacePlaceholders(player, section.getString(configPath + ".Head-Textures"), placeholders);
        if (textures == null) return;
        if (is.getItemMeta() instanceof SkullMeta) {
            is.setItemMeta(SkullManager.getHeadWithTextures(textures).getItemMeta());
        }
    }
}
