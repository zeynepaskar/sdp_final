package com.thanos.participant3.patterns.decorator;

/** Decorator: the hit cannot be blocked. */
public final class SpaceStoneDecorator extends AttackDecorator {
    public SpaceStoneDecorator(Attack wrapped) { super(wrapped); }

    public boolean unblockable() { return true; }
    public String description() { return wrapped.description() + " + Space Stone (unblockable)"; }
}
