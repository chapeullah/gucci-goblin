package com.chapeullah.GucciGoblin.repository;

import com.chapeullah.GucciGoblin.entity.WarAttackEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarAttackRepository extends JpaRepository<WarAttackEntity, Long> {

    boolean existsByWarKeyAndTag(@NonNull String warKey, @NonNull String tag);

    Optional<WarAttackEntity> findByWarKeyAndTag(@NonNull String warKey, @NonNull String tag);

}
