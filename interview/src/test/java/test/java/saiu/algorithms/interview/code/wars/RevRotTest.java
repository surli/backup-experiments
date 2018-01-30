package test.java.saiu.algorithms.interview.code.wars;

import org.junit.Ignore;
import org.junit.Test;
import ru.siksmfp.basic.interview.code.wars.RevRot;

import static org.junit.Assert.assertEquals;

/**
 * @author Artem Karnov @date 10/27/2017.
 * artem.karnov@t-systems.com
 */
@Ignore
public class RevRotTest {

    @Test
    public void test1() {
        assertEquals(RevRot.revRot("1234", 0), "");
    }

    @Test
    public void test3() {
        assertEquals(RevRot.revRot("", 0), "");
    }

    @Test
    public void test4() {
        assertEquals(RevRot.revRot("1234", 5), "");
    }

    @Test
    public void test5() {
        assertEquals(RevRot.revRot("733049910872815764", 5), "330479108928157");
    }

    @Test
    public void test6() {
        assertEquals(RevRot.revRot("123456987654", 6), "234561876549");
    }

    @Test
    public void test7() {
        assertEquals(RevRot.revRot("123456987653", 6), "234561356789");
    }

    @Test
    public void test8() {
        assertEquals(RevRot.revRot("66443875", 4), "44668753");
    }

    @Test
    public void test9() {
        assertEquals(RevRot.revRot("66443875", 8), "64438756");
    }

    @Test
    public void test10() {
        assertEquals(RevRot.revRot("664438769", 8), "67834466");
    }

    @Test
    public void test11() {
        assertEquals(RevRot.revRot("123456779", 8), "23456771");
    }

    @Test
    public void test12() {
        assertEquals(RevRot.revRot("", 8), "");
    }

    @Test
    public void test13() {
        assertEquals(RevRot.revRot("123456779", 0), "");
    }

    @Test
    public void test14() {
        assertEquals(RevRot.revRot("563000655734469485", 4), "0365065073456944");
    }


}