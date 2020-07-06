package studio.trc.bukkit.litesignin.event.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import studio.trc.bukkit.litesignin.reward.SignInRewardQueue;

public class SignInRewardEvent
    extends Event
    implements Cancellable
{
    public static HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final SignInRewardQueue rewardQueue;
    
    private boolean cancelled = false;
    
    public SignInRewardEvent(Player player, SignInRewardQueue rewardQueue) {
        this.player = player;
        this.rewardQueue = rewardQueue;
    }
    
    public SignInRewardQueue getRewardQueue() {
        return rewardQueue;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
