package test.java.saiu.algorithms.interview.other.max.depth.binary.tree;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.siksmfp.basic.interview.other.MaxDepthBinaryTreeFinder;
import ru.siksmfp.basic.structure.tree.doubled.seacrh.tree.TreeList;

/**
 * @author Artem Karnov @date 25.02.17.
 * artem.karnov@t-systems.com
 */

public class MaxDepthBinaryTreeFinderTest {
    private MaxDepthBinaryTreeFinder maxDepthBinaryTreeFinder;
    private TreeList<Integer> tree;

    @Before
    public void setUp() {
        maxDepthBinaryTreeFinder = new MaxDepthBinaryTreeFinder();
    }

    //                     5
    //                  4     6
    public TreeList<Integer> generateFirstTree() {
        tree = new TreeList<>(5, 5);
        tree.setLeftChildren(new TreeList<>(4, 4));
        tree.setRightChildren(new TreeList<>(6, 6));

        return tree;
    }

    //                     3
    //                  2     6
    //                     5     8
    //                   4
    public TreeList<Integer> generateSecondTree() {
        TreeList<Integer> right;
        TreeList<Integer> left;

        tree = new TreeList<>(3, 3);
        tree.setLeftChildren(new TreeList<>(2, 2));
        tree.setRightChildren(new TreeList<>(6, 6));
        right = tree.getRightChildren();
        right = tree.getLeftChildren();

        right.setLeftChildren(new TreeList<>(5, 5));
        right.setRightChildren(new TreeList<>(8, 8));

        left = right.getLeftChildren();
        left.setLeftChildren(new TreeList<>(4, 4));

        return tree;
    }

    //                     3
    //                  2     6
    //                     5     8
    //                   4         10
    public TreeList<Integer> generateThirdTree() {
        TreeList<Integer> right;
        TreeList<Integer> left;

        tree = new TreeList<>(3, 3);
        tree.setLeftChildren(new TreeList<>(2, 2));
        tree.setRightChildren(new TreeList<>(6, 6));
        right = tree.getRightChildren();
        right = tree.getLeftChildren();

        right.setLeftChildren(new TreeList<>(5, 5));
        right.setRightChildren(new TreeList<>(8, 8));

        left = right.getLeftChildren();
        left.setLeftChildren(new TreeList<>(4, 4));

        right = right.getRightChildren();
        right.setRightChildren(new TreeList<>(10, 10));

        return tree;
    }

    @Test
    public void getMaxDepthTestOne() throws Exception {
        tree = generateFirstTree();
        Assert.assertEquals(2, maxDepthBinaryTreeFinder.getMaxDepth(tree));
    }

    @Test
    public void getMaxDepthTestTwo() throws Exception {
        tree = generateSecondTree();
        Assert.assertEquals(4, maxDepthBinaryTreeFinder.getMaxDepth(tree));
    }

    @Test
    public void getMaxDepthTestThree() throws Exception {
        tree = generateThirdTree();
        Assert.assertEquals(4, maxDepthBinaryTreeFinder.getMaxDepth(tree));
    }

}