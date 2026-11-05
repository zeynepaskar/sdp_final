package com.thanos.participant1.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.thanos.participant1.patterns.facade.*;

/** Connects libGDX input and screens to the game facade. */
public class ThanosGame extends Game {
    private final GameFacade facade;
    private Stage stage;
    private UiTheme theme;
    private WorldRenderer renderer;
    private GamePhase displayedPhase;
    private Label location;
    private boolean menu = true;
    private float elapsed;

    public ThanosGame(GameFacade facade) {
        this.facade = facade;
    }

    public GameFacade facade() { return facade; }

    public void create() {
        theme = new UiTheme();
        stage = new Stage(new FitViewport(1280, 800));
        renderer = new WorldRenderer();
        Gdx.input.setInputProcessor(stage);
        showMenu();
    }

    public void showMenu() {
        menu = true;
        facade.showMenu();
        rebuildPanel();
    }

    public void showGameplay() {
        menu = false;
        rebuildPanel();
    }

    private void startGame() {
        facade.newGame();
        showGameplay();
    }

    private void rebuildPanel() {
        stage.clear();
        displayedPhase = facade.phase();
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        if (!menu) {
            location = theme.label(facade.world().name(), 2);
            location.setPosition(80, 742);
            stage.addActor(location);
            if (displayedPhase == GamePhase.PLAYING) return;
        }
        Table panel = new Table();
        panel.setBackground(theme.frame());
        panel.pad(32);
        root.add(panel).width(430);
        String title = menu ? "Thanos" : displayedPhase == GamePhase.PAUSED ? "Paused" : "Complete!";
        panel.add(theme.label(title, 3)).padBottom(28).row();
        if (menu) {
            addButton(panel, "Start expedition", this::startGame);
            TextButton resume = addButton(panel, "Resume expedition", () -> {
                facade.resumeGame();
                showGameplay();
            });
            resume.setDisabled(!facade.canResume());
            addButton(panel, "Quit", () -> Gdx.app.exit());
        } else {
            if (displayedPhase == GamePhase.PAUSED) {
                addButton(panel, "Resume", facade::resumeGame);
                addButton(panel, "Restart expedition", this::startGame);
            } else addButton(panel, "Play again", this::startGame);
            addButton(panel, "Main menu", this::showMenu);
        }
    }

    private TextButton addButton(Table panel, String text, Runnable action) {
        TextButton button = theme.button(text, action);
        panel.add(button).width(330).height(52).padBottom(10).row();
        return button;
    }

    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), .05f);
        handleInput(delta);
        if (displayedPhase != facade.phase()) rebuildPanel();
        ScreenUtils.clear(UiTheme.PAPER);
        stage.getViewport().apply();
        if (!menu) {
            if (facade.phase() == GamePhase.PLAYING) elapsed += delta;
            location.setText(facade.world().name());
            renderer.setProjection(stage.getCamera().combined);
            renderer.render(facade, elapsed);
        }
        stage.act(delta);
        stage.draw();
    }

    private void handleInput(float delta) {
        if (menu) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) startGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (facade.phase() == GamePhase.PLAYING) facade.pauseGame();
            else facade.resumeGame();
        }
        if (facade.phase() != GamePhase.PLAYING) return;
        float x = axis(Input.Keys.A, Input.Keys.LEFT, Input.Keys.D, Input.Keys.RIGHT);
        float y = axis(Input.Keys.S, Input.Keys.DOWN, Input.Keys.W, Input.Keys.UP);
        facade.update(delta, y == 0 ? x : 0, y);
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) facade.interactWithPortal();
    }

    private int axis(int negative, int alternateNegative, int positive, int alternatePositive) {
        int minus = Gdx.input.isKeyPressed(negative) || Gdx.input.isKeyPressed(alternateNegative) ? 1 : 0;
        int plus = Gdx.input.isKeyPressed(positive) || Gdx.input.isKeyPressed(alternatePositive) ? 1 : 0;
        return plus - minus;
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void pause() {
        facade.pauseGame();
    }

    public void dispose() {
        Gdx.input.setInputProcessor(null);
        stage.dispose();
        renderer.dispose();
        theme.dispose();
        facade.dispose();
    }
}
