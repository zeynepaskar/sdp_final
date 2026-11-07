package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.ui.BattleScreen;
import com.thanos.participant3.ui.PixelSprite;
import com.thanos.participant3.ui.Sprites;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SpritesTest {
    @Test
    void rejectsRaggedRowsAndUnknownKeys() {
        Map<Character, Integer> palette = Map.of('K', 0x000000FF);

        assertThrows(IllegalArgumentException.class, () -> new PixelSprite(palette, "KK", "K"));
        assertThrows(IllegalArgumentException.class, () -> new PixelSprite(palette, "KX"));
        assertEquals(2, new PixelSprite(palette, "K.", ".K").width());
    }

    @Test
    void everyStoneHasATwoByTwoSocketInTheGauntlet() {
        for (InfinityStone stone : InfinityStone.values()) {
            int[] socket = Sprites.socket(stone);
            char key = Sprites.STONE_KEYS.charAt(stone.ordinal());

            assertEquals(stone, Sprites.stoneOf(key));
            assertEquals(key, Sprites.THANOS.key(socket[0] + 1, socket[1]));
            assertEquals(key, Sprites.THANOS.key(socket[0], socket[1] + 1));
            assertEquals(key, Sprites.THANOS.key(socket[0] + 1, socket[1] + 1));
        }
        assertNull(Sprites.stoneOf('K'));
    }

    @Test
    void gauntletSlotsAreSeparateAndTheRewardLandsInThem() {
        Set<Vector2> slots = new HashSet<>();
        for (InfinityStone stone : InfinityStone.values()) slots.add(BattleScreen.gauntletSlot(stone));

        assertEquals(6, slots.size());
        Vector2 soul = BattleScreen.gauntletSlot(InfinityStone.SOUL);
        Vector2 power = BattleScreen.gauntletSlot(InfinityStone.POWER);
        Vector2 space = BattleScreen.gauntletSlot(InfinityStone.SPACE);
        assertEquals(soul.y, power.y);
        assertTrue(power.x > soul.x);
        assertTrue(space.y < soul.y, "the second row is lower on screen");
    }

    @Test
    void regularEnemiesAndEldersHaveSprites() {
        assertSame(Sprites.OUTRIDER, Sprites.forEnemy("Outrider"));
        assertSame(Sprites.CHITAURI, Sprites.forEnemy("Chitauri"));
        assertSame(Sprites.SAKAARAN, Sprites.forEnemy("Sakaaran"));
        assertSame(Sprites.IN_BETWEENER, Sprites.forEnemy("In-Betweener"));
        assertSame(Sprites.GRANDMASTER, Sprites.forEnemy("Grandmaster"));
        for (String elder : new String[] {"Champion", "Gardener", "Runner", "Collector"}) {
            assertNotNull(Sprites.forEnemy(elder), elder);
        }
        assertNull(Sprites.forEnemy("Nobody"));
        assertNull(Sprites.forEnemy(null));
    }
}
