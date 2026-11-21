package com.brightline.loyalty;

import java.math.BigDecimal;
import java.util.Optional;
import com.brightline.loyalty.app.PointsCalculator;
import com.brightline.loyalty.infrastructure.InMemoryMemberRepository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class PointsCalculatorTest {

    @Test
    void awardsPointsToAStandardMember() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertEquals(
                10,
                calculator.calculatePoints("m1", new BigDecimal("100.00")));
    }

    @Test
    void calculatesGoldPoints() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertEquals(
                50,
                calculator.calculatePoints("m2", new BigDecimal("250.00")));
    }

    @Test
    void calculatesSilverPoints() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertEquals(
                30,
                calculator.calculatePoints("m3", new BigDecimal("200.00")));
    }

    @Test
    void throwsExceptionForUnknownMember() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePoints(
                        "unknown", new BigDecimal("100.00")));
    }

    @Test
    void rejectsNullAmountBeforeLookingUpMember() {
        PointsCalculator calculator =
                new PointsCalculator(repositoryThatFailsIfLookedUp());

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePoints("m1", null));
    }

    @Test
    void rejectsZeroAmountBeforeLookingUpMember() {
        PointsCalculator calculator =
                new PointsCalculator(repositoryThatFailsIfLookedUp());

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePoints("m1", BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeAmountBeforeLookingUpMember() {
        PointsCalculator calculator =
                new PointsCalculator(repositoryThatFailsIfLookedUp());

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePoints(
                        "m1", BigDecimal.valueOf(-100)));
    }

    @Test
    void rejectsNullMemberIdBeforeLookingUpMember() {
        PointsCalculator calculator =
                new PointsCalculator(repositoryThatFailsIfLookedUp());

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePoints(
                        null, new BigDecimal("100")));
    }

    @Test
    void rejectsBlankMemberIdBeforeLookingUpMember() {
        PointsCalculator calculator =
                new PointsCalculator(repositoryThatFailsIfLookedUp());

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePoints(
                        "   ", new BigDecimal("100")));
    }

    @Test
    void appliesNoBonusAtExactlyFiveHundred() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertEquals(
                50,
                calculator.calculatePoints("m1", new BigDecimal("500.00")));
    }

    @Test
    void appliesBonusAboveFiveHundred() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertEquals(
                100,
                calculator.calculatePoints("m1", new BigDecimal("500.01")));
    }

    @Test
    void appliesNoBonusBelowFiveHundred() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertEquals(
                49,
                calculator.calculatePoints("m1", new BigDecimal("499.99")));
    }

    @Test
    void roundsFractionalSilverPointsDown() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertEquals(
                7,
                calculator.calculatePoints("m3", new BigDecimal("50")));
    }

    @Test
    void detectsOverflowWhenConvertingBasePoints() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertThrows(
                ArithmeticException.class,
                () -> calculator.calculatePoints(
                        "m1", new BigDecimal("21474836480")));
    }

    @Test
    void detectsOverflowWhenAddingTheBonus() {
        PointsCalculator calculator =
                new PointsCalculator(new InMemoryMemberRepository());

        assertThrows(
                ArithmeticException.class,
                () -> calculator.calculatePoints(
                        "m1", new BigDecimal("21474836470")));
    }

    @Test
    void usesTheInjectedRepository() {
        InMemoryMemberRepository repository =
                new InMemoryMemberRepository();
        repository.save(new Member(
                "custom", "John Doe", MembershipTier.STANDARD));
        PointsCalculator calculator = new PointsCalculator(repository);

        assertEquals(
                10,
                calculator.calculatePoints(
                        "custom", new BigDecimal("100.00")));
    }

    @Test
    void requiresARepository() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PointsCalculator(null));

        assertEquals("MemberRepository cannot be null", exception.getMessage());
    }

    @Test
    void requiresATierStrategyFactory() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PointsCalculator(
                        new InMemoryMemberRepository(), null));

        assertEquals(
                "TierStrategyFactory cannot be null",
                exception.getMessage());
    }

    private MemberRepository repositoryThatFailsIfLookedUp() {
        return new MemberRepository() {
            @Override
            public Optional<Member> findById(String id) {
                throw new AssertionError(
                        "Invalid amounts must fail before lookup");
            }

            @Override
            public void save(Member member) {
                throw new AssertionError("No save is expected in this test");
            }
        };
    }
}
