package com.thanos.participant3.ui;

/** Blink state for the energy bar; time is passed in so it is testable without a clock. */
public final class EnergyFlash {
    public static final long DURATION_MS = 1500;
    public static final long BLINK_MS = 150;
    private int from;
    private int regular;
    private int bonus;
    private long start = Long.MIN_VALUE;

    public void gained(int before, int after, int regularMax, long now) {
        int total = after - before;
        if (total <= 0) {
            start = Long.MIN_VALUE;
            return;
        }
        from = before;
        regular = Math.min(total, regularMax);
        bonus = total - regular;
        start = now;
    }

    public boolean active(long now) { return start != Long.MIN_VALUE && now - start < DURATION_MS; }

    public boolean bright(long now) { return (now - start) / BLINK_MS % 2 == 0; }

    public int from() { return from; }
    public int regular() { return regular; }
    public int bonus() { return bonus; }
}
