package com.shadowcs.optimizer.nydusnetwork.breathfirst;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class BFS {

    public <T> List<List<BFSNode<T>>> bfs(Set<BFSNode<T>> start, Set<BFSNode<T>> goals, boolean findAll) {
        return bfs(start, goals, findAll, true, 0);
    }
    /**
     *
     * @param start what nodes are we starting from
     * @param goals what goals are we looking for1
     * @param findAll Are we looking for all goals or just the first one
     * @param markVist Should we mark nodes we visit so we only ever check them once?
     * @param maxLength If markVist is false then this will be the max length a path is allowed to be
     * @return
     * @param <T>
     */
    public <T> List<List<BFSNode<T>>> bfs(Set<BFSNode<T>> start, Set<BFSNode<T>> goals, boolean findAll, boolean markVist, int maxLength) {

        List<List<BFSNode<T>>> pathsToGoals = new ArrayList<>(); // A list of our paths

        Set<BFSNode<T>> closedSet = new HashSet<>(); // Set of all nodes we have checked
        Queue<List<BFSNode<T>>> queue = new LinkedList<>();

        // Add our starting nodes to the path so we can do something with them
        for(BFSNode<T> sNode: start) {
            List<BFSNode<T>> path = new ArrayList<>();
            path.add(sNode);
            closedSet.add(sNode);

            queue.add(path);
        }

        while(!queue.isEmpty()) {
            var path = queue.poll();
            var node = path.get(path.size() - 1);

            // Check if we are in a goal state
            if (goals.contains(node)) {
                pathsToGoals.add(new ArrayList<>(path));

                // Are we looking for all paths or are we just looking for the first one
                if (!findAll) {
                    break;
                }
            }

            // Explore neighbors
            for (var neighbor : node.neighbors()) {
                if(markVist) {
                    if (!closedSet.contains(neighbor)) {
                        List<BFSNode<T>> newPath = new ArrayList<>(path);
                        newPath.add(neighbor);
                        queue.add(newPath);

                        closedSet.add(neighbor);
                    }
                } else {
                    List<BFSNode<T>> newPath = new ArrayList<>(path);
                    newPath.add(neighbor);
                    if(newPath.size() < maxLength) {
                        queue.add(newPath);
                    }
                }
            }
        }

        return pathsToGoals;
    }
}
