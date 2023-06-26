package com.shadowcs.optimizer.engibay.build;

import java.util.UUID;

public record Order(UUID uuid) {

    public Order() {
        this(UUID.randomUUID());
    }
}
