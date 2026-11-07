package com.thanos.participant3.patterns.decorator;

/** Decorator: ignores the target's defense. */
public final class RealityStoneDecorator extends AttackDecorator {
    public RealityStoneDecorator(Attack wrapped) { super(wrapped); }

    public boolean ignoresDefense() { return true; }
    public String description() { return wrapped.description() + " + Reality Stone (ignores defense)"; }
}
