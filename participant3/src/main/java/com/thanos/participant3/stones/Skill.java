package com.thanos.participant3.stones;

/** One of Thanos's eight skills: its plain form and the stronger form it becomes once upgraded. */
public record Skill(Ability basic, Ability upgraded) {
    public Skill {
        if (basic == null || upgraded == null || basic.stone() != upgraded.stone()) {
            throw new IllegalArgumentException("Skill needs two forms of the same stone");
        }
    }

    public InfinityStone stone() { return basic.stone(); }

    public Ability form(boolean isUpgraded) { return isUpgraded ? upgraded : basic; }
}
