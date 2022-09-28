package studio.trc.bukkit.litesignin.thread;

import lombok.Getter;

public class LiteSignInTask
{
    @Getter
    private final Runnable task;
    @Getter
    private final long totalExecuteTimes;
    @Getter
    private final boolean onlyPlayersOnline;
    @Getter
    private long executeTimes = 0;

    public LiteSignInTask(Runnable task, long totalExecuteTimes) {
        this.task = task;
        this.totalExecuteTimes = totalExecuteTimes;
        onlyPlayersOnline = false;
    }

    public LiteSignInTask(Runnable task, long totalExecuteTimes, boolean onlyPlayersOnline) {
        this.task = task;
        this.totalExecuteTimes = totalExecuteTimes;
        this.onlyPlayersOnline = onlyPlayersOnline;
    }
    
    public void run() {
        task.run();
        if (totalExecuteTimes != -1) {
            executeTimes++;
        }
    }
}
