package studio.trc.bukkit.litesignin.command.subcommand;

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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.config.ConfigurationType;
import studio.trc.bukkit.litesignin.config.ConfigurationUtil;
import studio.trc.bukkit.litesignin.util.MessageUtil;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.queue.SignInQueueElement;
import studio.trc.bukkit.litesignin.thread.LiteSignInThread;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.SignInPluginUtils;

public class LeaderboardCommand
    implements SignInSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        Runnable task = () -> {};
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        SignInDate today = SignInDate.getInstance(new Date());
        if (args.length == 1) {
            int page = 1;
            int numberOfSinglePage = 10;
            try {
                numberOfSinglePage = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
            } catch (NumberFormatException ex) {}
            final int finalPage = page;
            final int finalNumberOfSinglePage = numberOfSinglePage;
            task = () -> sendLeaderBoard(sender, today, finalPage, finalNumberOfSinglePage);
        } else if (args.length == 2) {
            SignInDate date = SignInDate.getInstance(args[1]);
            if (date != null) {
                if (!date.equals(today)) {
                    if (!SignInPluginUtils.hasCommandPermission(sender, "LeaderBoard.Designated-Date", true)) {
                        return;
                    }
                    if (!PluginControl.useMySQLStorage() && !PluginControl.useSQLiteStorage()) {
                        MessageUtil.sendCommandMessage(sender, "LeaderBoard.Non-Database-mode");
                        return;
                    }
                    int page = 1;
                    int numberOfSinglePage = 10;
                    try {
                        numberOfSinglePage = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                    } catch (NumberFormatException ex) {}
                    final int finalPage = page;
                    final int finalNumberOfSinglePage = numberOfSinglePage;
                    task = () -> sendLeaderBoard(sender, date, finalPage, finalNumberOfSinglePage);
                } else {
                    int page = 1;
                    int numberOfSinglePage = 10;
                    try {
                        numberOfSinglePage = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                    } catch (NumberFormatException ex) {}
                    final int finalPage = page;
                    final int finalNumberOfSinglePage = numberOfSinglePage;
                    task = () -> sendLeaderBoard(sender, date, finalPage, finalNumberOfSinglePage);
                }
            } else {
                placeholders.put("{date}", args[1]);
                MessageUtil.sendCommandMessage(sender, "LeaderBoard.Date-Not-Exist", placeholders);
                return;
            }
        } else if (args.length >= 3) {
            SignInDate date = SignInDate.getInstance(args[1]);
            if (date != null) {
                if (!date.equals(today)) {
                    if (!SignInPluginUtils.hasCommandPermission(sender, "LeaderBoard.Designated-Date", true)) {
                        return;
                    }
                    if (!PluginControl.useMySQLStorage() && !PluginControl.useSQLiteStorage()) {
                        MessageUtil.sendCommandMessage(sender, "LeaderBoard.Non-Database-mode");
                        return;
                    }
                    int page;
                    int numberOfSinglePage = 10;
                    try {
                        page = Integer.valueOf(args[2]);
                    } catch (NumberFormatException ex) {
                        placeholders.put("{page}", args[2]);
                        MessageUtil.sendCommandMessage(sender, "LeaderBoard.Invalid-Number", placeholders);
                        return;
                    }
                    try {
                        numberOfSinglePage = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                    } catch (NumberFormatException ex) {}
                    final int finalPage = page;
                    final int finalNumberOfSinglePage = numberOfSinglePage;
                    task = () -> sendLeaderBoard(sender, date, finalPage, finalNumberOfSinglePage);
                } else {
                    int page;
                    int numberOfSinglePage = 10;
                    try {
                        page = Integer.valueOf(args[2]);
                    } catch (NumberFormatException ex) {
                        placeholders.put("{page}", args[2]);
                        MessageUtil.sendCommandMessage(sender, "LeaderBoard.Invalid-Number", placeholders);
                        return;
                    }
                    try {
                        numberOfSinglePage = Integer.valueOf(MessageUtil.getMessage("Command-Messages.LeaderBoard.Number-Of-Single-Page"));
                    } catch (NumberFormatException ex) {}
                    final int finalPage = page;
                    final int finalNumberOfSinglePage = numberOfSinglePage;
                    task = () -> sendLeaderBoard(sender, date, finalPage, finalNumberOfSinglePage);
                }
            } else {
                placeholders.put("{date}", args[1]);
                MessageUtil.sendCommandMessage(sender, "LeaderBoard.Date-Not-Exist", placeholders);
                return;
            }
        }
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Async-Thread-Settings.Async-Task-Settings.Leaderboard-View")) {
            LiteSignInThread.runTask(task);
        } else {
            task.run();
        }
    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        return new ArrayList();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.LEADERBOARD;
    }

    private void sendLeaderBoard(CommandSender sender, SignInDate date, int page, int nosp) {
        SignInQueue queue = SignInQueue.getInstance(date);
        if (queue.isEmpty()) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
            MessageUtil.sendCommandMessage(sender, "LeaderBoard.Empty", placeholders);
            return;
        }
        int maxPage = queue.size() % nosp == 0 ? queue.size() / nosp : queue.size() / nosp + 1;
        if (page > maxPage) {
            page = 1;
        }
        if (page <= 0) {
            page = maxPage;
        }
        boolean today = date.equals(SignInDate.getInstance(new Date()));
        String listFormatPath = today ? "Today" : "Historical-Date";
        for (String message : today ? 
                MessageUtil.getMessageList("Command-Messages.LeaderBoard.LeaderBoard-Messages") : 
                MessageUtil.getMessageList("Command-Messages.LeaderBoard.Historical-Date-LeaderBoard-Messages")) {
            if (message.toLowerCase().contains("%leaderboard%")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int ranking = queue.getRank(player.getUniqueId());
                    for (int rank = page * nosp - nosp + 1;rank <= queue.size() && rank <= page * nosp;rank++) {
                        List<SignInQueueElement> userArray = queue.getRankingUser(rank);
                        if (userArray.isEmpty()) continue;
                        if (ranking != rank) {
                            if (userArray.size() == 1) {
                                SignInQueueElement element = userArray.get(0);
                                String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players").toLowerCase().contains("%player%")) {
                                    String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                                    if (name == null) {
                                        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                        if (offlineplayer != null) {
                                            name = offlineplayer.getName();
                                        }
                                    }
                                    if (name != null) {
                                        Map<String, String> playerName = new HashMap();
                                        playerName.put("{player}", name);
                                        BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), playerName));
                                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                        List<BaseComponent> hoverText = new ArrayList();
                                        int end = 0;
                                        List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                        for (String hover : array) {
                                            end++;
                                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                            placeholders.put("{player}", name);
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                            if (end != array.size()) {
                                                hoverText.add(new TextComponent("\n"));
                                            }
                                        }
                                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                        click.setClickEvent(ce);
                                        click.setHoverEvent(he);
                                        Map<String, BaseComponent> baseComponents = new HashMap();
                                        baseComponents.put("%player%", click);
                                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                        placeholders.put("{player}", name);
                                        placeholders.put("{total}", String.valueOf(queue.size()));
                                        placeholders.put("{date}", dateName);
                                        placeholders.put("{ranking}", String.valueOf(rank));
                                        placeholders.put("{time}", timeName);
                                        placeholders.put("{page}", String.valueOf(page));
                                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                                        MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players", placeholders, baseComponents);
                                    } else {
                                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                        Map<String, String> uuid = new HashMap();
                                        uuid.put("{uuid}", element.getUUID().toString());
                                        placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                        placeholders.put("{total}", String.valueOf(queue.size()));
                                        placeholders.put("{date}", dateName);
                                        placeholders.put("{ranking}", String.valueOf(rank));
                                        placeholders.put("{time}", timeName);
                                        placeholders.put("{page}", String.valueOf(page));
                                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                                        MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players", placeholders);
                                    }
                                } else {
                                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                    placeholders.put("{total}", String.valueOf(queue.size()));
                                    placeholders.put("{date}", dateName);
                                    placeholders.put("{ranking}", String.valueOf(rank));
                                    placeholders.put("{time}", timeName);
                                    placeholders.put("{page}", String.valueOf(page));
                                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                                    MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players", placeholders);
                                }
                            } else {
                                for (SignInQueueElement user : userArray) {
                                    SignInQueueElement element = user;
                                    String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                    String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                    if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players").toLowerCase().contains("%player%")) {
                                        String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                                        if (name == null) {
                                            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                            if (offlineplayer != null) {
                                                name = offlineplayer.getName();
                                            }
                                        }
                                        if (name != null) {
                                            Map<String, String> playerName = new HashMap();
                                            playerName.put("{player}", name);
                                            BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), playerName));
                                            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                            List<BaseComponent> hoverText = new ArrayList();
                                            int end = 0;
                                            List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                            for (String hover : array) {
                                                end++;
                                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                                placeholders.put("{player}", name);
                                                placeholders.put("{total}", String.valueOf(queue.size()));
                                                placeholders.put("{date}", dateName);
                                                placeholders.put("{ranking}", String.valueOf(rank));
                                                placeholders.put("{time}", timeName);
                                                placeholders.put("{page}", String.valueOf(page));
                                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                                if (end != array.size()) {
                                                    hoverText.add(new TextComponent("\n"));
                                                }
                                            }
                                            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                            click.setClickEvent(ce);
                                            click.setHoverEvent(he);
                                            Map<String, BaseComponent> baseComponents = new HashMap();
                                            baseComponents.put("%player%", click);
                                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                            placeholders.put("{player}", name);
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            MessageUtil.sendMessage(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders, baseComponents);
                                        } else {
                                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                            Map<String, String> uuid = new HashMap();
                                            uuid.put("{uuid}", element.getUUID().toString());
                                            placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                                        }
                                    } else {
                                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                        placeholders.put("{total}", String.valueOf(queue.size()));
                                        placeholders.put("{date}", dateName);
                                        placeholders.put("{ranking}", String.valueOf(rank));
                                        placeholders.put("{time}", timeName);
                                        placeholders.put("{page}", String.valueOf(page));
                                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                                        sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                                    }
                                }
                            }
                        } else {
                            if (userArray.size() == 1) {
                                String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                String timeName = queue.getElement(player.getUniqueId()).getSignInDate().hasTimePeriod() ? queue.getElement(player.getUniqueId()).getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Self").toLowerCase().contains("%player%")) {
                                    String name = player.getName();
                                    Map<String, String> playerName = new HashMap();
                                    playerName.put("{player}", name);
                                    BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Self"), playerName));
                                    ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                    List<BaseComponent> hoverText = new ArrayList();
                                    int end = 0;
                                    List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                    for (String hover : array) {
                                        end++;
                                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                        placeholders.put("{player}", name);
                                        placeholders.put("{total}", String.valueOf(queue.size()));
                                        placeholders.put("{date}", dateName);
                                        placeholders.put("{ranking}", String.valueOf(rank));
                                        placeholders.put("{time}", timeName);
                                        placeholders.put("{page}", String.valueOf(page));
                                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                                        hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                        if (end != array.size()) {
                                            hoverText.add(new TextComponent("\n"));
                                        }
                                    }
                                    HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                    click.setHoverEvent(he);
                                    click.setClickEvent(ce);
                                    Map<String, BaseComponent> baseComponents = new HashMap();
                                    baseComponents.put("%player%", click);
                                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                    placeholders.put("{player}", name);
                                    placeholders.put("{total}", String.valueOf(queue.size()));
                                    placeholders.put("{date}", dateName);
                                    placeholders.put("{ranking}", String.valueOf(rank));
                                    placeholders.put("{time}", timeName);
                                    placeholders.put("{page}", String.valueOf(page));
                                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                                    MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Self", placeholders, baseComponents);
                                } else {
                                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                    placeholders.put("{total}", String.valueOf(queue.size()));
                                    placeholders.put("{date}", dateName);
                                    placeholders.put("{ranking}", String.valueOf(rank));
                                    placeholders.put("{time}", timeName);
                                    placeholders.put("{page}", String.valueOf(page));
                                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                                    MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Self", placeholders);
                                }
                            } else {
                                for (SignInQueueElement user : userArray) {
                                    if (user.getUUID().equals(player.getUniqueId())) {
                                        String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                        String timeName = queue.getElement(player.getUniqueId()).getSignInDate().hasTimePeriod() ? queue.getElement(player.getUniqueId()).getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                        if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Self").toLowerCase().contains("%player%")) {
                                            String name = player.getName();
                                            Map<String, String> playerName = new HashMap();
                                            playerName.put("{player}", name);
                                            BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Self"), playerName));
                                            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                            List<BaseComponent> hoverText = new ArrayList();
                                            int end = 0;
                                            List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                            for (String hover : array) {
                                                end++;
                                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                                placeholders.put("{player}", name);
                                                placeholders.put("{total}", String.valueOf(queue.size()));
                                                placeholders.put("{date}", dateName);
                                                placeholders.put("{ranking}", String.valueOf(rank));
                                                placeholders.put("{time}", timeName);
                                                placeholders.put("{page}", String.valueOf(page));
                                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                                if (end != array.size()) {
                                                    hoverText.add(new TextComponent("\n"));
                                                }
                                            }
                                            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                            click.setHoverEvent(he);
                                            click.setClickEvent(ce);
                                            Map<String, BaseComponent> baseComponents = new HashMap();
                                            baseComponents.put("%player%", click);
                                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                            placeholders.put("{player}", name);
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Self", placeholders, baseComponents);
                                        } else {
                                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Self", placeholders);
                                        }
                                    } else {
                                        SignInQueueElement element = user;
                                        String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
                                        String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                        if (MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players").toLowerCase().contains("%player%")) {
                                            String name = element.getName() != null ? element.getName() : null;
                                            if (name == null) {
                                                OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                                if (offlineplayer != null) {
                                                    name = offlineplayer.getName();
                                                }
                                            }
                                            if (name != null) {
                                                Map<String, String> playerName = new HashMap();
                                                playerName.put("{player}", name);
                                                BaseComponent click = new TextComponent(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), playerName));
                                                ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName));
                                                List<BaseComponent> hoverText = new ArrayList();
                                                int end = 0;
                                                List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover");
                                                for (String hover : array) {
                                                    end++;
                                                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                                    placeholders.put("{player}", name);
                                                    placeholders.put("{total}", String.valueOf(queue.size()));
                                                    placeholders.put("{date}", dateName);
                                                    placeholders.put("{ranking}", String.valueOf(rank));
                                                    placeholders.put("{time}", timeName);
                                                    placeholders.put("{page}", String.valueOf(page));
                                                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                    hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                                                    if (end != array.size()) {
                                                        hoverText.add(new TextComponent("\n"));
                                                    }
                                                }
                                                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                                                click.setClickEvent(ce);
                                                click.setHoverEvent(he);
                                                Map<String, BaseComponent> baseComponents = new HashMap();
                                                baseComponents.put("%player%", click);
                                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                                placeholders.put("{total}", String.valueOf(queue.size()));
                                                placeholders.put("{date}", dateName);
                                                placeholders.put("{ranking}", String.valueOf(rank));
                                                placeholders.put("{time}", timeName);
                                                placeholders.put("{page}", String.valueOf(page));
                                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                MessageUtil.sendMessage(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders, baseComponents);
                                            } else {
                                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                                Map<String, String> uuid = new HashMap();
                                                uuid.put("{uuid}", element.getUUID().toString());
                                                placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                                placeholders.put("{total}", String.valueOf(queue.size()));
                                                placeholders.put("{date}", dateName);
                                                placeholders.put("{ranking}", String.valueOf(rank));
                                                placeholders.put("{time}", timeName);
                                                placeholders.put("{page}", String.valueOf(page));
                                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                                sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                                            }
                                        } else {
                                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                            placeholders.put("{total}", String.valueOf(queue.size()));
                                            placeholders.put("{date}", dateName);
                                            placeholders.put("{ranking}", String.valueOf(rank));
                                            placeholders.put("{time}", timeName);
                                            placeholders.put("{page}", String.valueOf(page));
                                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                                            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (int rank = page * nosp - nosp + 1;rank <= queue.size() && rank <= page * nosp;rank++) {
                        List<SignInQueueElement> userArray = queue.getRankingUser(rank);
                        if (userArray.isEmpty()) continue;
                        //Whether there are multiple players signing in at the same time in the same second
                        if (userArray.size() == 1) {
                            SignInQueueElement element = userArray.get(0);
                            String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                            if (name == null) {
                                OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                if (offlineplayer != null) {
                                    name = offlineplayer.getName();
                                }
                            }
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                            String playerName;
                            if (name != null) {
                                Map<String, String> subPlaceholders = new HashMap();
                                subPlaceholders.put("{player}", name);
                                playerName = MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), subPlaceholders);
                            } else {
                                Map<String, String> subPlaceholders = new HashMap();
                                subPlaceholders.put("{uuid}", element.getUUID().toString());
                                playerName = MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), subPlaceholders);
                            }
                            placeholders.put("%player%", playerName);
                            placeholders.put("{total}", String.valueOf(queue.size()));
                            placeholders.put("{date}", element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                            placeholders.put("{ranking}", String.valueOf(rank));
                            String timeFormat;
                            if (element.getSignInDate().hasTimePeriod()) {
                                timeFormat = element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format"));
                            } else {
                                timeFormat = MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                            }
                            placeholders.put("{time}", timeFormat);
                            placeholders.put("{page}", String.valueOf(page));
                            placeholders.put("{maxPage}", String.valueOf(maxPage));
                            sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players"), placeholders)));
                        } else {
                            for (SignInQueueElement element : userArray) {
                                String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                                if (name == null) {
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                    if (offlineplayer != null) {
                                        name = offlineplayer.getName();
                                    }
                                }
                                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                                String playerName;
                                if (name != null) {
                                    Map<String, String> subPlaceholders = new HashMap();
                                    subPlaceholders.put("{player}", name);
                                    playerName = MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), subPlaceholders);
                                } else {
                                    Map<String, String> subPlaceholders = new HashMap();
                                    subPlaceholders.put("{uuid}", element.getUUID().toString());
                                    playerName = MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), subPlaceholders);
                                }
                                placeholders.put("%player%", playerName);
                                placeholders.put("{total}", String.valueOf(queue.size()));
                                placeholders.put("{date}", element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                                placeholders.put("{ranking}", String.valueOf(rank));
                                String timeFormat;
                                if (element.getSignInDate().hasTimePeriod()) {
                                    timeFormat = element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format"));
                                } else {
                                    timeFormat = MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                }
                                placeholders.put("{time}", timeFormat);
                                placeholders.put("{page}", String.valueOf(page));
                                placeholders.put("{maxPage}", String.valueOf(maxPage));
                                sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players"), placeholders)));
                            }
                        }
                    }
                }
            } else {
                if (!(sender instanceof Player)) {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("%previousPage%", MessageUtil.getMessage("Command-Messages.LeaderBoard.Previous-Page.Text"));
                    placeholders.put("%nextPage%", MessageUtil.getMessage("Command-Messages.LeaderBoard.Next-Page.Text"));
                    placeholders.put("{total}", String.valueOf(queue.size()));
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                    placeholders.put("{page}", String.valueOf(page));
                    placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                    placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                    sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, message, placeholders)));
                    continue;
                }
                Map<String, BaseComponent> baseComponents = new HashMap();
                if (message.toLowerCase().contains("%previouspage%")) {
                    BaseComponent click = new TextComponent(MessageUtil.getMessage("Command-Messages.LeaderBoard.Previous-Page.Text"));
                    List<BaseComponent> hoverText = new ArrayList();
                    int end = 0;
                    List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Previous-Page.Hover");
                    for (String hover : array) {
                        end++;
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{total}", String.valueOf(queue.size()));
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                        placeholders.put("{page}", String.valueOf(page));
                        placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                        placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                        hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                        if (end != array.size()) {
                            hoverText.add(new TextComponent("\n"));
                        }
                    }
                    HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                    ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/litesignin leaderboard " + date.getDataText(false) + " " + (page - 1));
                    click.setClickEvent(ce);
                    click.setHoverEvent(he);
                    baseComponents.put("%previousPage%", click);
                }
                if (message.toLowerCase().contains("%nextpage%")) {
                    BaseComponent click = new TextComponent(MessageUtil.getMessage("Command-Messages.LeaderBoard.Next-Page.Text"));
                    List<BaseComponent> hoverText = new ArrayList();
                    int end = 0;
                    List<String> array = MessageUtil.getMessageList("Command-Messages.LeaderBoard.Next-Page.Hover");
                    for (String hover : array) {
                        end++;
                        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                        placeholders.put("{total}", String.valueOf(queue.size()));
                        placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                        placeholders.put("{page}", String.valueOf(page));
                        placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                        placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                        placeholders.put("{maxPage}", String.valueOf(maxPage));
                        hoverText.add(new TextComponent(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, hover, placeholders))));
                        if (end != array.size()) {
                            hoverText.add(new TextComponent("\n"));
                        }
                    }
                    HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[] {}));
                    ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/litesignin leaderboard " + date.getDataText(false) + " " + (page + 1));
                    click.setClickEvent(ce);
                    click.setHoverEvent(he);
                    baseComponents.put("%nextPage%", click);
                }
                if (baseComponents.isEmpty()) {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{total}", String.valueOf(queue.size()));
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                    placeholders.put("{page}", String.valueOf(page));
                    placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                    placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                    sender.sendMessage(MessageUtil.toColor(MessageUtil.replacePlaceholders(sender, message, placeholders)));
                } else {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{total}", String.valueOf(queue.size()));
                    placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
                    placeholders.put("{page}", String.valueOf(page));
                    placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
                    placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
                    placeholders.put("{maxPage}", String.valueOf(maxPage));
                    MessageUtil.sendMessage(sender, message, placeholders, baseComponents);
                }
            }
        }
    }
}
