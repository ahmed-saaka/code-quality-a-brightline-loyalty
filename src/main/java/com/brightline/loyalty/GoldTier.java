package com.brightline.loyalty;

public class GoldTier implements TierStrategy {

    private static final int GOLD_MULTIPLIER = 2;

    @Override
    public int calculatePoints(int basePoints) {
        if (basePoints < 0) {
            throw new IllegalArgumentException(
                    "Points cannot be negative");
        }

        return Math.multiplyExact(basePoints, GOLD_MULTIPLIER);
    }
}
