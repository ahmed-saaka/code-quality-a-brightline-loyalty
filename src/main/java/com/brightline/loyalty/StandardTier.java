package com.brightline.loyalty;

public class StandardTier implements TierStrategy {

    @Override
    public int calculatePoints(int basePoints) {
        if (basePoints < 0) {
            throw new IllegalArgumentException(
                    "Points cannot be negative");
        }

        return basePoints;
    }
}
