package com.thanos.participant3.patterns.decorator;

/** Base decorator: forwards to the wrapped attack; subclasses override one effect. */
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
    public int selfHeal() { return wrapped.selfHeal(); }
    public int rewindPercent() { return wrapped.rewindPercent(); }
    public boolean ignoresDefense() { return wrapped.ignoresDefense(); }
    public boolean unblockable() { return wrapped.unblockable(); }
    public int slowPercent() { return wrapped.slowPercent(); }
    public int stunTurns() { return wrapped.stunTurns(); }
}
