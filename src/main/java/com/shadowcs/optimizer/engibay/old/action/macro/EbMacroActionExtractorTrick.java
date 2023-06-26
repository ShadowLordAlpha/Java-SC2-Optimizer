package com.shadowcs.optimizer.engibay.old.action.macro;

import com.shadowcs.optimizer.engibay.old.EbBuildOrder;

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
