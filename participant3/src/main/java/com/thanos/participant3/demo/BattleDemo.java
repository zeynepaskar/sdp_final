package com.thanos.participant3.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.thanos.participant3.SimpleCombatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.SimpleTurnBattle;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.ui.BattleMessages;
import com.thanos.participant3.ui.BattleScreen;
import com.thanos.participant3.ui.ConsoleLog;
import com.thanos.participant3.ui.Hud;

/** Standalone turn-based battle against the six Elders, using the stand-in battle rules. */
public final class BattleDemo extends ApplicationAdapter {
    // Same health and power as the bosses in participant 2's BossType, in story order.
    private static final String[] NAMES = {"In-Betweener", "Champion", "Gardener", "Runner", "Collector", "Grandmaster"};
    private static final int[] HEALTH = {600, 800, 550, 500, 650, 700};
    private static final int[] POWER = {68, 90, 53, 75, 60, 68};
    private final EventBus events = new EventBus();
    private final SimpleCombatant thanos = new SimpleCombatant("Thanos", 1000, 10);
    private final Inventory inventory = new Inventory(events, 60);
    private final BattleMessages messages = new BattleMessages();
    private final FitViewport viewport = new FitViewport(1280, 800);
    private SimpleTurnBattle battle;
    private BattleScreen screen;
    private int bossIndex;

    public void create() {
        events.subscribe(messages);
        events.subscribe(new ConsoleLog());
        startBattle();
    }

    private void startBattle() {
        if (screen != null) screen.dispose();
        messages.clear();
        thanos.heal(thanos.maxHealth());
        inventory.restoreEnergy(40);
        battle = new SimpleTurnBattle(events, thanos,
                new SimpleCombatant(NAMES[bossIndex], HEALTH[bossIndex], 10), POWER[bossIndex], true);
        screen = new BattleScreen(battle, inventory, messages, InfinityStone.values()[bossIndex].rgba());
    }

    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) Gdx.app.exit();
        if (screen.isFinished()) {
            if (battle.state() == BattleState.WON) {
                inventory.addStone(InfinityStone.values()[bossIndex]);
                bossIndex = (bossIndex + 1) % NAMES.length;
            }
            startBattle();
        }
        screen.update();
        ScreenUtils.clear(Hud.PAPER);
        viewport.apply();
        screen.render(viewport.getCamera().combined);
    }

    public void resize(int width, int height) { viewport.update(width, height, true); }

    public void dispose() { screen.dispose(); }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Thanos | Participant 3 battle demo");
        configuration.setWindowedMode(1280, 800);
        configuration.setWindowSizeLimits(800, 500, -1, -1);
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);
        configuration.disableAudio(true);
        new Lwjgl3Application(new BattleDemo(), configuration);
    }
}
