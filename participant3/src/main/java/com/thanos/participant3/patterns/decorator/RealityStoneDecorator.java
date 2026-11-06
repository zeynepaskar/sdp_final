package com.thanos.participant3.patterns.decorator;

/** Reality Stone: rewrites the rules, so the target's defense does not count. */
public final class RealityStoneDecorator extends AttackDecorator {
    public RealityStoneDecorator(Attack wrapped) { super(wrapped); }

    public boolean ignoresDefense() { return true; }
    public String description() { return wrapped.description() + " + Reality Stone (ignores defense)"; }
}
