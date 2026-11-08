package com.thanos.participant1.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.thanos.participant1.patterns.facade.GameFacade;
import com.thanos.participant1.patterns.facade.GamePhase;
import com.thanos.participant3.ui.BattleScreen;
import com.thanos.participant3.ui.InventoryScreen;
import com.thanos.participant3.ui.StoneRewardView;

public class ThanosGame extends Game {
    private final GameFacade facade;
    private final MenuCursor menu = new MenuCursor();
    private Stage stage;
    private UiTheme theme;
    private WorldRenderer renderer;
    private InventoryScreen bag;
    private StoneRewardView reward;
    private BattleScreen battle;
    private GamePhase displayedPhase;
    private Label location;
    private Label prompt;
    private StoryPanel storyPanel;
    private String displayedWorld;
    private float elapsed;

    public ThanosGame(GameFacade facade) { this.facade = facade; }
    public GameFacade facade() { return facade; }

    @Override
    public void create() {
        theme = new UiTheme();
        storyPanel = new StoryPanel(theme);
        stage = new Stage(new FitViewport(1280, 800));
        renderer = new WorldRenderer();
        Gdx.input.setInputProcessor(stage);
        showMenu();
    }

    public void showMenu() {
        facade.showMenu();
        rebuildPanel();
    }

    private void startGame() {
        closeSessionViews();
        facade.newGame();
        bag = new InventoryScreen(facade.inventory(), facade.progress());
        reward = new StoneRewardView(facade.rewards(), BattleScreen::gauntletSlot);
        rebuildPanel();
    }

    private void rebuildPanel() {
        stage.clear();
        menu.clear();
        displayedPhase = facade.phase();
        location = null;
        prompt = null;
        if (displayedPhase == GamePhase.BATTLE) return;
        if (displayedPhase != GamePhase.MENU) {
            location = theme.label("", 2);
            displayedWorld = null;
            location.setPosition(80, 742);
            stage.addActor(location);
            if (displayedPhase == GamePhase.PLAYING) {
                prompt = theme.label("", 1.45f);
                prompt.setAlignment(Align.right);
                prompt.setBounds(790, 733, 410, 40);
                stage.addActor(prompt);
                stage.addActor(storyPanel);
                return;
            }
            if (displayedPhase == GamePhase.ALERT) return;
        }

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        Table panel = new Table();
        panel.setBackground(theme.frame());
        panel.pad(32);
        root.add(panel).width(430);
        switch (displayedPhase) {
            case MENU -> {
                title(panel, "Thanos");
                addButton(panel, "Start expedition", this::startGame);
                addButton(panel, "Resume expedition", facade::resumeGame).setDisabled(!facade.canResume());
                addButton(panel, "Quit", () -> Gdx.app.exit());
            }
            case PAUSED -> {
                title(panel, "Paused");
                addButton(panel, "Resume", facade::resumeGame);
                addButton(panel, "Restart expedition", this::startGame);
                addButton(panel, "Main menu", this::showMenu);
            }
            case COMPLETE -> {
                title(panel, "Six stones collected");
                addButton(panel, "Play again", this::startGame);
                addButton(panel, "Main menu", this::showMenu);
            }
            default -> throw new IllegalStateException("Unexpected menu phase " + displayedPhase);
        }
    }

    private void title(Table panel, String text) {
        panel.add(theme.label(text, 2.5f)).padBottom(28).row();
    }

