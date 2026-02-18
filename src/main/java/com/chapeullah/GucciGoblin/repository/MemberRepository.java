package com.chapeullah.GucciGoblin.repository;

import com.chapeullah.GucciGoblin.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {}
