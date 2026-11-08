package brova.toshiba.client.utils.time;

public class TimerController {

    private static float timer = 1.0F;

    public static void setTimer(float timerValue) {
        timer = timerValue;
    }

    public static float getTimer() {
        return timer;
    }

    public static void reset() {
        timer = 1.0F;
    }
}
