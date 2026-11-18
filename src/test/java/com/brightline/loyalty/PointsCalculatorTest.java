
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PointsCalculatorTest {

    @Test
    void awardsPointsToAMember() {
        PointsCalculator calculator = new PointsCalculator();

        int points = calculator.calculatePoints(
                "m1", new BigDecimal("100.00"));

        assertEquals(10, points);
    }

    @Test
    void calculatesGoldPoints() {
        PointsCalculator calculator = new PointsCalculator();

        int points = calculator.calculatePoints(
                "m2", new BigDecimal("250.00"));

        assertEquals(50, points);
    }

    @Test
    void awardsPointsForAPurchase() {
        PointsCalculator calculator = new PointsCalculator();

        int points = calculator.calculatePoints(
                "m3", new BigDecimal("200.00"));

        assertEquals(30, points);
    }

    @Test
    void throwsExceptionForUnknownMember() {
        PointsCalculator calculator = new PointsCalculator();

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculatePoints(
                    "unknown", new BigDecimal("100.00"));
        });
    }

    @Test
    void throwsExceptionForNegativeAmounts() {
        PointsCalculator calculator = new PointsCalculator();

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculatePoints(
                    "m1", new BigDecimal("-100.00"));
        });
    }

    @Test
    void throwsExceptionForNullAmount() {
        PointsCalculator calculator = new PointsCalculator();

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculatePoints("m1", null);
        });
    }
}
