
package com.brightline.loyalty;

public class GoldTier implements TierStrategy {

    private static final int GOLD_MULTIPLIER = 2;

    @Override
    public int calculatePoints(int points) {

        if (points < 0) {
            throw new IllegalArgumentException(
                    "Points cannot be negative");
        }

        return Math.multiplyExact(points, GOLD_MULTIPLIER);
    }
}
