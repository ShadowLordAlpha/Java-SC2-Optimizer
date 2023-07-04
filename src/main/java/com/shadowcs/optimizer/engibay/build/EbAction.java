package com.shadowcs.optimizer.engibay.build;

import com.shadowcs.optimizer.engibay.EbBuildOrder;

public interface EbAction {

    boolean isValid(EbBuildOrder candidate);

    int canExecute(EbBuildOrder candidate);

    void execute(EbBuildOrder candidate);

    String name();
}
