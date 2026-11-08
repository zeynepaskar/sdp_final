package com.thanos.participant3.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.thanos.participant3.SimpleCombatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.SimpleTurnBattle;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.progress.Thanos;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.stones.SoulHarvest;
import com.thanos.participant3.stones.SpacePortal;
import com.thanos.participant3.ui.BattleMessages;
import com.thanos.participant3.ui.BattleScreen;
import com.thanos.participant3.ui.ConsoleLog;
import com.thanos.participant3.ui.Hud;
import com.thanos.participant3.ui.InventoryScreen;
import com.thanos.participant3.ui.StoneRewardView;
import com.thanos.participant3.ui.StoneRewards;
import java.util.Random;

/** Standalone libGDX launcher for BattleScreen against the six bosses and participant 2's regular enemies. */
public final class BattleDemo extends ApplicationAdapter {
    private static final String[] NAMES = {"In-Betweener", "Champion", "Gardener", "Runner", "Collector", "Grandmaster"};
    private static final int[] HEALTH = {600, 800, 550, 500, 650, 700};
    private static final int[] POWER = {68, 90, 53, 75, 60, 68};
    private static final String[] REGULAR_NAMES = {"Outrider", "Chitauri", "Sakaaran"};
    private static final int[] REGULAR_HEALTH = {100, 80, 150};
    private static final int[] REGULAR_POWER = {15, 12, 25};
    private static final String KEYS = "I inventory   F1 stone   F2 +100 XP   F3 regular   F4 next boss   F10 exit";
    private final EventBus events = new EventBus();
    private final Thanos thanos = new Thanos(events);
    private final Inventory inventory = new Inventory(events, 60);
    private final BattleMessages messages = new BattleMessages();
    private final StoneRewards rewards = new StoneRewards(inventory);
    private final HitResolver resolver = new HitResolver(events, new SpacePortal(inventory, new Random()));
    private final FitViewport viewport = new FitViewport(1280, 800);
    private SpriteBatch batch;
    private BitmapFont font;
    private SimpleTurnBattle battle;
    private BattleScreen screen;
    private StoneRewardView rewardView;
    private InventoryScreen inventoryScreen;
    private int bossIndex;
    private int regularIndex = -1;
    private int nextRegular;
    private boolean rewarded;

    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        events.subscribe(messages);
        events.subscribe(new ConsoleLog());
        events.subscribe(rewards);
        events.subscribe(new SoulHarvest(thanos, inventory, events));
        rewardView = new StoneRewardView(rewards, BattleScreen::gauntletSlot);
        inventoryScreen = new InventoryScreen(inventory, thanos);
        startBattle();
    }

    private void startBattle() {
        if (screen != null) screen.dispose();
        messages.clear();
        rewarded = false;
        thanos.heal(thanos.maxHealth());
        inventory.restoreEnergy(40);
        if (regularIndex >= 0) {
            battle = new SimpleTurnBattle(events, resolver, thanos, new SimpleCombatant(REGULAR_NAMES[regularIndex],
                    REGULAR_HEALTH[regularIndex], 0), REGULAR_POWER[regularIndex], false);
            screen = new BattleScreen(battle, inventory, messages, Color.rgba8888(Hud.INK));
            return;
        }
        battle = new SimpleTurnBattle(events, resolver, thanos,
                new SimpleCombatant(NAMES[bossIndex], HEALTH[bossIndex], 10), POWER[bossIndex], true);
        screen = new BattleScreen(battle, inventory, messages, InfinityStone.values()[bossIndex].rgba());
    }

    private void giveNextStone() {
        for (InfinityStone stone : InfinityStone.values()) {
            if (inventory.addStone(stone)) return;
        }
    }

    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) Gdx.app.exit();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) giveNextStone();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) thanos.gainExperience(100);
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3) && !rewards.isPlaying()) {
            regularIndex = nextRegular;
            nextRegular = (nextRegular + 1) % REGULAR_NAMES.length;
            startBattle();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4) && !rewards.isPlaying()) {
            if (regularIndex < 0) bossIndex = (bossIndex + 1) % NAMES.length;
            regularIndex = -1;
            startBattle();
        }
        if (screen.isFinished() && battle.state() == BattleState.WON && !rewarded) {
            rewarded = true;
            if (regularIndex < 0) inventory.addStone(InfinityStone.values()[bossIndex]);
            thanos.gainExperience(Thanos.experienceFor(battle.enemy()));
        }
        if (screen.isFinished() && !rewards.isPlaying()) {
            if (battle.state() == BattleState.WON && regularIndex < 0) bossIndex = (bossIndex + 1) % NAMES.length;
            regularIndex = -1;
            startBattle();
        }
        boolean menuKey = Gdx.input.isKeyJustPressed(Input.Keys.I);
        if (inventoryScreen.isOpen()) {
            inventoryScreen.update(viewport);
        } else if (menuKey && !rewards.isPlaying()) {
            inventoryScreen.open();
        } else if (rewards.isPlaying()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Z)
                    || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) rewards.skip();
            rewards.update(Math.min(Gdx.graphics.getDeltaTime(), 0.1f));
        } else {
            screen.update(viewport);
        }
        ScreenUtils.clear(Hud.PAPER);
        viewport.apply();
        screen.hideStone(rewards.current());
        screen.render(viewport.getCamera().combined);
        rewardView.render(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.GRAY);
        font.draw(batch, KEYS, 760, 785);
        batch.end();
        inventoryScreen.render(viewport.getCamera().combined);
    }

    public void resize(int width, int height) { viewport.update(width, height, true); }

    public void dispose() {
        screen.dispose();
        rewardView.dispose();
        inventoryScreen.dispose();
        batch.dispose();
        font.dispose();
    }

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
