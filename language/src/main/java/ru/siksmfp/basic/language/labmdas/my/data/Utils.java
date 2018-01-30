package ru.siksmfp.basic.language.labmdas.my.data;

/**
 * @author Artem Karnov @date 17.02.2017.
 *         artem.karnov@t-systems.com
 */

public class Utils {
    public static Parent makeParent(Citizen person) {
        // TODO: 11.03.17 finish it
        Parent result = new Parent(person);
        result.changeParentStatus("GOOD");
        return result;
    }

}
