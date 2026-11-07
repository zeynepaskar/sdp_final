package com.thanos.participant3.stones;

import com.thanos.participant3.patterns.decorator.Attack;
import java.util.function.UnaryOperator;

/** Stone ability: an energy cost and a function that decorates an attack. */
public record Ability(String name, InfinityStone stone, int energyCost, String description,
                      UnaryOperator<Attack> effect) {
    public Ability {
        if (name == null || name.isBlank() || stone == null || energyCost < 0 || description == null
                || effect == null) {
            throw new IllegalArgumentException("Ability needs a name, a stone, an effect and nonnegative cost");
        }
    }

    public Ability(String name, InfinityStone stone, int energyCost, String description) {
        this(name, stone, energyCost, description, stone == null ? null : stone::empower);
    }

    public Attack apply(Attack attack) { return effect.apply(attack); }
}
