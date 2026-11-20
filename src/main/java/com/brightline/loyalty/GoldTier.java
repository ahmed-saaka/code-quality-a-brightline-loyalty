package com.brightline.loyalty;

public class GoldTier implements TierStrategy {

    @Override
    public int calculatePoints(int points) {
        return Math.multiplyExact(points, 2);
    }
}