    private TextButton addButton(Table panel, String text, Runnable action) {
        TextButton button = theme.button(text, action);
        panel.add(button).width(330).height(52).padBottom(10).row();
        menu.add(button, text, action);
        return button;
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), .05f);
        update(delta);
        if (displayedPhase != facade.phase()) rebuildPanel();
        ScreenUtils.clear(UiTheme.PAPER);
        stage.getViewport().apply();
        if (facade.phase() == GamePhase.BATTLE || showingReward()) {
            ensureBattleView();
            battle.hideStone(facade.rewards().current());
            battle.render(stage.getCamera().combined);
            reward.render(stage.getCamera().combined);
        } else {
            if (location != null) {
                if (!facade.world().id().equals(displayedWorld)) {
                    location.setText(facade.world().name() + "   /   " + facade.world().subtitle());
                    displayedWorld = facade.world().id();
                }
                renderer.setProjection(stage.getCamera().combined);
                renderer.render(facade, elapsed);
            }
            if (prompt != null) {
                var dialogue = facade.dialogue();
                storyPanel.show(dialogue);
                var marker = dialogue == null ? facade.nearbyMarker() : null;
                prompt.setText(marker == null ? "" : "Z  " + marker.name());
            }
            stage.act(delta);
            stage.draw();
        }
        if (bag != null && bag.isOpen()) bag.render(stage.getCamera().combined);
    }

    private boolean showingReward() {
        return facade.phase() == GamePhase.PLAYING && battle != null && facade.rewards().isPlaying();
    }

    private void update(float delta) {
        GamePhase phase = facade.phase();
        if (bag != null && bag.isOpen()) {
            bag.update(stage.getViewport());
            return;
        }
        if (showingReward()) {
            if (pressed(Input.Keys.Z)
                    || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) facade.rewards().skip();
            facade.rewards().update(delta);
            if (!facade.rewards().isPlaying()) closeBattle();
            return;
        }
        if (phase == GamePhase.PLAYING && facade.dialogue() != null) {
            if (pressed(Input.Keys.X)) facade.cancel();
            else if (pressed(Input.Keys.Z)) facade.confirm();
            else if (pressed(Input.Keys.UP) || pressed(Input.Keys.LEFT)) facade.navigateStory(-1);
            else if (pressed(Input.Keys.DOWN) || pressed(Input.Keys.RIGHT)) facade.navigateStory(1);
            return;
        }
        if ((phase == GamePhase.PLAYING || phase == GamePhase.BATTLE)
                && pressed(Input.Keys.I)) {
            bag.open();
            return;
        }
        switch (phase) {
            case MENU, PAUSED, COMPLETE -> {
                if (pressed(Input.Keys.X)) {
                    if (phase == GamePhase.PAUSED) facade.resumeGame();
                    if (phase == GamePhase.COMPLETE) showMenu();
                } else if (pressed(Input.Keys.Z)) menu.confirm();
                else if (pressed(Input.Keys.UP)) menu.move(-1);
                else if (pressed(Input.Keys.DOWN)) menu.move(1);
            }
            case PLAYING -> {
                if (pressed(Input.Keys.X)) {
                    facade.cancel();
                    return;
                }
                if (pressed(Input.Keys.Z)) {
                    facade.confirm();
                    return;
                }
                float x = axis(Input.Keys.LEFT, Input.Keys.RIGHT);
                float y = axis(Input.Keys.DOWN, Input.Keys.UP);
                facade.update(delta, x, y);
                elapsed += delta;
            }
            case ALERT -> facade.update(delta, 0, 0);
            case BATTLE -> {
                ensureBattleView();
                battle.update(stage.getViewport());
                if (battle.isFinished()) {
                    facade.finishBattle();
                    if (!facade.rewards().isPlaying()) closeBattle();
                }
            }
        }
    }

    private void ensureBattleView() {
        if (battle == null) {
            battle = new BattleScreen(facade.battle(), facade.inventory(), facade.messages(), facade.world().accentRgba());
        }
    }

    private static boolean pressed(int key) { return Gdx.input.isKeyJustPressed(key); }

    private static int axis(int negative, int positive) {
        int minus = Gdx.input.isKeyPressed(negative) ? 1 : 0;
        int plus = Gdx.input.isKeyPressed(positive) ? 1 : 0;
        return plus - minus;
    }

    @Override
    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    @Override
    public void pause() {
        if (bag != null) bag.close();
        if (!showingReward()) facade.pauseGame();
    }

    private void closeBattle() {
        if (battle != null) battle.dispose();
        battle = null;
    }

    private void closeSessionViews() {
        closeBattle();
        if (bag != null) bag.dispose();
        if (reward != null) reward.dispose();
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        closeSessionViews();
        stage.dispose();
        renderer.dispose();
        theme.dispose();
        facade.dispose();
    }
}
