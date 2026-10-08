package com.chapeullah.guccigoblin.member.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberHouseElement {

    @Column(name = "element_id", nullable = false)
    private Integer elementId;

    @Column(name = "element_type", nullable = false)
    private String elementType;

    public MemberHouseElement(
            @NonNull Integer elementId,
            @NonNull String elementType) {
        if (elementType.isBlank()) {
            throw new IllegalArgumentException(
                    "Member house element type must not be blank");
        }

        this.elementId = elementId;
        this.elementType = elementType;
    }
}
