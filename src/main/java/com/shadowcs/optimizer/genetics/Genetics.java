package com.shadowcs.optimizer.genetics;

import java.util.List;

@FunctionalInterface
public interface Genetics<E> {

    List<E> available(Chromosome<E> chromo, int index);
}
