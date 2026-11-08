package com.thanos.participant1.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.thanos.participant1.characters.PlayerView;
import com.thanos.participant1.levels.Encounter;
import com.thanos.participant1.patterns.facade.GameFacade;
import com.thanos.participant1.patterns.facade.GamePhase;
import com.thanos.participant3.ui.CharacterSprites;

final class WorldRenderer implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final MapArt art = new MapArt();
    private final CharacterSprites characters = new CharacterSprites();

    void setProjection(Matrix4 projection) { batch.setProjectionMatrix(projection); }

    void render(GameFacade facade, float elapsed) {
        PlayerView player = facade.player();
        batch.begin();
        art.drawTerrain(batch, facade.world(), facade.exitUnlocked(), elapsed);
        drawTrainers(facade, player.y(), true);
        art.drawPlayer(batch, player, elapsed);
        drawTrainers(facade, player.y(), false);
        if (facade.phase() == GamePhase.ALERT) {
            Encounter trainer = facade.activeEncounter();
            batch.draw(art.alert(), MapArt.LEFT + trainer.x() + 8, MapArt.BOTTOM + trainer.y() + 44, 24, 24);
        }
        batch.end();
    }

    private void drawTrainers(GameFacade facade, float playerY, boolean behindPlayer) {
        for (Encounter enemy : facade.world().encounters()) {
            if (facade.isCleared(enemy) || (enemy.y() >= playerY) != behindPlayer) continue;
            TextureRegion sprite = characters.enemy(enemy.kind().displayName());
            float size = enemy.isBoss() ? 50 : 40;
            float cx = MapArt.LEFT + enemy.x() + 20, y = MapArt.BOTTOM + enemy.y();
            batch.draw(sprite, cx - size / 2, y, size, size);
            Encounter.Facing facing = enemy.facing();
            batch.draw(art.facing(facing.ordinal()), cx - 10 + 23 * facing.dx(), y + 8 + 23 * facing.dy(), 20, 20);
        }
    }

    @Override
    public void dispose() {
        characters.dispose();
        art.dispose();
        batch.dispose();
    }
}
