
package com.thanos.participant2.enemies;

public class BasicEnemy extends Enemy {

    public BasicEnemy(float x, float y) {
        super(x, y, 100, 15, 2);
    }

    public String getName() {
        return "Outrider";
    }
}
