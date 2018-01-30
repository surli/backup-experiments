package test.java.saiu.algorithms.interview.other;

import org.junit.Ignore;
import org.junit.Test;
import ru.siksmfp.basic.interview.other.LinkCutter;

import static org.junit.Assert.assertEquals;

/**
 * @author Artem Karnov @date 16.06.2017.
 *         artem.karnov@t-systems.com
 */
@Ignore
public class LinkCutterTest {

    @Test
    public void firstTest() {
        String fullLink = "ASDFGHasdfg";
        String shortLink = LinkCutter.getShortLink(fullLink);
        String newFullLink = LinkCutter.getFullQualifiedLink(shortLink);
        assertEquals(fullLink, newFullLink);
    }


    @Test
    public void secondTest() {
        String fullLink = "ASDFGHasdkj2  h234hg2h4g2   09089834hu2!@#g34gyh23y4uf234f23f4u2t3f4tfg";
        String shortLink = LinkCutter.getShortLink(fullLink);
        String newFullLink = LinkCutter.getFullQualifiedLink(shortLink);
        assertEquals(fullLink, newFullLink);
    }


    @Test
    public void thirdTest() {
        String shortLink = "TODO_PUT_LINK";
        String expectedFullLink = "TODO_PUT_LINK";
        String newFullLink = LinkCutter.getFullQualifiedLink(shortLink);
        assertEquals(expectedFullLink, newFullLink);
    }


    @Test
    public void fourthTest() {
        String shortLink = "NewTODO_PUT_LINK";
        String expectedFullLink = "NewTODO_PUT_LINK";
        String newFullLink = LinkCutter.getFullQualifiedLink(shortLink);
        assertEquals(expectedFullLink, newFullLink);
    }

    @Test
    public void fifthTest() {
        String fullLink = null;
        String shortLink = LinkCutter.getShortLink(fullLink);
        String newFullLink = LinkCutter.getFullQualifiedLink(shortLink);
        assertEquals(fullLink, newFullLink);
    }


    @Test
    public void sixthTest() {
        String shortLink = null;
        String expectedFullLink = null;
        String newFullLink = LinkCutter.getFullQualifiedLink(shortLink);
        assertEquals(expectedFullLink, newFullLink);
    }

}