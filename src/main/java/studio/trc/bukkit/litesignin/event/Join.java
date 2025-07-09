package studio.trc.bukkit.litesignin.event;

import java.util.Date;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.database.util.RollBackUtil;
import studio.trc.bukkit.litesignin.message.JSONComponent;
import studio.trc.bukkit.litesignin.thread.LiteSignInThread;
import studio.trc.bukkit.litesignin.util.OnlineTimeRecord;
import studio.trc.bukkit.litesignin.util.Updater;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.LiteSignInUtils;

public class Join
    implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        if (BackupUtil.isBackingUp() || RollBackUtil.isRollingback()) {
            return;
        }
        Player player = event.getPlayer();
        OnlineTimeRecord.getJoinTimeRecord().put(player.getUniqueId(), System.currentTimeMillis());
        Runnable task = () -> {
            if (LiteSignInUtils.checkInDisabledWorlds(player.getUniqueId())) return;
            Storage data = Storage.getPlayer(player);
            boolean unableToHoldCards = false;
            boolean autoSignIn = false;
            if (!LiteSignInUtils.hasPermission(player, "Retroactive-Card.Hold")) {
                unableToHoldCards = true;
            }
            if (PluginControl.enableJoinEvent()) {
                if (!data.alreadySignIn()) {
                    if (PluginControl.autoSignIn() && LiteSignInUtils.hasPermission(player, "Join-Auto-SignIn")) {
                        autoSignIn = true;
                    } else if (OnlineTimeRecord.getSignInRequirement(player) == -1) {
                        SignInDate date = SignInDate.getInstance(new Date());
                        MessageUtil.getMessageList("Join-Event.Messages").stream().forEach(text -> {
                            if (text.toLowerCase().contains("%opengui%")) {
                                JSONComponent jsonComponent = new JSONComponent(MessageUtil.getMessage("Join-Event.Open-GUI"), MessageUtil.getMessageList("Join-Event.Hover-Text"), "RUN_COMMAND", "/litesignin:signin gui");
                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                MessageUtil.sendMessageWithJSONComponent(player, text, placeholders, "%openGUI%", jsonComponent);
                            } else {
                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                placeholders.put("{date}", date.getName(ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
                                MessageUtil.sendMessage(player, text, placeholders);
                            }
                        });
                    }
                }
            }
            schedule(data, player, unableToHoldCards, autoSignIn);
        };
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Async-Thread-Settings.Async-Task-Settings.Load-Data")) {
            LiteSignInThread.runTask(task, ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("Join-Event.Delay"));
        } else {
            PluginControl.runBukkitTask(task, (long) (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getDouble("Join-Event.Delay") * 20));
        }
        if (Updater.isFoundANewVersion() && PluginControl.enableUpdater()) {
            if (LiteSignInUtils.hasPermission(player, "Updater")) {
                String nowVersion = Main.getInstance().getDescription().getVersion();
                MessageUtil.getMessageList("Updater.Checked").stream().forEach(text -> {
                    if (text.toLowerCase().contains("%link%")) {
                        JSONComponent jsonComponent = new JSONComponent(MessageUtil.getMessage("Updater.Link.Message"), MessageUtil.getMessageList("Updater.Link.Hover-Text"), "OPEN_URL", Updater.getLink());
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        MessageUtil.sendMessageWithJSONComponent(player, text, placeholders, "%link%", jsonComponent);
                    } else {
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{nowVersion}", nowVersion);
                        placeholders.put("{version}", Updater.getNewVersion());
                        placeholders.put("{link}", Updater.getLink());
                        placeholders.put("{description}", Updater.getDescription());
                        MessageUtil.sendMessage(player, text, placeholders);
                    }
                });
                if (!Updater.getExtraMessages().isEmpty()) {
                    Updater.getExtraMessages().forEach(message -> {
                        MessageUtil.sendMessage(player, message);
                    });
                }
            }
        }
    }
    
    public void schedule(Storage data, Player player, boolean unableToHoldCards, boolean autoSignIn) {
        PluginControl.runBukkitTask(() -> {
            if (unableToHoldCards) {
                if (data.getRetroactiveCard() > 0) {
                    data.takeRetroactiveCard(data.getRetroactiveCard());
                    MessageUtil.sendMessage(player, ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "GUI-SignIn-Messages.Unable-To-Hold");
                }
            }
            if (autoSignIn) {
                if (OnlineTimeRecord.getSignInRequirement(player) == -1) {
                    data.signIn();
                }
            }
        }, 0);
    }
}
