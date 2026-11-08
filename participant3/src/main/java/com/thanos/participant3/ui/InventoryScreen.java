package com.thanos.participant3.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.thanos.participant3.Combatant;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.progress.Thanos;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.InfinityStone;
import java.util.List;

/** Renders InventoryPage and handles its input. */
public final class InventoryScreen implements Disposable {
    private static final Color SHADOW = Color.valueOf("D8D2B0");
    private static final Color HEALTH = Color.valueOf("C0392B");
    private static final Color ENERGY = Color.valueOf("2E86DE");
    private static final Color EXPERIENCE = Color.valueOf("9B59B6");
    private static final Color MUTED = Color.valueOf("9A9480");
    private static final Color TITAN = Color.valueOf("E2B33C");
    private static final float ROW_TOP = 392, ROW_HEIGHT = 30;
    private final InventoryPage page;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Color color = new Color();
    private final Vector2 pointer = new Vector2();
    private boolean open;

    public InventoryScreen(Inventory inventory, Combatant thanos) {
        page = new InventoryPage(inventory, thanos);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public boolean isOpen() { return open; }

    public void open() {
        open = true;
        page.reset();
    }

    public void close() { open = false; }

    public void update(Viewport viewport) {
        if (!open) return;
        if (pressed(Input.Keys.I) || pressed(Input.Keys.TAB) || pressed(Input.Keys.ESCAPE)
                || Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            close();
            return;
        }
        if (pressed(Input.Keys.DOWN) || pressed(Input.Keys.S)) page.move(1);
        if (pressed(Input.Keys.UP) || pressed(Input.Keys.W)) page.move(-1);
        if (viewport == null) return;
        viewport.unproject(pointer.set(Gdx.input.getX(), Gdx.input.getY()));
        boolean click = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (click && pointer.x >= 1190 && pointer.x < 1240 && pointer.y >= ROW_TOP - 24 && pointer.y < ROW_TOP + 10) {
            page.move(-1);
        } else if (click && pointer.x >= 1190 && pointer.x < 1240 && pointer.y >= 48 && pointer.y < 82) {
            page.move(1);
        } else if (pointer.x >= 48 && pointer.x < 1180 && pointer.y <= ROW_TOP + 8
                && pointer.y > ROW_TOP + 8 - InventoryPage.VISIBLE_ROWS * ROW_HEIGHT) {
            int row = page.firstRow() + (int) ((ROW_TOP + 8 - pointer.y) / ROW_HEIGHT);
            if (row < page.abilities().size()) page.select(row);
        }
    }

    private static boolean pressed(int key) { return Gdx.input.isKeyJustPressed(key); }

    public void render(Matrix4 projection) {
        if (!open) return;
        Inventory inventory = page.inventory();
        Combatant thanos = page.thanos();
        Thanos leveled = page.leveled();
        List<Ability> abilities = page.abilities();
        shapes.setProjectionMatrix(projection);
        batch.setProjectionMatrix(projection);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        panel(0, 0, 1280, 800);
        panel(32, 470, 410, 260);
        panel(458, 470, 790, 260);
        panel(32, 32, 1216, 422);
        bar(60, 640, 230, 16, thanos.health(), thanos.maxHealth(), HEALTH);
        bar(60, 612, 230, 12, inventory.energy(), inventory.maxEnergy(), ENERGY);
        if (leveled != null) bar(60, 588, 230, 10, leveled.experience(), leveled.experienceToNextLevel(), EXPERIENCE);
        InfinityStone[] stones = InfinityStone.values();
        for (int i = 0; i < stones.length; i++) {
            float y = 674 - i * 33;
            shapes.setColor(Hud.INK);
            shapes.rect(480, y, 22, 22);
            shapes.setColor(inventory.has(stones[i]) ? color.set(stones[i].rgba()) : SHADOW);
            shapes.rect(483, y + 3, 16, 16);
        }
        for (int row = page.firstRow(); row < Math.min(abilities.size(), page.firstRow() + InventoryPage.VISIBLE_ROWS); row++) {
            float y = ROW_TOP - (row - page.firstRow()) * ROW_HEIGHT - 14;
            if (row == page.cursor()) {
                shapes.setColor(SHADOW);
                shapes.rect(48, y - 8, 1136, ROW_HEIGHT);
            }
            Ability ability = abilities.get(row);
            shapes.setColor(ability.isBase() ? color.set(TITAN) : color.set(ability.stone().rgba()));
            shapes.rect(64, y, 12, 12);
        }
        shapes.end();

        batch.begin();
        text("INVENTORY", 40, 782, 2.2f, Hud.INK);
        text("I / TAB / ESC close     arrows scroll", 900, 774, 1.1f, MUTED);
        text(thanos.name().toUpperCase() + (leveled != null ? "    Lv " + leveled.level() : ""), 60, 712, 1.6f, Hud.INK);
        text("HP " + thanos.health() + "/" + thanos.maxHealth(), 304, 654, 1f, Hud.INK);
        text("EN " + inventory.energy() + "/" + inventory.maxEnergy(), 304, 624, 1f, Hud.INK);
        if (leveled != null) text("XP " + leveled.experience() + "/" + leveled.experienceToNextLevel(), 304, 599, 1f, Hud.INK);
        List<String> stats = page.stats();
        for (int i = 0; i < stats.size(); i++) text(stats.get(i), 60, 556 - i * 24, 1.2f, Hud.INK);
        text("STONES  " + inventory.stoneCount() + "/6", 480, 716, 1.3f, Hud.INK);
        for (int i = 0; i < stones.length; i++) {
            boolean owned = inventory.has(stones[i]);
            float y = 691 - i * 33;
            text(stones[i].displayName(), 514, y, 1.2f, owned ? Hud.INK : MUTED);
            text(owned ? stones[i].passive() : "not collected", 660, y, 1.05f, owned ? Hud.INK : MUTED);
        }
        text("ABILITIES  " + abilities.size(), 52, 438, 1.3f, Hud.INK);
        for (int row = page.firstRow(); row < Math.min(abilities.size(), page.firstRow() + InventoryPage.VISIBLE_ROWS); row++) {
            Ability ability = abilities.get(row);
            float y = ROW_TOP - (row - page.firstRow()) * ROW_HEIGHT;
            text(ability.name(), 90, y, 1.2f, Hud.INK);
            text(inventory.costOf(ability) + " EN", 290, y, 1.2f, Hud.INK);
            text(ability.description(), 380, y, 1.1f, Hud.INK);
        }
        if (page.firstRow() > 0) text("^", 1210, ROW_TOP, 1.4f, Hud.INK);
        if (page.firstRow() + InventoryPage.VISIBLE_ROWS < abilities.size()) text("v", 1210, 74, 1.4f, Hud.INK);
        batch.end();
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
