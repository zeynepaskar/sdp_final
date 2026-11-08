package com.thanos.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.thanos.participant1.game.ThanosGame;
import com.thanos.participant1.levels.WorldCatalog;
import com.thanos.participant1.patterns.facade.GameFacade;

public final class DesktopLauncher {
    private DesktopLauncher() { }

    public static void main(String[] args) {
        GameFacade facade = new GameFacade(WorldCatalog.createCampaign());
        ThanosGame game = new ThanosGame(facade);
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Thanos");
        configuration.setWindowedMode(1280, 800);
        configuration.setWindowSizeLimits(800, 500, -1, -1);
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);
        configuration.disableAudio(true);
        new Lwjgl3Application(game, configuration);
    }
}
