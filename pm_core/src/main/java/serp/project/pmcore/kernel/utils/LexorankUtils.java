/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.utils;

/**
 * Simple lexicographic rank utility for ordering work items.
 *
 * <p>This implementation is intentionally lightweight for the current PM Core
 * use cases. It guarantees monotonic append-at-tail generation and provides a
 * best-effort midpoint generator for future reorder operations. It is not a
 * full Jira LexoRank implementation.
 */
public final class LexorankUtils {

    private static final char MIN_CHAR = 'a';
    private static final char MAX_CHAR = 'z';
    private static final char MID_CHAR = 'n';
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
        validateRank(rank);

        char[] chars = rank.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] < MAX_CHAR) {
                chars[i] = (char) (chars[i] + 1);
                for (int j = i + 1; j < chars.length; j++) {
                    chars[j] = MIN_CHAR;
                }
                return new String(chars);
            }
        }

        return rank + MIN_CHAR;
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

        validateRank(before);
        validateRank(after);
        if (before.compareTo(after) >= 0) {
            throw new IllegalArgumentException("'before' rank must be lower than 'after' rank");
        }

        String midpoint = tryGenerateMidpoint(before, after);
        if (midpoint != null) {
            return midpoint;
        }

        String fallback = before + MID_CHAR;
        if (fallback.compareTo(after) < 0) {
            return fallback;
        }

        throw new IllegalArgumentException(
                "Cannot generate midpoint between adjacent ranks using simplified lexicographic strategy"
        );
    }

    private static String generateRankBefore(String rank) {
        if (rank == null || rank.isEmpty()) {
            return INITIAL_RANK;
        }
        validateRank(rank);

        char[] chars = rank.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] > MIN_CHAR) {
                chars[i] = (char) (chars[i] - 1);
                for (int j = i + 1; j < chars.length; j++) {
                    chars[j] = MAX_CHAR;
                }
                return new String(chars);
            }
        }

        if (rank.length() > 1) {
            return rank.substring(0, rank.length() - 1);
        }

        throw new IllegalArgumentException("Cannot generate a rank before the minimum single-character rank");
    }

    private static String tryGenerateMidpoint(String before, String after) {
        StringBuilder prefix = new StringBuilder();

        for (int i = 0; ; i++) {
            int left = charToDigit(charAtOrZero(before, i));
            int right = charToDigit(charAtOrSentinel(after, i));

            if (right - left > 1) {
                int mid = (left + right) / 2;
                return prefix.append(digitToChar(mid)).toString();
            }

            if (left == right) {
                if (i >= before.length()) {
                    return null;
                }
                prefix.append(before.charAt(i));
                continue;
            }

            if (left == 0) {
                return null;
            }

            prefix.append(before.charAt(i));
        }
    }

    private static void validateRank(String rank) {
        for (int i = 0; i < rank.length(); i++) {
            char current = rank.charAt(i);
            if (current < MIN_CHAR || current > MAX_CHAR) {
                throw new IllegalArgumentException("Rank contains unsupported character: '" + current + "'");
            }
        }
    }

    private static char charAtOrZero(String rank, int index) {
        return index < rank.length() ? rank.charAt(index) : 0;
    }

    private static char charAtOrSentinel(String rank, int index) {
        return index < rank.length() ? rank.charAt(index) : '{';
    }

    private static int charToDigit(char value) {
        if (value == 0) {
            return 0;
        }
        if (value == '{') {
            return (MAX_CHAR - MIN_CHAR) + 2;
        }
        return (value - MIN_CHAR) + 1;
    }

    private static char digitToChar(int digit) {
        if (digit <= 0 || digit > (MAX_CHAR - MIN_CHAR) + 1) {
            throw new IllegalArgumentException("Digit is out of rank alphabet range: " + digit);
        }
        return (char) (MIN_CHAR + digit - 1);
    }
}
