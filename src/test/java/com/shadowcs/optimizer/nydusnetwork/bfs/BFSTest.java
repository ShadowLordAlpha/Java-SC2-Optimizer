package com.shadowcs.optimizer.nydusnetwork.bfs;

import com.shadowcs.optimizer.nydusnetwork.breathfirst.BFS;
import com.shadowcs.optimizer.nydusnetwork.breathfirst.BFSNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BFSTest {

    @Test
    public void singleStartSingleGoal() {

        List<BFSNode<Integer>> bfsNodes = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            bfsNodes.add(new BFSNode<>(i));
        }

        bfsNodes.get(2).neighbors().add(bfsNodes.get(3));
        bfsNodes.get(3).neighbors().add(bfsNodes.get(4));
        bfsNodes.get(4).neighbors().add(bfsNodes.get(5));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(6));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(9));

        bfsNodes.get(6).neighbors().add(bfsNodes.get(7));
        bfsNodes.get(7).neighbors().add(bfsNodes.get(8));
        bfsNodes.get(8).neighbors().add(bfsNodes.get(9));

        var start = bfsNodes.get(2);
        var end = bfsNodes.get(9);

        var paths = BFS.bfs(Set.of(start), Set.of(end), false);
        System.out.println(paths);

        assert paths.size() == 1;
        assert paths.get(0).equals(Arrays.asList(bfsNodes.get(2), bfsNodes.get(3), bfsNodes.get(4), bfsNodes.get(5), bfsNodes.get(9)));
    }

    @Test
    public void singleStartSingleGoalFindAll() {

        List<BFSNode<Integer>> bfsNodes = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            bfsNodes.add(new BFSNode<>(i));
        }

        bfsNodes.get(2).neighbors().add(bfsNodes.get(3));
        bfsNodes.get(3).neighbors().add(bfsNodes.get(4));
        bfsNodes.get(4).neighbors().add(bfsNodes.get(5));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(6));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(9));

        bfsNodes.get(6).neighbors().add(bfsNodes.get(7));
        bfsNodes.get(7).neighbors().add(bfsNodes.get(8));
        bfsNodes.get(8).neighbors().add(bfsNodes.get(9));

        var start = bfsNodes.get(2);
        var end = bfsNodes.get(9);

        var paths = BFS.bfs(Set.of(start), Set.of(end), true);
        System.out.println(paths);

        // Even though there are 2 methods to get to 9 BFS will only get the first one, the second is ignored
        assert paths.size() == 1;
        assert paths.get(0).equals(Arrays.asList(bfsNodes.get(2), bfsNodes.get(3), bfsNodes.get(4), bfsNodes.get(5), bfsNodes.get(9)));
    }

    @Test
    public void singleStartMultiGoal() {

        List<BFSNode<Integer>> bfsNodes = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            bfsNodes.add(new BFSNode<>(i));
        }

        bfsNodes.get(2).neighbors().add(bfsNodes.get(3));
        bfsNodes.get(3).neighbors().add(bfsNodes.get(4));
        bfsNodes.get(4).neighbors().add(bfsNodes.get(5));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(6));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(9));

        bfsNodes.get(6).neighbors().add(bfsNodes.get(7));
        bfsNodes.get(7).neighbors().add(bfsNodes.get(8));
        bfsNodes.get(8).neighbors().add(bfsNodes.get(9));

        var start = bfsNodes.get(2);
        var end = bfsNodes.get(9);
        var end2 = bfsNodes.get(4);

        var paths = BFS.bfs(Set.of(start), Set.of(end, end2), false);
        System.out.println(paths);

        assert paths.size() == 1;
        assert paths.get(0).equals(Arrays.asList(bfsNodes.get(2), bfsNodes.get(3), bfsNodes.get(4)));
    }

    @Test
    public void singleStartMultiGoalFindAll() {

        List<BFSNode<Integer>> bfsNodes = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            bfsNodes.add(new BFSNode<>(i));
        }

        bfsNodes.get(2).neighbors().add(bfsNodes.get(3));
        bfsNodes.get(3).neighbors().add(bfsNodes.get(4));
        bfsNodes.get(4).neighbors().add(bfsNodes.get(5));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(6));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(9));

        bfsNodes.get(6).neighbors().add(bfsNodes.get(7));
        bfsNodes.get(7).neighbors().add(bfsNodes.get(8));
        bfsNodes.get(8).neighbors().add(bfsNodes.get(9));

        var start = bfsNodes.get(2);
        var end = bfsNodes.get(9);
        var end2 = bfsNodes.get(4);

        var paths = BFS.bfs(Set.of(start), Set.of(end, end2), true);
        System.out.println(paths);

        assert paths.size() == 2;
        assert paths.get(0).equals(Arrays.asList(bfsNodes.get(2), bfsNodes.get(3), bfsNodes.get(4)));
        assert paths.get(1).equals(Arrays.asList(bfsNodes.get(2), bfsNodes.get(3), bfsNodes.get(4), bfsNodes.get(5), bfsNodes.get(9)));
    }

    @Test
    public void singleStartNoGoalPath() {

        List<BFSNode<Integer>> bfsNodes = new ArrayList<>();
        for(int i = 0; i < 11; i++) {
            bfsNodes.add(new BFSNode<>(i));
        }

        bfsNodes.get(2).neighbors().add(bfsNodes.get(3));
        bfsNodes.get(3).neighbors().add(bfsNodes.get(4));
        bfsNodes.get(4).neighbors().add(bfsNodes.get(5));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(6));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(9));

        bfsNodes.get(6).neighbors().add(bfsNodes.get(7));
        bfsNodes.get(7).neighbors().add(bfsNodes.get(8));
        bfsNodes.get(8).neighbors().add(bfsNodes.get(9));

        var start = bfsNodes.get(2);
        var end = bfsNodes.get(10);

        var paths = BFS.bfs(Set.of(start), Set.of(end), false);
        System.out.println(paths);

        assert paths.isEmpty();
    }

    @Test
    public void singleStartNoGoalPathFindAll() {

        List<BFSNode<Integer>> bfsNodes = new ArrayList<>();
        for(int i = 0; i < 11; i++) {
            bfsNodes.add(new BFSNode<>(i));
        }

        bfsNodes.get(2).neighbors().add(bfsNodes.get(3));
        bfsNodes.get(3).neighbors().add(bfsNodes.get(4));
        bfsNodes.get(4).neighbors().add(bfsNodes.get(5));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(6));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(9));

        bfsNodes.get(6).neighbors().add(bfsNodes.get(7));
        bfsNodes.get(7).neighbors().add(bfsNodes.get(8));
        bfsNodes.get(8).neighbors().add(bfsNodes.get(9));

        var start = bfsNodes.get(2);
        var end = bfsNodes.get(10);

        var paths = BFS.bfs(Set.of(start), Set.of(end), true);
        System.out.println(paths);

        assert paths.isEmpty();
    }

    @Test
    public void singleStartMultiGoalNoPathFindAll() {

        List<BFSNode<Integer>> bfsNodes = new ArrayList<>();
        for(int i = 0; i < 11; i++) {
            bfsNodes.add(new BFSNode<>(i));
        }

        bfsNodes.get(2).neighbors().add(bfsNodes.get(3));
        bfsNodes.get(3).neighbors().add(bfsNodes.get(4));
        bfsNodes.get(4).neighbors().add(bfsNodes.get(5));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(6));
        bfsNodes.get(5).neighbors().add(bfsNodes.get(9));

        bfsNodes.get(6).neighbors().add(bfsNodes.get(7));
        bfsNodes.get(7).neighbors().add(bfsNodes.get(8));
        bfsNodes.get(8).neighbors().add(bfsNodes.get(9));

        var start = bfsNodes.get(2);
        var end = bfsNodes.get(10);
        var end2 = bfsNodes.get(4);

        var paths = BFS.bfs(Set.of(start), Set.of(end, end2), true);
        System.out.println(paths);

        assert paths.size() == 1;
        assert paths.get(0).equals(Arrays.asList(bfsNodes.get(2), bfsNodes.get(3), bfsNodes.get(4)));
    }

    // TODO: multi start single goal
    // TODO: multi start multi goal
    // TODO: multi start multi goal seperated
}
