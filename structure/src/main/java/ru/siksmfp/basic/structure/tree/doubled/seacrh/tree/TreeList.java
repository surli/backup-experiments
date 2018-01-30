package ru.siksmfp.basic.structure.tree.doubled.seacrh.tree;

/**
 * @author Artem Tarnov @date 08.12.16.
 *         artem.karnov@t-systems.com
 **/
public class TreeList<T> {
    private long key;
    private T data;
    private TreeList<T> leftChildren;
    private TreeList<T> rightChildren;

    public TreeList(long key, T data) {
        this.key = key;
        this.data = data;
        this.rightChildren = null;
        this.leftChildren = null;
    }

    public T getData() {
        return data;
    }

    public long getKey() {
        return key;
    }

    public TreeList getLeftChildren() {
        return leftChildren;
    }

    public void setLeftChildren(TreeList<T> leftChildren) {
        this.leftChildren = leftChildren;
    }

    public TreeList getRightChildren() {
        return rightChildren;
    }

    public void setRightChildren(TreeList<T> rightChildren) {
        this.rightChildren = rightChildren;
    }

    public String toString() {
        return "{ " + key + ") " + data + " }";
    }

}
