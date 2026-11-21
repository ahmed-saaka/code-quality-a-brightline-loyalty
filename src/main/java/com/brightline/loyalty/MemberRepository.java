package com.brightline.loyalty;

import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findById(String id);

    void save(Member member);
}