package com.thanos.participant3.patterns.observer;

/** Observer: anything that wants to react to game events, such as the HUD or a battle log. */
public interface GameEventListener {
    void onEvent(GameEvent event);
}
