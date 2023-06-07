package com.shadowcs.optimizer.genetics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A gene defines a specific gene as well as all if any requirements of the gene itself.
 */
@Data
public class Gene {

    /**
     * The data object of this gene. Normally the actual overall instruction for the fitness instruction.
     */
    private Object data;

    public <T> Gene data(T data) {
        this.data = data;
        return this;
    }

    public <T> T data() {
        return (T)  data;
    }
}
