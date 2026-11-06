
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BossEnemy;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.strategy.CombatStrategy;
import com.thanos.participant2.strategy.MeleeAttackStrategy;
import com.thanos.participant2.strategy.RangedAttackStrategy;
import com.thanos.participant2.strategy.HeavyAttackStrategy;
import com.thanos.participant2.enemies.BossType;

public class StrategyTest {

    public static void main(String[] args) {

        Enemy boss = new BossEnemy(
                100, 100, BossType.CHAMPION );
        CombatStrategy strategy = new MeleeAttackStrategy();

        System.out.println("Melee damage: "
                + strategy.attack(boss, 101, 100));

        strategy = new RangedAttackStrategy();

        System.out.println("Ranged damage: "
                + strategy.attack(boss, 105, 100));

        strategy = new HeavyAttackStrategy();

        System.out.println("Heavy damage: "
                + strategy.attack(boss, 101, 100));
    }
}