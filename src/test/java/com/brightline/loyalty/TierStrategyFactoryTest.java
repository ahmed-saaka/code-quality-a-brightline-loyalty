
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TierStrategyFactoryTest {

    private final TierStrategyFactory factory =
            new TierStrategyFactory();

    @Test
    void shouldReturnStandardTier() {
        TierStrategy strategy = factory.getStrategy("STANDARD");

        assertInstanceOf(StandardTier.class, strategy);
    }

    @Test
    void shouldReturnSilverTier() {
        TierStrategy strategy = factory.getStrategy("SILVER");

        assertInstanceOf(SilverTier.class, strategy);
    }

    @Test
    void shouldReturnGoldTier() {
        TierStrategy strategy = factory.getStrategy("GOLD");

        assertInstanceOf(GoldTier.class, strategy);
    }

    @Test
    void shouldAcceptLowercaseTier() {
        TierStrategy strategy = factory.getStrategy("gold");

        assertInstanceOf(GoldTier.class, strategy);
    }

    @Test
    void shouldAcceptMixedCaseTier() {
        TierStrategy strategy = factory.getStrategy("SiLvEr");

        assertInstanceOf(SilverTier.class, strategy);
    }

    @Test
    void shouldRejectNullTier() {
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy(null)
        );
    }

    @Test
    void shouldRejectEmptyTier() {
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy("")
        );
    }

    @Test
    void shouldRejectWhitespaceTier() {
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy("   ")
        );
    }

    @Test
    void shouldRejectUnsupportedTier() {
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy("PLATINUM")
        );
    }
}
