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
    public void onJoin(PlayerJoinEvent e) {
        if (BackupUtil.isBackingUp()) {
            return;
        }
        Player player = e.getPlayer();
        new Thread(() -> {
            Storage data = Storage.getPlayer(player);
            boolean unableToHoldCards = false;
            boolean autoSignIn = false;
            if (!PluginControl.hasPermission(player, "Permissions.Retroactive-Card.Hold")) {
                unableToHoldCards = true;
            }
            if (PluginControl.enableJoinEvent()) {
                if (!data.alreadySignIn()) {
                    if (PluginControl.autoSignIn() && PluginControl.hasPermission(player, "Permissions.Join-Auto-SignIn")) {
                        autoSignIn = true;
                    } else {
                        SignInDate date = SignInDate.getInstance(new Date());
                        for (String text : MessageUtil.getMessageList("Join-Event.Messages")) {
                            if (text.toLowerCase().contains("%opengui%")) {
                                BaseComponent click = new TextComponent(MessageUtil.getMessage("Join-Event.Open-GUI"));
                                List<BaseComponent> hoverText = new ArrayList();
                                int end = 0;
                                List<String> array = MessageUtil.getMessageList("Join-Event.Hover-Text");
                                for (String hover : array) {
                                    end++;
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("{date}", date.getName(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
                                    hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, hover, placeholders))));
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
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("{date}", date.getName(ConfigurationUtil.getConfig(ConfigurationType.GUISETTINGS).getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
                                player.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, text, placeholders)));
                            }
                        }
                    }
                }
            }
            schedule(data, player, unableToHoldCards, autoSignIn);
        }, "AsyncPlayerJoinThread").start();
        if (CheckUpdater.isFoundANewVersion() && PluginControl.enableUpdater()) {
            if (PluginControl.hasPermission(player, "Permissions.Updater")) {
                String nowVersion = Bukkit.getPluginManager().getPlugin("LiteSignIn").getDescription().getVersion();
                MessageUtil.getMessageList("Updater.Checked").stream().forEach(text -> {
                    if (text.toLowerCase().contains("%link%")) {
                        BaseComponent click = new TextComponent(MessageUtil.getMessage("Updater.Link.Message"));
                        List<BaseComponent> hoverText = new ArrayList();
                        int end = 0;
                        List<String> array = MessageUtil.getMessageList("Updater.Link.Hover-Text");
                        for (String hover : array) {
                            end++;
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("{nowVersion}", nowVersion);
                            placeholders.put("{version}", CheckUpdater.getNewVersion());
                            placeholders.put("{link}", CheckUpdater.getLink());
                            placeholders.put("{description}", CheckUpdater.getDescription());
                            hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, hover, placeholders))));
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
                        Map<String, String> placeholders = new HashMap();
                            placeholders.put("{nowVersion}", nowVersion);
                            placeholders.put("{version}", CheckUpdater.getNewVersion());
                            placeholders.put("{link}", CheckUpdater.getLink());
                            placeholders.put("{description}", CheckUpdater.getDescription());
                        player.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(player, text, placeholders)));
                    }
                });
            }
        }
    }
    
    public void schedule(Storage data, Player player, boolean unableToHoldCards, boolean autoSignIn) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (unableToHoldCards) {
                    if (data.getRetroactiveCard() > 0) {
                        data.takeRetroactiveCard(data.getRetroactiveCard());
                        MessageUtil.sendMessage(player, "GUI-SignIn-Messages.Unable-To-Hold");
                    }
                }
                if (autoSignIn) {
                    data.signIn();
                }
            }
        }.runTask(Main.getInstance());
    }
}
