package com.shadowcs.optimizer.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<E, K> {
    private final E first;
    private final K second;
}
