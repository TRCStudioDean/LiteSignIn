package studio.trc.bukkit.litesignin.event.custom;

import java.util.Date;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.gui.SignInInventory;

public class SignInGUIOpenEvent
    extends Event
    implements Cancellable
{
    public static HandlerList handlers = new HandlerList();
    
    private boolean cancelled = false;
    private int month = SignInDate.getInstance(new Date()).getMonth();
    private int year = SignInDate.getInstance(new Date()).getYear();
    
    private final Player player;
    private final SignInInventory inventory;
    
    public SignInGUIOpenEvent(Player player, SignInInventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }
    
    public SignInGUIOpenEvent(Player player, SignInInventory inventory, int month) {
        this.player = player;
        this.inventory = inventory;
        this.month = month;
    }
    
    public SignInGUIOpenEvent(Player player, SignInInventory inventory, int month, int year) {
        this.player = player;
        this.inventory = inventory;
        this.month = month;
        this.year = year;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public SignInInventory getInventory() {
        return inventory;
    }
    
    public int getMonth() {
        return month;
    }
    
    public int getYear() {
        return year;
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
