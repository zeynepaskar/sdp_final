package com.thanos.participant3.ui;

import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventListener;
import com.thanos.participant3.patterns.observer.GameEventType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Set;

/** Observer: queues battle events as lines shown one at a time. */
public final class BattleMessages implements GameEventListener {
    private static final Set<GameEventType> SHOWN = EnumSet.of(GameEventType.BATTLE_STARTED,
            GameEventType.ABILITY_USED, GameEventType.DAMAGE, GameEventType.HEAL, GameEventType.DEFENDED,
            GameEventType.TURN_SKIPPED, GameEventType.FLED, GameEventType.BATTLE_WON, GameEventType.BATTLE_LOST,
            GameEventType.STONE_ACQUIRED, GameEventType.XP_GAINED, GameEventType.LEVEL_UP, GameEventType.PORTAL);
    private final Deque<GameEvent> pending = new ArrayDeque<>();

    public void onEvent(GameEvent event) {
        if (SHOWN.contains(event.type())) pending.addLast(event);
    }

    public boolean hasPending() { return !pending.isEmpty(); }
    public String current() { return hasPending() ? pending.peekFirst().message() : null; }
    public GameEvent currentEvent() { return pending.peekFirst(); }
    public void advance() { pending.pollFirst(); }
    public void clear() { pending.clear(); }
}
