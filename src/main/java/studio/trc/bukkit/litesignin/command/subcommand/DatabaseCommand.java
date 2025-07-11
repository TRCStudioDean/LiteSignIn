package studio.trc.bukkit.litesignin.command.subcommand;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.database.util.RollBackUtil;
import studio.trc.bukkit.litesignin.message.JSONComponent;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.LiteSignInUtils;

public class DatabaseCommand
    implements SignInSubCommand
{
    private final Map<CommandSender, Confirm> confirmCache = new HashMap();
    
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (args.length == 1) {
            MessageUtil.sendCommandMessage(sender, "Database.Help");
        } else {
            String subCommandType = args[1];
            if (subCommandType.equalsIgnoreCase("help")) {
                MessageUtil.sendCommandMessage(sender, "ItemCollection.Help");
            } else if (subCommandType.equalsIgnoreCase("backup")) {
                command_backup(sender, args);
            } else if (subCommandType.equalsIgnoreCase("rollback")) {
                command_rollback(sender, args);
            } else if (subCommandType.equalsIgnoreCase("confirm")) {
                command_confirm(sender, args);
            } else {
                MessageUtil.sendCommandMessage(sender, "Database.Help");
            }
        }
    }

    @Override
    public String getName() {
        return "database";
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
            if (args[1].equalsIgnoreCase("rollback")) {
                List<String> list = new ArrayList();
                PluginControl.getBackupFiles().stream().filter(fileName -> fileName.toLowerCase().startsWith(args[2].toLowerCase())).forEach(fileName -> {
                    list.add(fileName);
                });
                return list;
            }
        }
        return new ArrayList();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.DATABASE;
    }

    private void command_backup(CommandSender sender, String[] args) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (LiteSignInUtils.hasCommandPermission(sender, SubCommandType.BACKUP.commandPermissionPath, true)) {
            if (!ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Backup.Enabled")) {
                return;
            }
            JSONComponent jsonComponent = new JSONComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.Database.Confirm.Button.Text"), new HashMap()), MessageUtil.getMessageList("Command-Messages.Database.Confirm.Button.Hover"), "RUN_COMMAND", "/litesignin:signin database confirm");
            MessageUtil.getMessageList("Command-Messages.Database.Confirm.Need-Confirm").stream().forEach(message -> MessageUtil.sendMessageWithJSONComponent(sender, message, placeholders, "%button%", jsonComponent));
            confirmCache.put(sender, Confirm.BACKUP);
        }
    }

    private void command_rollback(CommandSender sender, String[] args) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (!ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Rollback.Enabled")) {
            return;
        }
        if (args.length == 2) {
            MessageUtil.sendCommandMessage(sender, "Database.Rollback.Help");
            return;
        }
        File file = new File(ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Database-Management.Rollback.Backup-Folder-Path") + args[2]);
        if (!file.exists()) {
            placeholders.put("{file}", ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("Database-Management.Rollback.Backup-Folder-Path") + args[2]);
            MessageUtil.sendCommandMessage(sender, "Database.Rollback.File-Not-Exist", placeholders);
            return;
        }
        JSONComponent jsonComponent = new JSONComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.Database.Confirm.Button.Text"), new HashMap()), MessageUtil.getMessageList("Command-Messages.Database.Confirm.Button.Hover"), "RUN_COMMAND", "/litesignin:signin database confirm");
        MessageUtil.getMessageList("Command-Messages.Database.Confirm.Need-Confirm").stream().forEach(message -> MessageUtil.sendMessageWithJSONComponent(sender, message, placeholders, "%button%", jsonComponent));
        Confirm confirm = Confirm.ROLLBACK;
        confirm.setTargetFile(file);
        confirmCache.put(sender, confirm);
    }

    private void command_confirm(CommandSender sender, String[] args) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (confirmCache.containsKey(sender)) {
            switch (confirmCache.get(sender)) {
                case BACKUP: {
                    MessageUtil.sendCommandMessage(sender, "Database.Backup");
                    confirmCache.remove(sender);
                    if (sender instanceof Player) {
                        BackupUtil.startBackup(sender, Bukkit.getConsoleSender());
                    } else {
                        BackupUtil.startBackup(Bukkit.getConsoleSender());
                    }
                    break;
                }
                case ROLLBACK: {
                    placeholders.put("{file}", confirmCache.get(sender).getTargetFile().getPath());
                    MessageUtil.sendCommandMessage(sender, "Database.Rollback.Start-Rollback", placeholders);
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
            MessageUtil.sendCommandMessage(sender, "Database.Confirm.Invalid-Operation");
        }
    }
    
    public enum SubCommandType {
        /**
         * /signin database backup
         */
        BACKUP("backup", "Database.Backup"),
        
        /**
         * /signin database roolback
         */
        ROLLBACK("rollback", "Database.Rollback"),
        
        /**
         * /signin database confirm
         */
        CONFIRM("confirm", "Database");
        
        @Getter
        private final String commandName;
        @Getter
        private final String commandPermissionPath;
        
        private SubCommandType(String commandName, String commandPermissionPath) {
            this.commandName = commandName;
            this.commandPermissionPath = commandPermissionPath;
        }
    }
    
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
