package studio.trc.bukkit.litesignin.util;

import java.util.Date;
import java.util.Random;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.queue.SignInQueue;

import org.bukkit.entity.Player;

public class Placeholders
    extends PlaceholderExpansion
{
    private final Random random = new Random();
    
    private static final Placeholders instance = new Placeholders();
    
    public Placeholders() {
        super();
    }
    
    public static Placeholders getInstance() {
        return instance;
    }
  
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.toLowerCase().startsWith("random")) {
            String[] random = identifier.split("_");
            try {
                int number1 = Integer.valueOf(random[1]);
                int number2 = Integer.valueOf(random[2]);
                if (number1 == number2) {
                    return String.valueOf(number1);
                } else if (number1 > number2) {
                    return String.valueOf(this.random.nextInt(number1 - number2 + 1) + number2);
                } else if (number2 > number1) {
                    return String.valueOf(this.random.nextInt(number2 - number1 + 1) + number1);
                }
            } catch (NumberFormatException ex) {
                return "0";
            }
        }
        if (player != null) {
            Storage data = Storage.getPlayer(player);
            if (identifier.equalsIgnoreCase("signed-in")) {
                return String.valueOf(data.alreadySignIn(SignInDate.getInstance(new Date())));
            } else if (identifier.equalsIgnoreCase("queue")) {
                return String.valueOf(SignInQueue.getInstance().getRank(player.getUniqueId()));
            } else if (identifier.equalsIgnoreCase("cards_amount")) {
                return String.valueOf(data.getRetroactiveCard());
            } else if (identifier.equalsIgnoreCase("group")) {
                return data.getGroup().getGroupName();
            } else if (identifier.equalsIgnoreCase("total")) {
                return String.valueOf(data.getCumulativeNumber());
            } else if (identifier.equalsIgnoreCase("continuity")) {
                return String.valueOf(data.getContinuousSignIn());
            } else if (identifier.equalsIgnoreCase("last_year")) {
                return String.valueOf(data.getYear());
            } else if (identifier.equalsIgnoreCase("last_month")) {
                return String.valueOf(data.getMonth());
            } else if (identifier.equalsIgnoreCase("last_day")) {
                return String.valueOf(data.getDay());
            } else if (identifier.equalsIgnoreCase("last_hour")) {
                return String.valueOf(data.getHour());
            } else if (identifier.equalsIgnoreCase("last_minute")) {
                return String.valueOf(data.getMinute());
            } else if (identifier.equalsIgnoreCase("last_second")) {
                return String.valueOf(data.getSecond());
            }
        }
        return null;
    }

    @Override
    public String getPlugin() {
        return "LiteSignIn";
    }

    @Override
    public String getIdentifier() {
        return "litesignin";
    }

    @Override
    public String getAuthor() {
        return "TRCRedstoner";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
