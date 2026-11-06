package com.thanos.participant3.patterns.decorator;

/** Concrete component: a plain hit without any stone effects. */
public record BasicAttack(String name, int damage) implements Attack {
    public BasicAttack {
        if (name == null || name.isBlank() || damage < 0) {
            throw new IllegalArgumentException("Attack needs a name and nonnegative damage");
        }
    }

    public String description() { return name + " (" + damage + ")"; }
}
