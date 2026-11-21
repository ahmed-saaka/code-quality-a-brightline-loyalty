
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoldTierTest {

    private final GoldTier goldTier = new GoldTier();

    @Test
    void doublesPoints() {
        assertEquals(200, goldTier.calculatePoints(100));
    }

    @Test
    void rejectsNegativePoints() {
        assertThrows(
                IllegalArgumentException.class,
                () -> goldTier.calculatePoints(-1));
    }

    @Test
    void detectsOverflow() {
        assertThrows(
                ArithmeticException.class,
                () -> goldTier.calculatePoints(Integer.MAX_VALUE));
    }

    @Test
    void acceptsZeroPoints() {
        assertEquals(0, goldTier.calculatePoints(0));
    }
}
