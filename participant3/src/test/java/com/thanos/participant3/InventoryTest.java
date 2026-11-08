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
        assertEquals(8, inventory.abilities().size());
    }

    @Test
    void stonesUnlockAndUpgradeInTheOrderFound() {
        inventory.addStone(InfinityStone.TIME);
        inventory.addStone(InfinityStone.SOUL);

        assertEquals(List.of(InfinityStone.TIME, InfinityStone.SOUL), inventory.stonesInOrder());
        assertEquals(List.of("Crushing Blow", "Titan's Wrath", "Rewind", "Soul Drain"),
                inventory.abilities().stream().map(Ability::name).toList());
    }

    @Test
    void baseAbilitiesNeedNoStone() {
        Ability fury = AbilityBook.baseSkills().get(1).basic();

        assertEquals(150, inventory.use(fury, new BasicAttack("Punch", 100)).orElseThrow().damage());
        assertEquals(25, inventory.energy());
        assertTrue(events.stream().anyMatch(e -> e.message().equals("Thanos used Titan's Fury")));
    }

    @Test
    void usingAnAbilitySpendsEnergyAndDecoratesTheAttack() {
        inventory.addStone(InfinityStone.POWER);
        Ability energyBlast = AbilityBook.forStone(InfinityStone.POWER).basic();

        Optional<Attack> attack = inventory.use(energyBlast, new BasicAttack("Punch", 100));

        assertEquals(225, attack.orElseThrow().damage());
        assertEquals(10, inventory.energy());
        assertTrue(events.stream().anyMatch(e -> e.type() == GameEventType.ABILITY_USED));
    }

    @Test
    void abilityNeedsItsStone() {
        Ability mindBlast = AbilityBook.forStone(InfinityStone.MIND).basic();

        assertTrue(inventory.use(mindBlast, new BasicAttack("Punch", 100)).isEmpty());
        assertEquals(50, inventory.energy());
    }

    @Test
    void abilityNeedsEnoughEnergy() {
        inventory.addStone(InfinityStone.REALITY);
        inventory.spendEnergy(40);
        Ability distortion = AbilityBook.forStone(InfinityStone.REALITY).basic();

        assertTrue(inventory.use(distortion, new BasicAttack("Punch", 100)).isEmpty());
        assertEquals(10, inventory.energy());
    }

    @Test
    void energyStaysWithinLimits() {
        inventory.restoreEnergy(500);

        assertEquals(Inventory.BASE_MAX_ENERGY, inventory.energy());
        assertFalse(inventory.spendEnergy(Inventory.BASE_MAX_ENERGY + 1));
        assertThrows(IllegalArgumentException.class, () -> new Inventory(bus, Inventory.BASE_MAX_ENERGY + 1));
    }

    @Test
    void stonesViewCannotBeModified() {
        inventory.addStone(InfinityStone.SPACE);

        assertThrows(UnsupportedOperationException.class, () -> inventory.stones().add(InfinityStone.MIND));
    }
}
