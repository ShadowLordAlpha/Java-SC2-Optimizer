package com.shadowcs.optimizer.sc2data.engibay.action.macro;

import com.shadowcs.optimizer.sc2data.engibay.EbBuildOrder;

public class EbMacroActionExtractorTrick implements EbMacroAction {

    @Override
    public String name() {
        return "Extractor Trick";
    }

    @Override
    public boolean isPossible(EbBuildOrder current) {
        return false;
    }

    @Override
    public boolean isValid(EbBuildOrder current) {
        return false;
    }

    @Override
    public void execute(EbBuildOrder current) {

    }
}
