package com.thanos.participant1.story;

import com.thanos.participant1.levels.World;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Per-run story flags and the input state for the overworld. */
public final class StorySession {
    public interface Host {
        void interactNearby();
        void pauseGame();
        void visit(World sideArea);
        void returnToRoute();
    }

    private final Host host;
    private final Set<String> flags = new HashSet<>();
    private final Set<String> readableFlags = Collections.unmodifiableSet(flags);
    private final State exploring = new Exploring();
    private State state = exploring;

    public StorySession(Host host) { this.host = host; }

    public boolean exploring() { return state == exploring; }
    public boolean hasFlag(String flag) { return flags.contains(flag); }
    public DialogueView view() { return state.view(); }
    public void confirm() { state.confirm(); }
    public void cancel() { state.cancel(); }
    public void navigate(int direction) { state.navigate(direction); }
    public void reset() { flags.clear(); state = exploring; }

    void open(Function<Set<String>, Dialogue> conversation) { showPage(conversation.apply(readableFlags), 0); }

    private void showPage(Dialogue dialogue, int index) {
        boolean question = index == dialogue.pages().size() - 1 && !dialogue.options().isEmpty();
        state = question ? new Choosing(dialogue) : new Reading(dialogue, index);
    }

    private void finish(Dialogue dialogue) {
        if (dialogue.completionFlag() != null) flags.add(dialogue.completionFlag());
    }
    void visit(World area) { host.visit(area); }
    void returnToRoute() { host.returnToRoute(); }

    private interface State {
        void confirm();
        void cancel();
        default void navigate(int direction) { }
        default DialogueView view() { return null; }
    }

    private final class Exploring implements State {
        public void confirm() { host.interactNearby(); }
        public void cancel() { host.pauseGame(); }
    }

    private final class Reading implements State {
        private final Dialogue dialogue;
        private final int page;

        Reading(Dialogue dialogue, int page) {
            this.dialogue = dialogue;
            this.page = page;
        }

        public DialogueView view() {
            return new DialogueView(dialogue.speaker(), dialogue.pages().get(page), List.of(), -1);
        }

        public void confirm() {
            if (page + 1 < dialogue.pages().size()) {
                showPage(dialogue, page + 1);
            } else {
                finish(dialogue);
                state = exploring;
            }
        }

        public void cancel() { state = exploring; }
    }

    private final class Choosing implements State {
        private final Dialogue dialogue;
        private final List<String> labels;
        private int selected;

        Choosing(Dialogue dialogue) {
            this.dialogue = dialogue;
            labels = dialogue.options().stream().map(Dialogue.Option::label).toList();
        }

        public DialogueView view() {
            return new DialogueView(dialogue.speaker(), dialogue.pages().get(dialogue.pages().size() - 1), labels, selected);
        }

        public void navigate(int direction) {
            selected = Math.floorMod(selected + Integer.signum(direction), labels.size());
        }

        public void confirm() {
            Dialogue.Option choice = dialogue.options().get(selected);
            finish(dialogue);
            flags.add(choice.flag());
            showPage(choice.reply(), 0);
        }

        public void cancel() { state = exploring; }
    }
}
