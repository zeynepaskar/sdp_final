package com.thanos.participant3.patterns.decorator;

/** Base decorator: forwards everything to the wrapped attack; subclasses change one effect each. */
public abstract class AttackDecorator implements Attack {
    protected final Attack wrapped;

    protected AttackDecorator(Attack wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Decorator needs an attack to wrap");
        }
        this.wrapped = wrapped;
    }

    public int damage() { return wrapped.damage(); }
    public String description() { return wrapped.description(); }
    public int lifestealPercent() { return wrapped.lifestealPercent(); }
    public boolean ignoresDefense() { return wrapped.ignoresDefense(); }
    public boolean unblockable() { return wrapped.unblockable(); }
    public int slowPercent() { return wrapped.slowPercent(); }
    public int stunTurns() { return wrapped.stunTurns(); }
}
