package com.chapeullah.guccigoblin.member.dto;

import com.chapeullah.guccigoblin.member.Member;

public record MemberSnapshot(String tag, String name) {
    public static MemberSnapshot from(Member member) {
        return new MemberSnapshot(
                member.getTag(),
                member.getName());
    }
}