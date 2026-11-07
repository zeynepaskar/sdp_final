package com.thanos.participant3.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.thanos.participant3.SimpleCombatant;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.HitResolver;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.InfinityStone;
import com.thanos.participant3.ui.ConsoleLog;
import com.thanos.participant3.ui.Hud;
import com.thanos.participant3.ui.HudModel;

/** Standalone libGDX launcher for the HUD, Inventory and decorated attacks. */
public final class HudDemo extends ApplicationAdapter {
    private static final String[][] ENEMIES = {{"Iron Man", "300", "15"}, {"Thor", "400", "25"},
            {"Captain America", "350", "30"}};
    private static final int ENEMY_DAMAGE = 60;
    private static final String HELP = "1-6  get a stone     SPACE  basic punch     A  punch with every stone you own\n"
            + "H  unsubscribe / subscribe the HUD     ESC  quit";
    private final EventBus events = new EventBus();
    private final SimpleCombatant thanos = new SimpleCombatant("Thanos", 1000, 10);
    private final Inventory inventory = new Inventory(events, 60);
    private final HitResolver resolver = new HitResolver(events);
    private final FitViewport viewport = new FitViewport(1280, 800);
    private SimpleCombatant enemy;
    private int enemyIndex, enemyStun, enemySlow;
    private HudModel hudModel;
    private boolean hudSubscribed = true;
    private Hud hud;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    public void create() {
        hudModel = new HudModel(thanos, inventory, 3);
        events.subscribe(hudModel);
        events.subscribe(new ConsoleLog());
        hud = new Hud(hudModel);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Hud.INK);
        spawnEnemy();
    }

    public void render() {
        handleInput();
        ScreenUtils.clear(Hud.PAPER);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Hud.INK);
        shapes.rect(440, 420, 400, 24);
        shapes.setColor(Color.valueOf("D8D2B0"));
        shapes.rect(443, 423, 394, 18);
        shapes.setColor(Color.valueOf("C0392B"));
        shapes.rect(443, 423, 394f * enemy.health() / enemy.maxHealth(), 18);
        shapes.end();

        batch.begin();
        font.getData().setScale(2f);
        font.draw(batch, "Participant 3 demo", 80, 760);
        font.draw(batch, enemy.name(), 440, 490);
        font.getData().setScale(1.1f);
        font.draw(batch, HELP, 80, 715);
        font.draw(batch, enemy.health() + "/" + enemy.maxHealth() + " HP   defense " + enemy.defense()
                + (enemyStun > 0 ? "   STUNNED" : "") + (enemySlow > 0 ? "   slowed " + enemySlow + "%" : ""), 440, 405);
        font.draw(batch, hudSubscribed ? "HUD is subscribed" : "HUD is UNSUBSCRIBED: it froze, the battle goes on",
                80, 190);
        batch.end();

        hud.render(viewport.getCamera().combined);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
        for (int i = 0; i < InfinityStone.values().length; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) inventory.addStone(InfinityStone.values()[i]);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) playerTurn(false);
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) playerTurn(true);
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            hudSubscribed = !hudSubscribed;
            if (hudSubscribed) events.subscribe(hudModel);
            else events.unsubscribe(hudModel);
        }
    }

    private void playerTurn(boolean useStones) {
        Attack attack = new BasicAttack("Punch", 80);
        if (useStones) {
            for (Ability ability : inventory.abilities()) {
                attack = inventory.use(ability, attack).orElse(attack);
            }
        }
        resolver.resolve(thanos, enemy, attack);
        enemyStun += attack.stunTurns();
        enemySlow = attack.slowPercent();
        if (!enemy.isAlive()) {
            enemyIndex = (enemyIndex + 1) % ENEMIES.length;
            spawnEnemy();
        } else {
            enemyTurn();
        }
        inventory.restoreEnergy(10);
    }

    private void enemyTurn() {
        if (enemyStun > 0) {
            enemyStun--;
            return;
        }
        resolver.resolve(enemy, thanos, new BasicAttack(enemy.name() + " strike",
                ENEMY_DAMAGE * (100 - enemySlow) / 100));
        enemySlow = 0;
        if (!thanos.isAlive()) {
            thanos.heal(thanos.maxHealth());
            events.publish(GameEventType.HEAL, thanos.name(), "Lady Death sends Thanos back to the fight",
                    thanos.maxHealth());
        }
    }

    private void spawnEnemy() {
        String[] stats = ENEMIES[enemyIndex];
        enemy = new SimpleCombatant(stats[0], Integer.parseInt(stats[1]), Integer.parseInt(stats[2]));
        enemyStun = 0;
        enemySlow = 0;
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {
        hud.dispose();
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Thanos | Participant 3 HUD demo");
        configuration.setWindowedMode(1280, 800);
        configuration.setWindowSizeLimits(800, 500, -1, -1);
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);
        configuration.disableAudio(true);
        new Lwjgl3Application(new HudDemo(), configuration);
    }
}
