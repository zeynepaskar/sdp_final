package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.progress.Thanos;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ThanosTest {
    private final EventBus bus = new EventBus();
    private final List<GameEvent> events = new ArrayList<>();
    private final Thanos thanos = new Thanos(bus);

    ThanosTest() { bus.subscribe(events::add); }

    private long count(GameEventType type) { return events.stream().filter(e -> e.type() == type).count(); }

    @Test
    void startsAtLevelOneWithBaseStats() {
        assertEquals(1, thanos.level());
        assertEquals(1000, thanos.maxHealth());
        assertEquals(10, thanos.defense());
        assertEquals(60, thanos.punchDamage());
        assertEquals(50, thanos.experienceToNextLevel());
    }

    @Test
    void enoughExperienceRaisesTheLevel() {
        thanos.takeDamage(300);

        thanos.gainExperience(75);

        assertEquals(2, thanos.level());
        assertEquals(25, thanos.experience());
        assertEquals(100, thanos.experienceToNextLevel());
        assertEquals(1100, thanos.maxHealth());
        assertEquals(800, thanos.health());
        assertEquals(12, thanos.defense());
        assertEquals(70, thanos.punchDamage());
        assertTrue(events.contains(new GameEvent(GameEventType.LEVEL_UP, "Thanos",
                "Thanos reached level 2! HP +100, defense +2, punch +10, energy +10", 2)));
    }

    @Test
    void notEnoughExperienceOnlyFillsTheBar() {
        thanos.gainExperience(40);

        assertEquals(1, thanos.level());
        assertEquals(40, thanos.experience());
        assertEquals(1, count(GameEventType.XP_GAINED));
        assertEquals(0, count(GameEventType.LEVEL_UP));
    }

    @Test
    void aBigRewardCanRaiseSeveralLevels() {
        thanos.gainExperience(150);

        assertEquals(3, thanos.level());
        assertEquals(0, thanos.experience());
        assertEquals(2, count(GameEventType.LEVEL_UP));
    }

    @Test
    void levelsStopAtTheMaximum() {
        thanos.gainExperience(100_000);
        thanos.gainExperience(500);

        assertEquals(Thanos.MAX_LEVEL, thanos.level());
        assertEquals(0, thanos.experience());
    }

    @Test
    void rewardIsAThirdOfTheEnemysHealth() {
        assertEquals(266, Thanos.experienceFor(new FakeCombatant("Champion", 800, 800, 10)));
    }

    @Test
    void theSixBossesAloneReachTheMaxLevelAtTheFinalBoss() {
        int[] bossHealth = {600, 800, 550, 500, 650, 700};
        for (int i = 0; i < 5; i++) thanos.gainExperience(bossHealth[i] / 3);
        assertEquals(Thanos.MAX_LEVEL - 1, thanos.level());

        thanos.gainExperience(bossHealth[5] / 3);

        assertEquals(Thanos.MAX_LEVEL, thanos.level());
    }

    @Test
    void levelUpRaisesTheEnergyLimitAndFillsTheGain() {
        Inventory inventory = new Inventory(bus, 50);

        thanos.gainExperience(150);

        assertEquals(Inventory.BASE_MAX_ENERGY + 20, inventory.maxEnergy());
        assertEquals(70, inventory.energy());
    }
}
