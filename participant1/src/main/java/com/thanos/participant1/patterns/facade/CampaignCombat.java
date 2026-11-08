package com.thanos.participant1.patterns.facade;

import com.thanos.participant1.levels.Encounter;
import com.thanos.participant2.combat.CombatManager;
import com.thanos.participant2.enemies.BossType;
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
    private final EventBus events = new EventBus();
    final Thanos progress = new Thanos(events);
    final Inventory inventory = new Inventory(events, Inventory.BASE_MAX_ENERGY);
    final BattleMessages messages = new BattleMessages();
    final StoneRewards rewards = new StoneRewards(inventory);
    private final CombatManager combat;
    private TurnBattle battle;

    CampaignCombat(Random random) {
        events.subscribe(messages);
        events.subscribe(rewards);
        events.subscribe(new SoulHarvest(progress, inventory, events));
        combat = new CombatManager(events, progress, new SpacePortal(inventory, random));
    }

    void start(Encounter encounter) {
        messages.clear();
        if (encounter.isBoss()) {
            battle = combat.startBossBattle(bossType(encounter), encounter.x(), encounter.y());
        } else {
            EnemyFactory factory = switch (encounter.kind()) {
                case OUTRIDER -> new BasicEnemyFactory();
                case CHITAURI -> new ChitauriEnemyFactory();
                case SAKAARAN -> new SakaaranEnemyFactory();
                default -> throw new IllegalArgumentException("Unknown regular enemy: " + encounter.kind());
            };
            battle = combat.startBattle(factory.create(encounter.x(), encounter.y()));
        }
    }

    TurnBattle battle() { return battle; }

    void reward(Encounter encounter) {
        if (encounter.isBoss()) inventory.addStone(bossType(encounter).stone());
        progress.gainExperience(Thanos.experienceFor(battle.enemy()));
    }

    void restoreAtEntrance() {
        progress.heal(progress.maxHealth());
        inventory.restoreEnergy(inventory.maxEnergy());
    }

    void finish() {
        combat.finishBattle();
        battle = null;
    }

    private static BossType bossType(Encounter encounter) {
        return BossType.valueOf(encounter.kind().name());
    }
}
