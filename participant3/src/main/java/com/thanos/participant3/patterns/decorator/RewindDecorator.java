package com.thanos.participant3.patterns.decorator;

/** Decorator: heals a percent of the attacker's last wound. */
public final class RewindDecorator extends AttackDecorator {
    private final int percent;

    public RewindDecorator(Attack wrapped, int percent) {
        super(wrapped);
        if (percent <= 0 || percent > 100) {
            throw new IllegalArgumentException("Rewind percent must be within 1..100");
        }
        this.percent = percent;
    }

    public int rewindPercent() { return Math.min(100, wrapped.rewindPercent() + percent); }
    public String description() { return wrapped.description() + " + rewind " + percent + "% of the last wound"; }
}
