package com.thanos.participant3.patterns.decorator;

/** Decorator: damage x1.5. */
public final class PowerStoneDecorator extends AttackDecorator {
    public PowerStoneDecorator(Attack wrapped) { super(wrapped); }

    public int damage() { return wrapped.damage() * 3 / 2; }
    public String description() { return wrapped.description() + " + Power Stone (x1.5 damage)"; }
}
