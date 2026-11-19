
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    
    @ExtendWith(MockitoExtension.class)
    @Mock
    MemberRepository mockRepository;
    @InjectMocks 
    PointsCalculator calculator;
    @Test
    void testCalculatePointsWithMock() {
        // Set up the mock repository to return a specific member
        Member mockMember = new Member("m1", "John Doe", "STANDARD");
        
        when(mockRepository.findById("m1")).thenReturn(mockMember);

        // Call the method under test
        int points = calculator.calculatePoints("m1", new BigDecimal("100.00"));

        // Verify the result
        assertEquals(10, points);
        verify(mockRepository, times(1)).findById("m1");
    }

    @Test
void doesNotAwardBonusForPurchaseOfExactly500() {
    // Arrange
    MemberRepository repository = new InMemoryMemberRepository();
    PointsCalculator calculator = new PointsCalculator(repository);

    // Act
    int points = calculator.calculatePoints(
            "m1", new BigDecimal("500.00"));

    // Assert
    assertEquals(50, points);
}

@Test
void awardsBonusForPurchaseAbove500() {
    // Arrange
    MemberRepository repository = new InMemoryMemberRepository();
    PointsCalculator calculator = new PointsCalculator(repository);

    // Act
    int points = calculator.calculatePoints(
            "m1", new BigDecimal("500.01"));

    // Assert
    assertEquals(100, points);
}


}
