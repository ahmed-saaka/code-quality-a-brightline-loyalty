
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PointsCalculatorTest {

    @Test
    void awardsPointsToAMember() {
        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);

        int points = calculator.calculatePoints(
                "m1", new BigDecimal("100.00"));

        assertEquals(10, points);
    }

    @Test
    void calculatesGoldPoints() {
        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);

        int points = calculator.calculatePoints(
                "m2", new BigDecimal("250.00"));

        assertEquals(50, points);
    }

    @Test
    void awardsPointsForAPurchase() {
        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);

        int points = calculator.calculatePoints(
                "m3", new BigDecimal("200.00"));

        assertEquals(30, points);
    }

    @Test
    void throwsExceptionForUnknownMember() {
        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculatePoints(
                    "unknown", new BigDecimal("100.00"));
        });
    }

    @Test
    void throwsExceptionForNegativeAmounts() {
        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculatePoints(
                    "m1", new BigDecimal("-100.00"));
        });
    }

    @Test
    void throwsExceptionForNullAmount() {
        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculatePoints("m1", null);
        });
    }
}
