package com.shadowcs.optimizer.build.genetics;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;

import javax.annotation.Nullable;

/**
 *
 * @param caster What unit type is casting this ability
 * @param ability
 * @param chrono
 * @param morph
 * @param time
 * @param created
 */
public record BuildOrderGeneData(UnitType caster,
                                 Abilities ability,
                                 boolean chrono,
                                 boolean morph,
                                 int time,
                                 @Nullable UnitType... created) {


}
