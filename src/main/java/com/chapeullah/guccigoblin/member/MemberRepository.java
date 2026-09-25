package com.chapeullah.guccigoblin.member;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    @Modifying
    @Query("UPDATE Member m SET m.lastActivity = :ts WHERE m.tag = :tag")
    int updateLastActivity(@NonNull @Param("tag") String tag, @NonNull @Param("ts") Instant ts);

    @Query("SELECT m.lastActivity FROM Member m WHERE m.tag = :tag")
    Instant findLastActivity(@NonNull @Param("tag") String tag);

    @Query("SELECT m.name FROM Member m WHERE m.tag = :tag")
    String findNameByTag(@NonNull @Param("tag") String tag);

}
