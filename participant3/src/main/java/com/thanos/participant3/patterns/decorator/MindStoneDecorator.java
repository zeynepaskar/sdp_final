package com.thanos.participant3.patterns.decorator;

/** Decorator: adds a stun turn. */
public final class MindStoneDecorator extends AttackDecorator {
    public MindStoneDecorator(Attack wrapped) { super(wrapped); }

    public int stunTurns() { return wrapped.stunTurns() + 1; }
    public String description() { return wrapped.description() + " + Mind Stone (stun 1 turn)"; }
}
