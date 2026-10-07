package com.chapeullah.guccigoblin.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
        name = "member_house_elements",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_house_elements_member_element",
                columnNames = {"member_tag", "element_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberHouseElement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_tag", nullable = false)
    private Member member;

    @Column(name = "element_id", nullable = false)
    private Integer elementId;

    @Column(name = "element_type", nullable = false)
    private String elementType;

    public MemberHouseElement(
            @NonNull Member member,
            @NonNull Integer elementId,
            @NonNull String elementType) {
        if (elementType.isBlank()) {
            throw new IllegalArgumentException("Member house element type must not be blank");
        }
        this.member = member;
        this.elementId = elementId;
        this.elementType = elementType;
    }
}
