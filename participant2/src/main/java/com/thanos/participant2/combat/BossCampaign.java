package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BossType;
import com.thanos.participant2.enemies.Enemy;
import com.thanos.participant2.factory.BasicEnemyFactory;
import com.thanos.participant2.factory.ChitauriEnemyFactory;
import com.thanos.participant2.factory.EnemyFactory;
import com.thanos.participant2.factory.SakaaranEnemyFactory;
import com.thanos.participant2.strategy.CombatStrategy;
import com.thanos.participant2.strategy.HeavyAttackStrategy;
import com.thanos.participant2.strategy.MeleeAttackStrategy;
import com.thanos.participant2.strategy.RangedAttackStrategy;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.progress.Thanos;
import com.thanos.participant3.stones.InfinityStone;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Game-level rules around {@link CombatManager}: which boss guards which world,
 * restoring Thanos before a fight, and rewards afterwards.
 *
 * The game (participant 1) only needs three calls: {@link #startBossBattle},
 * show the returned battle, then {@link #finishBattle}.
 */
public class BossCampaign {

    /** Energy given to Thanos at the start of every fight. */
    public static final int ENERGY_PER_BATTLE = 40;

    private final Thanos thanos;
    private final Inventory inventory;
    private final CombatManager combat;

    private final Set<BossType> defeated = EnumSet.noneOf(BossType.class);

    private final EnemyFactory[] regularFactories = {
            new BasicEnemyFactory(),
            new ChitauriEnemyFactory(),
            new SakaaranEnemyFactory()
    };
    private final CombatStrategy[] regularStrategies = {
            new MeleeAttackStrategy(),
            new RangedAttackStrategy(),
            new HeavyAttackStrategy()
    };

    private BossType currentBoss;

    public BossCampaign(EventBus events, Thanos thanos, Inventory inventory) {
        if (events == null || thanos == null || inventory == null) {
            throw new IllegalArgumentException(
                    "Campaign requires events, Thanos and inventory"
            );
        }

        this.thanos = thanos;
        this.inventory = inventory;
        this.combat = new CombatManager(events, thanos);
    }

    /** One boss per world, in order; extra worlds wrap around. */
    public BossType bossForWorld(int worldIndex) {
        if (worldIndex < 0) {
            throw new IllegalArgumentException(
                    "World index must not be negative"
            );
        }

        BossType[] bosses = BossType.values();
        return bosses[worldIndex % bosses.length];
    }

    /** The stone a boss drops: the stones and bosses are in the same order. */
    public InfinityStone stoneFor(BossType boss) {
        if (boss == null) {
            throw new IllegalArgumentException("Boss type is required");
        }

        InfinityStone[] stones = InfinityStone.values();
        return stones[boss.ordinal() % stones.length];
    }

    public TurnBattleController startBossBattle(
            int worldIndex,
            float x,
            float y
    ) {
        BossType boss = bossForWorld(worldIndex);

        prepareThanos();
        TurnBattleController battle = combat.startBossBattle(boss, x, y);
        currentBoss = boss;

        return battle;
    }

    /** Regular encounters cycle through Outrider, Chitauri and Sakaaran. */
    public TurnBattleController startRegularBattle(int encounterIndex) {
        if (encounterIndex < 0) {
            throw new IllegalArgumentException(
                    "Encounter index must not be negative"
            );
        }

        int kind = encounterIndex % regularFactories.length;
        Enemy enemy = regularFactories[kind].create(0, 0);

        prepareThanos();
        TurnBattleController battle = combat.startBattle(
                enemy,
                regularStrategies[kind]
        );
        currentBoss = null;

        return battle;
    }

    /**
     * Ends the finished battle, pays out the rewards and returns the result.
     * A win gives experience; a boss win also gives its stone.
     */
    public BattleState finishBattle() {
        TurnBattleController battle = combat.currentBattle()
                .orElseThrow(() -> new IllegalStateException(
                        "There is no active battle"
                ));

        BattleState result = battle.state();
        combat.finishBattle();

        if (result == BattleState.WON) {
            thanos.gainExperience(Thanos.experienceFor(battle.enemy()));

            if (currentBoss != null) {
                defeated.add(currentBoss);
                inventory.addStone(stoneFor(currentBoss));
            }
        }

        currentBoss = null;
        return result;
    }

    public boolean isBossDefeated(int worldIndex) {
        return defeated.contains(bossForWorld(worldIndex));
    }

    public boolean allBossesDefeated() {
        return defeated.size() == BossType.values().length;
    }

    public Optional<TurnBattleController> currentBattle() {
        return combat.currentBattle();
    }

    public boolean hasActiveBattle() {
        return combat.hasActiveBattle();
    }

    public CombatManager combat() {
        return combat;
    }

    private void prepareThanos() {
        thanos.heal(thanos.maxHealth());
        inventory.restoreEnergy(ENERGY_PER_BATTLE);
    }
}
