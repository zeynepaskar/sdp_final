package com.thanos.participant3.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.thanos.participant3.stones.InfinityStone;
import java.util.Map;

public final class CharacterSprites implements Disposable {
    public static final String ATLAS = "art/battle/characters.atlas";

    public record Socket(int x, int yFromTop) {}

    // Upper left pixel of each 2×2 recess in Thanos's 64×64 frame.
    private static final Map<InfinityStone, Socket> SOCKETS = Map.of(
            InfinityStone.SOUL, new Socket(46, 28), InfinityStone.POWER, new Socket(50, 27),
            InfinityStone.TIME, new Socket(53, 27), InfinityStone.SPACE, new Socket(46, 31),
            InfinityStone.REALITY, new Socket(50, 31), InfinityStone.MIND, new Socket(53, 30));

    public static Socket socket(InfinityStone stone) { return SOCKETS.get(stone); }

    private final TextureAtlas atlas = new TextureAtlas(Gdx.files.classpath(ATLAS));
    private final TextureRegion fallback = atlas.findRegion("Outrider");
    final TextureRegion thanos = atlas.findRegion("Thanos");

    public TextureRegion enemy(String name) {
        TextureRegion region = atlas.findRegion(name);
        return region == null || region == thanos ? fallback : region;
    }

    @Override
    public void dispose() { atlas.dispose(); }
}
