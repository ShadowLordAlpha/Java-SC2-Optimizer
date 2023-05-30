package com.shadowcs.optimizer.genetics;

import lombok.Data;

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

    /**
     * What Genes are preconditions for this gene to be valid to be done.
     */
    private final Set<Set<Gene>> preconditions = new HashSet<>();

    /**
     * Return true if at least one set of genes within the required list is fully satisfied.
     *
     * @param comm
     * @return true iff at least one set or requirements is met
     */
    public boolean valid(Set<Gene> comm) {

        for(var con: preconditions) {
            if(comm.containsAll(con)) {
                return true;
            }
        }

        return false;
    }

    public <T> Gene data(T data) {
        this.data = data;
        return this;
    }

    public <T> T data() {
        return (T)  data;
    }
}
