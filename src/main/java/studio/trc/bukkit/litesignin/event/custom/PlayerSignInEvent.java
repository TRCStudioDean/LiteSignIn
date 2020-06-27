package studio.trc.bukkit.litesignin.event.custom;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import studio.trc.bukkit.litesignin.util.SignInDate;

public class PlayerSignInEvent
    extends Event
    implements Cancellable
{
    public static HandlerList handlers = new HandlerList();
    
    private boolean cancelled = false;
    private final UUID uuid;
    private final SignInDate date;
    private final boolean usingRetroactiveCard;
    
    public PlayerSignInEvent(UUID uuid, SignInDate date, boolean usingRetroactiveCard) {
        this.uuid = uuid;
        this.date = date;
        this.usingRetroactiveCard = usingRetroactiveCard;
    }
    
    public UUID getUUID() {
        return uuid;
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
    
    public SignInDate getDate() {
        return date;
    }
    
    public boolean usingRetroactiveCard() {
        return usingRetroactiveCard;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
