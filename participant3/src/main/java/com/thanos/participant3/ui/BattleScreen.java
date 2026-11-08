package com.thanos.participant3.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.thanos.participant3.Combatant;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.TurnBattle;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.Attack;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.observer.GameEvent;
import com.thanos.participant3.patterns.observer.GameEventType;
import com.thanos.participant3.progress.Thanos;
import com.thanos.participant3.stones.Ability;
import com.thanos.participant3.stones.InfinityStone;
import java.util.List;

/** Battle view and input over TurnBattle; call update() then render() every frame. */
public final class BattleScreen implements Disposable {
    public static final int PUNCH_DAMAGE = 60;
    private static final int VISIBLE_ROWS = 7;
    private static final int SCROLL_UP = -2, SCROLL_DOWN = -3;
    private static final String[] MAIN = {"Attack", "Stones", "Defend", "Flee"};
    private static final Color SHADOW = Color.valueOf("D8D2B0");
    private static final Color HEALTH = Color.valueOf("C0392B");
    private static final Color ENERGY = Color.valueOf("2E86DE");
    private static final Color EXPERIENCE = Color.valueOf("9B59B6");
    private static final Color MUTED = Color.valueOf("9A9480");
    private static final Color ENERGY_LIGHT = Color.valueOf("9CCBFF");
    private static final Color TIME_LIGHT = Color.valueOf("8FE3B0");
    private static final Color PORTAL = Color.valueOf("2E86DE");
    private static final Color PORTAL_LIGHT = Color.valueOf("9CCBFF");
    private static final Color PORTAL_CORE = Color.valueOf("0B1E3A");
    private static final float PORTAL_X = 530, PORTAL_Y = 340, EXIT_X = 870, EXIT_Y = 560;
    private static final float ENEMY_X = 980, ENEMY_Y = 560;
    private static final float CHARACTER_SCALE = 3;
    private static final float THANOS_X = 264, THANOS_Y = 236;
    private enum Menu { MAIN, ATTACK, STONES }

    private final TurnBattle battle;
    private final Inventory inventory;
    private final BattleMessages messages;
    private final int enemyRgba;
    private final Backdrop backdrop;
    private final CharacterSprites sprites = new CharacterSprites();
    private final TextureRegion enemySprite;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Color color = new Color();
    private final Vector2 pointer = new Vector2();
    private Menu menu = Menu.MAIN;
    private int cursor;
    private int firstRow;
    private int lastMouseX = -1, lastMouseY = -1;
    private String hint = "";
    private InfinityStone hiddenStone;
    private final EnergyFlash energyFlash = new EnergyFlash();
    private GameEvent portalEvent;
    private long portalStart;

