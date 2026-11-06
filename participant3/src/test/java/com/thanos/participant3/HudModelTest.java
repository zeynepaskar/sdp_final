package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.ui.HudModel;
import java.util.List;
import org.junit.jupiter.api.Test;

class HudModelTest {
    private final EventBus bus = new EventBus();
    private final FakeCombatant thanos = new FakeCombatant("Thanos", 1000, 1000, 0);
    private final Inventory inventory = new Inventory(bus, 50);
    private final HudModel hud = new HudModel(thanos, inventory, 2);

    @Test
    void showsTheStartingState() {
        assertEquals(1000, hud.health());
        assertEquals(50, hud.energy());
        assertEquals(0, hud.stoneCount());
        assertTrue(hud.feed().isEmpty());
    }

    @Test
    void updatesWhenNotified() {
        bus.subscribe(hud);

        inventory.addStone(InfinityStone.POWER);
        new HitResolver(bus).resolve(new FakeCombatant("Thor", 400, 400, 0), thanos, new BasicAttack("Hammer", 60));

        assertTrue(hud.hasStone(InfinityStone.POWER));
        assertEquals(940, hud.health());
        assertEquals(List.of("Thanos health: 940/1000", "Thanos received 60 damage"), hud.feed());
    }

    @Test
    void freezesWhenUnsubscribedWhileTheGameGoesOn() {
        bus.subscribe(hud);
        bus.unsubscribe(hud);

        inventory.addStone(InfinityStone.MIND);

        assertTrue(inventory.has(InfinityStone.MIND));
        assertFalse(hud.hasStone(InfinityStone.MIND));
    }
}
