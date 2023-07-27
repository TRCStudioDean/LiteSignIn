package studio.trc.bukkit.litesignin.thread;

import lombok.Getter;

public class LiteSignInTask
{
    @Getter
    private final Runnable task;
    @Getter
    private final long totalExecuteTimes;
    @Getter
    private final long tickInterval;
    @Getter
    private final boolean onlyPlayersOnline;
    @Getter
    private long executeTimes = 0;
    @Getter
    private long tickedTimes = 0;

    public LiteSignInTask(Runnable task, long totalExecuteTimes, long tickInterval) {
        this.task = task;
        this.totalExecuteTimes = totalExecuteTimes;
        this.tickInterval = tickInterval;
        onlyPlayersOnline = false;
    }

    public LiteSignInTask(Runnable task, long totalExecuteTimes, long tickInterval, boolean onlyPlayersOnline) {
        this.task = task;
        this.totalExecuteTimes = totalExecuteTimes;
        this.tickInterval = tickInterval;
        this.onlyPlayersOnline = onlyPlayersOnline;
    }
    
    public void run() {
        if (totalExecuteTimes != -1 || tickInterval > 0) {
            tickedTimes++;
        }
        if (tickInterval <= 0 || tickedTimes % tickInterval == 0) {
            task.run();
            executeTimes++;
        }
    }
}
