package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.BossType;

public class BossStrategySelector {

    public CombatStrategy select(BossType bossType) {

        if (bossType == null) {
            throw new IllegalArgumentException("Boss type is required");
        }

        return switch (bossType) {
            case IN_BETWEENER -> new SoulBlastStrategy();
            case CHAMPION -> new PowerPunchStrategy();
            case GARDENER -> new TimeControlStrategy();
            case RUNNER -> new TeleportStrikeStrategy();
            case COLLECTOR -> new RealityTrapStrategy();
            case GRANDMASTER -> new MindControlStrategy();
        };
    }
}