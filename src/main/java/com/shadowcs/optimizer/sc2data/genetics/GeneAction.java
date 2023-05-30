package com.shadowcs.optimizer.sc2data.genetics;

import lombok.Data;

@Data
public class GeneAction {

    // This is the unit doing the action
    private int unitId;

    // If we are performing some action other than a game defined one, for example a command to mine gas, land at a reactor, or similar
    private CustomAction customAction;

    // This is the action we are doing
    private int actionId;

    // This is the unit that will be created if any when finished
    private int compUnitId;

    // This is the research that will be created if any when finished
    private int compResearchId;

    // Does this action happen on a unit that is not yet finished building? This is basically only for cancel but makes the state easier to calculate
    private boolean incompleteAction;

    public enum CustomAction {

    }
}
