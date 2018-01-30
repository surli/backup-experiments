package test.java.saiu.algorithms.interview.code.wars;


import org.junit.Before;
import org.junit.Test;
import ru.siksmfp.basic.interview.code.wars.Abbreviator;

import static org.junit.Assert.assertEquals;

/**
 * @author Artem Karnov @date 07.03.17.
 *         artem.karnov@t-systems.com
 */
public class AbbreviatorTests {

    private Abbreviator abbr;

    @Before
    public void setUp() {
        abbr = new Abbreviator();

    }

    @Test
    public void testInternationalization() {
        assertEquals("i18n", abbr.abbreviate("internationalization"));
    }

    @Test
    public void testSentence() {
        assertEquals("e6t-r3s are r4y fun!", abbr.abbreviate("elephant-rides are really fun!"));
    }

    @Test
    public void testThrowTheKitchenSinkAtEm() {
        assertEquals("m[8c'sat: the: sat: ca]t", abbr.abbreviate("m[8c'sat: the: sat: ca]t"));
    }

    @Test
    public void testThrowTheKitchenSink() {
        assertEquals("t[he, the. s2s; b5n-on. s2s, d4e-b6d's2s'd4d]e-b6d",
                abbr.abbreviate("t[he, the. saas; bwedcsn-on. saas, deeeee-bfggthjd'skks'dfdvfd]e-brtteddd"));
    }

    @Test
    public void testLongSentence() {
        assertEquals("You n[2d, n2d not w2t, t]o c6e t2s c2e-w2s m5...>",
                abbr.abbreviate("You n[2d, need not what, t]o cookkeee tees crre-wvvs m5...>"));

    }

    @Test
    public void throwTheKitchenSinkAtEm() {
        assertEquals("... a. is: the, a, a: s[it]s",
                abbr.abbreviate("... a. is: the, a, a: s[it]s"));

    }
}