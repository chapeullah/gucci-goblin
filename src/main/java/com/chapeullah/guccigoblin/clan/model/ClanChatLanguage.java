package com.chapeullah.guccigoblin.clan.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "chat_languages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClanChatLanguage {

    @Id
    @Column(name = "id",
            nullable = false)
    private Integer id;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "language_code",
            nullable = true)
    private String languageCode;

    public ClanChatLanguage(
            @NonNull Integer id,
            @NonNull String name,
            String languageCode) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Clan telegram chat language name must not be blank");
        }
        this.id = id;
        this.name = name;
        this.languageCode = languageCode;
    }

    public void updateFrom(ClanChatLanguage clanChatLanguage) {
        if (!this.id.equals(clanChatLanguage.id)) {
            throw new IllegalArgumentException("Clan telegram chat language id mismatch");
        }
        this.name = clanChatLanguage.name;
        this.languageCode = clanChatLanguage.languageCode;
    }

}
