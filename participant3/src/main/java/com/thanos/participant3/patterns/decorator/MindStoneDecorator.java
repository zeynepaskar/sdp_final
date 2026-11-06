package com.thanos.participant3.patterns.decorator;

/** Mind Stone: stuns the target for one more turn. */
public final class MindStoneDecorator extends AttackDecorator {
    public MindStoneDecorator(Attack wrapped) { super(wrapped); }

    public int stunTurns() { return wrapped.stunTurns() + 1; }
    public String description() { return wrapped.description() + " + Mind Stone (stun 1 turn)"; }
}
