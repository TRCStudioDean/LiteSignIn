package studio.trc.bukkit.litesignin.event;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.event.custom.SignInGUICloseEvent;
import studio.trc.bukkit.litesignin.event.custom.SignInGUIOpenEvent;
import studio.trc.bukkit.litesignin.gui.SignInGUIColumn;
import studio.trc.bukkit.litesignin.gui.SignInGUI;
import studio.trc.bukkit.litesignin.gui.SignInInventory;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.SignInPluginUtils;
import studio.trc.bukkit.litesignin.queue.SignInQueue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Menu
    implements Listener
{
    public static final Map<UUID, SignInInventory> menuOpening = new HashMap();
    
    public static void openGUI(Player player) {
        Thread thread = new Thread(() -> {
            SignInInventory inventory = SignInGUI.getGUI(player);
            SignInGUIOpenEvent event = new SignInGUIOpenEvent(player, inventory);
            callEvent(event, player, inventory);
        }, "LiteSignIn-OpeningGUI");
        thread.start();
    }
    
    public static void openGUI(Player player, int month) {
        Thread thread = new Thread(() -> {
            SignInInventory inventory = SignInGUI.getGUI(player, month);
            SignInGUIOpenEvent event = new SignInGUIOpenEvent(player, inventory, month);
            callEvent(event, player, inventory);
        }, "LiteSignIn-OpeningGUI");
        thread.start();
    }
    
    public static void openGUI(Player player, int month, int year) {
        Thread thread = new Thread(() -> {
            SignInInventory inventory = SignInGUI.getGUI(player, month, year);
            SignInGUIOpenEvent event = new SignInGUIOpenEvent(player, inventory, month, year);
            callEvent(event, player, inventory);
        }, "LiteSignIn-OpeningGUI");
        thread.start();
    }
    
    public static void callEvent(SignInGUIOpenEvent event, Player player, SignInInventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    player.openInventory(inventory.getInventory());
                    menuOpening.put(player.getUniqueId(), inventory);
                }
            }
        }.runTask(Main.getInstance());
    }
    
    @EventHandler
    public void click(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            if (menuOpening.get(player.getUniqueId()) != null) {
                if (BackupUtil.isBackingUp()) {
                    MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Database-Management.Backup.BackingUp");
                    player.closeInventory();
                    return;
                }
                e.setCancelled(true);
                Storage data = Storage.getPlayer(player);
                SignInInventory inv = menuOpening.get(player.getUniqueId());
                int nextPageMonth = inv.getNextPageMonth();
                int nextPageYear = inv.getNextPageYear();
                int previousPageMonth = inv.getPreviousPageMonth();
                int previousPageYear = inv.getPreviousPageYear();
                ItemStack item = e.getCurrentItem();
                for (SignInGUIColumn columns : inv.getButtons()) {
                    if (columns.getItemStack().equals(item)) {
                        if (columns.isKey()) {
                            SignInDate today = SignInDate.getInstance(new Date());
                            if (columns.getDate().equals(today) && !data.alreadySignIn()) {
                                data.signIn();
                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
                                placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
                                placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
                                placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
                                placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.SignIn-Messages", placeholders);
                                openGUI(player);
                            } else if (PluginControl.enableRetroactiveCard()) {
                                if (!SignInPluginUtils.hasPermission(player, "Retroactive-Card.Hold") && data.getRetroactiveCard() > 0) {
                                    data.takeRetroactiveCard(data.getRetroactiveCard());
                                    MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Unable-To-Hold");
                                } else if (!SignInPluginUtils.hasPermission(player, "Retroactive-Card.Use")) {
                                    MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.No-Permission");
                                } else if (today.compareTo(columns.getDate()) >= 0 && !data.alreadySignIn(columns.getDate())) {
                                    if (PluginControl.getRetroactiveCardMinimumDate() != null && columns.getDate().compareTo(PluginControl.getRetroactiveCardMinimumDate()) < 0) {
                                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                        placeholders.put("{date}", PluginControl.getRetroactiveCardMinimumDate().getName(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
                                        MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Minimum-Date", placeholders);
                                    } else if (data.isRetroactiveCardCooldown()) {
                                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                        placeholders.put("{second}", String.valueOf(data.getRetroactiveCardCooldown()));
                                        MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Retroactive-Card-Cooldown", placeholders);
                                    } else if (data.getRetroactiveCard() >= PluginControl.getRetroactiveCardQuantityRequired()) {
                                        data.takeRetroactiveCard(PluginControl.getRetroactiveCardQuantityRequired());
                                        data.signIn(columns.getDate());
                                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                        placeholders.put("{date}", columns.getDate().getName(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
                                        placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                        placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                        MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Retroactive-SignIn-Messages", placeholders);
                                        openGUI(player, columns.getDate().getMonth(), columns.getDate().getYear());
                                    } else {
                                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                        placeholders.put("{cards}", String.valueOf(PluginControl.getRetroactiveCardQuantityRequired() - data.getRetroactiveCard()));
                                        MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Need-More-Retroactive-Cards", placeholders);
                                    }
                                }
                            } else {
                                MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Unable-To-Re-SignIn");
                            }
                            if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getBoolean(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + columns.getKeyType().getSectionName() + ".Close-GUI")) {
                                player.closeInventory();
                            }
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                            placeholders.put("{dateText}", columns.getDate().getDataText(false));
                            placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
                            placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
                            placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
                            placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
                            placeholders.put("{player}", player.getName());
                            if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).contains(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + columns.getKeyType().getSectionName() + ".Commands")) {
                                for (String commands : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + columns.getKeyType().getSectionName() + ".Commands")) {
                                    runCommand(player, commands, placeholders);
                                }
                            }
                            if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).contains(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + columns.getKeyType().getSectionName() + ".Messages")) {
                                for (String message : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + columns.getKeyType().getSectionName() + ".Messages")) {
                                    player.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, message, placeholders)));
                                }
                            }
                        } else {
                            if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).contains(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + columns.getButtonName())) {
                                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getBoolean(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + columns.getButtonName() + ".Close-GUI")) {
                                    player.closeInventory();
                                }
                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
                                placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
                                placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
                                placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
                                placeholders.put("{player}", player.getName());
                                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).contains(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + columns.getButtonName() + ".Commands")) {
                                    for (String commands : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + columns.getButtonName() + ".Commands")) {
                                        runCommand(player, commands, placeholders);
                                    }
                                }
                                if (ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).contains(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + columns.getButtonName() + ".Messages")) {
                                    for (String message : ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getStringList(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + columns.getButtonName() + ".Messages")) {
                                        player.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, message, placeholders)));
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void close(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player) {
            Player player = (Player) e.getPlayer();
            if (menuOpening.get(player.getUniqueId()) != null) {
                menuOpening.remove(player.getUniqueId());
                Bukkit.getPluginManager().callEvent(new SignInGUICloseEvent(player));
            }
        }
    }
    
    public void runCommand(Player player, String commands, Map<String, String> placeholders) {
        if (commands.toLowerCase().startsWith("server:")) {
            Main.getInstance().getServer().dispatchCommand(Bukkit.getConsoleSender(), MessageUtil.toColor(MessageUtil.replacePlaceholders(player, commands.substring(7), placeholders)));
        } else if (commands.toLowerCase().startsWith("op:")) {
            String command = MessageUtil.toColor(MessageUtil.replacePlaceholders(player, commands.substring(3), placeholders));
            if (player.isOp()) {
                player.performCommand(command);
            } else {
                player.setOp(true);
                try {
                    player.performCommand(command);
                } catch (Throwable error) {
                    error.printStackTrace();
                }
                player.setOp(false);
            }
        } else {
            player.performCommand(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, commands, placeholders)));
        }
    }
}
