package com.thanos.participant3.stones;

import com.thanos.participant3.patterns.decorator.*;
import java.util.function.UnaryOperator;

/** The six stones in the order Thanos collects them in The Thanos Quest. */
public enum InfinityStone {
    SOUL("Soul Stone", 0xF39C12FF, "Heals Thanos for part of the damage", SoulStoneDecorator::new),
    POWER("Power Stone", 0x8E44ADFF, "Raises damage", PowerStoneDecorator::new),
    TIME("Time Stone", 0x27AE60FF, "Weakens the enemy's counterattack", TimeStoneDecorator::new),
    SPACE("Space Stone", 0x2E86DEFF, "Makes attacks unblockable", SpaceStoneDecorator::new),
    REALITY("Reality Stone", 0xE74C3CFF, "Ignores enemy defense", RealityStoneDecorator::new),
    MIND("Mind Stone", 0xF1C40FFF, "Stuns the enemy", MindStoneDecorator::new);

    private final String displayName;
    private final int rgba;
    private final String effect;
    private final UnaryOperator<Attack> decorator;

    InfinityStone(String displayName, int rgba, String effect, UnaryOperator<Attack> decorator) {
        this.displayName = displayName;
        this.rgba = rgba;
        this.effect = effect;
        this.decorator = decorator;
    }

    public String displayName() { return displayName; }
    public int rgba() { return rgba; }
    public String effect() { return effect; }

    /** Finds a stone by its display name, e.g. "Soul Stone" as other modules spell it. */
    public static InfinityStone fromDisplayName(String name) {
        for (InfinityStone stone : values()) {
            if (stone.displayName.equalsIgnoreCase(name)) return stone;
        }
        throw new IllegalArgumentException("Unknown stone: " + name);
    }

    /** Wraps the attack in this stone's decorator. */
    public Attack empower(Attack attack) { return decorator.apply(attack); }
}
