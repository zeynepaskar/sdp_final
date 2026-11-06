package com.thanos.participant3.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.thanos.participant3.Combatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.TurnBattle;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.InfinityStone;
import java.util.List;

/**
 * Turn-based battle screen on a 1280x800 view: enemy top right, Thanos bottom left,
 * messages and a menu along the bottom. Call update() then render() every frame.
 */
public final class BattleScreen implements Disposable {
    public static final int PUNCH_DAMAGE = 60;
    private static final String[] MAIN = {"Attack", "Stones", "Defend", "Flee"};
    private static final Color SHADOW = Color.valueOf("D8D2B0");
    private static final Color HEALTH = Color.valueOf("C0392B");
    private static final Color ENERGY = Color.valueOf("2E86DE");
    private static final Color MUTED = Color.valueOf("9A9480");
    private enum Menu { MAIN, ATTACK, STONES }

    private final TurnBattle battle;
    private final Inventory inventory;
    private final BattleMessages messages;
    private final int enemyRgba;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Color color = new Color();
    private Menu menu = Menu.MAIN;
    private int cursor;
    private String hint = "";

    /** messages must already be subscribed to the battle's EventBus. */
    public BattleScreen(TurnBattle battle, Inventory inventory, BattleMessages messages, int enemyRgba) {
        if (battle == null || inventory == null || messages == null) {
            throw new IllegalArgumentException("Battle screen needs a battle, an inventory and messages");
        }
        this.battle = battle;
        this.inventory = inventory;
        this.messages = messages;
        this.enemyRgba = enemyRgba;
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    /** True once the battle is over and the player has read every message. */
    public boolean isFinished() { return battle.state().isOver() && !messages.hasPending(); }

    public void update() {
        boolean confirm = pressed(Input.Keys.ENTER) || pressed(Input.Keys.SPACE);
        boolean back = pressed(Input.Keys.ESCAPE) || pressed(Input.Keys.BACKSPACE);
        if (messages.hasPending()) {
            if (confirm) messages.advance();
            return;
        }
        if (battle.state() == BattleState.ENEMY_TURN) {
            battle.enemyTurn();
            return;
        }
        if (battle.state() != BattleState.PLAYER_TURN) return;
        int size = menu == Menu.MAIN ? MAIN.length : menu == Menu.ATTACK ? attacks().size() + 1 : 1;
        if (menu == Menu.MAIN) {
            if (pressed(Input.Keys.RIGHT) || pressed(Input.Keys.D)) cursor = cursor ^ 1;
            if (pressed(Input.Keys.LEFT) || pressed(Input.Keys.A)) cursor = cursor ^ 1;
            if (pressed(Input.Keys.DOWN) || pressed(Input.Keys.S)) cursor = cursor ^ 2;
            if (pressed(Input.Keys.UP) || pressed(Input.Keys.W)) cursor = cursor ^ 2;
        } else {
            if (pressed(Input.Keys.DOWN) || pressed(Input.Keys.S)) cursor = (cursor + 1) % size;
            if (pressed(Input.Keys.UP) || pressed(Input.Keys.W)) cursor = (cursor + size - 1) % size;
        }
        if (back && menu != Menu.MAIN) backToMain();
        else if (confirm) choose();
    }

    private void choose() {
        hint = "";
        switch (menu) {
            case MAIN -> {
                switch (cursor) {
                    case 0 -> open(Menu.ATTACK);
                    case 1 -> open(Menu.STONES);
                    case 2 -> battle.playerDefend();
                    default -> {
                        if (battle.canFlee()) battle.flee();
                        else hint = "There is no escape from " + battle.enemy().name() + "!";
                    }
                }
            }
            case ATTACK -> {
                Attack punch = new BasicAttack("Punch", PUNCH_DAMAGE);
                if (cursor == 0) {
                    battle.playerAttack(punch);
                    open(Menu.MAIN);
                    return;
                }
                Ability ability = attacks().get(cursor - 1);
                inventory.use(ability, punch).ifPresentOrElse(attack -> {
                    battle.playerAttack(attack);
                    open(Menu.MAIN);
                }, () -> hint = "Not enough energy for " + ability.name());
            }
            case STONES -> backToMain();
        }
    }

    /** Returns to the main menu with the cursor on the submenu it came from. */
    private void backToMain() {
        int from = menu == Menu.STONES ? 1 : 0;
        open(Menu.MAIN);
        cursor = from;
    }

    private void open(Menu next) {
        menu = next;
        cursor = 0;
    }

    private List<Ability> attacks() { return inventory.abilities(); }

    private static boolean pressed(int key) { return Gdx.input.isKeyJustPressed(key); }

    public void render(Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
        batch.setProjectionMatrix(projection);
        Combatant enemy = battle.enemy(), thanos = battle.thanos();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(SHADOW);
        shapes.ellipse(800, 455, 360, 80);
        shapes.ellipse(150, 205, 420, 90);
        if (enemy.isAlive() || battle.state() != BattleState.WON) drawEnemy(enemy);
        drawThanos();
        panel(80, 600, 470, 110);
        bar(110, 620, 300, 16, enemy.health(), enemy.maxHealth(), HEALTH);
        panel(730, 250, 500, 150);
        bar(760, 320, 300, 16, thanos.health(), thanos.maxHealth(), HEALTH);
        bar(760, 280, 300, 12, inventory.energy(), Inventory.MAX_ENERGY, ENERGY);
        panel(16, 16, 760, 190);
        panel(792, 16, 472, 190);
        if (menu == Menu.STONES) {
            InfinityStone[] stones = InfinityStone.values();
            for (int i = 0; i < stones.length; i++) {
                shapes.setColor(Hud.INK);
                shapes.rect(40, 164 - i * 24, 18, 18);
                shapes.setColor(inventory.has(stones[i]) ? color.set(stones[i].rgba()) : SHADOW);
                shapes.rect(42, 166 - i * 24, 14, 14);
            }
        }
        shapes.end();

        batch.begin();
        text(enemy.name(), 110, 690, 1.6f, Hud.INK);
        text("HP", 420, 634, 1f, Hud.INK);
        text(thanos.name(), 760, 385, 1.6f, Hud.INK);
        text("HP " + thanos.health() + "/" + thanos.maxHealth(), 1075, 334, 1f, Hud.INK);
        text("EN " + inventory.energy() + "/" + Inventory.MAX_ENERGY, 1075, 292, 1f, Hud.INK);
        drawMessageBox();
        drawMenu();
        batch.end();
    }

    private void drawMessageBox() {
        if (menu == Menu.STONES && !messages.hasPending()) {
            InfinityStone[] stones = InfinityStone.values();
            for (int i = 0; i < stones.length; i++) {
                boolean owned = inventory.has(stones[i]);
                text(stones[i].displayName() + (owned ? "  -  " + stones[i].effect() : "  -  not collected"),
                        70, 181 - i * 24, 1.1f, owned ? Hud.INK : MUTED);
            }
            return;
        }
        String line = messages.hasPending() ? messages.current()
                : battle.state().isOver() ? "" : hint.isEmpty() ? "What will Thanos do?" : hint;
        text(line, 44, 160, 1.7f, Hud.INK);
        if (messages.hasPending()) text("ENTER", 680, 50, 1f, MUTED);
    }

    private void drawMenu() {
        boolean active = battle.state() == BattleState.PLAYER_TURN && !messages.hasPending();
        Color ink = active ? Hud.INK : MUTED;
        switch (menu) {
            case MAIN -> {
                for (int i = 0; i < MAIN.length; i++) {
                    float x = 830 + (i % 2) * 210, y = 150 - (i / 2) * 70;
                    text((active && cursor == i ? "> " : "  ") + MAIN[i], x, y, 1.8f, ink);
                }
            }
            case ATTACK -> {
                List<Ability> abilities = attacks();
                text((cursor == 0 ? "> " : "  ") + "Punch", 820, 190, 1.2f, ink);
                for (int i = 0; i < abilities.size(); i++) {
                    Ability ability = abilities.get(i);
                    boolean affordable = ability.energyCost() <= inventory.energy();
                    text((cursor == i + 1 ? "> " : "  ") + ability.name() + "   " + ability.energyCost() + " EN",
                            820, 190 - (i + 1) * 22, 1.2f, affordable ? ink : MUTED);
                }
                if (abilities.isEmpty()) text("  no stones yet", 820, 166, 1.2f, MUTED);
            }
            case STONES -> text("> Back", 830, 150, 1.8f, ink);
        }
    }

    private void drawEnemy(Combatant enemy) {
        shapes.setColor(Hud.INK);
        shapes.rect(918, 488, 124, 144);
        shapes.setColor(color.set(enemyRgba));
        shapes.rect(922, 492, 116, 136);
        shapes.setColor(Hud.INK);
        shapes.rect(950, 590, 14, 14);
        shapes.rect(996, 590, 14, 14);
    }

    private void drawThanos() {
        shapes.setColor(Hud.INK);
        shapes.rect(276, 236, 168, 196);
        shapes.setColor(color.set(0x6C4F8CFF));
        shapes.rect(280, 240, 160, 188);
        shapes.setColor(color.set(0xE0B03AFF));
        shapes.rect(420, 300, 46, 52);
        InfinityStone[] stones = InfinityStone.values();
        for (int i = 0; i < stones.length; i++) {
            if (!inventory.has(stones[i])) continue;
            shapes.setColor(color.set(stones[i].rgba()));
            shapes.rect(424 + (i % 3) * 14, 330 - (i / 3) * 14, 10, 10);
        }
    }

    private void panel(float x, float y, float w, float h) {
        shapes.setColor(Hud.INK);
        shapes.rect(x, y, w, h);
        shapes.setColor(Hud.PAPER);
        shapes.rect(x + 4, y + 4, w - 8, h - 8);
    }

    private void bar(float x, float y, float w, float h, int value, int max, Color fill) {
        shapes.setColor(Hud.INK);
        shapes.rect(x, y, w, h);
        shapes.setColor(SHADOW);
        shapes.rect(x + 2, y + 2, w - 4, h - 4);
        shapes.setColor(fill);
        shapes.rect(x + 2, y + 2, (w - 4) * Math.max(0, Math.min(1, value / (float) Math.max(1, max))), h - 4);
    }

    private void text(String value, float x, float y, float scale, Color tint) {
        font.getData().setScale(scale);
        font.setColor(tint);
        font.draw(batch, value, x, y);
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
