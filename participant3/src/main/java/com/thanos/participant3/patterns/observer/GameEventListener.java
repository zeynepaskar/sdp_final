package com.thanos.participant3.patterns.observer;

/** Observer interface. */
public interface GameEventListener {
    void onEvent(GameEvent event);
}
