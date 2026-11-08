package com.thanos.participant2.demo;

import com.thanos.participant2.enemies.*;
import com.thanos.participant2.strategy.*;

public class BossStrategyDemo {

    public static void main(String[] args) {

        for (BossType type : BossType.values()) {

            Enemy boss = new BossEnemy(type, 100, 100);

            CombatStrategy strategy =
                    new BossStrategySelector().select(type);

            float damage = strategy.createAttack(boss).damage();

            System.out.println(
                    type.getDisplayName() + " | "
                            + type.getAbilityName() + " | Damage: "
                            + damage
            );
        }
    }
}
