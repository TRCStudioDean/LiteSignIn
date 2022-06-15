package studio.trc.bukkit.litesignin.command.subcommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.SignInPluginUtils;

public class ClickCommand
    implements SignInSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (args.length == 1) {
            if (SignInPluginUtils.isPlayer(sender, true)) {
                Player player = (Player) sender;
                Storage data = Storage.getPlayer(player);
                if (data.alreadySignIn()) {
                    placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                    placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                    MessageUtil.sendCommandMessage(player, "Click.To-Self.Today-has-been-Signed-In", placeholders);
                } else {
                    data.signIn();
                    placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                    placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                    MessageUtil.sendCommandMessage(player, "Click.To-Self.Successfully-Signed-In", placeholders);
                }
            }
        } else if (args.length == 2) {
            SignInDate date = SignInDate.getInstance(args[1]);
            if (date == null) {
                if (!SignInPluginUtils.hasCommandPermission(sender, "Click-Others", false)) {
                    placeholders.put("{date}", args[1]);
                    MessageUtil.sendCommandMessage(sender, "Click.Invalid-Date", placeholders);
                    return;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player != null) {
                    Storage data = Storage.getPlayer(player);
                    if (data.alreadySignIn()) {
                        placeholders.put("{player}", player.getName());
                        placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                        placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Today-has-been-Signed-In", placeholders);
                    } else {
                        data.signIn();
                        placeholders.put("{player}", player.getName());
                        placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                        placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Successfully-Signed-In", placeholders);
                        Map<String, String> placeholders_2 = MessageUtil.getDefaultPlaceholders();
                        placeholders_2.put("{admin}", sender.getName());
                        placeholders_2.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                        placeholders_2.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                        MessageUtil.sendCommandMessage(player, "Click.To-Other-Player.Messages-at-Sign-In", placeholders_2);
                    }
                } else {
                    if (args[1].isEmpty()) {
                        placeholders.put("{player}", args[1]);
                        MessageUtil.sendCommandMessage(sender, "Click.Player-Not-Exist", placeholders);
                        return;
                    }
                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[1]);
                    if (offlineplayer != null) {
                        Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                        if (data.alreadySignIn()) {
                            placeholders.put("{player}", offlineplayer.getName());
                            placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                            placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                            MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Today-has-been-Signed-In", placeholders);
                        } else {
                            data.signIn();
                            placeholders.put("{player}", offlineplayer.getName());
                            placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                            placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                            MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Successfully-Signed-In", placeholders);
                        }
                    } else {
                        placeholders.put("{player}", args[1]);
                        MessageUtil.sendCommandMessage(sender, "Click.Player-Not-Exist", placeholders);
                    }
                }
            } else {
                if (SignInPluginUtils.isPlayer(sender, true)) {
                    Player player = (Player) sender;
                    SignInDate today = SignInDate.getInstance(new Date());
                    if (date.equals(today)) {
                        player.performCommand("litesignin:signin click");
                        return;
                    }
                    if (!SignInPluginUtils.hasCommandPermission(sender, "Click", false) || !SignInPluginUtils.hasPermission(sender, "Retroactive-Card.Use")) {
                        MessageUtil.sendMessage(sender, "No-Permission");
                        return;
                    }
                    if (!PluginControl.enableRetroactiveCard()) {
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Unable-To-Re-SignIn");
                        return;
                    }
                    Storage data = Storage.getPlayer(player);
                    if (!SignInPluginUtils.hasPermission(sender, "Retroactive-Card.Use") && data.getRetroactiveCard() > 0) {
                        data.takeRetroactiveCard(data.getRetroactiveCard());
                        MessageUtil.sendCommandMessage(sender, "Click.To-Self.Unable-To-Hold");
                    } else if (data.isRetroactiveCardCooldown()) {
                        placeholders.put("{second}", String.valueOf(data.getRetroactiveCardCooldown()));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Self.Retroactive-Card-Cooldown", placeholders);
                    } else if (SignInDate.getInstance(new Date()).compareTo(date) < 0) {
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                        MessageUtil.sendCommandMessage(sender, "Click.Invalid-Date", placeholders);
                    } else if (PluginControl.getRetroactiveCardMinimumDate() != null && date.compareTo(PluginControl.getRetroactiveCardMinimumDate()) < 0) {
                        placeholders.put("{date}", PluginControl.getRetroactiveCardMinimumDate().getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Self.Minimum-Date", placeholders);
                    } else if (data.alreadySignIn(date)) {
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Self.Specified-Date-has-been-Signed-In", placeholders);
                    } else if (data.getRetroactiveCard() >= PluginControl.getRetroactiveCardQuantityRequired()) {
                        data.takeRetroactiveCard(PluginControl.getRetroactiveCardQuantityRequired());
                        data.signIn(date);
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                        placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Self.Successfully-Retroactive-Signed-In", placeholders);
                    } else {
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                        placeholders.put("{cards}", String.valueOf(PluginControl.getRetroactiveCardQuantityRequired() - data.getRetroactiveCard()));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Self.Need-More-Retroactive-Cards", placeholders);
                    }
                }
            }
        } else if (args.length >= 3) {
            if (!SignInPluginUtils.hasCommandPermission(sender, "Click-Others", false)) {
                MessageUtil.sendMessage(sender, "No-Permission");
                return;
            }
            SignInDate date = SignInDate.getInstance(args[1]);
            Player player = Bukkit.getPlayer(args[2]);
            if (date == null) {
                placeholders.put("{date}", args[1]);
                MessageUtil.sendCommandMessage(sender, "Click.Invalid-Date", placeholders);
                return;
            }
            SignInDate today = SignInDate.getInstance(new Date());
            if (player != null) {
                if (date.equals(today)) {
                    if (sender instanceof Player) {
                        ((Player) sender).performCommand("litesignin click " + player.getName());
                    } else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litesignin click " + player.getName());
                    }
                    return;
                }
                Storage data = Storage.getPlayer(player);
                if (PluginControl.getRetroactiveCardMinimumDate() != null && date.compareTo(PluginControl.getRetroactiveCardMinimumDate()) < 0) {
                    placeholders.put("{date}", PluginControl.getRetroactiveCardMinimumDate().getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                    MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Minimum-Date", placeholders);
                } else if (data.alreadySignIn(date)) {
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                    placeholders.put("{player}", player.getName());
                    MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Specified-Date-has-been-Signed-In", placeholders);
                } else if (SignInDate.getInstance(new Date()).compareTo(date) < 0) {
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                    MessageUtil.sendCommandMessage(sender, "Click.Invalid-Date", placeholders);
                } else {
                    data.signIn(date);
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                    placeholders.put("{player}", player.getName());
                    placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                    placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                    MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Successfully-Retroactive-Signed-In", placeholders);
                    Map<String, String> placeholders_2 = MessageUtil.getDefaultPlaceholders();
                    placeholders_2.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                    placeholders_2.put("{admin}", sender.getName());
                    placeholders_2.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                    placeholders_2.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                    MessageUtil.sendCommandMessage(player, "Click.To-Other-Player.Messages-at-Retroactive-Sign-In", placeholders_2);
                }
            } else {
                if (args[2].isEmpty()) {
                    placeholders.put("{player}", args[2]);
                    MessageUtil.sendCommandMessage(sender, "Click.Player-Not-Exist", placeholders);
                    return;
                }
                OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                if (date.equals(today)) {
                    if (sender instanceof Player) {
                        ((Player) sender).performCommand("litesignin click " + offlineplayer.getName());
                    } else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litesignin click " + offlineplayer.getName());
                    }
                    return;
                }
                if (!PluginControl.enableRetroactiveCard()) {
                    MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Unable-To-Re-SignIn");
                    return;
                }
                if (offlineplayer != null) {
                    Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                    if (data.alreadySignIn(date)) {
                        placeholders.put("{player}", offlineplayer.getName());
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                        placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                        placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Specified-Date-has-been-Signed-In", placeholders);
                    } else if (SignInDate.getInstance(new Date()).compareTo(date) < 0) {
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                        MessageUtil.sendCommandMessage(sender, "Click.Invalid-Date", placeholders);
                    } else {
                        data.signIn(date);
                        placeholders.put("{player}", offlineplayer.getName());
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.Click.Date-Format")));
                        placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                        placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                        MessageUtil.sendCommandMessage(sender, "Click.To-Other-Player.Successfully-Retroactive-Signed-In", placeholders);
                    }
                } else {
                    placeholders.put("{player}", args[2]);
                    MessageUtil.sendCommandMessage(sender, "Click.Player-Not-Exist", placeholders);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "click";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        if (args.length == 2) {
            return tabGetPlayersName(args, 2);
        }
        return new ArrayList();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.CLICK;
    }
}
