package com.shadowcs.optimizer.nydusnetwork.breathfirst;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
public class BFSNode<T> {

    @EqualsAndHashCode.Include private final T node;
    @ToString.Exclude private final Set<BFSNode<T>> neighbors = new HashSet<>(); // TODO: embed this maybe? also exclude it from the string so we don't endlessly loop
}
