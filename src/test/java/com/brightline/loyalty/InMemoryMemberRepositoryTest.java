
package com.brightline.loyalty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryMemberRepositoryTest {

    @Test
    void savesNewMemberSuccessfully() {
        MemberRepository repository = new InMemoryMemberRepository();

        Member member = new Member(
                "m4", "John Doe", "STANDARD");

        repository.save(member);

        assertEquals(member, repository.findById("m4"));
    }

    @Test
    void findsExistingMemberById() {
        MemberRepository repository = new InMemoryMemberRepository();

        Member member = repository.findById("m1");

        assertNotNull(member);
        assertEquals("m1", member.getId());
        assertEquals("Kofi Mensah", member.getName());
    }

    @Test
    void returnsNullWhenMemberDoesNotExist() {
        MemberRepository repository = new InMemoryMemberRepository();

        Member member = repository.findById("m999");

        assertNull(member);
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
                "m1", "John Doe", "GOLD");

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
