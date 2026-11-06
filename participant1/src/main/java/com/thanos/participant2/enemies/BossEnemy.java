package com.thanos.participant2.enemies;

public class BossEnemy extends Enemy {

    private final BossType bossType;

    public BossEnemy(float x, float y, BossType bossType) {
        super(
                x,
                y,
                bossType.getHealth(),
                bossType.getDamage(),
                1.5f
        );

        this.bossType = bossType;
    }

    public String getName() {
        return bossType.getName();
    }

    public String getInfinityStone() {
        return bossType.getInfinityStone();
    }

    public String getSpecialAttack() {
        return bossType.getSpecialAttack();
    }

    public BossType getBossType() {
        return bossType;
    }
}
