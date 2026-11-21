
package com.brightline.loyalty.app;

import java.math.BigDecimal;

import com.brightline.loyalty.MemberRepository;
import com.brightline.loyalty.infrastructure.InMemoryMemberRepository;

public class Demo {

    public static void main(String[] args) {

        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);

        System.out.println("Result m1 (STANDARD), GHS 100 -> "
                + calculator.calculatePoints(
                        "m1", new BigDecimal("100.00")));

        System.out.println("Result m2 (GOLD), GHS 250 -> "
                + calculator.calculatePoints(
                        "m2", new BigDecimal("250.00")));

        System.out.println("Result m3 (SILVER), GHS 500 -> "
                + calculator.calculatePoints(
                        "m3", new BigDecimal("500.00")));

        try {
            calculator.calculatePoints(
                    "unknown", new BigDecimal("100.00"));

        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }
}
