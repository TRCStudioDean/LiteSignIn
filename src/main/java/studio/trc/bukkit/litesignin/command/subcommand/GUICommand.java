package studio.trc.bukkit.litesignin.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.event.Menu;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.SignInPluginUtils;

public class GUICommand
    implements SignInSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!PluginControl.enableSignInGUI()) {
            MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Unavailable-Feature");
            return;
        }
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (SignInPluginUtils.isPlayer(sender, true)) {
            Player player = (Player) sender;
            if (SignInPluginUtils.checkInDisabledWorlds(player.getUniqueId())) {
                MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Unable-To-SignIn-In-Disabled-World");
                return;
            }
            SignInDate target = SignInDate.getInstance(new Date());
            if (args.length == 1) {
                Menu.openGUI(player);
                MessageUtil.sendCommandMessage(player, "GUI.Normal");
            } else if (args.length == 2) {
                if (SignInPluginUtils.hasCommandPermission(sender, "Designated-GUI", true)) {
                    for (int month = 1;month <= 12;month++) {
                        if (args[1].equals(String.valueOf(month))) {
                            target.setMonth(month);
                            if (PluginControl.enableGUILimitDate() && target.compareTo(PluginControl.getGUILimitedDate()) < 0) {
                                placeholders.put("{year}", String.valueOf(PluginControl.getGUILimitedDateYear()));
                                placeholders.put("{month}", String.valueOf(PluginControl.getGUILimitedDateMonth()));
                                MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Minimum-GUI-Date", placeholders);
                                return;
                            }
                            Menu.openGUI(player, month);
                            placeholders.put("{month}", String.valueOf(month));
                            MessageUtil.sendCommandMessage(player, "GUI.Normal", placeholders);
                            return;
                        }
                    }
                    placeholders.put("{month}", args[1]);
                    MessageUtil.sendCommandMessage(player, "GUI.Invalid-Month", placeholders);
                }
            } else if (args.length >= 3) {
                if (SignInPluginUtils.hasCommandPermission(sender, "Designated-GUI", true)) {
                    int month = 0;
                    boolean invalidMonth = true;
                    for (int i = 1;i <= 12;i++) {
                        if (args[1].equals(String.valueOf(i))) {
                            month = i;
                            invalidMonth = false;
                        }
                    }
                    if (invalidMonth) {
                        placeholders.put("{month}", args[1]);
                        MessageUtil.sendCommandMessage(player, "GUI.Invalid-Month", placeholders);
                        return;
                    }
                    int year;
                    try {
                        year = Integer.valueOf(args[2]);
                    } catch (NumberFormatException ex) {
                        placeholders.put("{year}", args[2]);
                        MessageUtil.sendCommandMessage(player, "GUI.Invalid-Year", placeholders);
                        return;
                    }
                    if (year < 1970 || year > SignInDate.getInstance(new Date()).getYear()) {
                        placeholders.put("{year}", args[2]);
                        MessageUtil.sendCommandMessage(player, "GUI.Invalid-Year", placeholders);
                        return;
                    }
                    target.setMonth(month);
                    target.setYear(year);
                    if (PluginControl.enableGUILimitDate() && target.compareTo(PluginControl.getGUILimitedDate()) < 0) {
                        placeholders.put("{year}", String.valueOf(PluginControl.getGUILimitedDateYear()));
                        placeholders.put("{month}", String.valueOf(PluginControl.getGUILimitedDateMonth()));
                        MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Minimum-GUI-Date", placeholders);
                        return;
                    }
                    Menu.openGUI(player, month, year);
                    placeholders.put("{month}", String.valueOf(month));
                    placeholders.put("{year}", String.valueOf(year));
                    MessageUtil.sendCommandMessage(player, "GUI.Specified-Year", placeholders);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        if (args.length == 2) {
            if (SignInPluginUtils.hasCommandPermission(sender, "Designated-GUI", false)) {
                return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
            }
        }
        return new ArrayList();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.GUI;
    }
}
