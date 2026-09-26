package com.chapeullah.guccigoblin.member.dto;

import java.util.List;

public record MemberSyncResult(
        List<MemberSnapshot> joined,
        List<MemberSnapshot> left) {}