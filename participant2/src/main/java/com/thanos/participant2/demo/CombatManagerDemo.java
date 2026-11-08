package com.thanos.participant2.demo;

import com.thanos.participant2.combat.CombatManager;
import com.thanos.participant2.combat.TurnBattleController;
import com.thanos.participant2.factory.BasicEnemyFactory;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.progress.Thanos;

public class CombatManagerDemo {
    public static void main(String[] args) {
        EventBus events = new EventBus();
        events.subscribe(event -> System.out.println(event.message()));
        Thanos thanos = new Thanos(events);
        CombatManager manager = new CombatManager(events, thanos);
        TurnBattleController battle = manager.startBattle(new BasicEnemyFactory().create(100, 100));
        while (!battle.state().isOver()) {
            battle.playerAttack(new BasicAttack("Punch", thanos.punchDamage()));
            if (!battle.state().isOver()) battle.enemyTurn();
        }
        manager.finishBattle();
    }
}
