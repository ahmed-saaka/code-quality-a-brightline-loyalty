
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberTest {

    @Test
    void shouldCreateMemberWithValidDetails() {
        Member member =
                new Member("m1", "Kofi Mensah", MembershipTier.STANDARD);

        assertEquals("m1", member.getId());
        assertEquals("Kofi Mensah", member.getName());
        assertEquals(MembershipTier.STANDARD, member.getTier());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Member(null, "Kofi Mensah", MembershipTier.GOLD)
        );
    }

    @Test
    void shouldRejectEmptyId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Member("", "Kofi Mensah", MembershipTier.GOLD)
        );
    }

    @Test
    void shouldRejectWhitespaceId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Member("   ", "Kofi Mensah", MembershipTier.GOLD)
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Member("m1", null, MembershipTier.GOLD)
        );
    }

    @Test
    void shouldRejectEmptyName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Member("m1", "", MembershipTier.GOLD)
        );
    }

    @Test
    void shouldRejectWhitespaceName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Member("m1", "   ", MembershipTier.GOLD)
        );
    }

    @Test
    void shouldRejectNullTier() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Member("m1", "Kofi Mensah", null)
        );
    }
}
