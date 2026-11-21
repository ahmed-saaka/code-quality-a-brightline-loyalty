package com.brightline.loyalty;

public class Member {
    private final String id;
    private final String name;
    private final MembershipTier tier;

    public Member(String id, String name, MembershipTier tier) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
        this.id = id;
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (tier == null) {
            throw new IllegalArgumentException("tier cannot be null");
        }
        this.name = name;
        this.tier = tier;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MembershipTier getTier() {
        return tier;
    }
}
