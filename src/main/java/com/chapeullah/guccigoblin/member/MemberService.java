package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.clan.dto.ClanMemberResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public List<Member> upsertMembers(
            @NonNull Clan clan,
            @NonNull List<ClanMemberResponse> clanMemberResponses) {

        return List.of();
    }

}
