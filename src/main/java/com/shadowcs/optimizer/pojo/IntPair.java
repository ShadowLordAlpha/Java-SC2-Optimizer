package com.shadowcs.optimizer.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(cacheStrategy= EqualsAndHashCode.CacheStrategy.LAZY)
public class IntPair {
    private final int first;
    private final int second;
}
