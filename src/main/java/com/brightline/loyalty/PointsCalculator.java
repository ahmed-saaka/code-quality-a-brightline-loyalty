
package com.brightline.loyalty;

import java.math.BigDecimal;
import java.util.Objects;

public class PointsCalculator {

    // Business rule constants
    private static final BigDecimal BONUS_THRESHOLD =
            BigDecimal.valueOf(500);

    private static final int BONUS_POINTS = 50;

    private static final BigDecimal AMOUNT_PER_BASE_POINT =
            BigDecimal.TEN;

    // Dependencies
    private final MemberRepository repository;

    private final TierStrategyFactory strategyFactory =
            new TierStrategyFactory();

    public PointsCalculator(MemberRepository repository) {
        this.repository = Objects.requireNonNull(
                repository, "MemberRepository cannot be null");
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

        if (amount.compareTo(BONUS_THRESHOLD) > 0) {
            points = Math.addExact(points, BONUS_POINTS);
        }

        return points;
    }

    private int calculateBasePoints(BigDecimal amount) {

        return amount.divideToIntegralValue(AMOUNT_PER_BASE_POINT)
                .intValueExact();
    }
}
