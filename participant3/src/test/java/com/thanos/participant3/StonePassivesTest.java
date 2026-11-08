package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.battle.SimpleTurnBattle;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.AbilityBook;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.stones.SoulHarvest;
import com.thanos.participant3.ui.BattleMessages;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class StonePassivesTest {
    private final EventBus bus = new EventBus();
    private final Inventory inventory = new Inventory(bus, 50);
    private final FakeCombatant thanos = new FakeCombatant("Thanos", 600, 1000, 10);
    private final FakeCombatant runner = new FakeCombatant("Runner", 500, 500, 10);

    private static Ability ability(String name) {
        return AbilityBook.forStones(List.of(InfinityStone.values())).stream()
                .filter(a -> a.name().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void everyStoneHasANamedPassive() {
        for (InfinityStone stone : InfinityStone.values()) {
            assertFalse(stone.passiveName().isEmpty());
            assertTrue(stone.passive().startsWith(stone.passiveName() + ": "), stone.displayName());
        }
    }

    @Test
    void powerMakesEveryBlowStronger() {
        assertEquals(60, inventory.empower(new BasicAttack("Punch", 60)).damage());

        inventory.addStone(InfinityStone.POWER);

        assertEquals(66, inventory.empower(new BasicAttack("Punch", 60)).damage());
    }

    @Test
    void timeAddsEnergyEveryTurn() {
        assertEquals(10, inventory.energyPerTurn());

        inventory.addStone(InfinityStone.TIME);

        assertEquals(15, inventory.energyPerTurn());
    }

    @Test
    void realityMakesAbilitiesCheaper() {
        inventory.addStone(InfinityStone.SOUL);
        Ability drain = ability("Soul Drain");
        assertEquals(20, inventory.costOf(drain));

        inventory.addStone(InfinityStone.REALITY);
        inventory.use(drain, new BasicAttack("Punch", 60));

        assertEquals(15, inventory.costOf(drain));
        assertEquals(50 - 15, inventory.energy());
    }

    @Test
    void soulHealsAfterEachVictory() {
        bus.subscribe(new SoulHarvest(thanos, inventory, bus));
        bus.publish(GameEventType.BATTLE_WON, "Runner", "Runner is defeated. Thanos wins!", 0);
        assertEquals(600, thanos.health());

        inventory.addStone(InfinityStone.SOUL);
        bus.publish(GameEventType.BATTLE_WON, "Runner", "Runner is defeated. Thanos wins!", 0);

        assertEquals(750, thanos.health());
    }

    @Test
    void theHarvestLineComesAfterTheVictoryLine() {
        BattleMessages messages = new BattleMessages();
        bus.subscribe(messages);
        bus.subscribe(new SoulHarvest(thanos, inventory, bus));
        inventory.addStone(InfinityStone.SOUL);
        messages.clear();

        bus.publish(GameEventType.BATTLE_WON, "Runner", "Runner is defeated. Thanos wins!", 0);

        assertEquals("Runner is defeated. Thanos wins!", messages.current());
        messages.advance();
        assertEquals(GameEventType.HEAL, messages.currentEvent().type());
    }

    @Test
    void foresightTellsTheNextBlow() {
        SimpleTurnBattle battle = new SimpleTurnBattle(bus, thanos, runner, 75, true);
        assertEquals(OptionalInt.of(67), battle.nextEnemyDamage());

        battle.playerAttack(ability("Mind Blast").apply(new BasicAttack("Punch", 60)));
        assertEquals(OptionalInt.of(0), battle.nextEnemyDamage());
    }

    @Test
    void rewindUndoesTheLastWoundOnce() {
        HitResolver resolver = new HitResolver(bus);
        resolver.resolve(runner, thanos, new BasicAttack("Runner strike", 85));
        assertEquals(524, thanos.health());
        assertEquals(76, resolver.lastWound(thanos));

        resolver.resolve(thanos, runner, ability("Rewind").apply(new BasicAttack("Punch", 60)));
        assertEquals(600, thanos.health());
        assertEquals(473, runner.health());
        assertEquals(0, resolver.lastWound(thanos));

        resolver.resolve(thanos, runner, ability("Rewind").apply(new BasicAttack("Punch", 60)));
        assertEquals(600, thanos.health());
    }
}
