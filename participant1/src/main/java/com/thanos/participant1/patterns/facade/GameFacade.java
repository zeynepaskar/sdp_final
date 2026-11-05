package com.thanos.participant1.patterns.facade;

import com.thanos.participant1.characters.Thanos;
import com.thanos.participant1.characters.PlayerView;
import com.thanos.participant1.levels.World;
import java.util.List;

/** Coordinates player movement and world transitions behind one interface. */
public final class GameFacade {
    private final List<World> worlds;
    private final Thanos player;
    private GamePhase phase = GamePhase.MENU;
    private int worldIndex;
    private boolean sessionActive;
    private boolean disposed;

    public GameFacade(List<World> worlds) {
        this.worlds = List.copyOf(worlds);
        if (worlds.isEmpty()) {
            throw new IllegalArgumentException("A campaign needs at least one world");
        }
        player = new Thanos(world().spawnX(), world().spawnY());
    }

    public GamePhase phase() { return phase; }
    public boolean canResume() { return sessionActive; }
    public World world() { return worlds.get(worldIndex); }
    public List<World> worlds() { return worlds; }
    public void showMenu() { phase = GamePhase.MENU; }

    public void newGame() {
        if (disposed) {
            throw new IllegalStateException("Game has been disposed");
        }
        worldIndex = 0;
        sessionActive = true;
        phase = GamePhase.PLAYING;
        enterWorld();
    }

    public void pauseGame() {
        if (phase == GamePhase.PLAYING) phase = GamePhase.PAUSED;
    }

    public void resumeGame() {
        if (canResume()) phase = GamePhase.PLAYING;
    }

    public void update(float deltaSeconds, float horizontal, float vertical) {
        if (phase != GamePhase.PLAYING) return;
        player.move(horizontal, vertical, deltaSeconds, world());
    }

    public TransitionResult interactWithPortal() {
        if (phase != GamePhase.PLAYING) return TransitionResult.NOT_PLAYING;
        if (!world().overlapsPortal(player.x(), player.y(), player.width(), player.height())) {
            return TransitionResult.TOO_FAR;
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

    public PlayerView player() {
        return new PlayerView(player.x(), player.y(), player.width(), player.height());
    }

    public void dispose() {
        if (disposed) return;
        disposed = true;
        sessionActive = false;
        showMenu();
    }

    private void enterWorld() {
        player.spawnAt(world().spawnX(), world().spawnY());
    }
}
