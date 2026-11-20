
package com.brightline.loyalty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SilverTier implements TierStrategy {

    private static final BigDecimal SILVER_MULTIPLIER =
            new BigDecimal("1.5");

    @Override
    public int calculatePoints(int points) {

        if (points < 0) {
            throw new IllegalArgumentException(
                    "Points cannot be negative");
        }

        return BigDecimal.valueOf(points)
                .multiply(SILVER_MULTIPLIER)
                .setScale(0, RoundingMode.DOWN)
                .intValueExact();
    }
}
