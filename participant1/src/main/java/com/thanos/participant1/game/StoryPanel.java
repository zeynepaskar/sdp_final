package com.thanos.participant1.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.thanos.participant1.story.DialogueView;
import java.util.Objects;

final class StoryPanel extends Table {
    private static final Color SPEAKER = Color.valueOf("6C415B");
    private final UiTheme theme;
    private DialogueView page;

    StoryPanel(UiTheme theme) {
        this.theme = theme;
        setBackground(theme.frame());
        setTouchable(Touchable.disabled);
        defaults().left();
        pad(20, 28, 22, 28);
        setVisible(false);
    }

    void show(DialogueView next) {
        if (Objects.equals(page, next)) return;
        page = next;
        clearChildren();
        setVisible(page != null);
        if (page == null) return;
        Label speaker = theme.label(page.speaker(), 1.5f);
        speaker.setColor(SPEAKER);
        add(speaker).padBottom(8).row();
        Label line = theme.label(page.text(), 1.8f);
        line.setWrap(true);
        add(line).width(1060).row();
        for (int i = 0; i < page.choices().size(); i++) {
            String cursor = i == page.selected() ? ">  " : "    ";
            Label choice = theme.label(cursor + page.choices().get(i), 1.5f);
            if (i == page.selected()) choice.setColor(SPEAKER);
            add(choice).padTop(7).row();
        }
        pack();
        setPosition((1280 - getWidth()) / 2, 26);
    }
}