    public BattleScreen(TurnBattle battle, Inventory inventory, BattleMessages messages, int enemyRgba) {
        if (battle == null || inventory == null || messages == null) {
            throw new IllegalArgumentException("Battle screen needs a battle, an inventory and messages");
        }
        this.battle = battle;
        this.inventory = inventory;
        this.messages = messages;
        this.enemyRgba = enemyRgba;
        this.backdrop = new Backdrop(enemyRgba);
        this.enemySprite = sprites.enemy(battle.enemy().name());
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public boolean isFinished() { return battle.state().isOver() && !messages.hasPending(); }

    public void hideStone(InfinityStone stone) { hiddenStone = stone; }

    public void update() { update(null); }

    public void update(Viewport viewport) {
        boolean mouse = viewport != null;
        boolean click = mouse && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean confirm = pressed(Input.Keys.Z);
        boolean back = pressed(Input.Keys.X)
                || mouse && Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        if (messages.hasPending()) {
            if (confirm || click) messages.advance();
            return;
        }
        if (battle.state() == BattleState.ENEMY_TURN) {
            battle.enemyTurn();
            if (battle.state() == BattleState.PLAYER_TURN) {
                int before = inventory.energy();
                inventory.restoreEnergy(inventory.energyPerTurn());
                energyFlash.gained(before, inventory.energy(), Inventory.ENERGY_PER_TURN, TimeUtils.millis());
            }
            return;
        }
        if (battle.state() != BattleState.PLAYER_TURN || battle.skipPlayerTurnIfStunned()) return;
        int before = cursor;
        if (menu == Menu.MAIN) {
            if (pressed(Input.Keys.RIGHT)) cursor = cursor ^ 1;
            if (pressed(Input.Keys.LEFT)) cursor = cursor ^ 1;
            if (pressed(Input.Keys.DOWN)) cursor = cursor ^ 2;
            if (pressed(Input.Keys.UP)) cursor = cursor ^ 2;
        } else {
            int size = menu == Menu.ATTACK ? attacks().size() + 1 : 1;
            if (pressed(Input.Keys.DOWN)) cursor = (cursor + 1) % size;
            if (pressed(Input.Keys.UP)) cursor = (cursor + size - 1) % size;
        }
        int hovered = mouse ? itemUnderMouse(viewport) : -1;
        boolean moved = mouse && (Gdx.input.getX() != lastMouseX || Gdx.input.getY() != lastMouseY);
        if (mouse) {
            lastMouseX = Gdx.input.getX();
            lastMouseY = Gdx.input.getY();
        }
        if (hovered >= 0 && (moved || click)) cursor = hovered;
        if (click && hovered == SCROLL_UP) cursor = Math.max(0, cursor - 1);
        if (click && hovered == SCROLL_DOWN) cursor = Math.min(attacks().size(), cursor + 1);
        keepCursorVisible();
        if (cursor != before) hint = "";
        if (back && menu != Menu.MAIN) backToMain();
        else if (confirm || click && hovered >= 0) choose();
    }

    private int itemUnderMouse(Viewport viewport) {
        viewport.unproject(pointer.set(Gdx.input.getX(), Gdx.input.getY()));
        float x = pointer.x, y = pointer.y;
        switch (menu) {
            case MAIN -> {
                for (int i = 0; i < MAIN.length; i++) {
                    float itemX = 830 + (i % 2) * 210, itemY = 150 - (i / 2) * 70;
                    if (x >= itemX - 10 && x < itemX + 200 && y >= itemY - 40 && y < itemY + 12) return i;
                }
            }
            case ATTACK -> {
                if (x >= 1222 && x < 1262 && y >= 168 && y < 204) return SCROLL_UP;
                if (x >= 1222 && x < 1262 && y >= 20 && y < 56) return SCROLL_DOWN;
                if (x >= 800 && x < 1220 && y <= 194 && y > 194 - VISIBLE_ROWS * 22) {
                    int row = firstRow + (int) ((194 - y) / 22);
                    if (row <= attacks().size()) return row;
                }
            }
            case STONES -> {
                if (x >= 792 && x < 1264 && y >= 16 && y < 206) return 0;
            }
        }
        return -1;
    }

    private void keepCursorVisible() {
        if (cursor < firstRow) firstRow = cursor;
        if (cursor >= firstRow + VISIBLE_ROWS) firstRow = cursor - VISIBLE_ROWS + 1;
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
                Attack punch = punch();
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

    private void backToMain() {
        int from = menu == Menu.STONES ? 1 : 0;
        open(Menu.MAIN);
        cursor = from;
    }

    private void open(Menu next) {
        menu = next;
        cursor = 0;
        firstRow = 0;
    }

    private List<Ability> attacks() { return inventory.abilities(); }

    private Thanos leveled() { return battle.thanos() instanceof Thanos thanos ? thanos : null; }

    private int punchDamage() { return leveled() != null ? leveled().punchDamage() : PUNCH_DAMAGE; }

    private Attack punch() { return inventory.empower(new BasicAttack("Punch", punchDamage())); }

    private float portalTime() {
        GameEvent event = messages.currentEvent();
        if (event == null || event.type() != GameEventType.PORTAL) {
            portalEvent = null;
            return -1;
        }
        if (event != portalEvent) {
            portalEvent = event;
            portalStart = TimeUtils.millis();
        }
        return (TimeUtils.millis() - portalStart) / 1000f;
    }

    private static boolean pressed(int key) { return Gdx.input.isKeyJustPressed(key); }

    public void render(Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
        batch.setProjectionMatrix(projection);
        Combatant enemy = battle.enemy(), thanos = battle.thanos();
        Thanos leveled = leveled();
        float portal = portalTime();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        backdrop.draw(shapes);
        shapes.setColor(backdrop.shadow());
        shapes.ellipse(800, 455, 360, 80);
        shapes.ellipse(150, 205, 420, 90);
        shapes.end();

        batch.begin();
        if (enemy.isAlive() || battle.state() != BattleState.WON) drawEnemy(portal > 0.85f && portal < 1.2f);
        batch.draw(sprites.thanos, THANOS_X, THANOS_Y, sprites.thanos.getRegionWidth() * CHARACTER_SCALE,
                sprites.thanos.getRegionHeight() * CHARACTER_SCALE);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawStones();
        if (portal >= 0) drawPortals(portal);
        panel(80, 600, 470, 110);
        bar(110, 620, 300, 16, enemy.health(), enemy.maxHealth(), HEALTH);
        panel(730, 250, 500, 150);
        bar(760, 326, 300, 16, thanos.health(), thanos.maxHealth(), HEALTH);
        bar(760, 296, 300, 12, inventory.energy(), inventory.maxEnergy(), ENERGY);
        drawEnergyFlash(760, 296, 300, 12);
        if (leveled != null) bar(760, 272, 300, 10, leveled.experience(), leveled.experienceToNextLevel(), EXPERIENCE);
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
        boolean choosing = battle.state() == BattleState.PLAYER_TURN && !messages.hasPending();
        if (choosing && inventory.has(InfinityStone.MIND)) {
            battle.nextEnemyDamage().ifPresent(damage -> text(damage == 0 ? "Foresight: stunned"
                    : "Foresight: next blow " + damage, 330, 684, 1.1f, Hud.INK));
        }
        text("HP", 420, 634, 1f, Hud.INK);
        text(thanos.name(), 760, 385, 1.6f, Hud.INK);
        if (leveled != null) {
            text("Lv " + leveled.level(), 1075, 385, 1.6f, Hud.INK);
            text("XP " + leveled.experience() + "/" + leveled.experienceToNextLevel(), 1075, 284, 1f, Hud.INK);
        }
        text("HP " + thanos.health() + "/" + thanos.maxHealth(), 1075, 340, 1f, Hud.INK);
        text("EN " + inventory.energy() + "/" + inventory.maxEnergy(), 1075, 308, 1f, Hud.INK);
        drawMessageBox();
        drawMenu();
        batch.end();
    }

    private void drawMessageBox() {
        boolean choosing = !messages.hasPending() && battle.state() == BattleState.PLAYER_TURN;
        if (menu == Menu.STONES && choosing) {
            InfinityStone[] stones = InfinityStone.values();
            for (int i = 0; i < stones.length; i++) {
                boolean owned = inventory.has(stones[i]);
                String passive = stones[i].passiveName().isEmpty() ? "" : "   |   Passive: " + stones[i].passiveName();
                text(stones[i].displayName() + (owned ? "  -  " + stones[i].effect() + passive : "  -  not collected"),
                        70, 181 - i * 24, 1.1f, owned ? Hud.INK : MUTED);
            }
            return;
        }
        if (menu == Menu.ATTACK && choosing) {
            if (cursor == 0) {
                text("Punch", 44, 170, 1.5f, Hud.INK);
                text("A plain blow for " + punch().damage() + " damage", 44, 130, 1.2f, Hud.INK);
            } else {
                Ability ability = attacks().get(cursor - 1);
                text(ability.name() + "  (" + ability.stone().displayName() + ", " + inventory.costOf(ability) + " EN)",
                        44, 170, 1.5f, Hud.INK);
                text(ability.description(), 44, 130, 1.2f, Hud.INK);
            }
            if (!hint.isEmpty()) text(hint, 44, 80, 1.2f, HEALTH);
            return;
        }
        String line = messages.hasPending() ? messages.current()
                : battle.state().isOver() ? "" : hint.isEmpty() ? "What will Thanos do?" : hint;
        text(line, 44, 160, 1.7f, Hud.INK);
        if (messages.hasPending() && TimeUtils.millis() / 500 % 2 == 0) {
            text("Z or click  >>", 530, 52, 1.3f, Hud.INK);
        }
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
                int rows = abilities.size() + 1;
                int first = firstRow;
                for (int row = first; row < Math.min(rows, first + VISIBLE_ROWS); row++) {
                    Ability ability = row == 0 ? null : abilities.get(row - 1);
                    String label = ability == null ? "Punch" : ability.name() + "   " + inventory.costOf(ability) + " EN";
                    boolean affordable = ability == null || inventory.costOf(ability) <= inventory.energy();
                    text((cursor == row ? "> " : "  ") + label, 820, 190 - (row - first) * 22, 1.2f,
                            affordable ? ink : MUTED);
                }
                if (abilities.isEmpty()) text("  no stones yet", 820, 166, 1.2f, MUTED);
                if (first > 0) text("^", 1236, 190, 1.2f, MUTED);
                if (first + VISIBLE_ROWS < rows) text("v", 1236, 46, 1.2f, MUTED);
            }
            case STONES -> text("> Back", 830, 150, 1.8f, ink);
        }
    }

