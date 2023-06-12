package com.shadowcs.optimizer.sc2data.engibay.action;

import com.shadowcs.optimizer.sc2data.engibay.EbBuildOrder;

public interface EbAction {

    String name();

    /**
     * Is this action possible to do right now?
     *
     * @param current The current state of the build order
     * @return true iff the action is possible now
     */
    boolean isPossible(EbBuildOrder current);

    /**
     * Is this a valid action to do at this time? Valid actions are not always possible now but will be in the future
     * based on the current state.
     *
     * @param current The current state of the build order
     * @return true iff the action is valid and may be possibly now or in the future based on the current state
     */
    boolean isValid(EbBuildOrder current);

    /**
     * Are we able toe execute now?
     *
     * @param current The current state of the build order
     * @return
     */
    default boolean canExecute(EbBuildOrder current) {
        if(isPossible(current)) {
            return true;
        }

        // TODO: find out if the next action finishes first or if we should add an action before that. Then we need to
        //  recheck ourselves

        // It's easier to just wait 1 second and check again... so thats what we will do for now until we have a better solution
        current.executeFutureActions(23);

        return false;
    }

    void execute(EbBuildOrder current);

    default int[] unitRequirements() {
        return new int[]{};
    }

    default int[] researchRequirements() {
        return new int[]{};
    }
}
