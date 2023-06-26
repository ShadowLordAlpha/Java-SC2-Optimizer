package com.shadowcs.optimizer.engibay.build;

import com.github.ocraft.s2client.protocol.data.Ability;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * An action represents some action that the game is able to take. Note that Gas, Minerals, Food, and Energy are not
 * part of the requirements.
 *
 * @param type What type of action are we taking (makes some things easier)
 * @param name The name of what we are doing/making, this can be duplicated if we end up making the same building. This
 *             also doubles as what action we are doing for the action type
 * @param ability What ability if any are we using with this action. It may be a duplicate for some things and it may be
 *                INVALID (-1) as well
 * @param unitRequirements
 * @param upgradeRequirements
 */
public record EbAction(EbActionType type, String name, Ability ability, IntSet unitRequirements, IntSet upgradeRequirements) {
}
