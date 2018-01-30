package test.java.saiu.algorithms.interview.code.wars;

import org.junit.Test;
import ru.siksmfp.basic.interview.code.wars.ExpandForm;

import static org.junit.Assert.assertEquals;

/**
 * @author Artem Karnov @date 28.08.2017.
 * artem.karnov@t-systems.com
 */
public class ExpandFormTest {
    @Test
    public void testSomething() {
        assertEquals("10 + 2", ExpandForm.calculate(12));
        assertEquals("40 + 2", ExpandForm.calculate(42));
        assertEquals("70000 + 300 + 4", ExpandForm.calculate(70304));
    }

}