package com.thanos.participant2.demo;

import com.thanos.participant2.enemies.BossEnemy;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.strategy.CombatStrategy;
import com.thanos.participant2.strategy.MeleeAttackStrategy;
import com.thanos.participant2.strategy.RangedAttackStrategy;
import com.thanos.participant2.strategy.HeavyAttackStrategy;
import com.thanos.participant2.enemies.BossType;

public class StrategyDemo {

    public static void main(String[] args) {

        Enemy boss = new BossEnemy(
                BossType.CHAMPION, 100, 100 );
        CombatStrategy strategy = new MeleeAttackStrategy();

        System.out.println("Melee damage: "
                + strategy.createAttack(boss).damage());

        strategy = new RangedAttackStrategy();

        System.out.println("Ranged damage: "
                + strategy.createAttack(boss).damage());

        strategy = new HeavyAttackStrategy();

        System.out.println("Heavy damage: "
                + strategy.createAttack(boss).damage());
    }
}
