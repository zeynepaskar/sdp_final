package com.thanos.participant2.enemies;

public class BossEnemy extends Enemy {

    private final BossType bossType;

    public BossEnemy(BossType bossType, float x, float y) {
        super(
                requireBoss(bossType).getDisplayName(),
                requireBoss(bossType).getHealth(),
                requireBoss(bossType).getDamage(),
                requireBoss(bossType).getSpeed(),
                requireBoss(bossType).getDefense(),
                x,
                y
        );

        this.bossType = bossType;
    }

    private static BossType requireBoss(BossType bossType) {
        if (bossType == null) {
            throw new IllegalArgumentException("Boss type is required");
        }
        return bossType;
    }
    public String getAbilityName() {
        return bossType.getAbilityName();
    }
}