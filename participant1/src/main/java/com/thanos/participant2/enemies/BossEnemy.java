
package com.thanos.participant2.enemies;

public class BossEnemy extends Enemy {

    public BossEnemy(float x, float y) {
        super(x, y, 500, 40, 1.5f);
    }

    public String getName() {
        return "Soul Guardian";
    }

    public String getSpecialAttack() {
        return "Soul Blast";
    }
}
