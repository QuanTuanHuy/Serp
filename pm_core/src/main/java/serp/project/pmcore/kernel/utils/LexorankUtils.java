/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.utils;

/**
 * Simple Lexorank utility for generating lexicographic rank strings.
 * Uses mid-point string approach for ordering work items.
 */
public final class LexorankUtils {

    private static final String INITIAL_RANK = "aaa";

    private LexorankUtils() {
    }

    /**
     * Generate the initial rank for a new work item when no other items exist.
     */
    public static String generateInitialRank() {
        return INITIAL_RANK;
    }

    /**
     * Generate a rank that comes after the given rank (append to end).
     */
    public static String generateRankAfter(String rank) {
        if (rank == null || rank.isEmpty()) {
            return INITIAL_RANK;
        }
        char lastChar = rank.charAt(rank.length() - 1);
        if (lastChar < 'z') {
            return rank.substring(0, rank.length() - 1) + (char) (lastChar + 1);
        }
        return rank + "a";
    }

    /**
     * Generate a rank between two existing ranks.
     */
    public static String generateRankBetween(String before, String after) {
        if (before == null || before.isEmpty()) {
            return generateRankBefore(after);
        }
        if (after == null || after.isEmpty()) {
            return generateRankAfter(before);
        }

        int maxLen = Math.max(before.length(), after.length());
        String paddedBefore = padRight(before, maxLen, 'a');
        String paddedAfter = padRight(after, maxLen, 'a');

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxLen; i++) {
            char bc = paddedBefore.charAt(i);
            char ac = paddedAfter.charAt(i);
            if (bc == ac) {
                result.append(bc);
            } else if (ac - bc > 1) {
                result.append((char) (bc + (ac - bc) / 2));
                return result.toString();
            } else {
                result.append(bc);
                // Need to go deeper — append midpoint of next level
                result.append('n');
                return result.toString();
            }
        }
        // Strings are identical — extend
        return before + "n";
    }

    private static String generateRankBefore(String rank) {
        if (rank == null || rank.isEmpty()) {
            return INITIAL_RANK;
        }
        char firstChar = rank.charAt(0);
        if (firstChar > 'a') {
            return String.valueOf((char) (firstChar - 1)) + rank.substring(1);
        }
        return "a" + generateRankBefore(rank.substring(1));
    }

    private static String padRight(String s, int length, char padChar) {
        if (s.length() >= length) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }
}
