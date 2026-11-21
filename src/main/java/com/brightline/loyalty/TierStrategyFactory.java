package com.brightline.loyalty;

import java.util.Map;
import java.util.Objects;

public class TierStrategyFactory {

    private final Map<MembershipTier, TierStrategy> strategies;

    public TierStrategyFactory() {
        this(Map.of(
                MembershipTier.STANDARD, new StandardTier(),
                MembershipTier.SILVER, new SilverTier(),
                MembershipTier.GOLD, new GoldTier()));
    }

    public TierStrategyFactory(
            Map<MembershipTier, TierStrategy> strategies) {
        this.strategies = Map.copyOf(
                Objects.requireNonNull(strategies, "Strategies cannot be null"));
    }

    public TierStrategy getStrategy(MembershipTier tier) {
        if (tier == null) {
            throw new IllegalArgumentException("Membership tier cannot be null");
        }

        TierStrategy strategy = strategies.get(tier);
        if (strategy == null) {
            throw new IllegalStateException(
                    "No strategy registered for membership tier: " + tier);
        }

        return strategy;
    }
}
