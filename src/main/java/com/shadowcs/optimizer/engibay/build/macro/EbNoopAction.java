package com.shadowcs.optimizer.engibay.build.macro;

import com.shadowcs.optimizer.engibay.EbBuildOrder;
import com.shadowcs.optimizer.engibay.build.EbAction;

/**
 * A No-Op is an action that does nothing and just takes up space. We should honestly probably penalize them just for
 * existing tbh
 */
public record EbNoopAction() implements EbAction {
    @Override
    public boolean isValid(EbBuildOrder candidate) {
        // A No-Op is always valid, it literally does nothing and is design as a placeholder action
        return true;
    }

    @Override
    public int canExecute(EbBuildOrder candidate) {
        // We can always execute it now
        return 0;
    }

    @Override
    public void execute(EbBuildOrder candidate) {
        // We do nothing...
        candidate.noop(candidate.noop() + 1);
    }

    @Override
    public String name() {
        return "No-Op";
    }
}
