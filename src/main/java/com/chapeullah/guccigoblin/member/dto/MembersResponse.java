package com.chapeullah.guccigoblin.member.dto;

import lombok.NonNull;

import java.util.List;

public record MembersResponse(@NonNull List<MemberResponse> items) {}