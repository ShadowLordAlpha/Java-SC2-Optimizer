package com.shadowcs.optimizer.nydusnetwork.astar;

import lombok.Data;

@Data
public class AStarRouteNode<T> implements Comparable<AStarRouteNode<T>> {

    /**
     * This is the node that we are currently, while other values may change this one may not
     */
    private final T node;

    /**
     * The node we came from to get to this node, this may only be one node but we are allowed to swap this node out
     * with basically any other node
     */
    private T previous;

    /**
     * How far are we from the starting node
     */
    private double gCost;

    /**
     * How fare are we from the end node
     */
    private double hCost;

    @Override
    public int compareTo(AStarRouteNode other) {
        if (this.fCost() > other.fCost()) {
            return 1;
        } else if (this.fCost() < other.fCost()) {
            return -1;
        } else {
            return 0;
        }
    }

    public double fCost() {
        return gCost + hCost;
    }
}
