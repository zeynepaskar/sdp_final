package com.thanos.participant1.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.thanos.participant1.patterns.facade.GameFacade;

final class WorldRenderer implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final MapArt art = new MapArt();

    void setProjection(Matrix4 projection) { batch.setProjectionMatrix(projection); }

    void render(GameFacade facade, float elapsed) {
        batch.begin();
        art.drawTerrain(batch, facade.world(), true, elapsed);
        art.drawPlayer(batch, facade.player(), elapsed);
        batch.end();
    }

    @Override
    public void dispose() {
        art.dispose();
        batch.dispose();
    }
}
