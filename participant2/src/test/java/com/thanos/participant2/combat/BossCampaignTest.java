package com.thanos.participant2.combat;

import com.thanos.participant2.enemies.BossType;
import com.thanos.participant3.battle.BattleState;
import com.thanos.participant3.inventory.Inventory;
import com.thanos.participant3.patterns.decorator.BasicAttack;
import com.thanos.participant3.patterns.observer.EventBus;
import com.thanos.participant3.progress.Thanos;
import com.thanos.participant3.stones.InfinityStone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossCampaignTest {

    private final EventBus events = new EventBus();
    private final Thanos thanos = new Thanos(events);
    private final Inventory inventory = new Inventory(events, 60);
    private final BossCampaign campaign =
            new BossCampaign(events, thanos, inventory);

    /** Plays the battle with a punch strong enough to win in one turn. */
    private void win(TurnBattleController battle) {
        while (!battle.state().isOver()) {
            if (battle.state() == BattleState.PLAYER_TURN) {
                battle.playerAttack(new BasicAttack("Punch", 5000));
            } else {
                battle.enemyTurn();
            }
        }
    }

    /** Lets the enemy win: Thanos only defends until he falls. */
    private void lose(TurnBattleController battle) {
        thanos.takeDamage(thanos.health() - 1);
        while (!battle.state().isOver()) {
            if (battle.state() == BattleState.PLAYER_TURN) {
                battle.playerDefend();
            } else {
                battle.enemyTurn();
            }
        }
    }

    @Test
    void shouldAssignOneBossPerWorldInOrder() {
        assertEquals(BossType.IN_BETWEENER, campaign.bossForWorld(0));
        assertEquals(BossType.GRANDMASTER, campaign.bossForWorld(5));
        assertEquals(BossType.IN_BETWEENER, campaign.bossForWorld(6));
    }

    @Test
    void shouldRejectNegativeWorld() {
        assertThrows(IllegalArgumentException.class,
                () -> campaign.bossForWorld(-1));
    }

    @Test
    void shouldHealThanosAndGiveEnergyBeforeBattle() {
        thanos.takeDamage(500);

        campaign.startBossBattle(0, 0, 0);

        assertEquals(thanos.maxHealth(), thanos.health());
        assertTrue(inventory.energy() > 0);
    }

    @Test
    void shouldGiveStoneAndExperienceAfterBossWin() {
        TurnBattleController battle = campaign.startBossBattle(1, 0, 0);
        win(battle);

        BattleState result = campaign.finishBattle();

        assertEquals(BattleState.WON, result);
        assertTrue(inventory.stones().contains(InfinityStone.POWER));
        assertTrue(thanos.experience() > 0 || thanos.level() > 1);
        assertTrue(campaign.isBossDefeated(1));
        assertFalse(campaign.hasActiveBattle());
    }

    @Test
    void shouldNotRewardStoneAfterLoss() {
        TurnBattleController battle = campaign.startBossBattle(0, 0, 0);
        lose(battle);

        BattleState result = campaign.finishBattle();

        assertEquals(BattleState.LOST, result);
        assertTrue(inventory.stones().isEmpty());
        assertFalse(campaign.isBossDefeated(0));
    }

    @Test
    void regularWinGivesExperienceButNoStone() {
        TurnBattleController battle = campaign.startRegularBattle(0);
        win(battle);

        campaign.finishBattle();

        assertTrue(inventory.stones().isEmpty());
        assertTrue(thanos.experience() > 0);
        assertTrue(battle.canFlee());
    }

    @Test
    void regularEncountersCycleThroughEnemyKinds() {
        String first = campaign.startRegularBattle(0).enemy().name();
        campaign.combat().currentBattle().ifPresent(b -> b.flee());
        campaign.finishBattle();

        String second = campaign.startRegularBattle(1).enemy().name();

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotAllowSecondBattleBeforeFinishing() {
        campaign.startBossBattle(0, 0, 0);

        assertThrows(IllegalStateException.class,
                () -> campaign.startBossBattle(1, 0, 0));
    }

    @Test
    void shouldFailToFinishWithoutBattle() {
        assertThrows(IllegalStateException.class, campaign::finishBattle);
    }

    @Test
    void shouldFinishWholeCampaign() {
        for (int world = 0; world < BossType.values().length; world++) {
            win(campaign.startBossBattle(world, 0, 0));
            campaign.finishBattle();
        }

        assertTrue(campaign.allBossesDefeated());
        assertEquals(6, inventory.stones().size());
    }
}
