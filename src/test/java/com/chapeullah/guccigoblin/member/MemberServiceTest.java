package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.clan.dto.ClanMemberResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.member.dto.MemberHouseElementResponse;
import com.chapeullah.guccigoblin.member.dto.MemberHouseElementResponses;
import com.chapeullah.guccigoblin.member.model.Member;
import com.chapeullah.guccigoblin.member.model.MemberDelta;
import com.chapeullah.guccigoblin.member.model.MemberDelta.Snapshot;
import com.chapeullah.guccigoblin.member.model.MemberHouseElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MemberServiceTest {

    private final List<Member> stored = new ArrayList<>();
    private final RecordingDeltaService deltas = new RecordingDeltaService();
    private Clan clan;
    private MemberService service;
    private int houseFlushes;

    @BeforeEach
    void setUp() {
        clan = BeanUtils.instantiateClass(Clan.class);
        ReflectionTestUtils.setField(clan, "tag", "#CLAN");
        MemberRepository members = repository(MemberRepository.class, (proxy, method, args) ->
                switch (method.getName()) {
                    case "findAllByClanTag" -> {
                        assertEquals("#CLAN", args[0]);
                        yield new ArrayList<>(stored);
                    }
                    case "save" -> {
                        Member member = (Member) args[0];
                        stored.add(member);
                        yield member;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        MemberHouseElementRepository houses = repository(
                MemberHouseElementRepository.class, (proxy, method, args) ->
                        switch (method.getName()) {
                            case "flush" -> {
                                houseFlushes++;
                                yield null;
                            }
                            case "saveAll" -> args[0];
                            default -> throw new UnsupportedOperationException(method.getName());
                        });
        service = new MemberService(members, null, null,
                new MemberHouseElementService(houses), deltas);
    }

    @Test
    void syncUpdatesExistingRowsCreatesNewRowsAndMarksOmittedMembersLeftOnlyOnce() {
        Member existing = member("#P1", 10, 5);
        Member leaving = member("#P2", 20, 10);
        Instant existingJoined = existing.getJoinedAt();
        Instant leavingJoined = leaving.getJoinedAt();
        stored.addAll(List.of(existing, leaving));
        List<ClanMemberResponse> responses = List.of(
                response("#P1", "New name", 15, 8, null),
                response("#P3", "New member", 0, 0, null));

        List<Member> current = service.syncMembers(clan, responses);

        assertEquals(2, current.size());
        assertSame(existing, current.getFirst());
        assertEquals(3, stored.size());
        assertEquals(existingJoined, existing.getJoinedAt());
        assertEquals(leavingJoined, leaving.getJoinedAt());
        assertFalse(leaving.getInClan());
        assertNotNull(leaving.getLeftAt());
        assertEquals(leaving.getLeftAt(), leaving.getInClanChangedAt());
        assertEquals(MemberDelta.MembershipStatus.UNCHANGED, delta("#P1").getMembershipStatus());
        assertEquals("Name", delta("#P1").getNameDelta().oldValue());
        assertEquals("New name", delta("#P1").getNameDelta().newValue());
        assertEquals(MemberDelta.MembershipStatus.LEFT, delta("#P2").getMembershipStatus());
        assertEquals(MemberDelta.MembershipStatus.JOINED, delta("#P3").getMembershipStatus());

        Instant nameChangedAt = existing.getNameChangedAt();
        Instant leftAt = leaving.getLeftAt();
        service.syncMembers(clan, responses);

        assertTrue(deltas.last.isEmpty());
        assertEquals(3, stored.size());
        assertEquals(nameChangedAt, existing.getNameChangedAt());
        assertEquals(leftAt, leaving.getLeftAt());
        assertEquals(15, existing.getTotalDonations());
        assertEquals(8, existing.getTotalDonationsReceived());
    }

    @Test
    void returningMemberIsUpdatedBeforeMembershipChangesAndKeepsItsLastDeparture() {
        Member returning = member("#P1", 100, 40);
        Instant leftAt = Instant.now();
        returning.leave(leftAt);
        stored.add(returning);

        List<Member> current = service.syncMembers(clan,
                List.of(response("#P1", "Returned", 150, 60, null)));

        assertSame(returning, current.getFirst());
        assertEquals(1, stored.size());
        assertTrue(returning.getInClan());
        assertEquals(250, returning.getTotalDonations());
        assertEquals(100, returning.getTotalDonationsReceived());
        assertEquals(leftAt, returning.getLeftAt());
        assertEquals(returning.getJoinedAt(), returning.getInClanChangedAt());
        assertEquals(returning.getJoinedAt(), returning.getTotalDonationsChangedAt());
        assertEquals(MemberDelta.MembershipStatus.JOINED, delta("#P1").getMembershipStatus());
    }

    @Test
    void emptyRosterDoesNotMarkCurrentMembersAsLeft() {
        Member member = member("#P1", 10, 5);
        stored.add(member);

        assertThrows(IllegalArgumentException.class, () -> service.syncMembers(clan, List.of()));

        assertTrue(member.getInClan());
        assertNull(member.getLeftAt());
        assertTrue(deltas.last.isEmpty());
    }

    @Test
    void houseSynchronizationRetainsManagedCollectionAndTracksOnlyContentChanges() {
        Member member = member("#P1", 10, 5);
        stored.add(member);
        List<MemberHouseElement> collection = member.getMemberHouseElements();
        MemberHouseElementResponses roof = new MemberHouseElementResponses(
                List.of(new MemberHouseElementResponse("roof", 1)));

        service.syncMembers(clan, List.of(response("#P1", "Name", 10, 5, roof)));

        assertSame(collection, member.getMemberHouseElements());
        assertEquals(1, collection.size());
        assertEquals("roof", collection.getFirst().getElementType());
        Instant changedAt = member.getMemberHouseElementsChangedAt();
        assertNotNull(changedAt);
        MemberHouseElement oldElement = collection.getFirst();

        service.syncMembers(clan, List.of(response("#P1", "Name", 10, 5, roof)));
        assertEquals(changedAt, member.getMemberHouseElementsChangedAt());
        assertSame(oldElement, collection.getFirst());

        MemberHouseElementResponses changed = new MemberHouseElementResponses(
                List.of(new MemberHouseElementResponse("wall", 1)));
        service.syncMembers(clan, List.of(response("#P1", "Name", 10, 5, changed)));

        assertSame(collection, member.getMemberHouseElements());
        assertEquals("wall", collection.getFirst().getElementType());
        assertNotSame(oldElement, collection.getFirst());
        assertEquals(1, houseFlushes);
        assertFalse(member.getMemberHouseElementsChangedAt().isBefore(changedAt));
    }

    private MemberDelta delta(String tag) {
        return deltas.last.stream().filter(delta -> delta.getTag().equals(tag)).findFirst().orElseThrow();
    }

    private Member member(String tag, int donations, int received) {
        return new Member(tag, clan, "Name", "member", 10, 100,
                null, 1000, 900, 1, null, donations, received, null);
    }

    private static ClanMemberResponse response(
            String tag, String name, int donations, int received, MemberHouseElementResponses house) {
        return new ClanMemberResponse(tag, name, "member", 10, 100,
                null, null, 1000, 900, 1, null, donations, received, house, null);
    }

    private static final class RecordingDeltaService extends MemberDeltaService {
        private List<MemberDelta> last = List.of();

        @Override
        public void memberDeltaOutput(Map<String, Snapshot> oldMembers, Map<String, Member> newMembers) {
            last = memberDeltasFrom(oldMembers, newMembers);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T repository(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }
}
