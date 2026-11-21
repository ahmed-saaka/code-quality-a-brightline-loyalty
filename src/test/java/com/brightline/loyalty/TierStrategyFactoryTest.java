
package com.brightline.loyalty;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TierStrategyFactoryTest {

    private final TierStrategyFactory factory =
            new TierStrategyFactory();

    @Test
    void shouldReturnStandardTier() {
        TierStrategy strategy =
                factory.getStrategy(MembershipTier.STANDARD);

        assertInstanceOf(StandardTier.class, strategy);
    }

    @Test
    void shouldReturnSilverTier() {
        TierStrategy strategy =
                factory.getStrategy(MembershipTier.SILVER);

        assertInstanceOf(SilverTier.class, strategy);
    }

    @Test
    void shouldReturnGoldTier() {
        TierStrategy strategy =
                factory.getStrategy(MembershipTier.GOLD);

        assertInstanceOf(GoldTier.class, strategy);
    }

    @ParameterizedTest
    @EnumSource(MembershipTier.class)
    void shouldReturnNonNullStrategyForEveryTier(MembershipTier tier) {
        assertNotNull(factory.getStrategy(tier));
    }

    @Test
    void shouldRejectNullTier() {
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy(null)
        );
    }
}
