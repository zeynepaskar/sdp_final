
package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.BossType;

public class BossStrategySelector {

    public static CombatStrategy getStrategy(BossType type) {

        switch (type) {
            case IN_BETWEENER:
                return new SoulBlastStrategy();

            case CHAMPION:
                return new PowerPunchStrategy();

            case GARDENER:
                return new TimeControlStrategy();

            case RUNNER:
                return new TeleportStrikeStrategy();

            case COLLECTOR:
                return new RealityTrapStrategy();

            case GRANDMASTER:
                return new MindControlStrategy();

            default:
                throw new IllegalArgumentException("Unknown boss");
        }
    }
}
