package com.thanos.participant3.ui;

import com.thanos.participant3.patterns.decorator.Attack;
import java.util.ArrayList;
import java.util.List;

/** Mind Stone's Foresight text: the enemy's next attack, its damage and its effects, read from the Attack itself. */
public final class EnemyIntent {
    private EnemyIntent() { }

    /** attack may be null when the battle only reports the damage. */
    public static String describe(Attack attack, int damage) {
        if (damage == 0 && attack == null) return "the enemy is stunned and will skip its turn";
        if (attack == null) return "next blow " + damage;
        List<String> effects = new ArrayList<>();
        effects.add(damage + " damage");
        if (attack.stunTurns() > 0) effects.add("stuns you");
        if (attack.slowPercent() > 0) effects.add("your next hit -" + attack.slowPercent() + "%");
        if (attack.turnsDefenseIntoDamage()) effects.add("defense becomes damage");
        else if (attack.ignoresDefense() || attack.unblockable()) effects.add("ignores defense");
        if (attack.lifestealPercent() > 0) effects.add("drains " + attack.lifestealPercent() + "%");
        if (attack.selfHeal() > 0) effects.add("heals " + attack.selfHeal());
        return attack.name() + ":  " + String.join(", ", effects);
    }
}
