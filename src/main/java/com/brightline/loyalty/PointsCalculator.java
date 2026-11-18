
package com.brightline.loyalty;

import java.math.BigDecimal;

public class PointsCalculator {

    // The calculator makes its own repository.
    private final MemberRepository repository = new InMemoryMemberRepository();

    public int calculatePoints(String memberId, BigDecimal amount) {

        // Validate purchase amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must not be null or negative");
        }

        Member member = repository.findById(memberId);

        // Check whether member exists
        if (member == null) {
            throw new IllegalArgumentException("Member not found");
        }

        // Base points: one point for every GHS 10 spent
        int points = amount.divide(BigDecimal.TEN).intValue();

        // Apply the member's tier multiplier
        if (member.getTier().equals("STANDARD")) {
            points = points * 1;

        } else if (member.getTier().equals("SILVER")) {

            BigDecimal multiplier = new BigDecimal("1.5");

            points = BigDecimal.valueOf(points)
                    .multiply(multiplier)
                    .intValue();

        } else if (member.getTier().equals("GOLD")) {
            points = points * 2;
        }

        // Loyalty bonus for big spenders
        if (amount.compareTo(BigDecimal.valueOf(500)) > 0) {
            points = points + 50;
        }

        System.out.println("Awarded " + points + " points to " + member.getName());

        return points;
    }
}
