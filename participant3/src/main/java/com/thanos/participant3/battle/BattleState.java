package com.thanos.participant3.battle;

public enum BattleState {
    PLAYER_TURN, ENEMY_TURN, WON, LOST, FLED;

    public boolean isOver() { return this == WON || this == LOST || this == FLED; }
}
