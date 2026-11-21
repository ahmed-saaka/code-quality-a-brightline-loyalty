
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StandardTierTest {

    @Test
    void shouldReturnPointsWithoutChangingThem() {
        StandardTier standardTier = new StandardTier();

        int result = standardTier.calculatePoints(100);

        assertEquals(100, result);
    }

    @Test
    void shouldThrowExceptionWhenPointsAreNegative() {
        StandardTier standardTier = new StandardTier();

        assertThrows(
                IllegalArgumentException.class,
                () -> standardTier.calculatePoints(-10)
        );
    }

    @Test
    void shouldReturnZeroWhenPointsAreZero() {
        StandardTier standardTier = new StandardTier();

        int result = standardTier.calculatePoints(0);

        assertEquals(0, result);
    }
}

