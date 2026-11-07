package com.thanos.participant3.patterns.decorator;

/** Decorator: weakens the target's next counterattack by a fixed percent. */
public final class TimeStoneDecorator extends AttackDecorator {
    public static final int SLOW_PERCENT = 25;

    public TimeStoneDecorator(Attack wrapped) { super(wrapped); }

    public int slowPercent() { return Math.min(100, wrapped.slowPercent() + SLOW_PERCENT); }
    public String description() { return wrapped.description() + " + Time Stone (counterattack -25%)"; }
}
