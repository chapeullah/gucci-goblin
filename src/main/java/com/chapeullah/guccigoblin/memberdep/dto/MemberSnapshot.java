package com.chapeullah.guccigoblin.memberdep.dto;

import com.chapeullah.guccigoblin.memberdep.Member;

public record MemberSnapshot(String tag, String name) {
    public static MemberSnapshot from(Member member) {
        return new MemberSnapshot(
                member.getTag(),
                member.getName());
    }
}