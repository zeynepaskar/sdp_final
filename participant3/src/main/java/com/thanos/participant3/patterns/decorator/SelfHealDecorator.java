package com.thanos.participant3.patterns.decorator;

/** Decorator: flat heal for the attacker. */
public final class SelfHealDecorator extends AttackDecorator {
    private final int amount;

    public SelfHealDecorator(Attack wrapped, int amount) {
        super(wrapped);
        if (amount < 0) {
            throw new IllegalArgumentException("Healing must be nonnegative");
        }
        this.amount = amount;
    }

    public int selfHeal() { return wrapped.selfHeal() + amount; }
    public String description() { return wrapped.description() + " + heal " + amount; }
}
