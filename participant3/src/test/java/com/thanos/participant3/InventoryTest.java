package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.observer.*;
import com.thanos.participant3.stones.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InventoryTest {
    private final EventBus bus = new EventBus();
    private final List<GameEvent> events = new ArrayList<>();
    private final Inventory inventory = new Inventory(bus, 50);

    InventoryTest() {
        bus.subscribe(events::add);
    }

    @Test
    void acquiringAStoneIsAnnouncedOnce() {
        assertTrue(inventory.addStone(InfinityStone.SOUL));
        assertFalse(inventory.addStone(InfinityStone.SOUL));

        assertEquals(1, inventory.stoneCount());
        assertEquals(List.of(new GameEvent(GameEventType.STONE_ACQUIRED, "Thanos",
                "Thanos acquired the Soul Stone (1/6)", 1)), events);
    }

    @Test
    void allSixStonesCompleteTheGauntlet() {
        for (InfinityStone stone : InfinityStone.values()) inventory.addStone(stone);

        assertTrue(inventory.isComplete());
        assertEquals(6, inventory.abilities().size());
    }

    @Test
    void stonesUnlockTheirAbilitiesInStoryOrder() {
        inventory.addStone(InfinityStone.TIME);
        inventory.addStone(InfinityStone.SOUL);

        assertEquals(List.of("Soul Drain", "Aging"), inventory.abilities().stream().map(Ability::name).toList());
    }

    @Test
    void usingAnAbilitySpendsEnergyAndDecoratesTheAttack() {
        inventory.addStone(InfinityStone.POWER);
        Ability cosmicStrike = AbilityBook.forStone(InfinityStone.POWER).get(0);

        Optional<Attack> attack = inventory.use(cosmicStrike, new BasicAttack("Punch", 100));

        assertEquals(150, attack.orElseThrow().damage());
        assertEquals(25, inventory.energy());
        assertTrue(events.stream().anyMatch(e -> e.type() == GameEventType.ABILITY_USED));
    }

    @Test
    void abilityNeedsItsStone() {
        Ability mindBlast = AbilityBook.forStone(InfinityStone.MIND).get(0);

        assertTrue(inventory.use(mindBlast, new BasicAttack("Punch", 100)).isEmpty());
        assertEquals(50, inventory.energy());
    }

    @Test
    void abilityNeedsEnoughEnergy() {
        inventory.addStone(InfinityStone.REALITY);
        inventory.spendEnergy(40);
        Ability lawBreaker = AbilityBook.forStone(InfinityStone.REALITY).get(0);

        assertTrue(inventory.use(lawBreaker, new BasicAttack("Punch", 100)).isEmpty());
        assertEquals(10, inventory.energy());
    }

    @Test
    void energyStaysWithinLimits() {
        inventory.restoreEnergy(500);

        assertEquals(Inventory.MAX_ENERGY, inventory.energy());
        assertFalse(inventory.spendEnergy(Inventory.MAX_ENERGY + 1));
        assertThrows(IllegalArgumentException.class, () -> new Inventory(bus, Inventory.MAX_ENERGY + 1));
    }

    @Test
    void stonesViewCannotBeModified() {
        inventory.addStone(InfinityStone.SPACE);

        assertThrows(UnsupportedOperationException.class, () -> inventory.stones().add(InfinityStone.MIND));
    }
}
