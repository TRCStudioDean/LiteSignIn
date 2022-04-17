package studio.trc.bukkit.litesignin.command.subcommand;

import java.lang.reflect.InvocationTargetException;
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
import studio.trc.bukkit.litesignin.config.MessageUtil;
import studio.trc.bukkit.litesignin.nms.JsonItemStack;
import studio.trc.bukkit.litesignin.util.CustomItem;
import studio.trc.bukkit.litesignin.util.SignInPluginUtils;

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
                    .filter(type -> SignInPluginUtils.hasCommandPermission(sender, type.getCommandPermissionPath(), false))
                    .map(type -> type.getCommandName())
                    .collect(Collectors.toList());
            List<String> names = new ArrayList();
            commands.stream().filter(command -> command.toLowerCase().startsWith(subCommandType.toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        } else {
            if (subCommandType.equalsIgnoreCase("delete") && SignInPluginUtils.hasCommandPermission(sender, SubCommandType.DELETE.commandPermissionPath, false)) {
                return tab_delete(args);
            }
            if (subCommandType.equalsIgnoreCase("give") && SignInPluginUtils.hasCommandPermission(sender, SubCommandType.GIVE.commandPermissionPath, false)) {
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
        if (!SignInPluginUtils.hasCommandPermission(sender, SubCommandType.LIST.commandPermissionPath, true)) {
            return;
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        List<CustomItem> itemList = CustomItem.getItemStackCollection();
        if (itemList.isEmpty()) {
            MessageUtil.sendCommandMessage(sender, "ItemCollection.List.Empty");
        } else {
            for (String text : MessageUtil.getMessageList("Command-Messages.ItemCollection.List.Messages")) {
                if (text.toLowerCase().contains("%list%")) {
                    if (!(sender instanceof Player)) {
                        StringBuilder list = new StringBuilder();
                        itemList.stream().map(items -> {
                            String name;
                            try {
                                name = items.getItemStack().getItemMeta().hasDisplayName() ? items.getItemStack().getItemMeta().getDisplayName() : (String) items.getClass().getMethod("getI18NDisplayName").invoke(items.getItemStack());
                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                name = items.getItemStack().getItemMeta().hasDisplayName() ? items.getItemStack().getItemMeta().getDisplayName() : items.getItemStack().getType().toString().toLowerCase().replace("_", " ");
                            }
                            return name;
                        }).forEach(name -> {
                            list.append(name).append(", ");
                        });
                        placeholders.put("%list%", list.toString());
                        placeholders.put("{amount}", String.valueOf(itemList.size()));
                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, text, placeholders)));
                        continue;
                    }
                    String[] splitMessage = text.split("%list%");
                    List<BaseComponent> bc = new ArrayList();
                    for (int i = 0;i < splitMessage.length;i++) {
                        placeholders.put("{amount}", String.valueOf(itemList.size()));
                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders))));
                        if (i < splitMessage.length - 1 || text.endsWith("%list%")) {
                            bc.addAll(Arrays.asList(JsonItemStack.getJsonItemStackArray(itemList)));
                        }
                    }
                    MessageUtil.sendJsonMessage(sender, bc);
                } else {
                    placeholders.put("{amount}", String.valueOf(itemList.size()));
                    sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, text, placeholders)));
                }
            }
        }
    }

    private void command_add(CommandSender sender, String[] args) {
        if (!SignInPluginUtils.hasCommandPermission(sender, SubCommandType.ADD.commandPermissionPath, true)) {
            return;
        }
        if (!SignInPluginUtils.isPlayer(sender, true)) {
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
                if (MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully").toLowerCase().contains("%item%")) {
                    if (!(sender instanceof Player)) {
                        String name;
                        try {
                            name = is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : (String) is.getClass().getMethod("getI18NDisplayName").invoke(is);
                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            name = is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : is.getType().toString().toLowerCase().replace("_", " ");
                        }
                        placeholders.put("%item%", name);
                        placeholders.put("{name}", args[2]);
                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully"), placeholders)));
                        return;
                    }
                    String[] splitMessage = MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully").split("%item%");
                    List<BaseComponent> bc = new ArrayList();
                    for (int i = 0;i < splitMessage.length;i++) {
                        placeholders.put("{name}", args[2]);
                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders))));
                        if (i < splitMessage.length - 1 || MessageUtil.getMessage("Command-Messages.ItemCollection.Add.Successfully").endsWith("%item%")) {
                            bc.add(JsonItemStack.getJsonItemStack(is));
                        }
                    }
                    MessageUtil.sendJsonMessage(sender, bc);
                } else {
                    placeholders.put("{name}", args[2]);
                    MessageUtil.sendCommandMessage(sender, "ItemCollection.Add.Successfully", placeholders);
                }
            } else {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Add.Already-Exist", placeholders);
            }
        }
    }

    private void command_delete(CommandSender sender, String[] args) {
        if (!SignInPluginUtils.hasCommandPermission(sender, SubCommandType.DELETE.commandPermissionPath, true)) {
            return;
        }
        if (args.length == 2) {
            MessageUtil.sendCommandMessage(sender, "ItemCollection.Delete.Help");
        } else if (args.length >= 3) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
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
                        placeholders.put("%item%", name); 
                        placeholders.put("{name}", args[2]);
                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Delete.Successfully"), placeholders)));
                        return;
                    }
                    String[] splitMessage = MessageUtil.getMessage("Command-Messages.ItemCollection.Delete.Successfully").split("%item%");
                    List<BaseComponent> bc = new ArrayList();
                    for (int i = 0;i < splitMessage.length;i++) {
                        placeholders.put("{name}", args[2]);
                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders))));
                        if (i < splitMessage.length - 1 || MessageUtil.getMessage("Command-Messages.ItemCollection.Delete.Successfully").endsWith("%item%")) {
                            bc.add(JsonItemStack.getJsonItemStack(item.getItemStack()));
                        }
                    }
                    MessageUtil.sendJsonMessage(sender, bc);
                    item.delete();
                } else {
                    placeholders.put("{name}", args[2]);
                    MessageUtil.sendCommandMessage(sender, "ItemCollection.Delete.Successfully", placeholders);
                }
            } else {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Delete.Not-Exist", placeholders);
            }
        }
    }

    private void command_give(CommandSender sender, String[] args) {
        if (!SignInPluginUtils.hasCommandPermission(sender, SubCommandType.GIVE.commandPermissionPath, true)) {
            return;
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (args.length == 2) {
            MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Help");
        } else if (args.length == 3) {
            if (!SignInPluginUtils.isPlayer(sender, true)) {
                return;
            }
            CustomItem ci = CustomItem.getCustomItem(args[2]);
            if (ci == null) {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Not-Exist", placeholders);
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
                        placeholders.put("%item%", name); 
                        placeholders.put("{name}", args[2]);
                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Yourself"), placeholders)));
                        return;
                    }
                    String[] splitMessage = MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Yourself").split("%item%");
                    List<BaseComponent> bc = new ArrayList();
                    for (int i = 0;i < splitMessage.length;i++) {
                        placeholders.put("{name}", args[2]);
                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, splitMessage[i], placeholders))));
                        if (i < splitMessage.length - 1 || MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Yourself").endsWith("%item%")) {
                            bc.add(JsonItemStack.getJsonItemStack(ci.getItemStack()));
                        }
                    }
                    MessageUtil.sendJsonMessage(sender, bc);
                } else {
                    placeholders.put("{name}", args[2]);
                    MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Give-Yourself", placeholders);
                }
            }
        } else if (args.length >= 4) {
            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                placeholders.put("{player}", args[3]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Player-Offline", placeholders);
                return;
            }
            CustomItem ci = CustomItem.getCustomItem(args[2]);
            if (ci == null) {
                placeholders.put("{name}", args[2]);
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Not-Exist", placeholders);
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
                        placeholders.put("%item%", name);
                        placeholders.put("{player}", player.getName());
                        placeholders.put("{name}", args[2]);
                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Others"), placeholders)));
                        return;
                    }
                    String[] splitMessage = MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Others").split("%item%");
                    List<BaseComponent> bc = new ArrayList();
                    for (int i = 0;i < splitMessage.length;i++) {
                        placeholders.put("{player}", player.getName());
                        placeholders.put("{name}", args[2]);
                        bc.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, splitMessage[i], placeholders))));
                        if (i < splitMessage.length - 1 || MessageUtil.getMessage("Command-Messages.ItemCollection.Give.Give-Others").endsWith("%item%")) {
                            bc.add(JsonItemStack.getJsonItemStack(ci.getItemStack()));
                        }
                    }
                    MessageUtil.sendJsonMessage(sender, bc);
                } else {
                    placeholders.put("{player}", player.getName());
                    placeholders.put("{name}", args[2]);
                    MessageUtil.sendCommandMessage(sender, "ItemCollection.Give.Give-Others", placeholders);
                }
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
