package com.shadowcs.optimizer.sc2data.engibay.action.macro;

import com.shadowcs.optimizer.sc2data.engibay.EbBuildOrder;

/**
 * This action waits until all currently pending actions are finished and then executes and allows the rest of the build
 * order to happen.
 */
public class EbMacroActionWait implements EbMacroAction {

    @Override
    public String name() {
        return "Wait";
    }

    @Override
    public boolean isPossible(EbBuildOrder current) {
        return !isValid(current);
    }

    @Override
    public boolean isValid(EbBuildOrder current) {

        return current.futureActions().size() > 0;
    }

    @Override
    public boolean canExecute(EbBuildOrder current) {

        // TODO: is this needed?

        if(isPossible(current)) {
            return true;
        }

        current.executeNextAction();
        return false;
    }

    @Override
    public void execute(EbBuildOrder current) {
        current.waits(current.waits() + 1);
    }
}
