package com.thanos.participant3.stones;

import com.thanos.participant3.patterns.decorator.Attack;

/** A Thanos ability unlocked by a stone; using it wraps an attack in that stone's decorator. */
public record Ability(String name, InfinityStone stone, int energyCost, String description) {
    public Ability {
        if (name == null || name.isBlank() || stone == null || energyCost < 0 || description == null) {
            throw new IllegalArgumentException("Ability needs a name, a stone and nonnegative cost");
        }
    }

    public Attack apply(Attack attack) { return stone.empower(attack); }
}
