
package com.brightline.loyalty;

import java.math.BigDecimal;

public class Demo {

    public static void main(String[] args) {

        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);

        // STANDARD member
        System.out.println("Result m1 (STANDARD), GHS 100 -> "
                + calculator.calculatePoints(
                        "m1", new BigDecimal("100.00")));

        // GOLD member
        System.out.println("Result m2 (GOLD), GHS 250 -> "
                + calculator.calculatePoints(
                        "m2", new BigDecimal("250.00")));

        // SILVER member
        System.out.println("Result m3 (SILVER), GHS 500 -> "
                + calculator.calculatePoints(
                        "m3", new BigDecimal("500.00")));

        // Handle unknown member
        try {
            System.out.println("Result unknown member, GHS 100 -> "
                    + calculator.calculatePoints(
                            "unknown", new BigDecimal("100.00")));

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
