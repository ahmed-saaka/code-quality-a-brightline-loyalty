
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SilverTierTest {

    private final SilverTier silverTier = new SilverTier();

    @Test
    void shouldMultiplyPointsByOnePointFive() {
        int result = silverTier.calculatePoints(100);

        assertEquals(150, result);
    }

    @Test
    void shouldCalculateFractionalPointsAndRoundDown() {
        int result = silverTier.calculatePoints(11);

        assertEquals(16, result);
    }

    @Test
    void shouldReturnZeroWhenPointsAreZero() {
        int result = silverTier.calculatePoints(0);

        assertEquals(0, result);
    }

    @Test
    void shouldRejectNegativePoints() {
        assertThrows(
                IllegalArgumentException.class,
                () -> silverTier.calculatePoints(-10)
        );
    }
}
