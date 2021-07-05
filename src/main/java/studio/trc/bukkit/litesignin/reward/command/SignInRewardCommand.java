package studio.trc.bukkit.litesignin.reward.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.config.MessageUtil;

public class SignInRewardCommand
{
    private final SignInRewardCommandType type;
    private final String command;

    public SignInRewardCommand(SignInRewardCommandType type, String command) {
        this.type = type;
        this.command = command;
    }

    public SignInRewardCommandType getCommandType() {
        return type;
    }

    public String getCommand() {
        return command;
    }

    public void runWithThePlayer(Player player) {
        switch (type) {
            case PLAYER: {
                player.performCommand(MessageUtil.toColor(MessageUtil.toPlaceholderAPIResult(command.replace("{player}", player.getName()), player)));
                break;
            }
            case OP: {
                if (player.isOp()) {
                    player.performCommand(MessageUtil.toColor(MessageUtil.toPlaceholderAPIResult(command.replace("{player}", player.getName()), player)));
                } else {
                    player.setOp(true);
                    player.performCommand(MessageUtil.toColor(MessageUtil.toPlaceholderAPIResult(command.replace("{player}", player.getName()), player)));
                    player.setOp(false);
                }
                break;
            }
            case SERVER: {
                Main.getInstance().getServer().dispatchCommand(Bukkit.getConsoleSender(), MessageUtil.toColor(MessageUtil.toPlaceholderAPIResult(command.replace("{player}", player.getName()), player)));
                break;
            }
        }
    }
}
