package serp.project.pmcore.kernel.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LexorankUtilsTest {

    @Test
    void generateRankAfterShouldStayLexicographicallyIncreasing() {
        assertEquals("aab", LexorankUtils.generateRankAfter("aaa"));
        assertEquals("aba", LexorankUtils.generateRankAfter("aaz"));
        assertEquals("baa", LexorankUtils.generateRankAfter("azz"));
        assertEquals("zzza", LexorankUtils.generateRankAfter("zzz"));
    }

    @Test
    void generateRankBetweenShouldReturnRankWithinBoundsWhenPossible() {
        String rank = LexorankUtils.generateRankBetween("aaa", "aab");
        assertTrue("aaa".compareTo(rank) < 0);
        assertTrue(rank.compareTo("aab") < 0);
    }

    @Test
    void generateRankBetweenShouldHandleOpenEndedBounds() {
        String rankAfter = LexorankUtils.generateRankBetween("azz", null);
        assertTrue("azz".compareTo(rankAfter) < 0);

        String rankBefore = LexorankUtils.generateRankBetween(null, "bbb");
        assertTrue(rankBefore.compareTo("bbb") < 0);
    }
}
