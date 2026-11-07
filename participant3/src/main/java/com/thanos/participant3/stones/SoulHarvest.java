package com.thanos.participant3.stones;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventListener;
import com.thanos.participant3.patterns.observer.GameEventType;

/** Observer for BATTLE_WON; subscribe after BattleMessages to keep the message order. */
public final class SoulHarvest implements GameEventListener {
    public static final int HEAL_PERCENT = 15;
    private final Combatant thanos;
    private final Inventory inventory;
    private final EventBus events;

    public SoulHarvest(Combatant thanos, Inventory inventory, EventBus events) {
        if (thanos == null || inventory == null || events == null) {
            throw new IllegalArgumentException("Harvest needs Thanos, the inventory and the event bus");
        }
        this.thanos = thanos;
        this.inventory = inventory;
        this.events = events;
    }

    public void onEvent(GameEvent event) {
        if (event.type() != GameEventType.BATTLE_WON || !inventory.has(InfinityStone.SOUL) || !thanos.isAlive()) return;
        int healed = Math.min(thanos.maxHealth() * HEAL_PERCENT / 100, thanos.maxHealth() - thanos.health());
        if (healed == 0) return;
        thanos.heal(healed);
        events.publish(GameEventType.HEAL, thanos.name(),
                "The Soul Stone harvests the fallen: " + thanos.name() + " +" + healed + " health", healed);
        events.publish(GameEventType.HEALTH_CHANGED, thanos.name(),
                thanos.name() + " health: " + thanos.health() + "/" + thanos.maxHealth(), thanos.health());
    }
}
