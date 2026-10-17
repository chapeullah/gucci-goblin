package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.member.model.Member;
import com.chapeullah.guccigoblin.member.model.MemberDelta;
import com.chapeullah.guccigoblin.member.model.MemberDelta.Snapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MemberDeltaTest {

    @Test
    void scalarSnapshotSurvivesAnUpdateOfTheSameEntity() {
        Member member = member("#P1", "#CLAN");
        Snapshot before = Snapshot.from(member);

        member.update("New name", "admin", 11, 101,
                new LeagueTier(105000001, "Skeleton League 1", null, null),
                1100, 1000, 2, 1, 15, 8, null, member.getJoinedAt().plusSeconds(1));
        MemberDelta delta = MemberDelta.merge(before, member);

        assertEquals("Name", before.name());
        assertEquals(10, before.donations());
        assertNull(before.leagueTierId());
        assertTrue(delta.hasChanges());
        assertEquals(MemberDelta.MembershipStatus.UNCHANGED, delta.getMembershipStatus());
        assertEquals("Name", delta.getNameDelta().oldValue());
        assertEquals("New name", delta.getNameDelta().newValue());
        assertEquals(10, delta.getDonationsDelta().oldValue());
        assertEquals(15, delta.getDonationsDelta().newValue());
        assertNull(delta.getLeagueTierIdDelta().oldValue());
        assertEquals(105000001, delta.getLeagueTierIdDelta().newValue());
        assertTrue(delta.getClanRankDelta().changed());
    }

    @Test
    void unchangedMembersAndAlreadyAbsentMembersDoNotProduceRepeatedEvents() {
        Member member = member("#P1", "#CLAN");
        assertTrue(MemberDeltaService.memberDeltasFrom(
                Map.of("#P1", Snapshot.from(member)), Map.of("#P1", member)).isEmpty());

        member.leave(member.getJoinedAt().plusSeconds(1));
        assertTrue(MemberDeltaService.memberDeltasFrom(
                Map.of("#P1", Snapshot.from(member)), Map.of()).isEmpty());
    }

    @Test
    void leavingIsDetectedAfterTheOriginalEntityWasMutated() {
        Member member = member("#P1", "#CLAN");
        Snapshot before = Snapshot.from(member);
        member.leave(member.getJoinedAt().plusSeconds(1));

        List<MemberDelta> deltas = MemberDeltaService.memberDeltasFrom(
                Map.of("#P1", before), Map.of());

        assertEquals(1, deltas.size());
        assertEquals(MemberDelta.MembershipStatus.LEFT, deltas.getFirst().getMembershipStatus());
        assertEquals("Name", deltas.getFirst().getNameDelta().oldValue());
        assertNull(deltas.getFirst().getNameDelta().newValue());
        assertTrue(before.inClan());
        assertFalse(member.getInClan());
    }

    @Test
    void returningAndFirstTimeMembersProduceJoinedEvents() {
        Member returning = member("#P1", "#CLAN");
        returning.leave(returning.getJoinedAt().plusSeconds(1));
        Snapshot before = Snapshot.from(returning);
        returning.rejoin(returning.getLeftAt().plusSeconds(1));
        Member firstTime = member("#P2", "#CLAN");

        List<MemberDelta> deltas = MemberDeltaService.memberDeltasFrom(
                Map.of("#P1", before), Map.of("#P1", returning, "#P2", firstTime));

        assertEquals(2, deltas.size());
        assertTrue(deltas.stream().allMatch(
                delta -> delta.getMembershipStatus() == MemberDelta.MembershipStatus.JOINED));
        assertFalse(before.inClan());
    }

    @Test
    void differentPlayerOrClanCannotBeComparedAsTheSameMember() {
        Snapshot before = Snapshot.from(member("#P1", "#CLAN"));
        assertThrows(IllegalArgumentException.class,
                () -> MemberDelta.merge(before, member("#P2", "#CLAN")));
        assertThrows(IllegalArgumentException.class,
                () -> MemberDelta.merge(before, member("#P1", "#OTHER")));
    }

    private static Member member(String tag, String clanTag) {
        Clan clan = BeanUtils.instantiateClass(Clan.class);
        ReflectionTestUtils.setField(clan, "tag", clanTag);
        return new Member(tag, clan, "Name", "member", 10, 100,
                null, 1000, 900, 1, null, 10, 5, null);
    }
}
