package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.BossEnemyFactory;
import com.thanos.participant2.strategy.BossStrategySelector;
import com.thanos.participant2.strategy.CombatStrategy;
import com.thanos.participant2.strategy.RegularEnemyStrategySelector;
import com.thanos.participant3.Combatant;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.decorator.Portal;
import com.thanos.participant3.patterns.observer.EventBus;

import java.util.Optional;

public class CombatManager {

    private final EventBus events;
    private final Portal portal;
    private final Combatant thanos;

    private final BossEnemyFactory bossFactory;
    private final BossStrategySelector bossStrategySelector;

    private TurnBattleController currentBattle;

    public CombatManager(EventBus events, Combatant thanos) {
        this(events, thanos, Portal.NONE);
    }

    public CombatManager(EventBus events, Combatant thanos, Portal portal) {
        if (events == null || thanos == null || portal == null) {
            throw new IllegalArgumentException(
                    "CombatManager requires events and Thanos"
            );
        }

        this.events = events;
        this.thanos = thanos;
        this.portal = portal;

        this.bossFactory = new BossEnemyFactory();
        this.bossStrategySelector = new BossStrategySelector();
    }

    public TurnBattleController startBattle(Enemy enemy) {
        return startBattle(enemy, RegularEnemyStrategySelector.getStrategy(enemy));
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
                new HitResolver(events, portal),
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

        Enemy boss = bossFactory.create(
                bossType,
                x,
                y
        );

        CombatStrategy strategy = bossStrategySelector.select(bossType);

        currentBattle = new TurnBattleController(
                events,
                new HitResolver(events, portal),
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
