package com.chapeullah.GucciGoblin.service;

import com.chapeullah.GucciGoblin.entity.WarAttackEntity;
import com.chapeullah.GucciGoblin.model.enums.TownHallRel;
import com.chapeullah.GucciGoblin.repository.MemberRepository;
import com.chapeullah.GucciGoblin.repository.WarAttackRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarAttackService {

    private final WarAttackRepository warAttackRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void addPlayerAttacks(
            @NonNull String warKey,
            @NonNull String tag,
            Integer a1Stars,
            TownHallRel a1TownHallRel,
            Integer a2Stars,
            TownHallRel a2TownHallRel
    ) {
        if (warAttackRepository.existsByWarKeyAndTag(warKey, tag)) {
            throw new IllegalArgumentException("war attack already specified: warKey=" + warKey + " tag=" + tag);
        }

        validateAttack("a1", a1Stars, a1TownHallRel);
        validateAttack("a2", a2Stars, a2TownHallRel);

        String name = memberRepository.findNameByTag(tag);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("member not found by tag: " + tag);
        }

        WarAttackEntity e = new WarAttackEntity();
        e.setWarKey(warKey);
        e.setTag(tag);
        e.setName(name);

        e.setA1Stars(a1Stars);
        e.setA1TownHallRel(a1TownHallRel);
        e.setA2Stars(a2Stars);
        e.setA2TownHallRel(a2TownHallRel);

        warAttackRepository.save(e);
    }

    private static void validateAttack(@NonNull String label, Integer stars, TownHallRel rel) {
        if (stars == null && rel == null) return;
        if (stars == null || rel == null) {
            throw new IllegalArgumentException(label + ": stars and townHallRel must be both set or both null");
        }
        if (stars < 0 || stars > 3) { throw new IllegalArgumentException(label + ": stars must be 0..3"); }
    }

}
