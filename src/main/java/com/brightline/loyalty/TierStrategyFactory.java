
package com.brightline.loyalty;

public class TierStrategyFactory {

    public TierStrategy getStrategy(MembershipTier tier) {
        if (tier == null) {
            throw new IllegalArgumentException("Membership tier cannot be null");
        }

        return switch (tier) {
            case STANDARD -> new StandardTier();
            case SILVER -> new SilverTier();
            case GOLD -> new GoldTier();
        };
    }
}
