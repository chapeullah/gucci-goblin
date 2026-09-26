package com.chapeullah.guccigoblin.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository
        extends JpaRepository<Member, Long> {
    Optional<Member> findByTag(String tag);
    boolean existsByTag(String tag);
    void deleteAllByTagIn(List<String> tags);
}