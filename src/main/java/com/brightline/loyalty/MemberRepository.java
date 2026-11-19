package com.brightline.loyalty;

public interface MemberRepository {

    Member findById(String id);

    void save(Member member);
}