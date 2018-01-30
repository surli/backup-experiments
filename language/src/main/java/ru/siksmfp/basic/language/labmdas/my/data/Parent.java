package ru.siksmfp.basic.language.labmdas.my.data;

import java.util.Collection;

/**
 * @author Artem Karnov @date 17.02.2017.
 * artem.karnov@t-systems.com
 */
public class Parent {
    private Citizen citizen;
    private Collection<Citizen> child;
    private ParentStatus parentStatus;

    public Parent(Citizen parent) {
        this.citizen = parent;
    }

    public void addChildren(Citizen children) {
        child.add(children);
        parentStatus = ParentStatus.GOOD;
    }

    public void changeParentStatus(String status) {
        try {
            parentStatus = ParentStatus.valueOf(status);
        } catch (Exception ex) {
        }
    }

    private enum ParentStatus {
        GOOD, BAD, QUESTIONED
    }
}
