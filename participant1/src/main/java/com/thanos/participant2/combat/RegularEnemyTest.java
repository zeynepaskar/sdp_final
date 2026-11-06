
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.*;

public class RegularEnemyTest {

    public static void main(String[] args) {

        EnemyFactory basicFactory = new BasicEnemyFactory();
        EnemyFactory chitauriFactory = new ChitauriEnemyFactory();
        EnemyFactory sakaaranFactory = new SakaaranEnemyFactory();

        Enemy basic = basicFactory.spawnEnemy(100, 100);
        Enemy chitauri = chitauriFactory.spawnEnemy(200, 100);
        Enemy sakaaran = sakaaranFactory.spawnEnemy(300, 100);

        System.out.println("Outrider HP: " + basic.getHealth());
        System.out.println("Chitauri HP: " + chitauri.getHealth());
        System.out.println("Sakaaran HP: " + sakaaran.getHealth());
    }
}
