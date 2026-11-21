
package com.brightline.loyalty;

import java.util.Optional;

import com.brightline.loyalty.infrastructure.InMemoryMemberRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryMemberRepositoryTest {

    @Test
    void savesNewMemberSuccessfully() {
        MemberRepository repository = new InMemoryMemberRepository();

        Member member = new Member(
                "m4", "John Doe", MembershipTier.STANDARD);

        repository.save(member);

        assertEquals(Optional.of(member), repository.findById("m4"));
    }

    @Test
    void findsExistingMemberById() {
        MemberRepository repository = new InMemoryMemberRepository();

        Member member = repository.findById("m1").orElseThrow();

        assertEquals("m1", member.getId());
        assertEquals("Kofi Mensah", member.getName());
    }

    @Test
    void returnsEmptyOptionalWhenMemberDoesNotExist() {
        MemberRepository repository = new InMemoryMemberRepository();

        assertTrue(repository.findById("m999").isEmpty());
    }

    @Test
    void rejectsNullMemberId() {
        MemberRepository repository = new InMemoryMemberRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findById(null)
        );
    }

    @Test
    void rejectsEmptyMemberId() {
        MemberRepository repository = new InMemoryMemberRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findById("")
        );
    }

    @Test
    void rejectsWhitespaceMemberId() {
        MemberRepository repository = new InMemoryMemberRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findById("   ")
        );
    }

    @Test
    void rejectsDuplicateMemberId() {
        MemberRepository repository = new InMemoryMemberRepository();

        Member duplicateMember = new Member(
                "m1", "John Doe", MembershipTier.GOLD);

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(duplicateMember)
        );
    }

    @Test
    void rejectsNullMember() {
        MemberRepository repository = new InMemoryMemberRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );
    }
}
