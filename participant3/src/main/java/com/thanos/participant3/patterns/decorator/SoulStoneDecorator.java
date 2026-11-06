package com.thanos.participant3.patterns.decorator;

/** Soul Stone: heals the attacker for part of the damage dealt. */
public final class SoulStoneDecorator extends AttackDecorator {
    public static final int LIFESTEAL_PERCENT = 30;

    public SoulStoneDecorator(Attack wrapped) { super(wrapped); }

    public int lifestealPercent() { return Math.min(100, wrapped.lifestealPercent() + LIFESTEAL_PERCENT); }
    public String description() { return wrapped.description() + " + Soul Stone (heal 30% of damage)"; }
}
