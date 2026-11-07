package com.thanos.participant3.patterns.decorator;

/** Decorator: weakens the target's next counterattack by a given percent. */
public final class WeakenDecorator extends AttackDecorator {
    private final int percent;

    public WeakenDecorator(Attack wrapped, int percent) {
        super(wrapped);
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be within 0..100");
        }
        this.percent = percent;
    }

    public int slowPercent() { return Math.min(100, wrapped.slowPercent() + percent); }
    public String description() { return wrapped.description() + " (counterattack -" + percent + "%)"; }
}
