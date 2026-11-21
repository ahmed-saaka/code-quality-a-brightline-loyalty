package com.brightline.loyalty.infrastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.brightline.loyalty.Member;
import com.brightline.loyalty.MemberRepository;
import com.brightline.loyalty.MembershipTier;

public class InMemoryMemberRepository implements MemberRepository {

    private final Map<String, Member> members = new HashMap<>();

    public InMemoryMemberRepository() {
        members.put("m1", new Member(
                "m1", "Kofi Mensah", MembershipTier.STANDARD));
        members.put("m2", new Member(
                "m2", "Ama Owusu", MembershipTier.GOLD));
        members.put("m3", new Member(
                "m3", "Yaa Boateng", MembershipTier.SILVER));
    }

    @Override
    public Optional<Member> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Member ID cannot be null or empty");
        }

        return Optional.ofNullable(members.get(id));
    }

    @Override
    public void save(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        String id = member.getId();

        if (members.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Member ID already exists: " + id);
        }

        members.put(id, member);
    }
}
