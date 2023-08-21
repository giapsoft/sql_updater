package util;

public class Timer {
    Runnable run;
    int seconds;

    public Timer(Runnable runnable, int seconds) {
        this.run = runnable;
        this.seconds = seconds;
        new Thread(() -> {
            try {
                Thread.sleep(seconds * 1000L);
                if (run != null) {
                    run.run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public Timer(Runnable runnable) {
        this(runnable, 2);
    }

    public void cancel() {
        run = null;
    }
}
