
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.*;
import com.thanos.participant2.strategy.*;

public class BossStrategyTest {

    public static void main(String[] args) {

        for (BossType type : BossType.values()) {

            Enemy boss = new BossEnemy(100, 100, type);

            CombatStrategy strategy =
                    BossStrategySelector.getStrategy(type);

            float damage = strategy.attack(boss, 101, 100);

            System.out.println(
                    type.getName() + " | "
                            + type.getSpecialAttack() + " | Damage: "
                            + damage
            );
        }
    }
}
