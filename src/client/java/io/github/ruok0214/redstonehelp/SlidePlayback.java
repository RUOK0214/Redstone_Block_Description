package io.github.ruok0214.redstonehelp;

/** Only presentation time is tracked; world ticks do not control slideshow speed. */
public final class SlidePlayback {
    public static final long FRAME_MILLIS = 1800;
    private static String activeTopic;
    private static long started;
    private static long lastSeen;
    private SlidePlayback() {}

    public static int frame(String topic, int frameCount, long nowMillis) {
        if (!topic.equals(activeTopic) || nowMillis - lastSeen > 300 || nowMillis < lastSeen) {
            activeTopic = topic;
            started = nowMillis;
        }
        lastSeen = nowMillis;
        return (int) ((nowMillis - started) / FRAME_MILLIS % frameCount);
    }
    public static void reset() { activeTopic = null; started = 0; lastSeen = 0; }
}
