package ru.siksmfp.basic.structure.tree.doubled.seacrh.tree;

import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Artem Karnov @date 08.12.16.
 * artem.karnov@t-systems.com
 **/
public class SearchTree<T> {
    private static long numberGenerator;
    private TreeList<T> root;

    public SearchTree() {
        root = null;
    }

    // TODO: 13.12.2016 Generic problem (any key and any data) 
    // TODO: 12.12.2016   I hate my code
    // TODO: 12.12.2016 Although i wrote more effective code than Lafore (without addition Note
    // variable)

    /**
     * Addition list after max element
     *
     * @param element element for adding
     */
    public void add(T element) {
        TreeList current = root, parent = root;
        while (current != null) {
            parent = current;
            current = current.getRightChildren();
        }
        parent.setRightChildren(new TreeList(parent.getKey() + 1, element));
    }

    /**
     * Addition list to the tree by part tree traversal
     *
     * @param key     key of element
     * @param element element for adding
     */
    public void add(long key, T element) {
        if (root == null) {
            root = new TreeList<T>(key, element);
        } else {
            TreeList currentList = root;
            while (true) {
                //choosing the branch of the tree (new key > current key -> we go to right branch )
                if (key > currentList.getKey()) {
                    if (currentList.getRightChildren() == null) {
                        //we come to end of the right branch
                        //we add element and finish this operation
                        currentList.setRightChildren(new TreeList(key, element));
                        return;
                    }
                    //it isn't end of the branch, so move on
                    currentList = currentList.getRightChildren();

                } else if (key < currentList.getKey()) {
                    if (currentList.getLeftChildren() == null) {
                        //we come to end of the left branch
                        //we add element and finish this operation
                        currentList.setLeftChildren(new TreeList(key, element));
                        return;
                    }
                    //it isn't end of the branch, so move on
                    currentList = currentList.getLeftChildren();

                } else {
                    throw new IncorrectSizeException("Element with key=" + key + " already exists!");
                }
            }
        }
    }

    /**
     * Searching list's data by key
     *
     * @param key list's key for searching
     * @return list's data if element exists, null if doesn't
     */
    public T find(long key) {
        TreeList currentList = root;
        while (currentList != null) {
            if (key == currentList.getKey())
                return (T) currentList.getData();
            else if (key > currentList.getKey()) {
                currentList = currentList.getRightChildren();
            } else {
                currentList = currentList.getLeftChildren();
            }
        }
        return null;
    }

    /**
     * Searching list's key by full tree traversal
     *
     * @param element element for adding
     * @return key of list if element exists in the tree, null if doesn't
     */
    public Long indexOf(T element) {
        TreeList currentList = root;
        while (currentList != null) {
            if (currentList.getData() == element)
                return null;
        }
        return null;
    }

    /**
     * Removing element by key
     *
     * @param key for element removing
     */
    public void remove(long key) {
        TreeList currentList = root, parent = root;
        boolean isElementFound = false;
        //Searching emelemt for deleting
        while (currentList != null) {
            if (currentList.getKey() == key) {
                isElementFound = true;
                break;
            } else if (key > currentList.getKey()) {
                parent = currentList;
                currentList = currentList.getRightChildren();
            } else if (key < currentList.getKey()) {
                parent = currentList;
                currentList = currentList.getLeftChildren();
            }
        }
        if (!isElementFound) {
            throw new IncorrectSizeException("Element with key=" + key + " wasn't found");
        } else {
            // TODO: 13.12.16 all logic for removing
            // TODO: 13.12.2016 fuck this shit! Rewrite the method
            //Element for deleting is found.
            // Element is the root
            if (currentList == root) {
                root = null;
            }
            //Element has no childrenk
            else if (numberOfChildren(currentList) == 0) {
                if (parent.getLeftChildren() == currentList)
                    parent.setLeftChildren(null);
                else parent.setRightChildren(null);
                //Element has only one children
            } else if (numberOfChildren(currentList) == 1) {
                TreeList tempList = null;
                if (currentList.getLeftChildren() != null)
                    tempList = currentList.getLeftChildren();
                else currentList.getRightChildren();

                if (parent.getLeftChildren() == currentList)
                    parent.setLeftChildren(tempList);
                else parent.setRightChildren(tempList);
            }
            // TODO: 21.12.2016 Lafore cussed out whole sub tree!
            //Element has 2 children
            else if (numberOfChildren(currentList) == 2) {
                TreeList successor = getSuccessor(currentList);
                TreeList rightChildren = currentList.getRightChildren();
                TreeList leftChildren = currentList.getLeftChildren();
                if (successor == leftChildren) {
                    successor.setRightChildren(rightChildren);
                } else if (successor == rightChildren) {
                    successor.setLeftChildren(leftChildren);
                } else {
                    successor.setLeftChildren(leftChildren);
                    successor.setRightChildren(rightChildren);
                }

                if (parent.getKey() < currentList.getKey())
                    parent.setRightChildren(successor);
                else parent.setLeftChildren(successor);
            }
        }
    }

