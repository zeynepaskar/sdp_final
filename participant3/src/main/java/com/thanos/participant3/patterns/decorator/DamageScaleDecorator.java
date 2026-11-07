package com.thanos.participant3.patterns.decorator;

/** Decorator: scales damage by a percent. */
public final class DamageScaleDecorator extends AttackDecorator {
    private final int percent;

    public DamageScaleDecorator(Attack wrapped, int percent) {
        super(wrapped);
        if (percent < 0) {
            throw new IllegalArgumentException("Percent must be nonnegative");
        }
        this.percent = percent;
    }

    public int damage() { return wrapped.damage() * percent / 100; }
    public String description() { return wrapped.description() + " x" + percent + "%"; }
}
