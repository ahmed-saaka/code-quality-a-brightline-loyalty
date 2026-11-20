package com.brightline.loyalty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SilverTier implements TierStrategy {
    @Override
    public int calculatePoints(int points) {
          BigDecimal multiplier = new BigDecimal("1.5");

            return BigDecimal.valueOf(points)
                    .multiply(multiplier)
                    .setScale(0, RoundingMode.DOWN)
                    .intValueExact();
       
    }
    
}
