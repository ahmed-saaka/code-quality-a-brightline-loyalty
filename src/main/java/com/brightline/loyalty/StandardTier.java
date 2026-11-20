
package com.brightline.loyalty;

public class StandardTier implements TierStrategy {

    @Override
    public int calculatePoints(int points) {

        if (points < 0) {
            throw new IllegalArgumentException(
                    "Points cannot be negative");
        }

        return points;
    }
}
