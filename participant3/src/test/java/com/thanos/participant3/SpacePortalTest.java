package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.SimpleTurnBattle;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.stones.SpacePortal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SpacePortalTest {
    private final EventBus bus = new EventBus();
    private final List<GameEvent> events = new ArrayList<>();
    private final Inventory inventory = new Inventory(bus, 50);
    private final FakeCombatant thanos = new FakeCombatant("Thanos", 1000, 1000, 10);
    private final FakeCombatant runner = new FakeCombatant("Runner", 500, 500, 10);

    SpacePortalTest() { bus.subscribe(events::add); }

    private static Random rolls(int roll) {
        return new Random() {
            public int nextInt(int bound) { return roll; }
        };
    }

    @Test
    void opensOnlyForThanosWithTheSpaceStoneAndALowRoll() {
        SpacePortal lucky = new SpacePortal(inventory, rolls(SpacePortal.CHANCE_PERCENT - 1));
        SpacePortal unlucky = new SpacePortal(inventory, rolls(SpacePortal.CHANCE_PERCENT));

        assertFalse(lucky.opensFor(thanos));
        inventory.addStone(InfinityStone.SPACE);
        assertTrue(lucky.opensFor(thanos));
        assertFalse(unlucky.opensFor(thanos));
        assertFalse(lucky.opensFor(runner));
    }

    @Test
    void aBlowIntoThePortalHitsTheAttackerInstead() {
        inventory.addStone(InfinityStone.SPACE);
        HitResolver resolver = new HitResolver(bus, new SpacePortal(inventory, rolls(0)));

        int dealt = resolver.resolve(runner, thanos, new BasicAttack("Runner strike", 85));

        assertEquals(0, dealt);
        assertEquals(1000, thanos.health());
        assertEquals(500 - 30, runner.health());
        assertTrue(events.contains(new GameEvent(GameEventType.PORTAL, "Thanos",
                "Portal! Runner is hit by its own blow", 30)));
        assertTrue(events.contains(new GameEvent(GameEventType.DAMAGE, "Runner", "Runner received 30 damage", 30)));
    }

    @Test
    void withoutAPortalTheBlowLandsAsUsual() {
        inventory.addStone(InfinityStone.SPACE);
        HitResolver resolver = new HitResolver(bus, new SpacePortal(inventory, rolls(99)));

        resolver.resolve(runner, thanos, new BasicAttack("Runner strike", 85));

        assertEquals(1000 - 76, thanos.health());
        assertEquals(500, runner.health());
        assertTrue(events.stream().noneMatch(e -> e.type() == GameEventType.PORTAL));
    }

    @Test
    void thanosPunchesAreNeverSwallowed() {
        inventory.addStone(InfinityStone.SPACE);
        HitResolver resolver = new HitResolver(bus, new SpacePortal(inventory, rolls(0)));

        assertEquals(54, resolver.resolve(thanos, runner, new BasicAttack("Punch", 60)));
    }

    @Test
    void anEnemyKilledByItsOwnBlowLosesTheBattle() {
        inventory.addStone(InfinityStone.SPACE);
        FakeCombatant weak = new FakeCombatant("Runner", 10, 500, 10);
        SimpleTurnBattle battle = new SimpleTurnBattle(bus, new HitResolver(bus, new SpacePortal(inventory, rolls(0))),
                thanos, weak, 85, true);
        battle.playerDefend();

        battle.enemyTurn();

        assertEquals(BattleState.WON, battle.state());
        assertEquals(1000, thanos.health());
        assertEquals(1, events.stream().filter(e -> e.type() == GameEventType.BATTLE_WON).count());
    }

    @Test
    void theSpaceStoneDescribesItsPortal() {
        assertEquals("Portal", InfinityStone.SPACE.passiveName());
        assertTrue(InfinityStone.SPACE.passive().contains(SpacePortal.CHANCE_PERCENT + "%"));
    }
}
