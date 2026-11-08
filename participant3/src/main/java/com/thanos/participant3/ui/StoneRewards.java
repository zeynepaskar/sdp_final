package com.thanos.participant3.ui;

import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventListener;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.stones.InfinityStone;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Observer: queues a stone reveal on STONE_ACQUIRED and tracks its timing. */
public final class StoneRewards implements GameEventListener {
    public static final float APPEAR_SECONDS = 0.8f;
    public static final float SHOW_SECONDS = 2.2f;
    public static final float FLY_SECONDS = 0.7f;
    public enum Phase { APPEAR, SHOW, FLY }

    private final Inventory inventory;
    private final Set<InfinityStone> seen;
    private final Deque<InfinityStone> pending = new ArrayDeque<>();
    private final Deque<Integer> counts = new ArrayDeque<>();
    private float time;

    public StoneRewards(Inventory inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("Stone rewards need an inventory");
        }
        this.inventory = inventory;
        this.seen = EnumSet.noneOf(InfinityStone.class);
        seen.addAll(inventory.stones());
    }

    public void onEvent(GameEvent event) {
        if (event.type() != GameEventType.STONE_ACQUIRED) return;
        for (InfinityStone stone : inventory.stones()) {
            if (seen.add(stone)) {
                pending.addLast(stone);
                counts.addLast(event.value());
            }
        }
    }

    public boolean isPlaying() { return !pending.isEmpty(); }
    public InfinityStone current() { return pending.peekFirst(); }
    public int count() { return isPlaying() ? counts.peekFirst() : 0; }

    /** Stones in the order they were found, up to and including the one being shown. */
    public List<InfinityStone> foundUpToCurrent() {
        List<InfinityStone> found = inventory.stonesInOrder();
        int index = found.indexOf(current());
        return index < 0 ? found : found.subList(0, index + 1);
    }
    public float time() { return time; }

    public Phase phase() {
        if (time < APPEAR_SECONDS) return Phase.APPEAR;
        if (time < APPEAR_SECONDS + SHOW_SECONDS) return Phase.SHOW;
        return Phase.FLY;
    }

    public float phaseProgress() {
        return switch (phase()) {
            case APPEAR -> time / APPEAR_SECONDS;
            case SHOW -> (time - APPEAR_SECONDS) / SHOW_SECONDS;
            case FLY -> Math.min(1, (time - APPEAR_SECONDS - SHOW_SECONDS) / FLY_SECONDS);
        };
    }

    public void update(float delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("Time must not go back");
        }
        if (!isPlaying()) return;
        time += delta;
        if (time >= APPEAR_SECONDS + SHOW_SECONDS + FLY_SECONDS) next();
    }

    public void skip() {
        if (!isPlaying()) return;
        if (phase() == Phase.FLY) next();
        else time = APPEAR_SECONDS + SHOW_SECONDS;
    }

    private void next() {
        pending.pollFirst();
        counts.pollFirst();
        time = 0;
    }
}