    private void drawEnemy(boolean hit) {
        float width = enemySprite.getRegionWidth() * CHARACTER_SCALE;
        float height = enemySprite.getRegionHeight() * CHARACTER_SCALE;
        float shake = hit ? MathUtils.sin(TimeUtils.millis() / 15f) * 6 : 0;
        float left = Math.round(ENEMY_X - width / 2 + shake);
        batch.draw(enemySprite, left, 488, width, height);
        if (hit) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(1, 1, 1, .65f);
            batch.draw(enemySprite, left, 488, width, height);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawPortals(float t) {
        float closing = 1 - MathUtils.clamp((t - 1.3f) / 0.3f, 0, 1);
        float entry = MathUtils.clamp(t / 0.25f, 0, 1) * closing;
        float exit = MathUtils.clamp((t - 0.45f) / 0.2f, 0, 1) * closing;
        portal(PORTAL_X, PORTAL_Y, 32 * entry, 84 * entry, t);
        portal(EXIT_X, EXIT_Y, 26 * exit, 66 * exit, t);
        shapes.setColor(color.set(enemyRgba));
        if (t > 0.1f && t < 0.55f) {
            float p = (t - 0.1f) / 0.45f, size = 22 * (1 - 0.6f * p);
            shapes.rect(MathUtils.lerp(ENEMY_X - 60, PORTAL_X, p) - size / 2,
                    MathUtils.lerp(ENEMY_Y, PORTAL_Y, p) - size / 2, size, size);
        } else if (t >= 0.6f && t < 0.85f) {
            float p = (t - 0.6f) / 0.25f, size = 12 + 14 * p;
            shapes.rect(MathUtils.lerp(EXIT_X, ENEMY_X, p) - size / 2, MathUtils.lerp(EXIT_Y, ENEMY_Y, p) - size / 2, size, size);
        }
    }

    private void portal(float x, float y, float rx, float ry, float t) {
        if (rx < 2) return;
        shapes.setColor(Hud.INK);
        shapes.ellipse(x - rx - 3, y - ry - 3, 2 * rx + 6, 2 * ry + 6);
        int shift = (int) (t * 8);
        for (int ring = 0; ring < 5; ring++) {
            float scale = 1 - ring * 0.18f;
            shapes.setColor((ring + shift) % 2 == 0 ? PORTAL : PORTAL_LIGHT);
            shapes.ellipse(x - rx * scale, y - ry * scale, 2 * rx * scale, 2 * ry * scale);
        }
        shapes.setColor(PORTAL_CORE);
        shapes.ellipse(x - rx * 0.18f, y - ry * 0.18f, 2 * rx * 0.18f, 2 * ry * 0.18f);
        shapes.setColor(PORTAL_LIGHT);
        for (int i = 0; i < 6; i++) {
            float angle = t * 300 + i * 60;
            shapes.rect(x + MathUtils.cosDeg(angle) * rx * 1.3f - 3, y + MathUtils.sinDeg(angle) * ry * 1.12f - 3, 6, 6);
        }
    }

    private void drawStones() {
        for (InfinityStone stone : InfinityStone.values()) {
            if (stone == hiddenStone || !inventory.has(stone)) continue;
            Vector2 center = gauntletSlot(stone);
            shapes.setColor(color.set(stone.rgba()));
            shapes.rect(center.x - CHARACTER_SCALE, center.y - CHARACTER_SCALE, 2 * CHARACTER_SCALE, 2 * CHARACTER_SCALE);
        }
    }

    /** Centers of the gauntlet sockets in the battle sprite, converted to screen coordinates. */
    public static Vector2 gauntletSlot(InfinityStone stone) {
        CharacterSprites.Socket socket = CharacterSprites.socket(stone);
        return new Vector2(THANOS_X + (socket.x() + 1) * CHARACTER_SCALE,
                THANOS_Y + (64 - socket.yFromTop() - 1) * CHARACTER_SCALE);
    }

    private void panel(float x, float y, float w, float h) {
        shapes.setColor(Hud.INK);
        shapes.rect(x, y, w, h);
        shapes.setColor(Hud.PAPER);
        shapes.rect(x + 4, y + 4, w - 8, h - 8);
    }

    private void drawEnergyFlash(float x, float y, float w, float h) {
        long now = TimeUtils.millis();
        if (!energyFlash.active(now)) return;
        boolean bright = energyFlash.bright(now);
        int regularEnd = energyFlash.from() + energyFlash.regular();
        segment(x, y, w, h, energyFlash.from(), regularEnd, bright ? ENERGY_LIGHT : ENERGY);
        segment(x, y, w, h, regularEnd, regularEnd + energyFlash.bonus(),
                bright ? TIME_LIGHT : color.set(InfinityStone.TIME.rgba()));
    }

    private void segment(float x, float y, float w, float h, int from, int to, Color fill) {
        float max = Math.max(1, inventory.maxEnergy());
        int end = Math.min(to, inventory.energy());
        if (end <= from) return;
        shapes.setColor(fill);
        shapes.rect(x + 2 + (w - 4) * from / max, y + 2, (w - 4) * (end - from) / max, h - 4);
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
        sprites.dispose();
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
