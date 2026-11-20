
package com.brightline.loyalty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PointsCalculator {

    // Repository dependency supplied through the constructor.
    private final MemberRepository repository;
    private final TierStrategyFactory strategyFactory =
        new TierStrategyFactory();

    public PointsCalculator(MemberRepository repository) {
        this.repository = repository;
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Purchase amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Purchase amount must be greater than zero");
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

        points = applyBonus(amount, points);

        return points;
    }

    private int applyTierMultiplier(int points, String tier) {

    TierStrategy strategy = strategyFactory.getStrategy(tier);

    return strategy.calculatePoints(points);
}

    private int applyBonus(BigDecimal amount, int points) {

        if (amount.compareTo(BigDecimal.valueOf(500)) > 0) {
            points = Math.addExact(points, 50);
        }

        return points;
    }

    private int calculateBasePoints(BigDecimal amount) {

        return amount.divideToIntegralValue(BigDecimal.TEN)
                .intValueExact();
    }

   
}
