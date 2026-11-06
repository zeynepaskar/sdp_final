package com.thanos.participant3.patterns.decorator;

/** Power Stone: one and a half times the damage. */
public final class PowerStoneDecorator extends AttackDecorator {
    public PowerStoneDecorator(Attack wrapped) { super(wrapped); }

    public int damage() { return wrapped.damage() * 3 / 2; }
    public String description() { return wrapped.description() + " + Power Stone (x1.5 damage)"; }
}
