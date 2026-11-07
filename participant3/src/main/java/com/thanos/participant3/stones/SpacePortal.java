package com.thanos.participant3.stones;

import com.thanos.participant3.Combatant;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.Portal;
import com.thanos.participant3.progress.Thanos;
import java.util.Random;

/** Space Stone Portal; Random is injected for tests. */
public final class SpacePortal implements Portal {
    public static final int CHANCE_PERCENT = 15;
    public static final int RETURN_PERCENT = 40;
    private final Inventory inventory;
    private final Random random;

    public SpacePortal(Inventory inventory, Random random) {
        if (inventory == null || random == null) {
            throw new IllegalArgumentException("Portal needs an inventory and a random source");
        }
        this.inventory = inventory;
        this.random = random;
    }

    public boolean opensFor(Combatant target) {
        if (target == null || !Thanos.NAME.equals(target.name()) || !inventory.has(InfinityStone.SPACE)) return false;
        return random.nextInt(100) < CHANCE_PERCENT;
    }

    public int returnPercent() { return RETURN_PERCENT; }
}
