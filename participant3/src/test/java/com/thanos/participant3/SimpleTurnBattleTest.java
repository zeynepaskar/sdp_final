package com.thanos.participant3;

import static org.junit.jupiter.api.Assertions.*;

import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.battle.SimpleTurnBattle;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.decorator.MindStoneDecorator;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.ui.BattleMessages;
import org.junit.jupiter.api.Test;

class SimpleTurnBattleTest {
    private final EventBus bus = new EventBus();
    private final FakeCombatant thanos = new FakeCombatant("Thanos", 1000, 1000, 10);
    private final FakeCombatant champion = new FakeCombatant("Champion", 800, 800, 10);

    @Test
    void turnsAlternate() {
        SimpleTurnBattle battle = new SimpleTurnBattle(bus, thanos, champion, 90, true);

        battle.playerAttack(new BasicAttack("Punch", 45));
        assertEquals(BattleState.ENEMY_TURN, battle.state());
        battle.enemyTurn();

        assertEquals(BattleState.PLAYER_TURN, battle.state());
        assertEquals(765, champion.health());
        assertEquals(920, thanos.health());
        assertThrows(IllegalStateException.class, battle::enemyTurn);
    }

    @Test
    void defendingHalvesTheNextHit() {
        SimpleTurnBattle battle = new SimpleTurnBattle(bus, thanos, champion, 90, true);

        battle.playerDefend();
        battle.enemyTurn();

        assertEquals(965, thanos.health());
    }

    @Test
    void mindStoneMakesTheEnemySkipItsTurn() {
        SimpleTurnBattle battle = new SimpleTurnBattle(bus, thanos, champion, 90, true);

        battle.playerAttack(new MindStoneDecorator(new BasicAttack("Punch", 45)));
        battle.enemyTurn();

        assertEquals(1000, thanos.health());
    }

    @Test
    void bossesCannotBeEscapedButOthersCan() {
        assertThrows(IllegalStateException.class, () -> new SimpleTurnBattle(bus, thanos, champion, 90, true).flee());
        SimpleTurnBattle outrider = new SimpleTurnBattle(bus, thanos, new FakeCombatant("Outrider", 100, 100, 0), 15, false);

        outrider.flee();

        assertEquals(BattleState.FLED, outrider.state());
    }

    @Test
    void finishingBlowWinsAndEveryStepBecomesAMessage() {
        BattleMessages messages = new BattleMessages();
        bus.subscribe(messages);
        SimpleTurnBattle battle = new SimpleTurnBattle(bus, thanos, new FakeCombatant("Outrider", 30, 100, 0), 15, false);

        battle.playerAttack(new BasicAttack("Punch", 45));

        assertEquals(BattleState.WON, battle.state());
        assertEquals("Outrider blocks the way!", messages.current());
        messages.advance();
        assertEquals("Outrider received 45 damage", messages.current());
        messages.advance();
        assertEquals("Outrider is defeated. Thanos wins!", messages.current());
        messages.advance();
        assertFalse(messages.hasPending());
    }
}
