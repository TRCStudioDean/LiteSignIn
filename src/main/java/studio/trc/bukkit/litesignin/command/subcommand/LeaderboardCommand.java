package studio.trc.bukkit.litesignin.command.subcommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.message.JSONComponent;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.queue.SignInQueueElement;
import studio.trc.bukkit.litesignin.thread.LiteSignInThread;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.LiteSignInUtils;

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
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
            final int finalPage = page;
            final int finalNumberOfSinglePage = numberOfSinglePage;
            task = () -> sendLeaderBoard(sender, today, finalPage, finalNumberOfSinglePage);
        } else if (args.length == 2) {
            SignInDate date = SignInDate.getInstance(args[1]);
            if (date != null) {
                if (!date.equals(today)) {
                    if (!LiteSignInUtils.hasCommandPermission(sender, "LeaderBoard.Designated-Date", true)) {
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
                    if (!LiteSignInUtils.hasCommandPermission(sender, "LeaderBoard.Designated-Date", true)) {
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
        return new ArrayList<>();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.LEADERBOARD;
    }
    
    private void sendLeaderBoard(CommandSender sender, SignInDate date, int page, int numberOfSinglePage) {
        SignInQueue queue = SignInQueue.getInstance(date);
        if (queue.isEmpty()) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{date}", date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format")));
            MessageUtil.sendCommandMessage(sender, "LeaderBoard.Empty", placeholders);
            return;
        }
        int arraySize = queue.size();
        int maxPage = arraySize % numberOfSinglePage == 0 ? arraySize / numberOfSinglePage : arraySize / numberOfSinglePage + 1;
        if (page > maxPage) {
            page = 1;
        }
        if (page <= 0) {
            page = maxPage;
        }
        boolean today = date.equals(SignInDate.getInstance(new Date()));
        String dateName = date.getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Date-Format"));
        int ranking = sender instanceof Player ? queue.getRank(((Player) sender).getUniqueId()) : -1;
        String listFormatPath = today ? "Today" : "Historical-Date";
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put("{date}", dateName);
        placeholders.put("{total}", String.valueOf(arraySize));
        placeholders.put("{page}", String.valueOf(page));
        placeholders.put("{previousPage}", String.valueOf(page == 1 ? maxPage : page - 1));
        placeholders.put("{nextPage}", String.valueOf(page == maxPage ? 1 : page + 1));
        placeholders.put("{maxPage}", String.valueOf(maxPage));
        for (String message : today ? 
                MessageUtil.getMessageList("Command-Messages.LeaderBoard.LeaderBoard-Messages") : 
                MessageUtil.getMessageList("Command-Messages.LeaderBoard.Historical-Date-LeaderBoard-Messages")) {
            if (message.toLowerCase().contains("%leaderboard%")) {
                for (int rank = page * numberOfSinglePage - numberOfSinglePage + 1; rank <= arraySize && rank <= page * numberOfSinglePage; rank++) {
                    List<SignInQueueElement> tiedForUsers = queue.getRankingUser(rank);
                    if (tiedForUsers.isEmpty()) continue;
                    if (ranking != rank) {
                        if (tiedForUsers.size() == 1) {
                            SignInQueueElement element = tiedForUsers.get(0);
                            String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                            String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                            if (name == null) {
                                OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                if (offlineplayer != null) {
                                    name = offlineplayer.getName();
                                }
                            }
                            placeholders.put("{ranking}", String.valueOf(rank));
                            placeholders.put("{time}", timeName);
                            if (name != null) {
                                placeholders.put("{player}", name);
                                JSONComponent component = new JSONComponent(
                                    MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), placeholders),
                                    MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover").stream().map(line -> MessageUtil.replacePlaceholders(sender, line, placeholders)).collect(Collectors.toList()),
                                    "RUN_COMMAND",
                                    "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), placeholders)
                                );
                                MessageUtil.sendCommandMessageWithJSONComponent(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players", placeholders, "%player%", component);
                            } else {
                                Map<String, String> uuid = new HashMap();
                                uuid.put("{uuid}", element.getUUID().toString());
                                placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                placeholders.put("{player}", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Other-Players", placeholders);
                            }
                        } else {
                            for (SignInQueueElement user : tiedForUsers) {
                                SignInQueueElement element = user;
                                String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                                if (name == null) {
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                    if (offlineplayer != null) {
                                        name = offlineplayer.getName();
                                    }
                                }
                                placeholders.put("{ranking}", String.valueOf(rank));
                                placeholders.put("{time}", timeName);
                                if (name != null) {
                                    Map<String, String> playerName = new HashMap();
                                    playerName.put("{player}", name);
                                    JSONComponent component = new JSONComponent(
                                        MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Other-Players"), playerName),
                                        MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover").stream().map(line -> MessageUtil.replacePlaceholders(sender, line, playerName)).collect(Collectors.toList()),
                                        "RUN_COMMAND",
                                        "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName)
                                    );
                                    MessageUtil.sendCommandMessageWithJSONComponent(sender, "LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players", placeholders, "%player%", component);
                                } else {
                                    Map<String, String> uuid = new HashMap();
                                    uuid.put("{uuid}", element.getUUID().toString());
                                    placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                    placeholders.put("{player}", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                    MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking.Other-Players", placeholders);
                                }
                            }
                        }
                    } else {
                        if (tiedForUsers.size() == 1) {
                            SignInQueueElement element = tiedForUsers.get(0);
                            String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                            String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                            if (name == null) {
                                OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                if (offlineplayer != null) {
                                    name = offlineplayer.getName();
                                }
                            }
                            placeholders.put("{ranking}", String.valueOf(rank));
                            placeholders.put("{time}", timeName);
                            if (name != null) {
                                placeholders.put("{player}", name);
                                JSONComponent component = new JSONComponent(
                                    MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text.Self"), placeholders),
                                    MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover").stream().map(line -> MessageUtil.replacePlaceholders(sender, line, placeholders)).collect(Collectors.toList()),
                                    "RUN_COMMAND",
                                    "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), placeholders)
                                );
                                MessageUtil.sendCommandMessageWithJSONComponent(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Self", placeholders, "%player%", component);
                            } else {
                                Map<String, String> uuid = new HashMap();
                                uuid.put("{uuid}", element.getUUID().toString());
                                placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                placeholders.put("{player}", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Usually.Self", placeholders);
                            }
                        } else {
                            for (SignInQueueElement user : tiedForUsers) {
                                String target;
                                if (user.getUUID().equals(((Player) sender).getUniqueId())) {
                                    target = "Self";
                                } else {
                                    target = "Other-Players";
                                }
                                SignInQueueElement element = user;
                                String timeName = element.getSignInDate().hasTimePeriod() ? element.getSignInDate().getName(MessageUtil.getMessage("Command-Messages.LeaderBoard.Time-Format")) : MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Time");
                                String name = element.getName() != null && !element.getName().equals("null") ? element.getName() : null;
                                if (name == null) {
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(element.getUUID());
                                    if (offlineplayer != null) {
                                        name = offlineplayer.getName();
                                    }
                                }
                                placeholders.put("{ranking}", String.valueOf(rank));
                                placeholders.put("{time}", timeName);
                                if (name != null) {
                                    Map<String, String> playerName = new HashMap();
                                    playerName.put("{player}", name);
                                    JSONComponent component = new JSONComponent(
                                        MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Text." + target), playerName),
                                        MessageUtil.getMessageList("Command-Messages.LeaderBoard.Player-Show.Hover").stream().map(line -> MessageUtil.replacePlaceholders(sender, line, playerName)).collect(Collectors.toList()),
                                        "RUN_COMMAND",
                                        "/" + MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Player-Show.Command"), playerName)
                                    );
                                    MessageUtil.sendCommandMessageWithJSONComponent(sender, "LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking." + target, placeholders, "%player%", component);
                                } else {
                                    Map<String, String> uuid = new HashMap();
                                    uuid.put("{uuid}", element.getUUID().toString());
                                    placeholders.put("%player%", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                    placeholders.put("{player}", MessageUtil.replacePlaceholders(sender, MessageUtil.getMessage("Command-Messages.LeaderBoard.Unknown-Player"), uuid));
                                    MessageUtil.sendCommandMessage(sender, "LeaderBoard.List-Format." + listFormatPath + ".Tiel-Ranking." + target, placeholders);
                                }
                            }
                        }
                    }
                }
            } else {
                Map<String, JSONComponent> components = new HashMap<>();
                components.put("%previousPage%", new JSONComponent(
                    MessageUtil.getMessage("Command-Messages.LeaderBoard.Previous-Page.Text"), 
                    MessageUtil.getMessageList("Command-Messages.LeaderBoard.Previous-Page.Hover"),
                    "RUN_COMMAND",
                    "/litesignin:signin leaderboard " + date.getDataText(false) + " " + (page - 1)
                ));
                components.put("%nextPage%", new JSONComponent(
                    MessageUtil.getMessage("Command-Messages.LeaderBoard.Next-Page.Text"), 
                    MessageUtil.getMessageList("Command-Messages.LeaderBoard.Next-Page.Hover"),
                    "RUN_COMMAND",
                    "/litesignin:signin leaderboard " + date.getDataText(false) + " " + (page + 1)
                ));
                MessageUtil.sendMixedMessage(sender, message, placeholders, components, placeholders);
            }
        }
    }
}
