package studio.trc.bukkit.litesignin.queue;

import java.util.UUID;

import studio.trc.bukkit.litesignin.util.SignInDate;

public class SignInQueueElement
{
    private final UUID uuid;
    private final SignInDate date;
    private String name = null;
    
    public SignInQueueElement(UUID uuid, SignInDate date) {
        this.uuid = uuid;
        this.date = date;
    }
    
    public SignInQueueElement(UUID uuid, SignInDate date, String name) {
        this.uuid = uuid;
        this.date = date;
        this.name = name;
    }
    
    public UUID getUUID() {
        return uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public SignInDate getSignInDate() {
        return date;
    }
    
    @Override
    public String toString() {
        return uuid.toString() + ":" + date.getDataText() + "-" + date.getHour() + "-" + date.getMinute() + "-" + date.getSecond() + ":" + name;
    }
}
