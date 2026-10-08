package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findAllByClanTag(String clanTag);

}
