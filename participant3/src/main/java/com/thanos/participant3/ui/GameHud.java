package com.thanos.participant3.ui;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.thanos.participant3.Combatant;
import com.thanos.participant3.SimpleCombatant;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.stones.InfinityStone;

/** Facade for participant 1: EventBus, Inventory and HUD in one object. */
public final class GameHud implements Disposable {
    private final EventBus events = new EventBus();
    private final Combatant thanos;
    private final Inventory inventory = new Inventory(events, 50);
    private final HudModel model;
    private final Hud hud;

    public GameHud() {
        this(new SimpleCombatant("Thanos", 1000, 10));
    }

    public GameHud(Combatant thanos) {
        if (thanos == null) {
            throw new IllegalArgumentException("HUD needs Thanos");
        }
        this.thanos = thanos;
        model = new HudModel(thanos, inventory, 3);
        events.subscribe(model);
        hud = new Hud(model);
    }

    public EventBus events() { return events; }
    public Inventory inventory() { return inventory; }
    public Combatant thanos() { return thanos; }

    public boolean awardNextStone() {
        for (InfinityStone stone : InfinityStone.values()) {
            if (!inventory.has(stone)) return inventory.addStone(stone);
        }
        return false;
    }

    public void render(Matrix4 projection) { hud.render(projection); }

    public void dispose() { hud.dispose(); }
}
