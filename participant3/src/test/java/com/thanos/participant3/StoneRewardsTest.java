package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.ui.StoneRewards;
import com.thanos.participant3.ui.StoneRewards.Phase;
import org.junit.jupiter.api.Test;

class StoneRewardsTest {
    private static final float TOTAL = StoneRewards.APPEAR_SECONDS + StoneRewards.SHOW_SECONDS + StoneRewards.FLY_SECONDS;
    private final EventBus bus = new EventBus();
    private final Inventory inventory = new Inventory(bus, 50);
    private final StoneRewards rewards = new StoneRewards(inventory);

    StoneRewardsTest() { bus.subscribe(rewards); }

    @Test
    void aNewStoneStartsItsReveal() {
        assertFalse(rewards.isPlaying());

        inventory.addStone(InfinityStone.POWER);

        assertTrue(rewards.isPlaying());
        assertEquals(InfinityStone.POWER, rewards.current());
        assertEquals(1, rewards.count());
        assertEquals(Phase.APPEAR, rewards.phase());
    }

    @Test
    void theRevealGoesThroughItsPhasesAndEnds() {
        inventory.addStone(InfinityStone.SOUL);

        rewards.update(StoneRewards.APPEAR_SECONDS / 2);
        assertEquals(Phase.APPEAR, rewards.phase());
        assertEquals(0.5f, rewards.phaseProgress(), 1e-4);
        rewards.update(StoneRewards.APPEAR_SECONDS / 2);
        assertEquals(Phase.SHOW, rewards.phase());
        rewards.update(StoneRewards.SHOW_SECONDS);
        assertEquals(Phase.FLY, rewards.phase());
        rewards.update(StoneRewards.FLY_SECONDS);

        assertFalse(rewards.isPlaying());
        assertNull(rewards.current());
    }

    @Test
    void skipFirstSendsTheStoneFlyingThenEndsTheReveal() {
        inventory.addStone(InfinityStone.TIME);

        rewards.skip();
        assertEquals(Phase.FLY, rewards.phase());
        assertEquals(0, rewards.phaseProgress(), 1e-4);
        rewards.skip();

        assertFalse(rewards.isPlaying());
    }

    @Test
    void severalStonesPlayOneAfterAnother() {
        inventory.addStone(InfinityStone.SOUL);
        inventory.addStone(InfinityStone.MIND);

        rewards.update(TOTAL);

        assertEquals(InfinityStone.MIND, rewards.current());
        assertEquals(2, rewards.count());
        assertEquals(0, rewards.time(), 1e-4);
        rewards.update(TOTAL);
        assertFalse(rewards.isPlaying());
    }

    @Test
    void aStoneThanosAlreadyHasIsNotRevealedAgain() {
        inventory.addStone(InfinityStone.SPACE);
        rewards.update(TOTAL);

        inventory.addStone(InfinityStone.SPACE);

        assertFalse(rewards.isPlaying());
    }

    @Test
    void stonesOwnedBeforehandAreNotRevealed() {
        Inventory owned = new Inventory(new EventBus(), 50);
        owned.addStone(InfinityStone.REALITY);
        StoneRewards late = new StoneRewards(owned);

        late.onEvent(new GameEvent(GameEventType.STONE_ACQUIRED, "Thanos", "old news", 1));

        assertFalse(late.isPlaying());
    }

    @Test
    void timeCannotGoBack() {
        assertThrows(IllegalArgumentException.class, () -> rewards.update(-1));
    }
}
