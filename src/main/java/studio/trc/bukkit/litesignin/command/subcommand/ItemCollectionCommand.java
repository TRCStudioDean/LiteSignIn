package studio.trc.bukkit.litesignin.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.nms.NMSManager;
import studio.trc.bukkit.litesignin.util.AdventureUtils;
import studio.trc.bukkit.litesignin.util.CustomItem;
import studio.trc.bukkit.litesignin.util.LiteSignInUtils;

public class ItemCollectionCommand
    implements SignInSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (args.length == 1) {
            MessageUtil.sendCommandMessage(sender, "ItemCollection.Help");
        } else if (args.length >= 2) {
            String subCommandType = args[1];
            if (subCommandType.equalsIgnoreCase("help")) {
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Help");
            } else if (subCommandType.equalsIgnoreCase("list")) {
                command_list(sender, args);
            } else if (subCommandType.equalsIgnoreCase("add")) {
                command_add(sender, args);
            } else if (subCommandType.equalsIgnoreCase("delete")) {
                command_delete(sender, args);
            } else if (subCommandType.equalsIgnoreCase("give")) {
                command_give(sender, args);
            } else {
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Help");
            }
        }
    }

    @Override
    public String getName() {
        return "itemcollection";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        String subCommandType = args[1];
        if (args.length <= 2) {
            List<String> commands = Arrays.stream(SubCommandType.values())
                    .filter(type -> LiteSignInUtils.hasCommandPermission(sender, type.getCommandPermissionPath(), false))
                    .map(type -> type.getCommandName())
                    .collect(Collectors.toList());
            List<String> names = new ArrayList();
            commands.stream().filter(command -> command.toLowerCase().startsWith(subCommandType.toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        } else {
            if (subCommandType.equalsIgnoreCase("delete") && LiteSignInUtils.hasCommandPermission(sender, SubCommandType.DELETE.commandPermissionPath, false)) {
                return tab_delete(args);
            }
            if (subCommandType.equalsIgnoreCase("give") && LiteSignInUtils.hasCommandPermission(sender, SubCommandType.GIVE.commandPermissionPath, false)) {
                return tab_give(args);
            }
        }
        return new ArrayList();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.ITEM_COLLECTION;
    }

    private void command_list(CommandSender sender, String[] args) {
        if (!LiteSignInUtils.hasCommandPermission(sender, SubCommandType.LIST.commandPermissionPath, true)) {
            return;
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        List<CustomItem> itemList = CustomItem.getItemStackCollection();
        if (itemList.isEmpty()) {
            MessageUtil.sendCommandMessage(sender, "ItemCollection.List.Empty");
        } else {
            placeholders.put("{amount}", String.valueOf(itemList.size()));
            MessageUtil.getMessageList("Command-Messages.ItemCollection.List.Messages").stream().forEach(text -> {
                if (text.toLowerCase().contains("%list%")) {
                    if (MessageUtil.useAdventure()) {
                        MessageUtil.sendAdventureMessage(sender, text, placeholders, AdventureUtils.getItemDisplay(itemList));
                    } else {
                        String[] splitMessage = text.split("%list%");
                        List<BaseComponent> message = new ArrayList<>();
                        List<BaseComponent> components = new ArrayList<>();
                        for (int i = 0;i < itemList.size();i++) {
                            components.add(NMSManager.getBungeeJSONItemStack(itemList.get(i).getItemStack()));
                            if (i != itemList.size() - 1) {
                                components.add(new TextComponent(", "));
                            }
                        }
                        for (int i = 0;i < splitMessage.length;i++) {
                            message.add(new TextComponent(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders)));
                            if (i < splitMessage.length - 1 || text.endsWith("%list%")) {
                                message.addAll(components);
                            }
                        }
                        MessageUtil.sendBungeeJSONMessage(sender, message);
                    }
                } else {
                    MessageUtil.sendMessage(sender, text, placeholders);
                }
            });
        }
    }

    private void command_add(CommandSender sender, String[] args) {
        if (!LiteSignInUtils.hasCommandPermission(sender, SubCommandType.ADD.commandPermissionPath, true)) {
            return;
        }
        if (!LiteSignInUtils.isPlayer(sender, true)) {
            return;
        }
        if (args.length == 2) {
            MessageUtil.sendCommandMessage(sender, "ItemCollection.Add.Help");
        } else if (args.length >= 3) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            Player player = (Player) sender;
            ItemStack is = player.getItemInHand();
            if (is == null && is.getType().equals(Material.AIR)) {
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Add.Doesnt-Have-Item-In-Hand");
            } else if (CustomItem.addItemAsCollection(is, args[2])) {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendMessageWithItem(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully"), placeholders, is);
            } else {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Add.Already-Exist", placeholders);
            }
        }
    }

    private void command_delete(CommandSender sender, String[] args) {
        if (!LiteSignInUtils.hasCommandPermission(sender, SubCommandType.DELETE.commandPermissionPath, true)) {
            return;
        }
        if (args.length == 2) {
            MessageUtil.sendCommandMessage(sender, "ItemCollection.Delete.Help");
        } else if (args.length >= 3) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            CustomItem item = CustomItem.getCustomItem(args[2]);
            if (item != null) {
                item.delete();
                MessageUtil.sendMessageWithItem(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Delete.Successfully"), placeholders, item.getItemStack());
            } else {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Delete.Not-Exist", placeholders);
            }
        }
    }

    private void command_give(CommandSender sender, String[] args) {
        if (!LiteSignInUtils.hasCommandPermission(sender, SubCommandType.GIVE.commandPermissionPath, true)) {
            return;
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (args.length == 2) {
            MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Help");
        } else if (args.length == 3) {
            if (!LiteSignInUtils.isPlayer(sender, true)) {
                return;
            }
            CustomItem item = CustomItem.getCustomItem(args[2]);
            if (item == null) {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Not-Exist", placeholders);
            } else {
                item.give((Player) sender);
                MessageUtil.sendMessageWithItem(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Yourself"), placeholders, item.getItemStack());
            }
        } else if (args.length >= 4) {
            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                placeholders.put("{player}", args[3]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Player-Offline", placeholders);
                return;
            }
            CustomItem item = CustomItem.getCustomItem(args[2]);
            if (item == null) {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Not-Exist", placeholders);
            } else {
                item.give(player);
                MessageUtil.sendMessageWithItem(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Others"), placeholders, item.getItemStack());
            }
        }
    }

    private List<String> tab_delete(String[] args) {
        List<String> list = new ArrayList();
        if (args.length == 3) {
            CustomItem.getItemStackCollection().stream().filter(customItem -> customItem.getName().toLowerCase().startsWith(args[2].toLowerCase())).forEach(customItem -> {
                list.add(customItem.getName());
            });
        }
        return list;
    }

    private List<String> tab_give(String[] args) {
        List<String> list = new ArrayList();
        if (args.length == 3) {
            CustomItem.getItemStackCollection().stream().filter(customItem -> customItem.getName().toLowerCase().startsWith(args[2].toLowerCase())).forEach(customItem -> {
                list.add(customItem.getName());
            });
        } else if (args.length >= 4) {
            return tabGetPlayersName(args, 4);
        }
        return list;
    }
    
    public enum SubCommandType {
        /**
         * /signin itemcollection list
         */
        LIST("list", "ItemCollection.List"),
        
        /**
         * /signin itemcollection add
         */
        ADD("add", "ItemCollection.Add"),
        
        /**
         * /signin itemcollection delete
         */
        DELETE("delete", "ItemCollection.Delete"),
        
        /**
         * /signin itemcollection give
         */
        GIVE("give", "ItemCollection.Give");
        
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
