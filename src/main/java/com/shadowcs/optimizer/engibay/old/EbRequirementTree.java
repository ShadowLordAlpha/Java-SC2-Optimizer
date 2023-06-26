package com.shadowcs.optimizer.engibay.old;

import com.shadowcs.optimizer.engibay.old.action.EbAction;
import com.shadowcs.optimizer.engibay.old.action.macro.EbMacroActionMineGas;
import com.shadowcs.optimizer.engibay.old.action.macro.EbMacroActionMineMineral;
import com.shadowcs.optimizer.engibay.old.action.macro.EbMacroActionWait;
import com.shadowcs.optimizer.sc2data.models.TechTree;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This is used to reduce the search space so we don't need to check every unit and near every combination. Like if
 * we only want Barracks units we don't need to make a Factory or Starport.
 */
@Data
public class EbRequirementTree {

    /**
     * The set of all currently possible actions
     */
    private final List<EbAction> actionSet = new ArrayList<>();

    public EbRequirementTree(TechTree techTree, EbState destination) {

        for(var id: destination.unitCountMap().keySet()) {
            actionSet.addAll(techTree.unitGeneMap().get(id));
        }

        for(var upgrade: destination.upgradesMap().toArray()) {
            actionSet.add(techTree.upgradeGeneMap().get(upgrade));
        }

        for(var action: new HashSet<>(actionSet)) {
            addRequirements(techTree, action);
        }

        // TODO: we should add the other macro actions as well at this point
        actionSet.add(new EbMacroActionWait());

        if(true) {
            actionSet.add(new EbMacroActionMineGas());
            actionSet.add(new EbMacroActionMineMineral());
        }
    }

    public void addRequirements(TechTree techTree, EbAction req) {
        for(var reqAction: req.unitRequirements()) {
            for(var reqlist: techTree.unitGeneMap().getOrDefault(reqAction, new HashSet<>())) {
                if (!actionSet.contains(reqlist)) {
                    actionSet.add(reqlist);
                    addRequirements(techTree, reqlist);
                }
            }
        }

        for(var reqAction: req.researchRequirements()) {
            var rqu = techTree.upgradeGeneMap().get(reqAction);
            if (!actionSet.contains(rqu)) {
                actionSet.add(rqu);
                addRequirements(techTree, rqu);
            }
        }
    }
}
