package com.thanos.participant2.strategy;

import com.thanos.participant2.enemies.BossEnemy;
import com.thanos.participant2.enemies.BossType;
import com.thanos.participant3.patterns.decorator.Attack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossStrategyTest {

    @Test
    void everyBossShouldHaveUniqueAttack() {
        BossStrategySelector selector = new BossStrategySelector();

        for (BossType type : BossType.values()) {
            BossEnemy boss = new BossEnemy(type, 0, 0);

            CombatStrategy strategy = selector.select(type);
            Attack attack = strategy.createAttack(boss);

            assertNotNull(attack);
            assertTrue(attack.damage() > 0);
        }
    }

    @Test
    void championShouldUsePowerPunch() {
        BossEnemy champion = new BossEnemy(
                BossType.CHAMPION, 0, 0
        );

        Attack attack = new BossStrategySelector()
                .select(BossType.CHAMPION)
                .createAttack(champion);
        assertEquals(120, attack.damage());
        assertEquals("Power Punch (60) x200%", attack.description());
    }

    @Test
    void selectorShouldRejectNull() {
        BossStrategySelector selector = new BossStrategySelector();

        assertThrows(
                IllegalArgumentException.class,
                () -> selector.select(null)
        );
    }
}