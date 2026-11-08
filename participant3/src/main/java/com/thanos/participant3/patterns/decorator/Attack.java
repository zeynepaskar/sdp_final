package com.thanos.participant3.patterns.decorator;

/** Decorator component: effects are stacked by wrapping an attack. */
public interface Attack {
    int damage();
    String description();

    default int lifestealPercent() { return 0; }
    default int selfHeal() { return 0; }
    default int rewindPercent() { return 0; }
    default boolean ignoresDefense() { return false; }
    default boolean unblockable() { return false; }
    default boolean turnsDefenseIntoDamage() { return false; }
    default int slowPercent() { return 0; }
    default int stunTurns() { return 0; }
}
