
package com.brightline.loyalty.app;

import java.math.BigDecimal;
import java.util.Objects;

import com.brightline.loyalty.Member;
import com.brightline.loyalty.MemberRepository;
import com.brightline.loyalty.MembershipTier;
import com.brightline.loyalty.TierStrategy;
import com.brightline.loyalty.TierStrategyFactory;

public class PointsCalculator {

    private static final BigDecimal BONUS_THRESHOLD =
            BigDecimal.valueOf(500);
    private static final int BONUS_POINTS = 50;
    private static final BigDecimal AMOUNT_PER_BASE_POINT =
            BigDecimal.TEN;

    private final MemberRepository repository;
    private final TierStrategyFactory strategyFactory;

    public PointsCalculator(MemberRepository repository) {
        this(repository, new TierStrategyFactory());
    }

    public PointsCalculator(
            MemberRepository repository,
            TierStrategyFactory strategyFactory) {
        this.repository = Objects.requireNonNull(
                repository, "MemberRepository cannot be null");
        this.strategyFactory = Objects.requireNonNull(
                strategyFactory, "TierStrategyFactory cannot be null");
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
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException(
                    "Member ID cannot be null or empty");
        }

        return repository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    public int calculatePoints(String memberId, BigDecimal amount) {
        validateAmount(amount);

        Member member = findMember(memberId);

        int points = calculateBasePoints(amount);

        points = applyTierMultiplier(points, member.getTier());

        points = applyBonus(amount, points);

        return points;
    }

    private int applyTierMultiplier(int points, MembershipTier tier) {
        TierStrategy strategy = strategyFactory.getStrategy(tier);
        return strategy.calculatePoints(points);
    }

    private int applyBonus(BigDecimal amount, int points) {
        if (amount.compareTo(BONUS_THRESHOLD) > 0) {
            return Math.addExact(points, BONUS_POINTS);
        }

        return points;
    }

    private int calculateBasePoints(BigDecimal amount) {
        return amount.divideToIntegralValue(AMOUNT_PER_BASE_POINT)
                .intValueExact();
    }
}
