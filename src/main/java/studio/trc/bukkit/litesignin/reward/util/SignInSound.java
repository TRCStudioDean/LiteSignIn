package studio.trc.bukkit.litesignin.reward.util;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SignInSound
{
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final boolean broadcast;
    
    public SignInSound(Sound sound, float volume, float pitch, boolean broadcast) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.broadcast = broadcast;
    }
    
    public void playSound(Player... players) {
        if (broadcast) {
            Bukkit.getOnlinePlayers().stream().forEach((player) -> {
                player.playSound(player.getLocation(), sound, volume, pitch);
            });
        } else {
            for (Player player : players) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }
    
    @Override
    public String toString() {
        return "[SignInSound] -> [Sound:" + sound.name() + "," + volume + "," + pitch + "," + broadcast + "]";
    }
}
