package com.thanos.participant3.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.thanos.participant3.stones.AbilityBook;
import com.thanos.participant3.stones.InfinityStone;
import java.util.List;

/** Battle view and input over TurnBattle; call update() then render() every frame. */
public final class BattleScreen implements Disposable {
    public static final int PUNCH_DAMAGE = 60;
    /** Attack menu: four boxes a page, the arrow on the right turns the page. */
    private static final int PAGE = 4, PAGE_ARROW = -2;
    private static final float BOX_X = 804, BOX_Y = 28, BOX_W = 196, BOX_H = 80, BOX_GAP_X = 204, BOX_GAP_Y = 88;
    private static final float ARROW_X = 1234, ARROW_Y = 112;
    private static final String[] GEM = {"...#...", "..#L#..", ".#LCC#.", "#LCCCD#", ".#CCD#.", "..#D#..", "...#..."};
    private static final String[] MAIN = {"Attack", "Stones", "Defend", "Flee"};
    private static final Color SHADOW = Color.valueOf("D8D2B0");
    private static final Color HEALTH = Color.valueOf("C0392B");
    private static final Color ENERGY = Color.valueOf("2E86DE");
    private static final Color EXPERIENCE = Color.valueOf("9B59B6");
    private static final Color MUTED = Color.valueOf("9A9480");
    private static final Color TITAN = Color.valueOf("C9962A");
    private static final Color ENERGY_LIGHT = Color.valueOf("9CCBFF");
    private static final Color TIME_LIGHT = Color.valueOf("8FE3B0");
    private static final Color PORTAL = Color.valueOf("2E86DE");
    private static final Color PORTAL_LIGHT = Color.valueOf("9CCBFF");
    private static final Color PORTAL_CORE = Color.valueOf("0B1E3A");
    private static final float PORTAL_X = 530, PORTAL_Y = 340, EXIT_X = 870, EXIT_Y = 560;
    private static final float ENEMY_X = 980, ENEMY_Y = 560;
    private static final float THANOS_X = 278, THANOS_Y = 236, THANOS_SCALE = 5, ENEMY_SCALE = 8;
    private enum Menu { MAIN, ATTACK, STONES }

    private final TurnBattle battle;
    private final Inventory inventory;
    private final BattleMessages messages;
    private final int enemyRgba;
    private final Backdrop backdrop;
    private final PixelSprite enemySprite;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Color color = new Color();
    private final Vector2 pointer = new Vector2();
    private final GlyphLayout layout = new GlyphLayout();
    private GameEvent mightEvent;
    private long mightStart;
    private Menu menu = Menu.MAIN;
    private int cursor;
    private int lastMouseX = -1, lastMouseY = -1;
    private String hint = "";
    private InfinityStone hiddenStone;
    private final EnergyFlash energyFlash = new EnergyFlash();
    private GameEvent portalEvent;
    private long portalStart;
    private GameEvent harvestEvent;
    private long harvestStart;
    private static final int HARVEST_SPARKS = 16;
    private static final float HARVEST_SECONDS = 1.6f;

