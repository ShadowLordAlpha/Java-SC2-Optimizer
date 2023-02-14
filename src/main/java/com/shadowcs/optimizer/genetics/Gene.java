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
     * What Genes are required for this one to be buildable. Note that required Genes are genes that are needed and that
     * we unable to proceed without. Normally this should be blank as the fitness function itself should correct any
     * errors during the run time and instead the needed set should be used instead. Note that only one contained set must be valid.
     */
    private final Set<Set<Gene>> required = new HashSet<>();

    /**
     * What Genes are needed for this to be valid. This is different from the required list in that we don't need to use
     * a different gene as while we do need these first the Fitness function is able to properly handle the missing
     * data and create what is needed. Note that only one contained set must be valid.
     *
     * Examples of use of this would be using a more advanced version of a building. That would be a separate list
     * or requirements. This makes things a little more verbose but also more explicit.
     */
    private final Set<Set<Gene>> needed = new HashSet<>();

    /**
     * Return true if at least one set of genes within the required list is fully satisfied.
     *
     * @param comm
     * @return true iff at least one set or requirements is met
     */
    public boolean valid(Set<Gene> comm) {
        if(required().isEmpty()) {
            return true;
        }

        for(var req: required()) {
            if(comm.containsAll(req)) {
                return true;
            }
        }

        return false;
    }

    public Set<Gene> getNeeded(Collection<Gene> comm) {

        // There is some needed requirement still
        Set<Gene> needed = null;
        for(var subset: needed()) {
            Set<Gene> check = new HashSet<>(subset);
            check.removeIf(comm::contains);

            // We only need one set of needed so lets take the needed set with the smallest number of needed parts
            if(needed == null || needed.size() > check.size()) {
                needed = check;
            }
        }

        // The set of all completed and already known requirements. This helps prevent duplicates and complete the check
        if(needed != null) {
            Set<Gene> known = new HashSet<>(comm);
            known.addAll(needed);
            for (var gene : needed) {
                var subNeeded = gene.getNeeded(known);
                needed.addAll(subNeeded);
                known.addAll(subNeeded);
            }
        }

        return needed;
    }

    public <T> Gene data(T data) {
        this.data = data;
        return this;
    }

    public <T> T data() {
        return (T)  data;
    }

    @Ov
}
