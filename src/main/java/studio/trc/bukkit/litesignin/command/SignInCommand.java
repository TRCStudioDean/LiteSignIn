package studio.trc.bukkit.litesignin.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.database.util.RollBackUtil;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.LiteSignInUtils;
import studio.trc.bukkit.litesignin.util.Updater;

public class SignInCommand
    implements CommandExecutor, TabCompleter
{
    @Getter
    private static final Map<String, SignInSubCommand> subCommands = new HashMap();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        checkUpdate();
        if (BackupUtil.isBackingUp()) {
            MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Database-Management.Backup.BackingUp");
            return true;
        }
        if (RollBackUtil.isRollingback()) {
            MessageUtil.sendMessage(sender, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Database-Management.RollBack.RollingBack");
            return true;
        }
        if (args.length == 0) {
            MessageUtil.sendCommandMessage(sender, "Unknown-Command");
        } else if (args.length >= 1) {
            callSubCommand(sender, args);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return getNormallyTabComplete(sender, args[0]);
        } else if (args.length > 1) {
            return tabComplete(sender, args);
        } else {
            return new ArrayList();
        }
    }
    
    private void callSubCommand(CommandSender sender, String[] args) {
        String subCommand = args[0].toLowerCase();
        if (subCommands.get(subCommand) == null) {
            MessageUtil.sendCommandMessage(sender, "Unknown-Command");
            return;
        }
        SignInSubCommand command = subCommands.get(subCommand);
        if (LiteSignInUtils.hasCommandPermission(sender, command.getCommandType().getCommandPermissionPath(), true)) command.execute(sender, subCommand, args);
    }
    
    private void checkUpdate() {
        if (PluginControl.enableUpdater()) {
            String now = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String checkUpdateTime = new SimpleDateFormat("yyyy-MM-dd").format(Updater.getTimeOfLastCheckUpdate());
            if (!now.equals(checkUpdateTime)) {
                Updater.checkUpdate();
            }
        }
    }
    
    private List<String> getCommands(CommandSender sender) {
        List<String> commands = new ArrayList();
        subCommands.values().stream().filter(command -> LiteSignInUtils.hasCommandPermission(sender, command.getCommandType().getCommandPermissionPath(), false)).forEach(command -> {
            commands.add(command.getName());
        });
        return commands;
    }
    
    private List<String> getNormallyTabComplete(CommandSender sender, String args) {
        List<String> commands = getCommands(sender);
        if (args != null) {
            List<String> names = new ArrayList();
            commands.stream().filter(command -> command.toLowerCase().startsWith(args.toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        }
        return commands;
    }
    
    private List<String> tabComplete(CommandSender sender, String[] args) {
        String subCommand = args[0].toLowerCase();
        if (subCommands.get(subCommand) == null) {
            return new ArrayList();
        }
        SignInSubCommand command = subCommands.get(subCommand);
        return LiteSignInUtils.hasCommandPermission(sender, command.getCommandType().getCommandPermissionPath(), false) ? subCommands.get(subCommand).tabComplete(sender, subCommand, args) : new ArrayList();
    }
}
