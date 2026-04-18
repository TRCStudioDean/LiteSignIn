package studio.trc.bukkit.litesignin.reward.command;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.util.BukkitSchedulerManager;
import studio.trc.bukkit.litesignin.util.PluginControl;

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
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{player}", player.getName());
        String processedCommand = MessageUtil.replacePlaceholders(player, command, placeholders);
        switch (type) {
            case PLAYER: {
                BukkitSchedulerManager.runBukkitTask(() -> player.performCommand(processedCommand), 0, player);
                break;
            }
            case OP: {
                if (player.isOp()) {
                    BukkitSchedulerManager.runBukkitTask(() -> player.performCommand(processedCommand), 0, player);
                } else {
                    BukkitSchedulerManager.runBukkitTask(() -> {
                        player.setOp(true);
                        try {
                            player.performCommand(processedCommand);
                        } catch (Throwable error) {
                            error.printStackTrace();
                        } finally {
                            player.setOp(false);
                        }
                    }, 0,player);
                }
                break;
            }
            case SERVER: {
                BukkitSchedulerManager.runBukkitTask(() -> Main.getInstance().getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand), 0, null);
                break;
            }
        }
    }
}
