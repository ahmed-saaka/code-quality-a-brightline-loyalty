package com.brightline.loyalty;

/**
 * Defines the contract for calculating loyalty points
 * according to a membership tier.
 */
public interface TierStrategy {

    /**
     * Calculates points using the rules of a specific tier.
     *
     * @param basePoints the base points to calculate
     * @return the calculated loyalty points
     * @throws IllegalArgumentException if base points are negative
     */
    int calculatePoints(int basePoints);
}
