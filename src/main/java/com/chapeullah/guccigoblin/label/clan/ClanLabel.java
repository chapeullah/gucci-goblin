package com.chapeullah.guccigoblin.label.clan;

import com.chapeullah.guccigoblin.label.IconUrls;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "clan_labels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClanLabel {

    @Id
    @Column(name = "id",
            nullable = false)
    private Integer id;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Embedded
    private IconUrls iconUrls;

    public ClanLabel(
            Integer id,
            String name,
            IconUrls iconUrls) {
        this.id = id;
        this.name = name;
        this.iconUrls = iconUrls;
    }

}
