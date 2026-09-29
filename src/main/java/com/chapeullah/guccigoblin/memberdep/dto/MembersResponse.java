package com.chapeullah.guccigoblin.memberdep.dto;

import lombok.NonNull;

import java.util.List;

public record MembersResponse(@NonNull List<MemberResponse> items) {}