package com.chapeullah.guccigoblin.memberdep.dto;

import java.util.List;

public record MemberSyncResult(
        List<MemberSnapshot> joined,
        List<MemberSnapshot> left) {}