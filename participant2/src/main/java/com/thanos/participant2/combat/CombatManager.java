
package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BossEnemy;
import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.BossEnemyFactory;
import com.thanos.participant2.strategy.BossStrategySelector;
import com.thanos.participant2.strategy.CombatStrategy;
import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;

import java.util.Optional;

public class CombatManager {

    private final EventBus events;
    private final HitResolver resolver;
    private final Combatant thanos;

    private final BossEnemyFactory bossFactory;
    private final BossStrategySelector bossStrategySelector;

    private TurnBattleController currentBattle;

    public CombatManager(EventBus events, Combatant thanos) {
        if (events == null || thanos == null) {
            throw new IllegalArgumentException(
                    "CombatManager requires events and Thanos"
            );
        }

        this.events = events;
        this.thanos = thanos;
        this.resolver = new HitResolver(events);

        this.bossFactory = new BossEnemyFactory();
        this.bossStrategySelector = new BossStrategySelector();
    }

    public TurnBattleController startBattle(
            Enemy enemy,
            CombatStrategy strategy
    ) {
        if (enemy == null || strategy == null) {
            throw new IllegalArgumentException(
                    "Enemy and strategy are required"
            );
        }

        ensureNoActiveBattle();

        currentBattle = new TurnBattleController(
                events,
                resolver,
                thanos,
                enemy,
                strategy,
                false
        );

        return currentBattle;
    }

    public TurnBattleController startBossBattle(
            BossType bossType,
            float x,
            float y
    ) {
        if (bossType == null) {
            throw new IllegalArgumentException(
                    "Boss type must not be null"
            );
        }

        ensureNoActiveBattle();

        BossEnemy boss = (BossEnemy) bossFactory.create(
                bossType,
                x,
                y
        );

        CombatStrategy strategy = bossStrategySelector.select(bossType);

        currentBattle = new TurnBattleController(
                events,
                resolver,
                thanos,
                boss,
                strategy,
                true
        );

        return currentBattle;
    }

    public Optional<TurnBattleController> currentBattle() {
        return Optional.ofNullable(currentBattle);
    }

    public boolean hasActiveBattle() {
        return currentBattle != null
                && !currentBattle.state().isOver();
    }

    public void finishBattle() {
        if (currentBattle == null) {
            throw new IllegalStateException(
                    "There is no active battle"
            );
        }

        if (!currentBattle.state().isOver()) {
            throw new IllegalStateException(
                    "Cannot finish a battle that is still active"
            );
        }

        currentBattle = null;
    }

    public Combatant thanos() {
        return thanos;
    }

    public EventBus events() {
        return events;
    }

    private void ensureNoActiveBattle() {
        if (currentBattle != null) {
            throw new IllegalStateException(
                    "Previous battle must be finished before starting a new one"
            );
        }
    }
}
