package com.thanos.participant3.patterns.observer;

import java.util.ArrayList;
import java.util.List;

/** Subject: game logic publishes here and never needs to know who is listening. */
public final class EventBus {
    private final List<GameEventListener> listeners = new ArrayList<>();

    public void subscribe(GameEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void unsubscribe(GameEventListener listener) {
        listeners.remove(listener);
    }

    public int listenerCount() { return listeners.size(); }

    public void publish(GameEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        // Copy so a listener may unsubscribe while being notified.
        for (GameEventListener listener : List.copyOf(listeners)) {
            listener.onEvent(event);
        }
    }

    public void publish(GameEventType type, String source, String message, int value) {
        publish(new GameEvent(type, source, message, value));
    }
}
