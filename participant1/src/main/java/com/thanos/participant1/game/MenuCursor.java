package com.thanos.participant1.game;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import java.util.ArrayList;
import java.util.List;

final class MenuCursor {
    private record Entry(TextButton button, String label, Runnable action) {}
    private final List<Entry> entries = new ArrayList<>();
    private int selected;

    void clear() {
        entries.clear();
        selected = 0;
    }

    void add(TextButton button, String label, Runnable action) {
        entries.add(new Entry(button, label, action));
        if (entries.size() == 1) highlight(0);
    }

    void move(int direction) {
        for (int n = 1; n <= entries.size(); n++) {
            int next = Math.floorMod(selected + n * direction, entries.size());
            if (!entries.get(next).button().isDisabled()) {
                highlight(next);
                return;
            }
        }
    }

    private void highlight(int index) {
        Entry previous = entries.get(selected);
        previous.button().setText(previous.label());
        selected = index;
        Entry current = entries.get(selected);
        current.button().setText(">  " + current.label());
    }

    void confirm() {
        if (!entries.isEmpty() && !entries.get(selected).button().isDisabled()) {
            entries.get(selected).action().run();
        }
    }
}
