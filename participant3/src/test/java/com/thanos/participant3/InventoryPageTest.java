package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.progress.Thanos;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.ui.InventoryPage;
import java.util.List;
import org.junit.jupiter.api.Test;

class InventoryPageTest {
    private final EventBus bus = new EventBus();
    private final Thanos thanos = new Thanos(bus);
    private final Inventory inventory = new Inventory(bus, 50);
    private final InventoryPage page = new InventoryPage(inventory, thanos);

    @Test
    void statsIncludeLevelsAndPassives() {
        assertEquals(List.of("Punch  60", "Defense  10", "Energy per turn  +10"), page.stats());

        thanos.gainExperience(50);
        inventory.addStone(InfinityStone.POWER);
        inventory.addStone(InfinityStone.TIME);

        assertEquals(List.of("Punch  77", "Defense  12", "Energy per turn  +15"), page.stats());
    }

    @Test
    void theCursorStaysOnTheListAndScrolls() {
        for (InfinityStone stone : InfinityStone.values()) inventory.addStone(stone);

        page.move(-3);
        assertEquals(0, page.cursor());
        page.move(InventoryPage.VISIBLE_ROWS);
        assertEquals(InventoryPage.VISIBLE_ROWS, page.cursor());
        assertEquals(1, page.firstRow());
        page.move(100);
        assertEquals(17, page.cursor());
        assertEquals(18 - InventoryPage.VISIBLE_ROWS, page.firstRow());

        page.reset();
        assertEquals(0, page.cursor());
        assertEquals(0, page.firstRow());
    }

    @Test
    void anEmptyListKeepsTheCursorAtTheTop() {
        page.move(5);

        assertEquals(0, page.cursor());
        assertTrue(page.abilities().isEmpty());
    }
}
