
package com.brightline.loyalty;

import java.math.BigDecimal;

public class PointsCalculator {

    // Repository dependency supplied through the constructor.
    private final MemberRepository repository;

    public PointsCalculator(MemberRepository repository) {
        this.repository = repository;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Amount must not be null or negative");
        }
    }

    private Member findMember(String memberId) {
        Member member = repository.findById(memberId);

        if (member == null) {
            throw new IllegalArgumentException("Member not found");
        }

        return member;
    }

    public int calculatePoints(String memberId, BigDecimal amount) {
        validateAmount(amount);
        Member member = findMember(memberId);
        int points = calculateBasePoints(amount);
        points = applyTierMultiplier(points, member.getTier());
       points=applyBonus(amount, points);
        
        return points;
    }

    private int  applyBonus(BigDecimal amount, int points){
         if (amount.compareTo(BigDecimal.valueOf(500)) > 0) {
            points = points + 50;
        }
        return points;
    }

    private int calculateBasePoints(BigDecimal amount) {
        int points = amount.divide(BigDecimal.TEN).intValue();
        return points;
    }

    private int applyTierMultiplier(int points, String tier) {
        if (tier.equals("STANDARD")) {
            points = points * 1;

        } else if (tier.equals("SILVER")) {
            BigDecimal multiplier = new BigDecimal("1.5");

            points = BigDecimal.valueOf(points)
                    .multiply(multiplier)
                    .intValue();

        } else if (tier.equals("GOLD")) {
            points = points * 2;
        }

        return points;
    }
}
