package studio.trc.bukkit.litesignin.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInPluginUtils;

public class RetroactiveCardCommand
    implements SignInSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!PluginControl.enableRetroactiveCard()) {
            MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Unavailable-Feature");
        }
        if (args.length < 3) {
            MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Help");
        } else {
            Player player;
            if (args.length == 3) {
                if (SignInPluginUtils.isPlayer(sender, true)) {
                    player = (Player) sender;
                } else {
                    return;
                }
            } else {
                player = Bukkit.getPlayer(args[3]);
            }
            String subCommandType = args[1];
            if (subCommandType.equalsIgnoreCase("help")) {
                MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Help");
            } else if (subCommandType.equalsIgnoreCase("give")) {
                command_give(sender, args, player);
            } else if (subCommandType.equalsIgnoreCase("set")) {
                command_set(sender, args, player);
            } else if (subCommandType.equalsIgnoreCase("take")) {
                command_take(sender, args, player);
            } else {
                MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Help");
            }
        }
    }

    @Override
    public String getName() {
        return "retroactivecard";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        String subCommandType = args[1];
        if (args.length <= 2) {
            List<String> commands = Arrays.stream(SubCommandType.values())
                    .filter(type -> SignInPluginUtils.hasCommandPermission(sender, type.getCommandPermissionPath(), false))
                    .map(type -> type.getCommandName())
                    .collect(Collectors.toList());
            List<String> names = new ArrayList();
            commands.stream().filter(command -> command.toLowerCase().startsWith(subCommandType.toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        } else {
            if (args.length == 4) {
                return tabGetPlayersName(args, 4);
            }
        }
        return new ArrayList();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.RETROACTIVE_CARD;
    }

    private void command_give(CommandSender sender, String[] args, Player player) {
        if (!SignInPluginUtils.hasCommandPermission(sender, SubCommandType.GIVE.commandPermissionPath, true)) {
            return;
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        try {
            int number = Integer.valueOf(args[2]);
            if (player != null) {
                Storage data = Storage.getPlayer(player);
                data.giveRetroactiveCard(number);
                placeholders.put("{player}", player.getName());
                placeholders.put("{amount}", String.valueOf(number));
                MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Give", placeholders);
            } else {
                if (PluginControl.enableRetroactiveCardRequiredItem()) {
                    placeholders.put("{player}", args[3]);
                    MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Player-Offline", placeholders);
                } else {
                    if (args[3].isEmpty()) {
                        placeholders.put("{player}", args[3]);
                        MessageUtil.sendCommandMessage(sender, "Click.Player-Not-Exist", placeholders);
                        return;
                    }
                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[3]);
                    if (offlineplayer != null) {
                        Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                        data.setRetroactiveCard(data.getRetroactiveCard() + number, true);
                        placeholders.put("{player}", offlineplayer.getName());
                        placeholders.put("{amount}", String.valueOf(number));
                        MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Give", placeholders);
                    } else {
                        placeholders.put("{player}", args[3]);
                        MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Player-Not-Exist", placeholders);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            placeholders.put("{number}", args[2]);
            MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Invalid-Number", placeholders);
        }
    }

    private void command_set(CommandSender sender, String[] args, Player player) {
        if (!SignInPluginUtils.hasCommandPermission(sender, SubCommandType.SET.commandPermissionPath, true)) {
            return;
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        try {
            int number = Integer.valueOf(args[2]);
            if (player != null) {
                Storage data = Storage.getPlayer(player);
                data.setRetroactiveCard(number, true);
                placeholders.put("{player}", player.getName());
                placeholders.put("{amount}", String.valueOf(number));
                MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Set", placeholders);
            } else {
                if (PluginControl.enableRetroactiveCardRequiredItem()) {
                    placeholders.put("{player}", args[3]);
                    MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Player-Offline", placeholders);
                } else {
                    if (args[3].isEmpty()) {
                        placeholders.put("{player}", args[3]);
                        MessageUtil.sendCommandMessage(sender, "Click.Player-Not-Exist", placeholders);
                        return;
                    }
                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[3]);
                    if (offlineplayer != null) {
                        Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                        data.setRetroactiveCard(number, true);
                        placeholders.put("{player}", offlineplayer.getName());
                        placeholders.put("{amount}", String.valueOf(number));
                        MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Set", placeholders);
                    } else {
                        placeholders.put("{player}", args[3]);
                        MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Player-Not-Exist", placeholders);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            placeholders.put("{number}", args[2]);
            MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Invalid-Number", placeholders);
        }
    }

    private void command_take(CommandSender sender, String[] args, Player player) {
        if (!SignInPluginUtils.hasCommandPermission(sender, SubCommandType.TAKE.commandPermissionPath, true)) {
            return;
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        try {
            int number = Integer.valueOf(args[2]);
            if (player != null) {
                Storage data = Storage.getPlayer(player);
                data.takeRetroactiveCard(number);
                placeholders.put("{player}", player.getName());
                placeholders.put("{amount}", String.valueOf(number));
                MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Take", placeholders);
            } else {
                if (PluginControl.enableRetroactiveCardRequiredItem()) {
                    placeholders.put("{player}", args[3]);
                    MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Player-Offline", placeholders);
                } else {
                    if (args[3].isEmpty()) {
                        placeholders.put("{player}", args[3]);
                        MessageUtil.sendCommandMessage(sender, "Click.Player-Not-Exist", placeholders);
                        return;
                    }
                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[3]);
                    if (offlineplayer != null) {
                        Storage data = Storage.getPlayer(offlineplayer.getUniqueId());
                        data.setRetroactiveCard(data.getRetroactiveCard() - number, true);
                        placeholders.put("{player}", offlineplayer.getName());
                        placeholders.put("{amount}", String.valueOf(number));
                        MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Take", placeholders);
                    } else {
                        placeholders.put("{player}", args[3]);
                        MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Player-Not-Exist", placeholders);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            placeholders.put("{number}", args[2]);
            MessageUtil.sendCommandMessage(sender, "RetroactiveCard.Invalid-Number", placeholders);
        }
    }
    
    public enum SubCommandType {
        /**
         * /signin retroactivecard give
         */
        GIVE("give", "RetroactiveCard.Give"),
        
        /**
         * /signin retroactivecard set
         */
        SET("set", "RetroactiveCard.Set"),
        
        /**
         * /signin retroactivecard set
         */
        TAKE("take", "RetroactiveCard.Take");
        
        @Getter
        private final String commandName;
        @Getter
        private final String commandPermissionPath;
        
        private SubCommandType(String commandName, String commandPermissionPath) {
            this.commandName = commandName;
            this.commandPermissionPath = commandPermissionPath;
        }
    }
}
