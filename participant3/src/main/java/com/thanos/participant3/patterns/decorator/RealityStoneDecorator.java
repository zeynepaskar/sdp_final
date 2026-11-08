package com.thanos.participant3.patterns.decorator;

/** Decorator: the target's defense adds to the damage instead of blocking it. */
public final class RealityStoneDecorator extends AttackDecorator {
    public RealityStoneDecorator(Attack wrapped) { super(wrapped); }

    public boolean ignoresDefense() { return true; }
    public boolean turnsDefenseIntoDamage() { return true; }
    public String description() { return wrapped.description() + " + Reality Stone (defense becomes damage)"; }
}
