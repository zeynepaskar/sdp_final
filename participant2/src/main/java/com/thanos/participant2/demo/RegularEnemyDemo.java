package com.thanos.participant2.demo;

import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.*;

public class RegularEnemyDemo {

    public static void main(String[] args) {

        EnemyFactory basicFactory = new BasicEnemyFactory();
        EnemyFactory chitauriFactory = new ChitauriEnemyFactory();
        EnemyFactory sakaaranFactory = new SakaaranEnemyFactory();

        Enemy basic = basicFactory.create(100, 100);
        Enemy chitauri = chitauriFactory.create(200, 100);
        Enemy sakaaran = sakaaranFactory.create(300, 100);

        System.out.println("Outrider HP: " + basic.health());
        System.out.println("Chitauri HP: " + chitauri.health());
        System.out.println("Sakaaran HP: " + sakaaran.health());
    }
}