    public BattleScreen(TurnBattle battle, Inventory inventory, BattleMessages messages, int enemyRgba) {
        if (battle == null || inventory == null || messages == null) {
            throw new IllegalArgumentException("Battle screen needs a battle, an inventory and messages");
        }
        this.battle = battle;
        this.inventory = inventory;
        this.messages = messages;
        this.enemyRgba = enemyRgba;
        this.backdrop = new Backdrop(enemyRgba);
        this.enemySprite = Sprites.forEnemy(battle.enemy().name());
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public boolean isFinished() { return battle.state().isOver() && !messages.hasPending(); }

    public void hideStone(InfinityStone stone) { hiddenStone = stone; }

    public void update() { update(null); }

    public void update(Viewport viewport) {
        boolean mouse = viewport != null;
        boolean click = mouse && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean confirm = pressed(Input.Keys.ENTER) || pressed(Input.Keys.SPACE);
        boolean back = pressed(Input.Keys.ESCAPE) || pressed(Input.Keys.BACKSPACE)
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
        if (battle.state() != BattleState.PLAYER_TURN) return;
        int before = cursor;
        if (menu == Menu.MAIN) {
            if (pressed(Input.Keys.RIGHT) || pressed(Input.Keys.D)) cursor = cursor ^ 1;
            if (pressed(Input.Keys.LEFT) || pressed(Input.Keys.A)) cursor = cursor ^ 1;
            if (pressed(Input.Keys.DOWN) || pressed(Input.Keys.S)) cursor = cursor ^ 2;
            if (pressed(Input.Keys.UP) || pressed(Input.Keys.W)) cursor = cursor ^ 2;
        } else if (menu == Menu.ATTACK) {
            moveInGrid();
        }
        int hovered = mouse ? itemUnderMouse(viewport) : -1;
        boolean moved = mouse && (Gdx.input.getX() != lastMouseX || Gdx.input.getY() != lastMouseY);
        if (mouse) {
            lastMouseX = Gdx.input.getX();
            lastMouseY = Gdx.input.getY();
        }
        if (hovered >= 0 && (moved || click)) cursor = hovered;
        if (click && hovered == PAGE_ARROW) turnPage(page() + 1 < pages() ? page() + 1 : page() - 1, cursor % PAGE);
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
                if (pages() > 1 && x >= ARROW_X - 24 && x < ARROW_X + 24 && y >= ARROW_Y - 30 && y < ARROW_Y + 30) {
                    return PAGE_ARROW;
                }
                for (int slot = 0; slot < PAGE; slot++) {
                    int index = page() * PAGE + slot;
                    if (index < attacks().size() && x >= boxX(slot) && x < boxX(slot) + BOX_W
                            && y >= boxY(slot) && y < boxY(slot) + BOX_H) return index;
                }
            }
            case STONES -> {
                if (x >= 792 && x < 1264 && y >= 16 && y < 206) return 0;
            }
        }
        return -1;
    }

    /** Arrows move inside the 2x2 grid; stepping past its right or left edge turns the page. */
    private void moveInGrid() {
        int count = attacks().size(), slot = cursor % PAGE;
        if (pressed(Input.Keys.RIGHT) || pressed(Input.Keys.D)) {
            if (slot % 2 == 0 && cursor + 1 < count) cursor++;
            else if (slot % 2 == 1) turnPage(page() + 1, slot - 1);
        }
        if (pressed(Input.Keys.LEFT) || pressed(Input.Keys.A)) {
            if (slot % 2 == 1) cursor--;
            else turnPage(page() - 1, slot + 1);
        }
        boolean down = pressed(Input.Keys.DOWN) || pressed(Input.Keys.S);
        boolean up = pressed(Input.Keys.UP) || pressed(Input.Keys.W);
        if ((down || up) && (cursor ^ 2) < count) cursor ^= 2;
    }

    private void turnPage(int page, int slot) {
        if (page < 0 || page >= pages()) return;
        cursor = Math.min(attacks().size() - 1, page * PAGE + slot);
    }

    private int page() { return cursor / PAGE; }
    private int pages() { return (attacks().size() + PAGE - 1) / PAGE; }
    private static float boxX(int slot) { return BOX_X + (slot % 2) * BOX_GAP_X; }
    private static float boxY(int slot) { return BOX_Y + (1 - slot / 2) * BOX_GAP_Y; }

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
                Ability ability = attacks().get(cursor);
                inventory.use(ability, punch()).ifPresentOrElse(attack -> {
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

    /** Seconds since the Soul Stone's after-victory heal appeared, or -1 while it is not shown. */
    private float harvestTime() {
        GameEvent event = messages.currentEvent();
        if (event == null || event.type() != GameEventType.HEAL || battle.state() != BattleState.WON) {
            harvestEvent = null;
            return -1;
        }
        if (event != harvestEvent) {
            harvestEvent = event;
            harvestStart = TimeUtils.millis();
        }
        return (TimeUtils.millis() - harvestStart) / 1000f;
    }

    private static boolean pressed(int key) { return Gdx.input.isKeyJustPressed(key); }

    public void render(Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
        batch.setProjectionMatrix(projection);
        Combatant enemy = battle.enemy(), thanos = battle.thanos();
        Thanos leveled = leveled();
        float portal = portalTime();
        float harvest = harvestTime();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        backdrop.draw(shapes);
        shapes.setColor(backdrop.shadow());
        shapes.ellipse(800, 455, 360, 80);
        shapes.ellipse(150, 205, 420, 90);
        if (enemy.isAlive() || battle.state() != BattleState.WON) drawEnemy(portal > 0.85f && portal < 1.2f);
        drawThanos();
        if (portal >= 0) drawPortals(portal);
        if (harvest >= 0 && harvest < HARVEST_SECONDS) drawHarvest(harvest);
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
        if (menu == Menu.ATTACK) drawAttackBoxes();
        if (battle.state() == BattleState.PLAYER_TURN && !messages.hasPending() && inventory.has(InfinityStone.MIND)
                && battle.nextEnemyDamage().isPresent()) {
            panel(80, 546, 560, 44);
            drawGem(InfinityStone.MIND, 94, 557, 3);
        }
        shapes.end();

        batch.begin();
        text(enemy.name(), 110, 690, 1.6f, Hud.INK);
        boolean choosing = battle.state() == BattleState.PLAYER_TURN && !messages.hasPending();
        text("HP " + enemy.health() + "/" + enemy.maxHealth() + "     Defense " + enemy.defense() + "%"
                + (battle.canFlee() ? "" : "     Boss"), 110, 658, 1.1f, Hud.INK);
        if (choosing && inventory.has(InfinityStone.MIND)) {
            battle.nextEnemyDamage().ifPresent(damage -> text("Foresight:  " + EnemyIntent.describe(
                    battle.nextEnemyAttack().orElse(null), damage), 124, 574, 1.2f, Hud.INK));
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
            Ability ability = attacks().get(cursor);
            String source = ability.isBase() ? "Titan" : ability.stone().displayName();
            text(ability.name() + "  (" + source + ", " + inventory.costOf(ability) + " EN"
                    + (AbilityBook.isUpgraded(ability) ? ", upgraded" : "") + ")", 44, 170, 1.5f, Hud.INK);
            text(ability.description(), 44, 130, 1.2f, Hud.INK);
            text("Damage before defense: " + ability.apply(punch()).damage(), 44, 100, 1.2f, Hud.INK);
            if (!hint.isEmpty()) text(hint, 44, 60, 1.2f, HEALTH);
            return;
        }
        String line = messages.hasPending() ? messages.current()
                : battle.state().isOver() ? "" : hint.isEmpty() ? "What will Thanos do?" : hint;
        if (!drawMightHit()) text(line, 44, 160, 1.7f, Hud.INK);
        if (messages.hasPending() && TimeUtils.millis() / 500 % 2 == 0) {
            text("ENTER or click  >>", 530, 52, 1.3f, Hud.INK);
        }
    }

    /**
     * Power Stone passive: Thanos's blows show their damage in the stone's colour, flashing at first,
     * with a "Might +10%" tag under the line. Returns false when the current message is not such a blow.
     */
    private boolean drawMightHit() {
        GameEvent event = messages.currentEvent();
        String number = event == null ? "" : " " + event.value() + " ";
        boolean might = event != null && event.type() == GameEventType.DAMAGE && inventory.has(InfinityStone.POWER)
                && !event.source().equals(battle.thanos().name()) && event.message().contains(number);
        if (!might) {
            mightEvent = null;
            return false;
        }
        if (event != mightEvent) {
            mightEvent = event;
            mightStart = TimeUtils.millis();
        }
        long elapsed = TimeUtils.millis() - mightStart;
        String message = event.message();
        int at = message.lastIndexOf(number) + 1;
        String before = message.substring(0, at), value = String.valueOf(event.value());
        Color power = color.set(InfinityStone.POWER.rgba());
        if (elapsed < 600 && elapsed / 100 % 2 == 0) power.lerp(Color.WHITE, 0.6f);
        float x = 44 + width(before, 1.7f);
        text(before, 44, 160, 1.7f, Hud.INK);
        text(value, x, 160, 1.7f, power);
        text(message.substring(at + value.length()), x + width(value, 1.7f), 160, 1.7f, Hud.INK);
        text("Might +" + (Inventory.POWER_DAMAGE_PERCENT - 100) + "%", 44, 118, 1.2f,
                color.set(InfinityStone.POWER.rgba()));
        return true;
    }

    private float width(String value, float scale) {
        font.getData().setScale(scale);
        layout.setText(font, value);
        return layout.width;
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
                for (int slot = 0; slot < PAGE; slot++) {
                    int index = page() * PAGE + slot;
                    if (index >= abilities.size()) break;
                    Ability ability = abilities.get(index);
                    int cost = inventory.costOf(ability);
                    Color tint = cost <= inventory.energy() ? ink : MUTED;
                    float x = boxX(slot) + (ability.isBase() ? 16 : 44), y = boxY(slot);
                    text(ability.name(), x, y + BOX_H - 18, 1.3f, tint);
                    text(cost == 0 ? "free" : cost + " EN", x, y + 32, 1.1f, costColor(cost, tint));
                }
                if (pages() > 1) text((page() + 1) + "/" + pages(), ARROW_X - 12, 50, 1.1f, MUTED);
            }
            case STONES -> text("> Back", 830, 150, 1.8f, ink);
        }
    }

    /** Reality Stone passive: discounted costs are shown in the stone's colour. */
    private Color costColor(int cost, Color tint) {
        if (cost == 0 || tint == MUTED || !inventory.has(InfinityStone.REALITY)) return tint;
        return color.set(InfinityStone.REALITY.rgba());
    }

    private void drawAttackBoxes() {
        boolean active = battle.state() == BattleState.PLAYER_TURN && !messages.hasPending();
        List<Ability> abilities = attacks();
        for (int slot = 0; slot < PAGE; slot++) {
            int index = page() * PAGE + slot;
            float x = boxX(slot), y = boxY(slot), border = active && index == cursor ? 6 : 3;
            shapes.setColor(index < abilities.size() ? Hud.INK : SHADOW);
            shapes.rect(x, y, BOX_W, BOX_H);
            shapes.setColor(active && index == cursor ? SHADOW : Hud.PAPER);
            shapes.rect(x + border, y + border, BOX_W - 2 * border, BOX_H - 2 * border);
            if (index < abilities.size() && !abilities.get(index).isBase()) {
                drawGem(abilities.get(index).stone(), x + 14, y + BOX_H - 36, 3);
            }
            if (index < abilities.size() && AbilityBook.isUpgraded(abilities.get(index))) drawUpgradeMark(x + BOX_W - 22, y + 10);
        }
        if (pages() > 1) {
            shapes.setColor(Hud.INK);
            if (page() + 1 < pages()) {
                shapes.triangle(ARROW_X - 12, ARROW_Y + 18, ARROW_X - 12, ARROW_Y - 18, ARROW_X + 14, ARROW_Y);
            } else {
                shapes.triangle(ARROW_X + 12, ARROW_Y + 18, ARROW_X + 12, ARROW_Y - 18, ARROW_X - 14, ARROW_Y);
            }
        }
    }

    /** Gold plus in the corner of an upgraded ability's box. */
    private void drawUpgradeMark(float x, float y) {
        shapes.setColor(Hud.INK);
        shapes.rect(x + 4, y - 2, 8, 20);
        shapes.rect(x - 2, y + 4, 20, 8);
        shapes.setColor(TITAN);
        shapes.rect(x + 6, y, 4, 16);
        shapes.rect(x, y + 6, 16, 4);
    }

    /** A small pixel gem in the stone's colour that marks a stone ability. */
    private void drawGem(InfinityStone stone, float x, float y, float cell) {
        for (int row = 0; row < GEM.length; row++) {
            for (int col = 0; col < GEM[row].length(); col++) {
                char key = GEM[row].charAt(col);
                if (key == '.') continue;
                color.set(stone.rgba());
                if (key == '#') color.set(Hud.INK);
                else if (key == 'L') color.lerp(Color.WHITE, 0.55f);
                else if (key == 'D') color.mul(0.7f, 0.7f, 0.7f, 1);
                shapes.setColor(color);
                shapes.rect(x + col * cell, y + (GEM.length - 1 - row) * cell, cell, cell);
            }
        }
    }

    private void drawEnemy(boolean hit) {
        float shake = hit ? MathUtils.sin(TimeUtils.millis() / 15f) * 6 : 0;
        if (enemySprite != null) {
            // tall sprites (the Elders) are drawn at Thanos's finer pixel size
            float scale = enemySprite.height() > 24 ? THANOS_SCALE : ENEMY_SCALE;
            float x = ENEMY_X - enemySprite.width() * scale / 2 + shake;
            enemySprite.draw(shapes, x, 488, scale, key -> hit
                    ? Color.rgba8888(color.set(enemySprite.rgba(key)).lerp(Color.WHITE, 0.6f))
                    : enemySprite.rgba(key));
            return;
        }
        shapes.setColor(Hud.INK);
        shapes.rect(918 + shake, 488, 124, 144);
        color.set(enemyRgba);
        if (hit) color.lerp(Color.WHITE, 0.6f);
        shapes.setColor(color);
        shapes.rect(922 + shake, 492, 116, 136);
        shapes.setColor(Hud.INK);
        shapes.rect(950 + shake, 590, 14, 14);
        shapes.rect(996 + shake, 590, 14, 14);
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

    /** Soul Stone passive: sparks rise from where the enemy fell and arc into Thanos. */
    private void drawHarvest(float t) {
        float toX = THANOS_X + Sprites.THANOS.width() * THANOS_SCALE / 2f;
        float toY = THANOS_Y + Sprites.THANOS.height() * THANOS_SCALE / 2f;
        for (int i = 0; i < HARVEST_SPARKS; i++) {
            float p = MathUtils.clamp((t - i * 0.05f) / 0.8f, 0, 1);
            if (p <= 0 || p >= 1) continue;
            float fromX = ENEMY_X + (i * 53 % 120) - 60, fromY = ENEMY_Y - 40 + (i * 29 % 90);
            float eased = p * p * (3 - 2 * p);
            float x = MathUtils.lerp(fromX, toX, eased);
            float y = MathUtils.lerp(fromY, toY, eased) + MathUtils.sin(p * MathUtils.PI) * (70 + i * 37 % 80);
            float size = 16 * (1 - 0.5f * p);
            shapes.setColor(Hud.INK);
            shapes.rect(x - size / 2 - 2, y - size / 2 - 2, size + 4, size + 4);
            color.set(InfinityStone.SOUL.rgba());
            if (i % 3 == 0) color.lerp(Color.WHITE, 0.5f);
            shapes.setColor(color);
            shapes.rect(x - size / 2, y - size / 2, size, size);
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

    /** Stones Thanos has not got yet, or that are still flying in, show as empty sockets. */
    private void drawThanos() {
        Sprites.THANOS.draw(shapes, THANOS_X, THANOS_Y, THANOS_SCALE, key -> {
            InfinityStone stone = Sprites.stoneOf(key);
            if (stone == null) return Sprites.THANOS.rgba(key);
            return inventory.has(stone) && stone != hiddenStone ? stone.rgba() : Sprites.SOCKET;
        });
    }

    /** Centre of the stone's 2x2 socket in the gauntlet. */
    public static Vector2 gauntletSlot(InfinityStone stone) {
        int[] socket = Sprites.socket(stone);
        return new Vector2(THANOS_X + (socket[0] + 1) * THANOS_SCALE,
                THANOS_Y + (Sprites.THANOS.height() - 1 - socket[1]) * THANOS_SCALE);
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
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
