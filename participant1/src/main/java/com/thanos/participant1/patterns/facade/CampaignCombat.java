package com.thanos.participant1.patterns.facade;

import com.thanos.participant1.levels.Encounter;
import com.thanos.participant2.combat.CombatManager;
import com.thanos.participant2.factory.BasicEnemyFactory;
import com.thanos.participant2.factory.ChitauriEnemyFactory;
import com.thanos.participant2.factory.EnemyFactory;
import com.thanos.participant2.factory.SakaaranEnemyFactory;
import com.thanos.participant3.battle.TurnBattle;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.progress.Thanos;
import com.thanos.participant3.stones.SoulHarvest;
import com.thanos.participant3.stones.SpacePortal;
import com.thanos.participant3.ui.BattleMessages;
import com.thanos.participant3.ui.StoneRewards;

import java.util.Random;

final class CampaignCombat {
    final Thanos progress;
    final Inventory inventory;
    final BattleMessages messages = new BattleMessages();
    final StoneRewards rewards;
    private final CombatManager combat;

    CampaignCombat(Random random) {
        EventBus events = new EventBus();
        progress = new Thanos(events);
        inventory = new Inventory(events, Inventory.BASE_MAX_ENERGY);
        rewards = new StoneRewards(inventory);
        events.subscribe(messages);
        events.subscribe(rewards);
        events.subscribe(new SoulHarvest(progress, inventory, events));
        combat = new CombatManager(events, progress, new SpacePortal(inventory, random));
    }

    void start(Encounter encounter) {
        messages.clear();
        var boss = encounter.kind().bossType();
        if (boss.isPresent()) {
            combat.startBossBattle(boss.get(), encounter.x(), encounter.y());
            return;
        }
        EnemyFactory factory = switch (encounter.kind()) {
            case OUTRIDER -> new BasicEnemyFactory();
            case CHITAURI -> new ChitauriEnemyFactory();
            case SAKAARAN -> new SakaaranEnemyFactory();
            default -> throw new IllegalArgumentException("Unknown regular enemy: " + encounter.kind());
        };
        combat.startBattle(factory.create(encounter.x(), encounter.y()));
    }

    TurnBattle battle() { return combat.currentBattle().orElse(null); }

    void reward(Encounter encounter) {
        encounter.kind().bossType().ifPresent(boss -> inventory.addStone(boss.stone()));
        progress.gainExperience(Thanos.experienceFor(battle().enemy()));
    }

    void restoreAtEntrance() {
        progress.heal(progress.maxHealth());
        inventory.restoreEnergy(inventory.maxEnergy());
    }

    void finish() {
        combat.finishBattle();
    }
}
