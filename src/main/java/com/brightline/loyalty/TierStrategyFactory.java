
package com.brightline.loyalty;

import java.util.Locale;

public class TierStrategyFactory {

    public TierStrategy getStrategy(String tier) {

        if (tier == null || tier.isBlank()) {
            throw new IllegalArgumentException(
                    "Enter a valid membership tier: " + tier);
        }

        switch (tier.toUpperCase(Locale.ROOT)) {

            case "STANDARD":
                return new StandardTier();

            case "SILVER":
                return new SilverTier();

            case "GOLD":
                return new GoldTier();

            default:
                throw new IllegalArgumentException(
                        "Unsupported membership tier: " + tier);
        }
    }
}
