package studio.trc.bukkit.litesignin.command;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.config.MessageUtil;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.database.util.RollBackUtil;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.event.Menu;
import studio.trc.bukkit.litesignin.nms.JsonItemStack;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.queue.SignInQueueElement;
import studio.trc.bukkit.litesignin.util.Updater;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.CustomItem;
import studio.trc.bukkit.litesignin.util.PluginControl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SignInCommand
    implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (BackupUtil.isBackingUp()) {
            MessageUtil.sendMessage(sender, "Database-Management.Backup.BackingUp");
            return true;
        }
        if (PluginControl.enableUpdater()) {
            String now = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String checkUpdateTime = new SimpleDateFormat("yyyy-MM-dd").format(Updater.getTimeOfLastCheckUpdate());
            if (!now.equals(checkUpdateTime)) {
                Updater.checkUpdate();
            }
        }
        if (args.length == 0) {
            MessageUtil.sendMessage(sender, "Command-Messages.Unknown-Command");
        } else if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Help")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return true;
                }
                MessageUtil.sendMessage(sender, "Command-Messages.Help-Command");
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Reload")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return true;
                }
                PluginControl.reload();
                MessageUtil.sendMessage(sender, "Command-Messages.Reload");
            } else if (args[0].equalsIgnoreCase("gui")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.GUI")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return true;
                }
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length == 1) {
                        Menu.openGUI(player);
                        MessageUtil.sendMessage(player, "Command-Messages.GUI.Normal");
                    } else if (args.length == 2) {
                        if (!PluginControl.hasPermission(sender, "Permissions.Commands.Designated-GUI")) {
                            MessageUtil.sendMessage(sender, "No-Permission");
                            return true;
                        }
                        for (int i = 1;i <= 12;i++) {
                            if (args[1].equals(String.valueOf(i))) {
                                Menu.openGUI(player, i);
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{month}", String.valueOf(i));
                                MessageUtil.sendMessage(player, "Command-Messages.GUI.Normal", placeholders);
                                return true;
                            }
                        }
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{month}", args[1]);
                        MessageUtil.sendMessage(player, "Command-Messages.GUI.Invalid-Month", placeholders);
                    } else if (args.length >= 3) {
                        if (!PluginControl.hasPermission(sender, "Permissions.Commands.Designated-GUI")) {
                            MessageUtil.sendMessage(sender, "No-Permission");
                            return true;
                        }
                        int month = SignInDate.getInstance(new Date()).getMonth();
                        boolean invalidMonth = true;
                        for (int i = 1;i <= 12;i++) {
                            if (args[1].equals(String.valueOf(i))) {
                                month = i;
                                invalidMonth = false;
                            }
                        }
                        if (invalidMonth) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{month}", args[1]);
                            MessageUtil.sendMessage(player, "Command-Messages.GUI.Invalid-Month", placeholders);
                            return true;
                        }
                        int year;
                        try {
                            year = Integer.valueOf(args[2]);
                        } catch (NumberFormatException ex) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{year}", args[2]);
                            MessageUtil.sendMessage(player, "Command-Messages.GUI.Invalid-Year", placeholders);
                            return true;
                        }
                        if (year < 1970 || year > SignInDate.getInstance(new Date()).getYear()) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{year}", args[2]);
                            MessageUtil.sendMessage(player, "Command-Messages.GUI.Invalid-Year", placeholders);
                            return true;
                        }
                        Menu.openGUI(player, month, year);
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{month}", String.valueOf(month));
                        placeholders.put("{year}", String.valueOf(year));
                        MessageUtil.sendMessage(player, "Command-Messages.GUI.Specified-Year", placeholders);
                        return true;
                    }
                } else {
                    MessageUtil.sendMessage(sender, "Unavailable-Command");
                }
            } else if (args[0].equalsIgnoreCase("click")) {
                if (args.length == 1) {
                    if (sender instanceof Player) {
                        if (!PluginControl.hasPermission(sender, "Permissions.Commands.Click")) {
                            MessageUtil.sendMessage(sender, "No-Permission");
                            return true;
                        }
                        Player player = (Player) sender;
                        Storage data = Storage.getPlayer(player);
                        if (data.alreadySignIn()) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                            placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                            MessageUtil.sendMessage(player, "Command-Messages.Click.To-Self.Today-has-been-Signed-In", placeholders);
                        } else {
                            data.signIn();
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                            placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                            MessageUtil.sendMessage(player, "Command-Messages.Click.To-Self.Successfully-Signed-In", placeholders);
                        }
                    } else {
                        MessageUtil.sendMessage(sender, "Unavailable-Command");
                    }
                } else if (args.length == 2) {
                    SignInDate date = SignInDate.getInstance(args[1]);
                    if (date == null) {
                        if (!PluginControl.hasPermission(sender, "Permissions.Commands.Click-Others")) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{date}", args[1]);
                            MessageUtil.sendMessage(sender, "Command-Messages.Click.Invalid-Date", placeholders);
                            return true;
                        }
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player != null) {
                            Storage data = Storage.getPlayer(player);
                            if (data.alreadySignIn()) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", player.getName());
                                placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Today-has-been-Signed-In", placeholders);
                            } else {
                                data.signIn();
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", player.getName());
                                placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Successfully-Signed-In", placeholders);
                                Map<String, String> placeholders_2 = new HashMap();
                                placeholders_2.put("{admin}", sender.getName());
                                placeholders_2.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                placeholders_2.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                MessageUtil.sendMessage(player, "Command-Messages.Click.To-Other-Player.Messages-at-Sign-In", placeholders_2);
                            }
                        } else {
                            if (args[1].isEmpty()) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", args[1]);
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.Player-Not-Exist", placeholders);
                                return true;
                            }
                            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[1]);
                            if (offlineplayer != null) {
                                Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                                if (data.alreadySignIn()) {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{player}", offlineplayer.getName());
                                    placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                    placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                    MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Today-has-been-Signed-In", placeholders);
                                } else {
                                    data.signIn();
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{player}", offlineplayer.getName());
                                    placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                    placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                    MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Successfully-Signed-In", placeholders);
                                }
                            } else {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", args[1]);
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.Player-Not-Exist", placeholders);
                            }
                        }
                    } else {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            SignInDate today = SignInDate.getInstance(new Date());
                            if (date.equals(today)) {
                                player.performCommand("litesignin click");
                                return true;
                            }
                            if (!PluginControl.hasPermission(sender, "Permissions.Commands.Click") || !PluginControl.hasPermission(sender, "Permissions.Retroactive-Card.Use")) {
                                MessageUtil.sendMessage(sender, "No-Permission");
                                return true;
                            }
                            if (!PluginControl.enableRetroactiveCard()) {
                                MessageUtil.sendMessage(sender, "Unable-To-Re-SignIn");
                                return true;
                            }
                            Storage data = Storage.getPlayer(player);
                            if (!PluginControl.hasPermission(player, "Permissions.Retroactive-Card.Hold") && data.getRetroactiveCard() > 0) {
                                data.takeRetroactiveCard(data.getRetroactiveCard());
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Self.Unable-To-Hold");
                            } else if (data.isRetroactiveCardCooldown()) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{second}", String.valueOf(data.getRetroactiveCardCooldown()));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Self.Retroactive-Card-Cooldown", placeholders);
                            } else if (SignInDate.getInstance(new Date()).compareTo(date) < 0) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.Invalid-Date", placeholders);
                            } else if (PluginControl.getRetroactiveCardMinimumDate() != null && date.compareTo(PluginControl.getRetroactiveCardMinimumDate()) < 0) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{date}", PluginControl.getRetroactiveCardMinimumDate().getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Self.Minimum-Date", placeholders);
                            } else if (data.alreadySignIn(date)) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Self.Specified-Date-has-been-Signed-In", placeholders);
                            } else if (data.getRetroactiveCard() >= PluginControl.getRetroactiveCardQuantityRequired()) {
                                data.takeRetroactiveCard(PluginControl.getRetroactiveCardQuantityRequired());
                                data.signIn(date);
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                                placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Self.Successfully-Retroactive-Signed-In", placeholders);
                            } else {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                                placeholders.put("{cards}", String.valueOf(PluginControl.getRetroactiveCardQuantityRequired() - data.getRetroactiveCard()));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Self.Need-More-Retroactive-Cards", placeholders);
                            }
                        } else {
                            MessageUtil.sendMessage(sender, "Unavailable-Command");
                        }
                    }
                } else if (args.length >= 3) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.Click-Others")) {
                        MessageUtil.sendMessage(sender, "No-Permission");
                        return true;
                    }
                    SignInDate date = SignInDate.getInstance(args[1]);
                    Player player = Bukkit.getPlayer(args[2]);
                    if (date == null) {
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{date}", args[1]);
                        MessageUtil.sendMessage(sender, "Command-Messages.Click.Invalid-Date", placeholders);
                        return true;
                    }
                    SignInDate today = SignInDate.getInstance(new Date());
                    if (player != null) {
                        if (date.equals(today)) {
                            if (sender instanceof Player) {
                                ((Player) sender).performCommand("litesignin click " + player.getName());
                            } else {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litesignin click " + player.getName());
                            }
                            return true;
                        }
                        Storage data = Storage.getPlayer(player);
                        if (PluginControl.getRetroactiveCardMinimumDate() != null && date.compareTo(PluginControl.getRetroactiveCardMinimumDate()) < 0) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{date}", PluginControl.getRetroactiveCardMinimumDate().getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                            MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Minimum-Date", placeholders);
                        } else if (data.alreadySignIn(date)) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                            placeholders.put("{player}", player.getName());
                            MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Specified-Date-has-been-Signed-In", placeholders);
                        } else if (SignInDate.getInstance(new Date()).compareTo(date) < 0) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                            MessageUtil.sendMessage(sender, "Command-Messages.Click.Invalid-Date", placeholders);
                        } else {
                            data.signIn(date);
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                            placeholders.put("{player}", player.getName());
                            placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                            placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                            MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Successfully-Retroactive-Signed-In", placeholders);
                            Map<String, String> placeholders_2 = new HashMap();
                            placeholders_2.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                            placeholders_2.put("{admin}", sender.getName());
                            placeholders_2.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                            placeholders_2.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                            MessageUtil.sendMessage(player, "Command-Messages.Click.To-Other-Player.Messages-at-Retroactive-Sign-In", placeholders_2);
                        }
                    } else {
                        if (args[2].isEmpty()) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{player}", args[2]);
                            MessageUtil.sendMessage(sender, "Command-Messages.Click.Player-Not-Exist", placeholders);
                            return true;
                        }
                        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                        if (date.equals(today)) {
                            if (sender instanceof Player) {
                                ((Player) sender).performCommand("litesignin click " + offlineplayer.getName());
                            } else {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litesignin click " + offlineplayer.getName());
                            }
                            return true;
                        }
                        if (!PluginControl.enableRetroactiveCard()) {
                            MessageUtil.sendMessage(sender, "Unable-To-Re-SignIn");
                            return true;
                        }
                        if (offlineplayer != null) {
                            Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                            if (data.alreadySignIn(date)) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", offlineplayer.getName());
                                placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                                placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Specified-Date-has-been-Signed-In", placeholders);
                            } else if (SignInDate.getInstance(new Date()).compareTo(date) < 0) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.Invalid-Date", placeholders);
                            } else {
                                data.signIn(date);
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", offlineplayer.getName());
                                placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                                placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                                placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                                MessageUtil.sendMessage(sender, "Command-Messages.Click.To-Other-Player.Successfully-Retroactive-Signed-In", placeholders);
                            }
                        } else {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{player}", args[2]);
                            MessageUtil.sendMessage(sender, "Command-Messages.Click.Player-Not-Exist", placeholders);
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("retroactivecard")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.RetroactiveCard")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return true;
                }
                if (!PluginControl.enableRetroactiveCard()) {
                    MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Unavailable-Feature");
                }
                if (args.length < 3) {
                    MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Help");
                } else {
                    Player player;
                    if (args.length == 3) {
                        if (!(sender instanceof Player)) {
                            MessageUtil.sendMessage(sender, "Unavailable-Command");
                            return true;
                        } else {
                            player = (Player) sender;
                        }
                    } else {
                        player = Bukkit.getPlayer(args[3]);
                    }
                    if (args[1].equalsIgnoreCase("give")) {
                        try {
                            int number = Integer.valueOf(args[2]);
                            if (player != null) {
                                Storage data = Storage.getPlayer(player);
                                data.giveRetroactiveCard(number);
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", player.getName());
                                placeholders.put("{amount}", String.valueOf(number));
                                MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Give", placeholders);
                            } else {
                                if (PluginControl.enableRetroactiveCardRequiredItem()) {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{player}", args[3]);
                                    MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Player-Offline", placeholders);
                                    return true;
                                } else {
                                    if (args[3].isEmpty()) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", args[3]);
                                        MessageUtil.sendMessage(sender, "Command-Messages.Click.Player-Not-Exist", placeholders);
                                        return true;
                                    }
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[3]);
                                    if (offlineplayer != null) {
                                        Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                                        data.setRetroactiveCard(data.getRetroactiveCard() + number, true);
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", offlineplayer.getName());
                                        placeholders.put("{amount}", String.valueOf(number));
                                        MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Give", placeholders);
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", args[3]);
                                        MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Player-Not-Exist", placeholders);
                                    }
                                }
                            }
                        } catch (NumberFormatException ex) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{number}", args[2]);
                            MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Invalid-Number", placeholders);
                        }
                    } else if (args[1].equalsIgnoreCase("set")) {
                        try {
                            int number = Integer.valueOf(args[2]);
                            if (player != null) {
                                Storage data = Storage.getPlayer(player);
                                data.setRetroactiveCard(number, true);
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", player.getName());
                                placeholders.put("{amount}", String.valueOf(number));
                                MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Set", placeholders);
                            } else {
                                if (PluginControl.enableRetroactiveCardRequiredItem()) {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{player}", args[3]);
                                    MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Player-Offline", placeholders);
                                    return true;
                                } else {
                                    if (args[3].isEmpty()) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", args[3]);
                                        MessageUtil.sendMessage(sender, "Command-Messages.Click.Player-Not-Exist", placeholders);
                                        return true;
                                    }
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[3]);
                                    if (offlineplayer != null) {
                                        Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                                        data.setRetroactiveCard(number, true);
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", offlineplayer.getName());
                                        placeholders.put("{amount}", String.valueOf(number));
                                        MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Set", placeholders);
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", args[3]);
                                        MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Player-Not-Exist", placeholders);
                                    }
                                }
                            }
                        } catch (NumberFormatException ex) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{number}", args[2]);
                            MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Invalid-Number", placeholders);
                        }
                    } else if (args[1].equalsIgnoreCase("take")) {
                        try {
                            int number = Integer.valueOf(args[2]);
                            if (player != null) {
                                Storage data = Storage.getPlayer(player);
                                data.takeRetroactiveCard(number);
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", player.getName());
                                placeholders.put("{number}", String.valueOf(number));
                                MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Take", placeholders);
                            } else {
                                if (PluginControl.enableRetroactiveCardRequiredItem()) {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{player}", args[3]);
                                    MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Player-Offline", placeholders);
                                    return true;
                                } else {
                                    if (args[3].isEmpty()) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", args[3]);
                                        MessageUtil.sendMessage(sender, "Command-Messages.Click.Player-Not-Exist", placeholders);
                                        return true;
                                    }
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[3]);
                                    if (offlineplayer != null) {
                                        Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                                        data.setRetroactiveCard(data.getRetroactiveCard() - number, true);
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", offlineplayer.getName());
                                        placeholders.put("{amount}", String.valueOf(number));
                                        MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Take", placeholders);
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", args[3]);
                                        MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Player-Not-Exist", placeholders);
                                    }
                                }
                            }
                        } catch (NumberFormatException ex) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{number}", args[2]);
                            MessageUtil.sendMessage(sender, "Command-Messages.RetroactiveCard.Invalid-Number", placeholders);
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("database")) {
                if (args.length == 1) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Database.Help");
                } else if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("backup")) {
                        if (!PluginControl.hasPermission(sender, "Permissions.Commands.Database.Backup")) {
                            MessageUtil.sendMessage(sender, "No-Permission");
                            return true;
                        }
                        if (!ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Backup.Enabled")) {
                            return true;
                        }
                        BaseComponent click = new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.Database.Confirm.Button.Text"), new HashMap())));
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/litesignin database confirm");
                        List<BaseComponent> hoverText = new ArrayList();
                        int end = 0;
                        List<String> array = MessageUtil.getMessageList("Command-Messages.Database.Confirm.Button.Hover");
                        for (String hover : array) {
                            end++;
                            hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, new HashMap()))));
                            if (end != array.size()) {
                                hoverText.add(new TextComponent("\n"));
                            }
                        }
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                        click.setClickEvent(ce);
                        click.setHoverEvent(he);
                        Map<String, BaseComponent> baseComponents = new HashMap();
                        baseComponents.put("%button%", click);
                        for (String message : MessageUtil.getMessageList("Command-Messages.Database.Confirm.Need-Confirm")) {
                            MessageUtil.sendJsonMessage(sender, MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, message, new HashMap())), baseComponents);
                        }
                        confirmCache.put(sender, Confirm.BACKUP);
                    } else if (args[1].equalsIgnoreCase("rollback")) {
                        if (!PluginControl.hasPermission(sender, "Permissions.Commands.Database.Rollback")) {
                            MessageUtil.sendMessage(sender, "No-Permission");
                            return true;
                        }
                        if (!ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Rollback.Enabled")) {
                            return true;
                        }
                        if (args.length == 2) {
                            MessageUtil.sendMessage(sender, "Command-Messages.Database.Rollback.Help");
                            return true;
                        }
                        File file = new File(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Database-Management.Rollback.Backup-Folder-Path") + args[2]);
                        if (!file.exists()) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{file}", ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Database-Management.Rollback.Backup-Folder-Path") + args[2]);
                            MessageUtil.sendMessage(sender, "Command-Messages.Database.Rollback.File-Not-Exist", placeholders);
                            return true;
                        }
                        BaseComponent click = new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.Database.Confirm.Button.Text"), new HashMap())));
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/litesignin database confirm");
                        List<BaseComponent> hoverText = new ArrayList();
                        int end = 0;
                        List<String> array = MessageUtil.getMessageList("Command-Messages.Database.Confirm.Button.Hover");
                        for (String hover : array) {
                            end++;
                            hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, new HashMap()))));
                            if (end != array.size()) {
                                hoverText.add(new TextComponent("\n"));
                            }
                        }
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                        click.setClickEvent(ce);
                        click.setHoverEvent(he);
                        Map<String, BaseComponent> baseComponents = new HashMap();
                        baseComponents.put("%button%", click);
                        for (String message : MessageUtil.getMessageList("Command-Messages.Database.Confirm.Need-Confirm")) {
                            MessageUtil.sendJsonMessage(sender, MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, message, new HashMap())), baseComponents);
                        }
                        Confirm confirm = Confirm.ROLLBACK;
                        confirm.setTargetFile(file);
                        confirmCache.put(sender, confirm);
                    } else if (args[1].equalsIgnoreCase("confirm")) {
                        if (confirmCache.containsKey(sender)) {
                            switch (confirmCache.get(sender)) {
                                case BACKUP: {
                                    MessageUtil.sendMessage(sender, "Command-Messages.Database.Backup");
                                    confirmCache.remove(sender);
                                    if (sender instanceof Player) {
                                        BackupUtil.startBackup(sender, Bukkit.getConsoleSender());
                                    } else {
                                        BackupUtil.startBackup(Bukkit.getConsoleSender());
                                    }
                                    break;
                                }
                                case ROLLBACK: {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{file}", confirmCache.get(sender).getTargetFile().getPath());
                                    MessageUtil.sendMessage(sender, "Command-Messages.Database.Rollback.Start-Rollback", placeholders);
                                    if (sender instanceof Player) {
                                        RollBackUtil.startRollBack(confirmCache.get(sender).getTargetFile(), ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Rollback.Rollback-With-Backup"), sender, Bukkit.getConsoleSender());
                                    } else {
                                        RollBackUtil.startRollBack(confirmCache.get(sender).getTargetFile(), ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Rollback.Rollback-With-Backup"), Bukkit.getConsoleSender());
                                    }
                                    confirmCache.remove(sender);
                                    break;
                                }
                            }
                        } else {
                            MessageUtil.sendMessage(sender, "Command-Messages.Database.Confirm.Invalid-Operation");
                        }
                    } else {
                        MessageUtil.sendMessage(sender, "Command-Messages.Database.Help");
                    }
                }
            } else if (args[0].equalsIgnoreCase("save")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Save")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return true;
                }
                PluginControl.savePlayerData();
                MessageUtil.sendMessage(sender, "Command-Messages.Save");
            } else if (args[0].equalsIgnoreCase("leaderboard")) {
                SignInDate today = SignInDate.getInstance(new Date());
                if (args.length == 1) {
                    if (!PluginControl.hasPermission(sender, "Permissions.Commands.LeaderBoard")) {
                        MessageUtil.sendMessage(sender, "No-Permission");
                        return true;
                    }
                    int page = 1;
                    int nosp = 10;
                    try {
                        nosp = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                    } catch (NumberFormatException ex) {}
                    sendLeaderBoard(sender, today, page, nosp);
                } else if (args.length == 2) {
                    SignInDate date = SignInDate.getInstance(args[1]);
                    if (date != null) {
                        if (!date.equals(today)) {
                            if (!PluginControl.hasPermission(sender, "Permissions.Commands.LeaderBoard.Designated-Date")) {
                                MessageUtil.sendMessage(sender, "No-Permission");
                                return true;
                            }
                            if (!PluginControl.useMySQLStorage() && !PluginControl.useSQLiteStorage()) {
                                MessageUtil.sendMessage(sender, "Command-Messages.LeaderBoard.Non-Database-mode");
                                return true;
                            }
                            int page = 1;
                            int nosp = 10;
                            try {
                                nosp = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                            } catch (NumberFormatException ex) {}
                            sendLeaderBoard(sender, date, page, nosp);
                        } else {
                            if (!PluginControl.hasPermission(sender, "Permissions.Commands.LeaderBoard")) {
                                MessageUtil.sendMessage(sender, "No-Permission");
                                return true;
                            }
                            int page = 1;
                            int nosp = 10;
                            try {
                                nosp = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                            } catch (NumberFormatException ex) {}
                            sendLeaderBoard(sender, date, page, nosp);
                        }
                    } else {
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{date}", args[1]);
                        MessageUtil.sendMessage(sender, "Command-Messages.LeaderBoard.Date-Not-Exist", placeholders);
                    }
                } else if (args.length >= 3) {
                    SignInDate date = SignInDate.getInstance(args[1]);
                    if (date != null) {
                        if (!date.equals(today)) {
                            if (!PluginControl.hasPermission(sender, "Permissions.Commands.LeaderBoard.Designated-Date")) {
                                MessageUtil.sendMessage(sender, "No-Permission");
                                return true;
                            }
                            if (!PluginControl.useMySQLStorage() && !PluginControl.useSQLiteStorage()) {
                                MessageUtil.sendMessage(sender, "Command-Messages.LeaderBoard.Non-Database-mode");
                                return true;
                            }
                            int page;
                            int nosp = 10;
                            try {
                                page = Integer.valueOf(args[2]);
                            } catch (NumberFormatException ex) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{page}", args[2]);
                                MessageUtil.sendMessage(sender, "Command-Messages.LeaderBoard.Invalid-Number", placeholders);
                                return true;
                            }
                            try {
                                nosp = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                            } catch (NumberFormatException ex) {}
                            sendLeaderBoard(sender, date, page, nosp);
                        } else {
                            if (!PluginControl.hasPermission(sender, "Permissions.Commands.LeaderBoard")) {
                                MessageUtil.sendMessage(sender, "No-Permission");
                                return true;
                            }
                            int page;
                            int nosp = 10;
                            try {
                                page = Integer.valueOf(args[2]);
                            } catch (NumberFormatException ex) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{page}", args[2]);
                                MessageUtil.sendMessage(sender, "Command-Messages.LeaderBoard.Invalid-Number", placeholders);
                                return true;
                            }
                            try {
                                nosp = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                            } catch (NumberFormatException ex) {}
                            sendLeaderBoard(sender, date, page, nosp);
                        }
                    } else {
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{date}", args[1]);
                        MessageUtil.sendMessage(sender, "Command-Messages.LeaderBoard.Date-Not-Exist", placeholders);
                    }
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.Info")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return true;
                }
                if (args.length <= 1) {
                    MessageUtil.sendMessage(sender, "Command-Messages.Info.Help");
                } else {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player != null) {
                        Storage data = Storage.getPlayer(player.getUniqueId());
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{player}", player.getName());
                        placeholders.put("{group}", data.getGroup() != null ? data.getGroup().getGroupName() : MessageUtil.getMessage("Command-Messages.Info.Unknown-Group"));
                        placeholders.put("{signin}", String.valueOf(data.alreadySignIn()).replace("true", MessageUtil.getMessage("Command-Messages.Info.true")).replace("false", MessageUtil.getMessage("Command-Messages.Info.false")));
                        placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                        placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                        placeholders.put("{total}", String.valueOf(data.getCumulativeNumber()));
                        placeholders.put("{retroactivecard}", String.valueOf(data.getRetroactiveCard()));
                        MessageUtil.sendMessage(sender, "Command-Messages.Info.Info", placeholders);
                    } else {
                        if (args[1].isEmpty()) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{player}", args[1]);
                            MessageUtil.sendMessage(sender, "Command-Messages.Click.Player-Not-Exist", placeholders);
                            return true;
                        }
                        Storage data = Storage.getPlayer(args[1]);
                        if (data != null) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{player}", data.getName() != null ? data.getName() : args[1]);
                            placeholders.put("{group}", data.getGroup() != null ? data.getGroup().getGroupName() : MessageUtil.getMessage("Command-Messages.Info.Unknown-Group"));
                            placeholders.put("{signin}", String.valueOf(data.alreadySignIn()).replace("true", MessageUtil.getMessage("Command-Messages.Info.true")).replace("false", MessageUtil.getMessage("Command-Messages.Info.false")));
                            placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                            placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                            placeholders.put("{total}", String.valueOf(data.getCumulativeNumber()));
                            placeholders.put("{retroactivecard}", String.valueOf(data.getRetroactiveCard()));
                            MessageUtil.sendMessage(sender, "Command-Messages.Info.Info", placeholders);
                        } else {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{player}", args[1]);
                            MessageUtil.sendMessage(sender, "Command-Messages.Info.Player-Not-Exist", placeholders);
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("itemcollection")) {
                if (!PluginControl.hasPermission(sender, "Permissions.Commands.ItemCollection")) {
                    MessageUtil.sendMessage(sender, "No-Permission");
                    return true;
                }
                if (args.length == 1) {
                    MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Help");
                } else if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("list")) {
                        List<CustomItem> itemList = CustomItem.getItemStackCollection();
                        if (itemList.isEmpty()) {
                            MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.List.Empty");
                        } else {
                            for (String text :  MessageUtil.getMessageList("Command-Messages.ItemCollection.List.Messages")) {
                                if (text.toLowerCase().contains("%list%")) {
                                    if (!(sender instanceof Player)) {
                                        StringBuilder list = new StringBuilder();
                                        for (CustomItem items : itemList) {
                                            String name;
                                            try {
                                                name = items.getItemStack().getItemMeta().hasDisplayName() ? items.getItemStack().getItemMeta().getDisplayName() : (String) items.getClass().getMethod("getI18NDisplayName").invoke(items.getItemStack());
                                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                name = items.getItemStack().getItemMeta().hasDisplayName() ? items.getItemStack().getItemMeta().getDisplayName() : items.getItemStack().getType().toString().toLowerCase().replace("_", " ");
                                            }
                                            list.append(name).append(", ");
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%list%", list.toString());
                                        placeholders.put("{amount}", String.valueOf(itemList.size()));
                                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, text, placeholders)));
                                        continue;
                                    }
                                    String[] splitMessage = text.split("%list%");
                                    List<BaseComponent> bc = new ArrayList();
                                    for (int i = 0;i < splitMessage.length;i++) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{amount}", String.valueOf(itemList.size()));
                                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders))));
                                        if (i < splitMessage.length - 1 || text.endsWith("%list%")) {
                                            bc.addAll(Arrays.asList(JsonItemStack.getJsonItemStackArray(itemList)));
                                        }
                                    }
                                    ((Player) sender).spigot().sendMessage(bc.toArray(new BaseComponent[] {}));
                                } else {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{amount}", String.valueOf(itemList.size()));
                                    sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, text, placeholders)));
                                }
                            }
                        }
                    } else if (args[1].equalsIgnoreCase("add")) {
                        if (!(sender instanceof Player)) {
                            MessageUtil.sendMessage(sender, "Unavailable-Command");
                        } else if (args.length == 2) {
                            MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Add.Help");
                        } else if (args.length >= 3) {
                            Player player = (Player) sender;
                            ItemStack is = player.getItemInHand();
                            if (is == null && is.getType().equals(Material.AIR)) {
                                MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Add.Doesnt-Have-Item-In-Hand");
                            } else if (CustomItem.addItemAsCollection(is, args[2])) {
                                if (MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully").toLowerCase().contains("%item%")) {
                                    if (!(sender instanceof Player)) {
                                        String name;
                                        try {
                                            name = is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : (String) is.getClass().getMethod("getI18NDisplayName").invoke(is);
                                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                            name = is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : is.getType().toString().toLowerCase().replace("_", " ");
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%item%", name);
                                        placeholders.put("{name}", args[2]);
                                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully"), placeholders)));
                                        return true;
                                    }
                                    String[] splitMessage = MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully").split("%item%");
                                    List<BaseComponent> bc = new ArrayList();
                                    for (int i = 0;i < splitMessage.length;i++) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{name}", args[2]);
                                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders))));
                                        if (i < splitMessage.length - 1 || MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully").endsWith("%item%")) {
                                            bc.add(JsonItemStack.getJsonItemStack(is));
                                        }
                                    }
                                    ((Player) sender).spigot().sendMessage(bc.toArray(new BaseComponent[] {}));
                                } else {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{name}", args[2]);
                                    MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Add.Successfully", placeholders);
                                }
                            } else {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{name}", args[2]);
                                MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Add.Already-Exist", placeholders);
                            }
                        }
                    } else if (args[1].equalsIgnoreCase("delete")) {
                        if (args.length == 2) {
                            MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Delete.Help");
                        } else if (args.length >= 3) {
                            CustomItem item = CustomItem.getCustomItem(args[2]);
                            if (item != null) {
                                if (MessageUtil.getMessage("Command-Messages.ItemCollection.Delete.Successfully").toLowerCase().contains("%item%")) {
                                    if (!(sender instanceof Player)) {
                                        String name;
                                        try {
                                            name = item.getItemStack().getItemMeta().hasDisplayName() ? item.getItemStack().getItemMeta().getDisplayName() : (String) item.getItemStack().getClass().getMethod("getI18NDisplayName").invoke(item.getItemStack());
                                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                            name = item.getItemStack().getItemMeta().hasDisplayName() ? item.getItemStack().getItemMeta().getDisplayName() : item.getItemStack().getType().toString().toLowerCase().replace("_", " ");
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%item%", name); 
                                        placeholders.put("{name}", args[2]);
                                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Delete.Successfully"), placeholders)));
                                        return true;
                                    }
                                    String[] splitMessage = MessageUtil.getMessage("Command-Messages.ItemCollection.Delete.Successfully").split("%item%");
                                    List<BaseComponent> bc = new ArrayList();
                                    for (int i = 0;i < splitMessage.length;i++) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{name}", args[2]);
                                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders))));
                                        if (i < splitMessage.length - 1 || MessageUtil.getMessage("Command-Messages.ItemCollection.Delete.Successfully").endsWith("%item%")) {
                                            bc.add(JsonItemStack.getJsonItemStack(item.getItemStack()));
                                        }
                                    }
                                    ((Player) sender).spigot().sendMessage(bc.toArray(new BaseComponent[0]));
                                    item.delete();
                                } else {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{name}", args[2]);
                                    MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Delete.Successfully", placeholders);
                                }
                            } else {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{name}", args[2]);
                                MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Delete.Not-Exist", placeholders);
                            }
                        }
                    } else if (args[1].equalsIgnoreCase("give")) {
                        if (args.length == 2) {
                            MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Give.Help");
                        } else if (args.length == 3) {
                            if (!(sender instanceof Player)) {
                                MessageUtil.sendMessage(sender, "Unavailable-Command");
                            } else {
                                CustomItem ci = CustomItem.getCustomItem(args[2]);
                                if (ci == null) {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{name}", args[2]);
                                    MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Give.Not-Exist", placeholders);
                                } else {
                                    ci.give((Player) sender);
                                    if (MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Yourself").toLowerCase().contains("%item%")) {
                                        if (!(sender instanceof Player)) {
                                            String name;
                                            try {
                                                name = ci.getItemStack().getItemMeta().hasDisplayName() ? ci.getItemStack().getItemMeta().getDisplayName() : (String) ci.getItemStack().getClass().getMethod("getI18NDisplayName").invoke(ci.getItemStack());
                                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                name = ci.getItemStack().getItemMeta().hasDisplayName() ? ci.getItemStack().getItemMeta().getDisplayName() : ci.getItemStack().getType().toString().toLowerCase().replace("_", " ");
                                            }
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%item%", name); 
                                            placeholders.put("{name}", args[2]);
                                            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Yourself"), placeholders)));
                                            return true;
                                        }
                                        String[] splitMessage = MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Yourself").split("%item%");
                                        List<BaseComponent> bc = new ArrayList();
                                        for (int i = 0;i < splitMessage.length;i++) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("{name}", args[2]);
                                            bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders))));
                                            if (i < splitMessage.length - 1 || MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Yourself").endsWith("%item%")) {
                                                bc.add(JsonItemStack.getJsonItemStack(ci.getItemStack()));
                                            }
                                        }
                                        ((Player) sender).spigot().sendMessage(bc.toArray(new BaseComponent[] {}));
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{name}", args[2]);
                                        MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Give.Give-Yourself", placeholders);
                                    }
                                }
                            }
                        } else if (args.length >= 4) {
                            Player player = Bukkit.getPlayer(args[3]);
                            if (player == null) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{player}", args[3]);
                                MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Give.Player-Offline", placeholders);
                                return true;
                            }
                            CustomItem ci = CustomItem.getCustomItem(args[2]);
                            if (ci == null) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{name}", args[2]);
                                MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Give.Not-Exist", placeholders);
                                return true;
                            } else {
                                ci.give(player);
                                if (MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Others").toLowerCase().contains("%item%")) {
                                    if (!(sender instanceof Player)) {
                                        String name;
                                        try {
                                            name = ci.getItemStack().getItemMeta().hasDisplayName() ? ci.getItemStack().getItemMeta().getDisplayName() : (String) ci.getItemStack().getClass().getMethod("getI18NDisplayName").invoke(ci.getItemStack());
                                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                            name = ci.getItemStack().getItemMeta().hasDisplayName() ? ci.getItemStack().getItemMeta().getDisplayName() : ci.getItemStack().getType().toString().toLowerCase().replace("_", " ");
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%item%", name);
                                        placeholders.put("{player}", player.getName());
                                        placeholders.put("{name}", args[2]);
                                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Others"), placeholders)));
                                        return true;
                                    }
                                    String[] splitMessage = MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Others").split("%item%");
                                    List<BaseComponent> bc = new ArrayList();
                                    for (int i = 0;i < splitMessage.length;i++) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", player.getName());
                                        placeholders.put("{name}", args[2]);
                                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, splitMessage[i], placeholders))));
                                        if (i < splitMessage.length - 1 || MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Others").endsWith("%item%")) {
                                            bc.add(JsonItemStack.getJsonItemStack(ci.getItemStack()));
                                        }
                                    }
                                    ((Player) sender).spigot().sendMessage(bc.toArray(new BaseComponent[] {}));
                                } else {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{player}", player.getName());
                                    placeholders.put("{name}", args[2]);
                                    MessageUtil.sendMessage(sender, "Command-Messages.ItemCollection.Give.Give-Others", placeholders);
                                }
                            }
                        }
                    }
                }
            } else {
                MessageUtil.sendMessage(sender, "Command-Messages.Unknown-Command");
            }
        }
        return true;
    }
    
    private List<String> getCommands(String args) {
        List<String> commands = Arrays.asList("help", "reload", "gui", "click", "save", "info", "leaderboard", "itemcollection", "retroactivecard", "database");
        if (args != null) {
            List<String> names = new ArrayList();
            for (String command : commands) {
                if (command.startsWith(args.toLowerCase())) {
                    names.add(command);
                }
            }
            return names;
        }
        return commands;
    }
    
    private List<String> getPlayers(String args) {
        List<String> players = new ArrayList();
        for (Player ps : Bukkit.getOnlinePlayers()) {
            if (ps.getName().toLowerCase().startsWith(args.toLowerCase())) {
                players.add(ps.getName());
            }
        }
        return players;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return getCommands(args[0]);
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("click") && PluginControl.hasPermission(sender, "Permissions.Commands.Click-Others")) {
                return getPlayers(args[1]);
            }
            if (args[0].equalsIgnoreCase("info") && PluginControl.hasPermission(sender, "Permissions.Commands.Info")) {
                return getPlayers(args[1]);
            }
            if (args[0].equalsIgnoreCase("retroactivecard") && PluginControl.hasPermission(sender, "Permissions.Commands.RetroactiveCard")) {
                if (args.length == 2) {
                    List<String> list = new ArrayList();
                    for (String text : new String[] {"give", "set", "take"}) {
                        if (text.toLowerCase().startsWith(args[1])) {
                            list.add(text);
                        }
                    }
                    return list;
                } else if (args.length == 4) {
                    return getPlayers(args[3]);
                }
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("gui") && PluginControl.hasPermission(sender, "Permissions.Commands.Designated-GUI")) {
                return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
            }
            if (args.length >= 2 && args[0].equalsIgnoreCase("database")) {
                if (args.length == 2) {
                    List<String> list = new ArrayList();
                    for (String text : new String[] {"backup", "rollback", "confirm"}) {
                        if (text.toLowerCase().startsWith(args[1].toLowerCase())) {
                            list.add(text);
                        }
                    }
                    return list;
                } else if (args.length >= 3 && args[1].equalsIgnoreCase("rollback")) {
                    List<String> list = new ArrayList();
                    for (String string : PluginControl.getBackupFiles()) {
                        if (string.toLowerCase().startsWith(args[2].toLowerCase())) {
                            list.add(string);
                        }
                    }
                    return list;
                }
            }
            if (args.length >= 2 && args[0].equalsIgnoreCase("itemcollection") && PluginControl.hasPermission(sender, "Permissions.Commands.ItemCollection")) {
                if (args.length == 3 && (args[1].equalsIgnoreCase("delete")) || args[1].equalsIgnoreCase("give")) {
                    List<String> list = new ArrayList();
                    for (CustomItem ci : CustomItem.getItemStackCollection()) {
                        if (ci.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            list.add(ci.getName());
                        }
                    }
                    return list;
                }
                if (args.length == 4 && args[1].equalsIgnoreCase("give")) {
                    return getPlayers(args[3]);
                }
                if (args.length == 2) {
                    List<String> list = new ArrayList();
                    for (String name : Arrays.asList("list", "add", "delete", "give")) {
                        if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                            list.add(name);
                        }
                    }
                    return list;
                }
            }
        }
        return new ArrayList();
    }
    
    private void sendLeaderBoard(CommandSender sender, SignInDate date, int page, int nosp) {
        SignInQueue queue = SignInQueue.getInstance(date);
        if (queue.isEmpty()) {
            Map<String, String> placeholders = new HashMap();
            placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
            MessageUtil.sendMessage(sender, "Command-Messages.LeaderBoard.Empty", placeholders);
            return;
        }
        int maxPage = queue.size() % nosp == 0 ? queue.size() / nosp : queue.size() / nosp + 1;
        if (page > maxPage) {
            page = 1;
        }
        if (page <= 0) {
            page = maxPage;
        }
        boolean today = date.equals(SignInDate.getInstance(new Date()));
        String listFormatPath = today ? "Today" : "Historical-Date";
        for (String message : today ? 
                MessageUtil.getMessageList("Command-Messages.LeaderBoard.LeaderBoard-Messages") : 
                MessageUtil.getMessageList("Command-Messages.LeaderBoard.Historical-Date-LeaderBoard-Messages")) {
            if (message.toLowerCase().contains("%leaderboard%")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int ranking = queue.getRank(player.getUniqueId());
                    for (int rank = page * nosp - nosp + 1;rank <= queue.size() && rank <= page * nosp;rank++) {
                        List<SignInQueueElement> userArray = queue.getRankingUser(rank);
                        if (userArray.isEmpty()) continue;
                        if (ranking != rank) {
                            if (userArray.size() == 1) {
                                SignInQueueElement element = userArray.get(0);
                                String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players").toLowerCase().contains("%player%")) {
                                    String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                                    if (name == null) {
                                        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                        if (offlineplayer != null) {
                                            name = offlineplayer.getName();
                                        }
                                    }
                                    if (name != null) {
                                        Map<String, String> playerName = new HashMap();
                                        playerName.put("{player}", name);
                                        BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), playerName));
                                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                        List<BaseComponent> hoverText = new ArrayList();
                                        int end = 0;
                                        List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                        for (String hover : array) {
                                            end++;
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("{player}", name);
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                            if (end != array.size()) {
                                                hoverText.add(new TextComponent("\n"));
                                            }
                                        }
                                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                        click.setClickEvent(ce);
                                        click.setHoverEvent(he);
                                        Map<String, BaseComponent> baseComponents = new HashMap();
                                        baseComponents.put("%player%", click);
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", name);
                                        placeholders.put("{total}", String.valueOf(queue.size()));
                                        placeholders.put("{date}", dateName);
                                        placeholders.put("{ranking}", String.valueOf(rank));
                                        placeholders.put("{time}", timeName);
                                        placeholders.put("{page}", String.valueOf(page));
                                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                                        MessageUtil.sendJsonMessage(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players"), baseComponents, placeholders);
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        Map<String, String> uuid = new HashMap();
                                        uuid.put("{uuid}", element.getUUID().toString());
                                        placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                        placeholders.put("{total}", String.valueOf(queue.size()));
                                        placeholders.put("{date}", dateName);
                                        placeholders.put("{ranking}", String.valueOf(rank));
                                        placeholders.put("{time}", timeName);
                                        placeholders.put("{page}", String.valueOf(page));
                                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                                        sender.sendMessage(MessageUtil.toColor(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players"), placeholders))));
                                    }
                                } else {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{total}", String.valueOf(queue.size()));
                                    placeholders.put("{date}", dateName);
                                    placeholders.put("{ranking}", String.valueOf(rank));
                                    placeholders.put("{time}", timeName);
                                    placeholders.put("{page}", String.valueOf(page));
                                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                                    sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players"), placeholders)));
                                }
                            } else {
                                for (SignInQueueElement user : userArray) {
                                    SignInQueueElement element = user;
                                    String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                    String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                    if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players").toLowerCase().contains("%player%")) {
                                        String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                                        if (name == null) {
                                            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                            if (offlineplayer != null) {
                                                name = offlineplayer.getName();
                                            }
                                        }
                                        if (name != null) {
                                            Map<String, String> playerName = new HashMap();
                                            playerName.put("{player}", name);
                                            BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), playerName));
                                            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                            List<BaseComponent> hoverText = new ArrayList();
                                            int end = 0;
                                            List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                            for (String hover : array) {
                                                end++;
                                                Map<String, String> placeholders = new HashMap();
                                                placeholders.put("{player}", name);
                                                placeholders.put("{total}", String.valueOf(queue.size()));
                                                placeholders.put("{date}", dateName);
                                                placeholders.put("{ranking}", String.valueOf(rank));
                                                placeholders.put("{time}", timeName);
                                                placeholders.put("{page}", String.valueOf(page));
                                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                                if (end != array.size()) {
                                                    hoverText.add(new TextComponent("\n"));
                                                }
                                            }
                                            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                            click.setClickEvent(ce);
                                            click.setHoverEvent(he);
                                            Map<String, BaseComponent> baseComponents = new HashMap();
                                            baseComponents.put("%player%", click);
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("{player}", name);
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            MessageUtil.sendJsonMessage(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), baseComponents, placeholders);
                                        } else {
                                            Map<String, String> placeholders = new HashMap();
                                            Map<String, String> uuid = new HashMap();
                                            uuid.put("{uuid}", element.getUUID().toString());
                                            placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                                        }
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{total}", String.valueOf(queue.size()));
                                        placeholders.put("{date}", dateName);
                                        placeholders.put("{ranking}", String.valueOf(rank));
                                        placeholders.put("{time}", timeName);
                                        placeholders.put("{page}", String.valueOf(page));
                                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                                    }
                                }
                            }
                        } else {
                            if (userArray.size() == 1) {
                                String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                String timeName = queue.getElement(player.getUniqueId()).getSignInDate().hasTimePeriod() ? queue.getElement(player.getUniqueId()).getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Self").toLowerCase().contains("%player%")) {
                                    String name = player.getName();
                                    Map<String, String> playerName = new HashMap();
                                    playerName.put("{player}", name);
                                    BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Self"), playerName));
                                    ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                    List<BaseComponent> hoverText = new ArrayList();
                                    int end = 0;
                                    List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                    for (String hover : array) {
                                        end++;
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("{player}", name);
                                        placeholders.put("{total}", String.valueOf(queue.size()));
                                        placeholders.put("{date}", dateName);
                                        placeholders.put("{ranking}", String.valueOf(rank));
                                        placeholders.put("{time}", timeName);
                                        placeholders.put("{page}", String.valueOf(page));
                                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                                        hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                        if (end != array.size()) {
                                            hoverText.add(new TextComponent("\n"));
                                        }
                                    }
                                    HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                    click.setHoverEvent(he);
                                    click.setClickEvent(ce);
                                    Map<String, BaseComponent> baseComponents = new HashMap();
                                    baseComponents.put("%player%", click);
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{player}", name);
                                    placeholders.put("{total}", String.valueOf(queue.size()));
                                    placeholders.put("{date}", dateName);
                                    placeholders.put("{ranking}", String.valueOf(rank));
                                    placeholders.put("{time}", timeName);
                                    placeholders.put("{page}", String.valueOf(page));
                                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                                    MessageUtil.sendJsonMessage(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Self"), baseComponents, placeholders);
                                } else {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{total}", String.valueOf(queue.size()));
                                    placeholders.put("{date}", dateName);
                                    placeholders.put("{ranking}", String.valueOf(rank));
                                    placeholders.put("{time}", timeName);
                                    placeholders.put("{page}", String.valueOf(page));
                                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                                    sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Self"), placeholders)));
                                }
                            } else {
                                for (SignInQueueElement user : userArray) {
                                    if (user.getUUID().equals(player.getUniqueId())) {
                                        String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                        String timeName = queue.getElement(player.getUniqueId()).getSignInDate().hasTimePeriod() ? queue.getElement(player.getUniqueId()).getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                        if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Self").toLowerCase().contains("%player%")) {
                                            String name = player.getName();
                                            Map<String, String> playerName = new HashMap();
                                            playerName.put("{player}", name);
                                            BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Self"), playerName));
                                            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                            List<BaseComponent> hoverText = new ArrayList();
                                            int end = 0;
                                            List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                            for (String hover : array) {
                                                end++;
                                                Map<String, String> placeholders = new HashMap();
                                                placeholders.put("{player}", name);
                                                placeholders.put("{total}", String.valueOf(queue.size()));
                                                placeholders.put("{date}", dateName);
                                                placeholders.put("{ranking}", String.valueOf(rank));
                                                placeholders.put("{time}", timeName);
                                                placeholders.put("{page}", String.valueOf(page));
                                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                                if (end != array.size()) {
                                                    hoverText.add(new TextComponent("\n"));
                                                }
                                            }
                                            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                            click.setHoverEvent(he);
                                            click.setClickEvent(ce);
                                            Map<String, BaseComponent> baseComponents = new HashMap();
                                            baseComponents.put("%player%", click);
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("{player}", name);
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            MessageUtil.sendJsonMessage(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Self"), baseComponents, placeholders);
                                        } else {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Self"), placeholders)));
                                        }
                                    } else {
                                        SignInQueueElement element = user;
                                        String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                        String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                        if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players").toLowerCase().contains("%player%")) {
                                            String name = element.getName() != null ? element.getName() : null;
                                            if (name == null) {
                                                OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                                if (offlineplayer != null) {
                                                    name = offlineplayer.getName();
                                                }
                                            }
                                            if (name != null) {
                                                Map<String, String> playerName = new HashMap();
                                                playerName.put("{player}", name);
                                                BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), playerName));
                                                ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                                List<BaseComponent> hoverText = new ArrayList();
                                                int end = 0;
                                                List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                                for (String hover : array) {
                                                    end++;
                                                    Map<String, String> placeholders = new HashMap();
                                                    placeholders.put("{player}", name);
                                                    placeholders.put("{total}", String.valueOf(queue.size()));
                                                    placeholders.put("{date}", dateName);
                                                    placeholders.put("{ranking}", String.valueOf(rank));
                                                    placeholders.put("{time}", timeName);
                                                    placeholders.put("{page}", String.valueOf(page));
                                                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                    hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                                    if (end != array.size()) {
                                                        hoverText.add(new TextComponent("\n"));
                                                    }
                                                }
                                                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                                click.setClickEvent(ce);
                                                click.setHoverEvent(he);
                                                Map<String, BaseComponent> baseComponents = new HashMap();
                                                baseComponents.put("%player%", click);
                                                Map<String, String> placeholders = new HashMap();
                                                placeholders.put("{total}", String.valueOf(queue.size()));
                                                placeholders.put("{date}", dateName);
                                                placeholders.put("{ranking}", String.valueOf(rank));
                                                placeholders.put("{time}", timeName);
                                                placeholders.put("{page}", String.valueOf(page));
                                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                MessageUtil.sendJsonMessage(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), baseComponents, placeholders);
                                            } else {
                                                Map<String, String> placeholders = new HashMap();
                                                Map<String, String> uuid = new HashMap();
                                                uuid.put("{uuid}", element.getUUID().toString());
                                                placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                                placeholders.put("{total}", String.valueOf(queue.size()));
                                                placeholders.put("{date}", dateName);
                                                placeholders.put("{ranking}", String.valueOf(rank));
                                                placeholders.put("{time}", timeName);
                                                placeholders.put("{page}", String.valueOf(page));
                                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                                            }
                                        } else {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (int rank = page * nosp - nosp + 1;rank <= queue.size() && rank <= page * nosp;rank++) {
                        List<SignInQueueElement> userArray = queue.getRankingUser(rank);
                        if (userArray.isEmpty()) continue;
                        //Whether there are multiple players signing in at the same time in the same second
                        if (userArray.size() == 1) {
                            SignInQueueElement element = userArray.get(0);
                            String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                            if (name == null) {
                                OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                if (offlineplayer != null) {
                                    name = offlineplayer.getName();
                                }
                            }
                            Map<String, String> placeholders = new HashMap();
                            String playerName;
                            if (name != null) {
                                Map<String, String> subPlaceholders = new HashMap();
                                subPlaceholders.put("{player}", name);
                                playerName = MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), subPlaceholders);
                            } else {
                                Map<String, String> subPlaceholders = new HashMap();
                                subPlaceholders.put("{uuid}", element.getUUID().toString());
                                playerName = MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), subPlaceholders);
                            }
                            placeholders.put("%player%", playerName);
                            placeholders.put("{total}", String.valueOf(queue.size()));
                            placeholders.put("{date}", element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                            placeholders.put("{ranking}", String.valueOf(rank));
                            String timeFormat;
                            if (element.getSignInDate().hasTimePeriod()) {
                                timeFormat = element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format"));
                            } else {
                                timeFormat = MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                            }
                            placeholders.put("{time}", timeFormat);
                            placeholders.put("{page}", String.valueOf(page));
                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players"), placeholders)));
                        } else {
                            for (SignInQueueElement element : userArray) {
                                String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                                if (name == null) {
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                    if (offlineplayer != null) {
                                        name = offlineplayer.getName();
                                    }
                                }
                                Map<String, String> placeholders = new HashMap();
                                String playerName;
                                if (name != null) {
                                    Map<String, String> subPlaceholders = new HashMap();
                                    subPlaceholders.put("{player}", name);
                                    playerName = MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), subPlaceholders);
                                } else {
                                    Map<String, String> subPlaceholders = new HashMap();
                                    subPlaceholders.put("{uuid}", element.getUUID().toString());
                                    playerName = MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), subPlaceholders);
                                }
                                placeholders.put("%player%", playerName);
                                placeholders.put("{total}", String.valueOf(queue.size()));
                                placeholders.put("{date}", element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                                placeholders.put("{ranking}", String.valueOf(rank));
                                String timeFormat;
                                if (element.getSignInDate().hasTimePeriod()) {
                                    timeFormat = element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format"));
                                } else {
                                    timeFormat = MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                }
                                placeholders.put("{time}", timeFormat);
                                placeholders.put("{page}", String.valueOf(page));
                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                            }
                        }
                    }
                }
            } else {
                if (!(sender instanceof Player)) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("%previousPage%", MessageUtil.getMessage("Command-Messages.LeaderBoard.Previous-Page.Text"));
                    placeholders.put("%nextPage%", MessageUtil.getMessage("Command-Messages.LeaderBoard.Next-Page.Text"));
                    placeholders.put("{total}", String.valueOf(queue.size()));
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                    placeholders.put("{page}", String.valueOf(page));
                    placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                    placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                    sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, message, placeholders)));
                    continue;
                }
                Map<String, BaseComponent> baseComponents = new HashMap();
                if (message.toLowerCase().contains("%previouspage%")) {
                    BaseComponent click = new TextComponent(MessageUtil.getMessage("Command-Messages.LeaderBoard.Previous-Page.Text"));
                    List<BaseComponent> hoverText = new ArrayList();
                    int end = 0;
                    List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Previous-Page.Hover");
                    for (String hover : array) {
                        end++;
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{total}", String.valueOf(queue.size()));
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                        placeholders.put("{page}", String.valueOf(page));
                        placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                        placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                        hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                        if (end != array.size()) {
                            hoverText.add(new TextComponent("\n"));
                        }
                    }
                    HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                    ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/litesignin leaderboard " + date.getDataText(false) + " " + (page - 1));
                    click.setClickEvent(ce);
                    click.setHoverEvent(he);
                    baseComponents.put("%previousPage%", click);
                }
                if (message.toLowerCase().contains("%nextpage%")) {
                    BaseComponent click = new TextComponent(MessageUtil.getMessage("Command-Messages.LeaderBoard.Next-Page.Text"));
                    List<BaseComponent> hoverText = new ArrayList();
                    int end = 0;
                    List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Next-Page.Hover");
                    for (String hover : array) {
                        end++;
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("{total}", String.valueOf(queue.size()));
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                        placeholders.put("{page}", String.valueOf(page));
                        placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                        placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                        hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                        if (end != array.size()) {
                            hoverText.add(new TextComponent("\n"));
                        }
                    }
                    HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                    ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/litesignin leaderboard " + date.getDataText(false) + " " + (page + 1));
                    click.setClickEvent(ce);
                    click.setHoverEvent(he);
                    baseComponents.put("%nextPage%", click);
                }
                if (baseComponents.isEmpty()) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{total}", String.valueOf(queue.size()));
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                    placeholders.put("{page}", String.valueOf(page));
                    placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                    placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                    sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, message, placeholders)));
                } else {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("{total}", String.valueOf(queue.size()));
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                    placeholders.put("{page}", String.valueOf(page));
                    placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                    placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                    MessageUtil.sendJsonMessage(sender, message, baseComponents, placeholders);
                }
            }
        }
    }
    
    private final Map<CommandSender, Confirm> confirmCache = new HashMap();
    
    private enum Confirm {
        
        BACKUP,
        
        ROLLBACK;
        
        private File file = null;
        
        public void setTargetFile(File file) {
            this.file = file;
        }
        
        public File getTargetFile() {
            return file;
        }
    }
}
