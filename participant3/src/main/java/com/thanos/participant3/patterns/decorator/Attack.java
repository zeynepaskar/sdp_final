package com.thanos.participant3.patterns.decorator;

/** Component: an attack whose effects can be stacked by wrapping it in stone decorators. */
public interface Attack {
    int damage();
    String description();

    /** Percent of dealt damage that heals the attacker. */
    default int lifestealPercent() { return 0; }
    default boolean ignoresDefense() { return false; }
    /** For the combat system: the target cannot block this hit. */
    default boolean unblockable() { return false; }
    /** For the combat system: how much weaker the target's next counterattack is, in percent. */
    default int slowPercent() { return 0; }
    /** For the combat system: how many turns the target skips. */
    default int stunTurns() { return 0; }
}
