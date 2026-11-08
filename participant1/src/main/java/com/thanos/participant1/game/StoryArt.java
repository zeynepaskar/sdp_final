package com.thanos.participant1.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import static com.thanos.participant1.levels.WorldLayout.TILE_SIZE;

final class StoryArt implements Disposable {
    private final TextureAtlas atlas = new TextureAtlas(Gdx.files.classpath("art/story/markers.atlas"));

    void draw(SpriteBatch batch, String sprite, float tileX, float tileY) {
        TextureRegion region = atlas.findRegion(sprite);
        if (region == null) throw new IllegalArgumentException("Missing story sprite: " + sprite);
        float x = MapArt.LEFT + tileX + (TILE_SIZE - region.getRegionWidth()) / 2;
        batch.draw(region, x, MapArt.BOTTOM + tileY);
    }

    @Override
    public void dispose() { atlas.dispose(); }
}
