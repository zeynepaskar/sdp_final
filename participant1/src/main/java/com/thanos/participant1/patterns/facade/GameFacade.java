package com.thanos.participant1.patterns.facade;

import com.thanos.participant1.characters.PlayerView;
import com.thanos.participant1.characters.Thanos;
import com.thanos.participant1.levels.Encounter;
import com.thanos.participant1.levels.World;
import com.thanos.participant1.levels.WorldLayout;
import com.thanos.participant1.story.DialogueView;
import com.thanos.participant1.story.StoryMarker;
import com.thanos.participant1.story.StorySession;
import com.thanos.participant3.battle.TurnBattle;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.ui.BattleMessages;
import com.thanos.participant3.ui.StoneRewards;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public final class GameFacade {
    private static final float ALERT_SECONDS = 0.65f;
    private static final float MOVEMENT_STEP = WorldLayout.TILE_SIZE / (2 * Thanos.SPEED);
    private final List<World> worlds;
    private final Thanos player;
    private final Random random;
    private final Set<Encounter> cleared = new HashSet<>();
    private final Set<Encounter> escaped = new HashSet<>();
    private final StorySession story = new StorySession(new Exploration());
    private World sideArea;
    private PlayerView returnPoint;
    private CampaignCombat combat;
    private GamePhase phase = GamePhase.MENU;
    private GamePhase suspendedPhase = GamePhase.PLAYING;
    private Encounter activeEncounter;
    private float alertRemaining;
    private int worldIndex;
    private boolean sessionActive;
    private boolean disposed;

    public GameFacade(List<World> worlds) { this(worlds, new Random()); }

    GameFacade(List<World> worlds, Random random) {
        this.worlds = List.copyOf(worlds);
        this.random = Objects.requireNonNull(random);
        if (worlds.isEmpty()) throw new IllegalArgumentException("A campaign needs at least one world");
        player = new Thanos(world().spawnX(), world().spawnY());
    }

    public GamePhase phase() { return phase; }
    public boolean canResume() { return sessionActive; }
    public World world() { return inSideArea() ? sideArea : worlds.get(worldIndex); }
    public List<World> worlds() { return worlds; }
    public Encounter activeEncounter() { return activeEncounter; }
    public boolean isCleared(Encounter encounter) { return cleared.contains(encounter); }
    public boolean exitUnlocked() { return gateGuardian() == null; }
    public TurnBattle battle() { return combat == null ? null : combat.battle(); }
    public Inventory inventory() { return combat.inventory; }
    public BattleMessages messages() { return combat.messages; }
    public StoneRewards rewards() { return combat.rewards; }
    public com.thanos.participant3.progress.Thanos progress() { return combat.progress; }

    public boolean inSideArea() { return sideArea != null; }
    public DialogueView dialogue() { return story.view(); }
    public boolean hasStoryFlag(String flag) { return story.hasFlag(flag); }

    public void confirm() {
        if (phase == GamePhase.PLAYING) story.confirm();
    }

    public void cancel() {
        if (phase == GamePhase.PLAYING) story.cancel();
    }

    public void navigateStory(int direction) {
        if (phase == GamePhase.PLAYING) story.navigate(direction);
    }

    public StoryMarker nearbyMarker() {
        if (phase != GamePhase.PLAYING) return null;
        int column = (int) ((player.x() + Thanos.WIDTH / 2) / WorldLayout.TILE_SIZE);
        int row = (int) ((player.y() + Thanos.FEET_HEIGHT / 2) / WorldLayout.TILE_SIZE);
        for (StoryMarker marker : world().markers()) {
            if (Math.abs(marker.cell().column() - column) + Math.abs(marker.cell().row() - row) == 1) return marker;
        }
        return null;
    }

    public PlayerView player() {
        return new PlayerView(player.x(), player.y(), player.width(), player.height());
    }

    public void newGame() {
        if (disposed) throw new IllegalStateException("Game has been disposed");
        sideArea = null;
        returnPoint = null;
        story.reset();
        combat = new CampaignCombat(random);
        worldIndex = 0;
        sessionActive = true;
        enterWorld();
    }

    public void showMenu() {
        if (isInGame()) suspendedPhase = phase;
        phase = GamePhase.MENU;
    }

    public void pauseGame() {
        if (isInGame()) {
            suspendedPhase = phase;
            phase = GamePhase.PAUSED;
        }
    }

    public void resumeGame() {
        if (sessionActive && (phase == GamePhase.MENU || phase == GamePhase.PAUSED)) phase = suspendedPhase;
    }

    public void update(float delta, float horizontal, float vertical) {
        if (!story.exploring() || (phase != GamePhase.PLAYING && phase != GamePhase.ALERT)) return;
        if (!Float.isFinite(delta) || delta < 0) throw new IllegalArgumentException("Invalid elapsed time");
        if (phase == GamePhase.ALERT) {
            alertRemaining -= delta;
            if (alertRemaining <= 0) {
                combat.start(activeEncounter);
                phase = GamePhase.BATTLE;
            }
            return;
        }
        // Check sight along the path even when a slow frame crosses a whole tile.
        double remaining = delta;
        do {
            float step = (float) Math.min(remaining, MOVEMENT_STEP);
            float oldX = player.x(), oldY = player.y();
            player.move(horizontal, vertical, step, world(), this::canStandAt);
            checkSight();
            if (player.x() == oldX && player.y() == oldY) break;
            remaining -= step;
        } while (remaining > 0 && phase == GamePhase.PLAYING);
    }

    public boolean canStandAt(float x, float y) {
        if (!world().layout().isWalkable(x, y, Thanos.WIDTH, Thanos.FEET_HEIGHT)) return false;
        for (StoryMarker marker : world().markers()) {
            float left = marker.cell().column() * WorldLayout.TILE_SIZE;
            float bottom = marker.cell().row() * WorldLayout.TILE_SIZE;
            if (occupiesTile(x, y, left, bottom)) return false;
        }
        for (Encounter encounter : world().encounters()) {
            if (!isCleared(encounter)
                    && occupiesTile(x, y, encounter.x(), encounter.y())) {
                return false;
            }
        }
        return true;
    }

    private boolean occupiesTile(float x, float y, float left, float bottom) {
        return x < left + WorldLayout.TILE_SIZE && x + Thanos.WIDTH > left
                && y < bottom + WorldLayout.TILE_SIZE && y + Thanos.FEET_HEIGHT > bottom;
    }

    private void checkSight() {
        int column = (int) ((player.x() + player.width() / 2) / WorldLayout.TILE_SIZE);
        int row = (int) ((player.y() + Thanos.FEET_HEIGHT / 2) / WorldLayout.TILE_SIZE);
        for (Encounter encounter : world().encounters()) {
            if (isCleared(encounter)) continue;
            boolean inSight = encounter.sees(world().layout(), column, row);
            if (!inSight) escaped.remove(encounter);
            else if (!escaped.contains(encounter)) {
                beginAlert(encounter);
                break;
            }
        }
    }

    private void beginAlert(Encounter encounter) {
        activeEncounter = encounter;
        alertRemaining = ALERT_SECONDS;
        phase = GamePhase.ALERT;
    }

    public void finishBattle() {
        if (phase != GamePhase.BATTLE || !battle().state().isOver()) return;
        switch (battle().state()) {
            case WON -> {
                cleared.add(activeEncounter);
                combat.reward(activeEncounter);
            }
            case FLED -> escaped.add(activeEncounter);
            case LOST -> {
                combat.restoreAtEntrance();
                escaped.clear();
                player.spawnAt(world().spawnX(), world().spawnY());
            }
            default -> throw new IllegalStateException("Unfinished battle");
        }
        combat.finish();
        activeEncounter = null;
        phase = GamePhase.PLAYING;
    }

    public TransitionResult interactWithPortal() {
        if (phase != GamePhase.PLAYING || inSideArea() || !story.exploring()) return TransitionResult.NOT_PLAYING;
        if (!world().overlapsPortal(player.x(), player.y(), player.width(), player.height())) {
            return TransitionResult.TOO_FAR;
        }
        Encounter guardian = gateGuardian();
        if (guardian != null) {
            beginAlert(guardian);
            return TransitionResult.LOCKED;
        }
        if (worldIndex == worlds.size() - 1) {
            sessionActive = false;
            phase = GamePhase.COMPLETE;
            return TransitionResult.COMPLETED;
        }
        worldIndex++;
        enterWorld();
        return TransitionResult.ADVANCED;
    }

    private Encounter gateGuardian() {
        for (Encounter encounter : world().encounters()) {
            if (encounter.isBoss() && !isCleared(encounter)) return encounter;
        }
        return null;
    }

    public void dispose() {
        disposed = true;
        sessionActive = false;
        phase = GamePhase.MENU;
    }

    private boolean isInGame() {
        return phase == GamePhase.PLAYING || phase == GamePhase.ALERT || phase == GamePhase.BATTLE;
    }

    private final class Exploration implements StorySession.Host {
        public void interactNearby() {
            StoryMarker marker = nearbyMarker();
            if (marker == null) interactWithPortal();
            else marker.command().execute(story);
        }

        public void pauseGame() { GameFacade.this.pauseGame(); }

        public void visit(World area) {
            if (inSideArea()) throw new IllegalStateException("Return to the route before visiting another area");
            returnPoint = player();
            sideArea = area;
            player.spawnAt(area.spawnX(), area.spawnY());
        }

        public void returnToRoute() {
            if (!inSideArea()) throw new IllegalStateException("Already on the campaign route");
            sideArea = null;
            player.spawnAt(returnPoint.x(), returnPoint.y());
            returnPoint = null;
        }
    }

    private void enterWorld() {
        cleared.clear();
        escaped.clear();
        activeEncounter = null;
        player.spawnAt(world().spawnX(), world().spawnY());
        phase = GamePhase.PLAYING;
    }
}
