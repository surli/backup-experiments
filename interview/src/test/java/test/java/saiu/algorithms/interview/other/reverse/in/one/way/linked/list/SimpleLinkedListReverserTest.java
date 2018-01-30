package test.java.saiu.algorithms.interview.other.reverse.in.one.way.linked.list;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.siksmfp.basic.interview.other.LinkedListReverser;

import java.util.LinkedList;

/**
 * @author Artem Karnov @date 25.02.17.
 * artem.karnov@t-systems.com
 */

public class SimpleLinkedListReverserTest {

    private LinkedList<Integer> linkedList;
    private LinkedListReverser<Integer> reverser;
    private boolean result;

    @Before
    public void setUp() {
        reverser = new LinkedListReverser();
    }


    private LinkedList<Integer> getFirstLinkedList() {
        linkedList = new LinkedList<>();
        linkedList.add(10);
        linkedList.add(9);
        linkedList.add(8);
        linkedList.add(7);
        linkedList.add(6);
        linkedList.add(5);
        linkedList.add(4);
        linkedList.add(3);
        linkedList.add(2);
        linkedList.add(1);
        return linkedList;
    }

    private LinkedList<Integer> getFirstInvertedLinkedList() {
        linkedList = new LinkedList<>();
        linkedList.add(1);
        linkedList.add(2);
        linkedList.add(3);
        linkedList.add(4);
        linkedList.add(5);
        linkedList.add(6);
        linkedList.add(7);
        linkedList.add(8);
        linkedList.add(9);
        linkedList.add(10);
        return linkedList;
    }

    private LinkedList<Integer> getSecondLinkedList() {
        linkedList = new LinkedList<>();
        linkedList.add(-1);
        linkedList.add(1);
        linkedList.add(-2);
        linkedList.add(2);
        linkedList.add(-3);
        linkedList.add(3);
        linkedList.add(-4);
        linkedList.add(4);
        linkedList.add(-5);
        return linkedList;
    }

    private LinkedList<Integer> getSecondInvertedLinkedList() {
        linkedList = new LinkedList<>();
        linkedList.add(-5);
        linkedList.add(4);
        linkedList.add(-4);
        linkedList.add(3);
        linkedList.add(-3);
        linkedList.add(2);
        linkedList.add(-2);
        linkedList.add(1);
        linkedList.add(-1);
        return linkedList;
    }

    private LinkedList<Integer> getThirdLinkedList() {
        linkedList = new LinkedList<>();
        linkedList.add(-1);
        return linkedList;
    }

    private LinkedList<Integer> getThirdInvertedLinkedList() {
        linkedList = new LinkedList<>();
        linkedList.add(-1);
        return linkedList;
    }

    private LinkedList<Integer> getFourthLinkedList() {
        linkedList = new LinkedList<>();
        return linkedList;
    }

    private LinkedList<Integer> getFourthInvertedLinkedList() {
        linkedList = new LinkedList<>();
        return linkedList;
    }

    private boolean listComparator(LinkedList<Integer> first, LinkedList<Integer> second) {
        if (first.size() == second.size()) {
            for (int i = 0; i < first.size(); i++) {
                if (first.get(i) != second.get(i))
                    return false;
            }
        } else {
            return false;
        }
        return true;

    }

    @Test
    public void reverseLikedListTestOne() {
        result = listComparator(reverser.reverse(getFirstLinkedList()), getFirstInvertedLinkedList());
        Assert.assertTrue(result);
    }

    @Test
    public void reverseLikedListTestTwo() {
        result = listComparator(reverser.reverse(getSecondLinkedList()), getSecondInvertedLinkedList());
        Assert.assertTrue(result);
    }

    @Test
    public void reverseLikedListTestThree() {
        result = listComparator(reverser.reverse(getThirdLinkedList()), getThirdInvertedLinkedList());
        Assert.assertTrue(result);
    }

    @Test
    public void reverseLikedListTestFour() {
        result = listComparator(reverser.reverse(getFourthLinkedList()), getFourthInvertedLinkedList());
        Assert.assertTrue(result);
    }
}