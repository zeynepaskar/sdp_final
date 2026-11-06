package com.thanos.participant3.ui;

import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventListener;
import com.thanos.participant3.patterns.observer.GameEventType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Set;

/** Observer for the battle screen: queues battle events as lines the player reads one by one. */
public final class BattleMessages implements GameEventListener {
    private static final Set<GameEventType> SHOWN = EnumSet.of(GameEventType.BATTLE_STARTED,
            GameEventType.ABILITY_USED, GameEventType.DAMAGE, GameEventType.HEAL, GameEventType.DEFENDED,
            GameEventType.TURN_SKIPPED, GameEventType.FLED, GameEventType.BATTLE_WON, GameEventType.BATTLE_LOST,
            GameEventType.STONE_ACQUIRED);
    private final Deque<String> pending = new ArrayDeque<>();

    public void onEvent(GameEvent event) {
        if (SHOWN.contains(event.type())) pending.addLast(event.message());
    }

    public boolean hasPending() { return !pending.isEmpty(); }
    public String current() { return pending.peekFirst(); }
    public void advance() { pending.pollFirst(); }
    public void clear() { pending.clear(); }
}