    /**
     * Getting successor for deleting list with two children
     *
     * @param listForDeleting list for getting
     * @return successor
     */
    private TreeList getSuccessor(TreeList listForDeleting) {
        TreeList successorParent = listForDeleting;
        TreeList successor = listForDeleting;
        TreeList current = listForDeleting.getRightChildren();
        while (current != null) {
            successorParent = successor;
            successor = current;
            current = current.getLeftChildren();
        }
        if (successorParent != successor && successor != listForDeleting.getRightChildren())
            successorParent.setLeftChildren(null);
        return successor;
    }

    // TODO: 13.12.2016 @Vlad if you see it: it would be super if you can test private method like this!

    /**
     * Getting mount of list children
     *
     * @param list list for researching
     * @return mount of children
     */
    private short numberOfChildren(TreeList list) {
        if (list.getLeftChildren() == null && list.getRightChildren() == null)
            return 0;
            //if only one children (XOR)
        else if (list.getLeftChildren() == null ^ list.getRightChildren() == null) {
            return 1;
        } else return 2;
    }

    /**
     * Displaying full tree
     */
    public void showTree() {
        numberGenerator = 0;
        List<TreeList> rootOfTheTree = new ArrayList<TreeList>();
        rootOfTheTree.add(root);
        System.out.println("Root " + rootOfTheTree);
        System.out.println("----------------------------------------------");
        getChildrenLevel(rootOfTheTree);
    }

    // TODO: 12.12.2016 to remove last  empty level

    /**
     * Getting children of lists
     *
     * @param parentLevel array of lists
     * @return array of children for adjusted lists
     */
    public List<TreeList> getChildrenLevel(List<TreeList> parentLevel) {
        List<TreeList> childrenLevel = new ArrayList<TreeList>();
        for (TreeList currentList : parentLevel) {
            if (currentList.getLeftChildren() != null) {
                childrenLevel.add(currentList.getLeftChildren());
            }
            if (currentList.getRightChildren() != null) {
                childrenLevel.add(currentList.getRightChildren());
            }
        }
        if (childrenLevel.size() > 0) {
            System.out.println("Gen " + ++numberGenerator + "  " + childrenLevel);
            System.out.println("----------------------------------------------");
        }
        if (childrenLevel.size() > 0)
            return getChildrenLevel(childrenLevel);
        else return null;
    }

    /**
     * Displaying information of adjusted list
     *
     * @param list list for displaying
     */
    public void showListInfo(TreeList list) {
        System.out.println(list);
        System.out.println("Children " + list.getLeftChildren() + "   " + list.getRightChildren());
    }

    /**
     * Tree traversal in order
     */
    public void inOrderFullTraversal() {
        inOrderTraversal(root);
    }

    /**
     * Tree traversal in pre order
     */
    public void preOrderFullTraversal() {
        preOrderTraversal(root);
    }

    /**
     * Tree traversal in post order
     */
    public void postOrderFullTraversal() {
        postOrderTraversal(root);
    }

    /**
     * Recurrent mechanism for in order tree traversal
     *
     * @param currentList local root
     */
    private void inOrderTraversal(TreeList currentList) {
        if (currentList != null) {
            inOrderTraversal(currentList.getLeftChildren());
            System.out.println(currentList);
            inOrderTraversal(currentList.getRightChildren());
        }
    }

    /**
     * Recurrent mechanism for pre order tree traversal
     *
     * @param currentList local root
     */
    private void preOrderTraversal(TreeList currentList) {
        if (currentList != null) {
            System.out.println(currentList);
            preOrderTraversal(currentList.getLeftChildren());
            preOrderTraversal(currentList.getRightChildren());
        }
    }

    /**
     * Recurrent mechanism for post order tree traversal
     *
     * @param currentList local root
     */
    private void postOrderTraversal(TreeList currentList) {
        if (currentList != null) {
            postOrderTraversal(currentList.getLeftChildren());
            postOrderTraversal(currentList.getRightChildren());
            System.out.println(currentList);
        }
    }

    /**
     * Displaying tree in tree-view
     */
    public void displayTree() {
        Stack globalStack = new Stack();
        globalStack.push(root);
        int nBlanks = 32;
        boolean isRowEmpty = false;
        System.out.println("......................................................");
        while (isRowEmpty == false) {
            Stack localStack = new Stack();
            isRowEmpty = true;

            for (int j = 0; j < nBlanks; j++) System.out.print(' ');
            while (globalStack.isEmpty() == false) {
                TreeList temp = (TreeList) globalStack.pop();
                if (temp != null) {
                    System.out.print(temp.getData());
                    localStack.push(temp.getLeftChildren());
                    localStack.push(temp.getRightChildren());
                    if (temp.getLeftChildren() != null || temp.getLeftChildren() != null)
                        isRowEmpty = false;
                } else {
                    System.out.print("--");
                    localStack.push(null);
                    localStack.push(null);
                }
                for (int j = 0; j < nBlanks * 2 - 2; j++) System.out.print(' ');
            }
            System.out.println();
            nBlanks /= 2;
            while (localStack.isEmpty() == false) globalStack.push(localStack.pop());
        }
        System.out.println("......................................................");
    }
}