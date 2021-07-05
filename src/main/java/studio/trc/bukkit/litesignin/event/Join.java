package studio.trc.bukkit.litesignin.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.config.MessageUtil;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.updater.CheckUpdater;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.PluginControl;

public class Join
    implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void join(PlayerJoinEvent e) {
        if (BackupUtil.isBackingUp()) {
            return;
        }
        Player player = e.getPlayer();
        new Thread(() -> {
            Storage data = Storage.getPlayer(player);
            if (!PluginControl.hasPermission(player, "Permissions.Retroactive-Card.Hold")) {
                if (data.getRetroactiveCard() > 0) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            data.takeRetroactiveCard(data.getRetroactiveCard());
                            MessageUtil.sendMessage(player, "GUI-SignIn-Messages.Unable-To-Hold");
                        }
                    }.runTask(Main.getInstance());
                }
            }
            if (PluginControl.enableJoinReminderMessages() && !data.alreadySignIn()) {
                SignInDate date = SignInDate.getInstance(new Date());
                for (String text : MessageUtil.getMessageList("GUI-SignIn-Messages.Join-Messages")) {
                    if (text.contains("%opengui%")) {
                        BaseComponent click = new TextComponent(MessageUtil.getMessage("GUI-SignIn-Messages.Open-GUI"));
                        List<BaseComponent> hoverText = new ArrayList();
                        int end = 0;
                        List<String> array = MessageUtil.getMessageList("GUI-SignIn-Messages.Hover-Text");
                        for (String hover : array) {
                            end++;
                            hoverText.add(new TextComponent(MessageUtil.toColor(hover.replace("{date}", date.getName(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format"))).replace("{prefix}", PluginControl.getPrefix()))));
                            if (end != array.size()) {
                                hoverText.add(new TextComponent("\n"));
                            }
                        }
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/litesignin gui");
                        click.setClickEvent(ce);
                        click.setHoverEvent(he);
                        Map<String, BaseComponent> baseComponents = new HashMap();
                        baseComponents.put("%opengui%", click);
                        MessageUtil.sendJsonMessage(player, text, baseComponents);
                    } else {
                        player.sendMessage(MessageUtil.toColor(text.replace("{date}", date.getName(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format"))).replace("{prefix}", PluginControl.getPrefix())));
                    }
                }
            }
        }, "AsyncPlayerJoinThread").start();
        if (CheckUpdater.isFoundANewVersion() && PluginControl.enableUpdater()) {
            if (PluginControl.hasPermission(player, "Permissions.Updater")) {
                String nowVersion = Bukkit.getPluginManager().getPlugin("LiteSignIn").getDescription().getVersion();
                MessageUtil.getMessageList("Updater.Checked").stream().forEach(text -> {
                    if (text.contains("%link%")) {
                        BaseComponent click = new TextComponent(MessageUtil.getMessage("Updater.Link.Player-Text"));
                        List<BaseComponent> hoverText = new ArrayList();
                        int end = 0;
                        List<String> array = MessageUtil.getMessageList("Updater.Link.Hover-Text");
                        for (String hover : array) {
                            end++;
                            hoverText.add(new TextComponent(MessageUtil.toColor(hover.replace("{nowVersion}", nowVersion).replace("{version}", CheckUpdater.getNewVersion()).replace("{link}", CheckUpdater.getLink()).replace("{description}", CheckUpdater.getDescription()).replace("{prefix}", PluginControl.getPrefix()))));
                            if (end != array.size()) {
                                hoverText.add(new TextComponent("\n"));
                            }
                        }
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, CheckUpdater.getLink());
                        click.setClickEvent(ce);
                        click.setHoverEvent(he);
                        Map<String, BaseComponent> baseComponents = new HashMap();
                        baseComponents.put("%link%", click);
                        MessageUtil.sendJsonMessage(player, text, baseComponents);
                    } else {
                        player.sendMessage(MessageUtil.toColor(text.replace("{nowVersion}", nowVersion).replace("{version}", CheckUpdater.getNewVersion()).replace("{link}", CheckUpdater.getLink()).replace("{description}", CheckUpdater.getDescription()).replace("{prefix}", PluginControl.getPrefix())));
                    }
                });
            }
        }
    }
}
