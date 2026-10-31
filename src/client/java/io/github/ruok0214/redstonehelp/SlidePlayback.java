package io.github.ruok0214.redstonehelp;

/** Only presentation time is tracked; world ticks do not control slideshow speed. */
public final class SlidePlayback {
    public static final long FRAME_MILLIS = 1800;
    private static String activeTopic;
    private static long started;
    private static long lastSeen;
    private static int currentFrame;
    private static int count = 1;
    private static boolean paused;
    private SlidePlayback() {}

    public static int frame(String topic, int frameCount, long nowMillis) {
        if (!topic.equals(activeTopic) || (!ScrollTooltip.isPinned() && nowMillis - lastSeen > 300) || nowMillis < lastSeen) {
            activeTopic = topic;
            started = nowMillis;
            currentFrame = 0;
            paused = false;
        }
        count = frameCount;
        lastSeen = nowMillis;
        if (!paused && nowMillis - started >= FRAME_MILLIS) {
            currentFrame = (currentFrame + (int) ((nowMillis - started) / FRAME_MILLIS % count)) % count;
            started += (nowMillis - started) / FRAME_MILLIS * FRAME_MILLIS;
        }
        return currentFrame;
    }
    public static boolean paused() { return paused; }
    public static int currentFrame() { return currentFrame; }
    public static void togglePause(long nowMillis) { paused = !paused; started = lastSeen = nowMillis; }
    public static void step(int direction, long nowMillis) {
        currentFrame = Math.floorMod(currentFrame + direction, count);
        paused = true;
        started = lastSeen = nowMillis;
    }
    public static void reset() { activeTopic = null; started = lastSeen = 0; currentFrame = 0; count = 1; paused = false; }
}
