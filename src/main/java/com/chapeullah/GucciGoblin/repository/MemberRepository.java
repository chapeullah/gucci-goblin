package com.chapeullah.GucciGoblin.repository;

import com.chapeullah.GucciGoblin.entity.MemberEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {

    @Modifying
    @Query("UPDATE MemberEntity m SET m.lastActivity = :ts WHERE m.tag = :tag")
    int updateLastActivity(@NonNull @Param("tag") String tag, @NonNull @Param("ts") Instant ts);

    @Query("SELECT m.lastActivity FROM MemberEntity m WHERE m.tag = :tag")
    Instant findLastActivity(@NonNull @Param("tag") String tag);

}
