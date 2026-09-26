package com.chapeullah.guccigoblin;

import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberDelta;
import com.chapeullah.guccigoblin.member.dto.BuilderBaseLeagueResponse;
import com.chapeullah.guccigoblin.member.dto.LeagueTierResponse;
import com.chapeullah.guccigoblin.member.dto.MemberResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberDeltaTest {

    @Test
    void detectsLeagueAndClanRankChanges() {
        Member before = member(1, "Wood League V", 2, "Gold League I", 3);

        assertFalse(MemberDelta.merge(
                before,
                member(1, "Wood League V", 2, "Gold League I", 3)).hasChanges());
        assertTrue(MemberDelta.merge(
                before,
                member(10, "Wood League V", 2, "Gold League I", 3)).hasChanges());
        assertTrue(MemberDelta.merge(
                before,
                member(1, "Clay League V", 2, "Gold League I", 3)).hasChanges());
        assertTrue(MemberDelta.merge(
                before,
                member(1, "Wood League V", 20, "Gold League I", 3)).hasChanges());
        assertTrue(MemberDelta.merge(
                before,
                member(1, "Wood League V", 2, "Crystal League I", 3)).hasChanges());
        assertTrue(MemberDelta.merge(
                before,
                member(1, "Wood League V", 2, "Gold League I", 4)).hasChanges());
    }

    @Test
    void joinedAndLeftDeltasContainLeagueAndClanRankValues() {
        Member member = member(1, "Wood League V", 2, "Gold League I", 3);

        MemberDelta joined = MemberDelta.joined(member);
        assertEquals(new MemberDelta.Delta<>(null, 1), joined.getBuilderBaseLeagueIdDelta());
        assertEquals(new MemberDelta.Delta<>(null, "Wood League V"), joined.getBuilderBaseLeagueNameDelta());
        assertEquals(new MemberDelta.Delta<>(null, 2), joined.getLeagueTierIdDelta());
        assertEquals(new MemberDelta.Delta<>(null, "Gold League I"), joined.getLeagueTierNameDelta());
        assertEquals(new MemberDelta.Delta<>(null, 3), joined.getClanRankDelta());

        MemberDelta left = MemberDelta.left(member);
        assertEquals(new MemberDelta.Delta<>(1, null), left.getBuilderBaseLeagueIdDelta());
        assertEquals(new MemberDelta.Delta<>("Wood League V", null), left.getBuilderBaseLeagueNameDelta());
        assertEquals(new MemberDelta.Delta<>(2, null), left.getLeagueTierIdDelta());
        assertEquals(new MemberDelta.Delta<>("Gold League I", null), left.getLeagueTierNameDelta());
        assertEquals(new MemberDelta.Delta<>(3, null), left.getClanRankDelta());
    }

    private static Member member(
            int builderBaseLeagueId,
            String builderBaseLeagueName,
            int leagueTierId,
            String leagueTierName,
            int clanRank) {
        return Member.from(new MemberResponse(
                "#A",
                "Player A",
                "member",
                15,
                200,
                100,
                40,
                3_000,
                new BuilderBaseLeagueResponse(builderBaseLeagueId, builderBaseLeagueName),
                new LeagueTierResponse(leagueTierId, leagueTierName),
                clanRank));
    }
}
