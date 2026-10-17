package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.member.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    void creationInitializesMembershipAndTotalsWithoutInventingChangeDates() {
        Instant before = Instant.now();
        Member member = member(10, 5);
        Instant after = Instant.now();

        assertTrue(member.getInClan());
        assertFalse(member.getJoinedAt().isBefore(before));
        assertFalse(member.getJoinedAt().isAfter(after));
        assertNull(member.getLeftAt());
        assertNull(member.getInClanChangedAt());
        assertEquals(10, member.getTotalDonations());
        assertEquals(5, member.getTotalDonationsReceived());
        assertNull(member.getTotalDonationsChangedAt());
        assertNull(member.getTotalDonationsReceivedChangedAt());
    }

    @Test
    void identicalUpdatePreservesAllExistingChangeDatesAndMembership() {
        Member member = member(10, 5);
        Instant joinedAt = member.getJoinedAt();
        Instant houseChangedAt = joinedAt.plusSeconds(1);
        member.markHouseElementsChanged(houseChangedAt);

        updateCounters(member, 10, 5, joinedAt.plusSeconds(2));
        member.rejoin(joinedAt.plusSeconds(2));

        assertEquals(joinedAt, member.getJoinedAt());
        assertNull(member.getInClanChangedAt());
        assertNull(member.getNameChangedAt());
        assertNull(member.getRoleChangedAt());
        assertNull(member.getTownHallLevelChangedAt());
        assertNull(member.getExpLevelChangedAt());
        assertNull(member.getLeagueTierChangedAt());
        assertNull(member.getTrophiesChangedAt());
        assertNull(member.getBuilderBaseTrophiesChangedAt());
        assertNull(member.getClanRankChangedAt());
        assertNull(member.getPreviousClanRankChangedAt());
        assertNull(member.getDonationsChangedAt());
        assertNull(member.getDonationsReceivedChangedAt());
        assertNull(member.getTotalDonationsChangedAt());
        assertNull(member.getTotalDonationsReceivedChangedAt());
        assertNull(member.getBuilderBaseLeagueChangedAt());
        assertEquals(houseChangedAt, member.getMemberHouseElementsChangedAt());
    }

    @Test
    void profileUpdateTracksOneTimestampAndAcceptsAbsentOptionalValues() {
        Member member = member(10, 5);
        Instant now = member.getJoinedAt().plusSeconds(1);
        LeagueTier league = new LeagueTier(1, "League", null, null);
        BuilderBaseLeague builderLeague = new BuilderBaseLeague(2, "Builder League");

        member.update("New name", "admin", 11, 101, league,
                1100, 1000, 2, 1, 15, 7, builderLeague, now);

        assertEquals("New name", member.getName());
        assertEquals("admin", member.getRole());
        assertEquals(11, member.getTownHallLevel());
        assertEquals(101, member.getExpLevel());
        assertSame(league, member.getLeagueTier());
        assertEquals(1100, member.getTrophies());
        assertEquals(1000, member.getBuilderBaseTrophies());
        assertEquals(2, member.getClanRank());
        assertEquals(1, member.getPreviousClanRank());
        assertSame(builderLeague, member.getBuilderBaseLeague());
        assertEquals(now, member.getNameChangedAt());
        assertEquals(now, member.getRoleChangedAt());
        assertEquals(now, member.getTownHallLevelChangedAt());
        assertEquals(now, member.getExpLevelChangedAt());
        assertEquals(now, member.getLeagueTierChangedAt());
        assertEquals(now, member.getTrophiesChangedAt());
        assertEquals(now, member.getBuilderBaseTrophiesChangedAt());
        assertEquals(now, member.getClanRankChangedAt());
        assertEquals(now, member.getPreviousClanRankChangedAt());
        assertEquals(now, member.getBuilderBaseLeagueChangedAt());
        assertEquals(now, member.getDonationsChangedAt());
        assertEquals(now, member.getDonationsReceivedChangedAt());

        Instant later = now.plusSeconds(1);
        member.update("New name", "admin", 11, 101, null,
                null, null, 2, null, 15, 7, null, later);

        assertNull(member.getLeagueTier());
        assertNull(member.getBuilderBaseLeague());
        assertNull(member.getTrophies());
        assertNull(member.getBuilderBaseTrophies());
        assertNull(member.getPreviousClanRank());
        assertEquals(later, member.getLeagueTierChangedAt());
        assertEquals(later, member.getBuilderBaseLeagueChangedAt());
        assertEquals(later, member.getTrophiesChangedAt());
        assertEquals(later, member.getBuilderBaseTrophiesChangedAt());
        assertEquals(later, member.getPreviousClanRankChangedAt());
        assertEquals(now, member.getNameChangedAt());
        assertEquals(now, member.getRoleChangedAt());
        assertEquals("#P1", member.getTag());
        assertEquals("#CLAN", member.getClan().getTag());
    }

    @Test
    void donationTotalsCountIncreasesAndSeasonResetsWithoutTreatingResetAsDonation() {
        Member member = member(10, 5);
        Instant increase = member.getJoinedAt().plusSeconds(1);
        updateCounters(member, 15, 8, increase);
        assertEquals(15, member.getTotalDonations());
        assertEquals(8, member.getTotalDonationsReceived());

        Instant reset = increase.plusSeconds(1);
        updateCounters(member, 0, 0, reset);
        assertEquals(15, member.getTotalDonations());
        assertEquals(8, member.getTotalDonationsReceived());
        assertEquals(reset, member.getDonationsChangedAt());
        assertEquals(reset, member.getDonationsReceivedChangedAt());
        assertEquals(increase, member.getTotalDonationsChangedAt());
        assertEquals(increase, member.getTotalDonationsReceivedChangedAt());

        Instant donated = reset.plusSeconds(1);
        updateCounters(member, 3, 2, donated);
        updateCounters(member, 3, 2, donated.plusSeconds(1));
        assertEquals(18, member.getTotalDonations());
        assertEquals(10, member.getTotalDonationsReceived());
        assertEquals(donated, member.getTotalDonationsChangedAt());
        assertEquals(donated, member.getTotalDonationsReceivedChangedAt());
    }

    @Test
    void leaveAndRejoinKeepLastDepartureAndDoNotRefreshDatesOnRepeatedCalls() {
        Member member = member(10, 5);
        Instant joined = member.getJoinedAt();
        Instant left = joined.plusSeconds(1);
        member.leave(left);
        member.leave(left.plusSeconds(1));

        assertFalse(member.getInClan());
        assertEquals(joined, member.getJoinedAt());
        assertEquals(left, member.getLeftAt());
        assertEquals(left, member.getInClanChangedAt());

        Instant returned = left.plusSeconds(2);
        member.rejoin(returned);
        member.rejoin(returned.plusSeconds(1));
        assertTrue(member.getInClan());
        assertEquals(returned, member.getJoinedAt());
        assertEquals(returned, member.getInClanChangedAt());
        assertEquals(left, member.getLeftAt());
    }

    @Test
    void donationCountersUsePreviousMembershipUntilExplicitRejoin() {
        Member member = member(100, 40);
        Instant now = member.getJoinedAt().plusSeconds(1);
        member.leave(now);
        updateCounters(member, 150, 60, now.plusSeconds(1));
        assertFalse(member.getInClan());
        assertEquals(250, member.getTotalDonations());
        assertEquals(100, member.getTotalDonationsReceived());

        member.rejoin(now.plusSeconds(1));
        updateCounters(member, 150, 60, now.plusSeconds(2));
        assertEquals(250, member.getTotalDonations());
        assertEquals(100, member.getTotalDonationsReceived());
    }

    private static void updateCounters(Member member, int donations, int received, Instant now) {
        member.update(
                member.getName(), member.getRole(), member.getTownHallLevel(), member.getExpLevel(),
                member.getLeagueTier(), member.getTrophies(), member.getBuilderBaseTrophies(),
                member.getClanRank(), member.getPreviousClanRank(),
                donations, received, member.getBuilderBaseLeague(), now);
    }

    private static Member member(int donations, int received) {
        Clan clan = BeanUtils.instantiateClass(Clan.class);
        ReflectionTestUtils.setField(clan, "tag", "#CLAN");
        return new Member("#P1", clan, "Name", "member", 10, 100,
                null, 1000, 900, 1, null, donations, received, null);
    }
}
