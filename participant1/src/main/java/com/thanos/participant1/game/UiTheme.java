package com.thanos.participant1.game;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

final class UiTheme {
    static final Color INK = Color.valueOf("353B33");
    static final Color PAPER = Color.valueOf("F2EDD0");
    final BitmapFont font = new BitmapFont();
    private final Texture texture;
    private final NinePatchDrawable border;

    UiTheme() {
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        Pixmap pixels = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pixels.setColor(INK);
        pixels.fill();
        pixels.setColor(Color.WHITE);
        pixels.fillRectangle(2, 2, 4, 4);
        texture = new Texture(pixels);
        pixels.dispose();
        border = new NinePatchDrawable(new NinePatch(texture, 3, 3, 3, 3));
    }

    Drawable frame() { return border.tint(PAPER); }

    Label label(String text, float scale) {
        Label label = new Label(text, new Label.LabelStyle(font, INK));
        label.setFontScale(scale);
        return label;
    }

    TextButton button(String text, Runnable action) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(
                frame(), border.tint(Color.valueOf("CEBE89")), frame(), font);
        style.over = border.tint(Color.valueOf("E5DCAB"));
        style.fontColor = INK;
        style.disabledFontColor = Color.GRAY;
        TextButton button = new TextButton(text, style);
        button.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return button;
    }

    public void dispose() {
        font.dispose();
        texture.dispose();
    }
}
