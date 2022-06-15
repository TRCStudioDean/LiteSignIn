package studio.trc.bukkit.litesignin.command.subcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.queue.SignInQueue;

public class InfoCommand
    implements SignInSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (args.length <= 1) {
            MessageUtil.sendCommandMessage(sender, "Info.Help");
        } else {
            Player player = Bukkit.getPlayer(args[1]);
            if (player != null) {
                Storage data = Storage.getPlayer(player.getUniqueId());
                placeholders.put("{player}", player.getName());
                placeholders.put("{group}", data.getGroup() != null ? data.getGroup().getGroupName() : MessageUtil.getMessage("Command-Messages.Info.Unknown-Group"));
                placeholders.put("{signin}", String.valueOf(data.alreadySignIn()).replace("true", MessageUtil.getMessage("Command-Messages.Info.true")).replace("false", MessageUtil.getMessage("Command-Messages.Info.false")));
                placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                placeholders.put("{total}", String.valueOf(data.getCumulativeNumber()));
                placeholders.put("{retroactivecard}", String.valueOf(data.getRetroactiveCard()));
                MessageUtil.sendCommandMessage(sender, "Info.Info", placeholders);
            } else {
                if (args[1].isEmpty()) {
                    placeholders.put("{player}", args[1]);
                    MessageUtil.sendCommandMessage(sender, "Click.Player-Not-Exist", placeholders);
                    return;
                }
                Storage data = Storage.getPlayer(args[1]);
                if (data != null) {
                    placeholders.put("{player}", data.getName() != null ? data.getName() : args[1]);
                    placeholders.put("{group}", data.getGroup() != null ? data.getGroup().getGroupName() : MessageUtil.getMessage("Command-Messages.Info.Unknown-Group"));
                    placeholders.put("{signin}", String.valueOf(data.alreadySignIn()).replace("true", MessageUtil.getMessage("Command-Messages.Info.true")).replace("false", MessageUtil.getMessage("Command-Messages.Info.false")));
                    placeholders.put("{continuous}", String.valueOf(data.getContinuousSignIn()));
                    placeholders.put("{queue}", String.valueOf(SignInQueue.getInstance().getRank(data.getUserUUID())));
                    placeholders.put("{total}", String.valueOf(data.getCumulativeNumber()));
                    placeholders.put("{retroactivecard}", String.valueOf(data.getRetroactiveCard()));
                    MessageUtil.sendCommandMessage(sender, "Info.Info", placeholders);
                } else {
                    placeholders.put("{player}", args[1]);
                    MessageUtil.sendCommandMessage(sender, "Info.Player-Not-Exist", placeholders);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "info";
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
        return SignInSubCommandType.INFO;
    }
}
