package com.chapeullah.guccigoblin.player;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeagueService;
import com.chapeullah.guccigoblin.clan.Clan;
import com.chapeullah.guccigoblin.label.dto.LabelResponse;
import com.chapeullah.guccigoblin.label.player.PlayerLabelService;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.player.dto.*;
import com.chapeullah.guccigoblin.player.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final ClashOfClansClient client;

    private final PlayerRepository playerRepository;

    private final LeagueTierService leagueTierService;
    private final BuilderBaseLeagueService builderBaseLeagueService;
    private final PlayerLabelService playerLabelService;

    public void syncPlayer(String playerTag) {
        PlayerResponse response = client.getPlayer(playerTag);
        Clan clan = null;

        LeagueTier leagueTier = leagueTierService.syncLeagueTiers()
                .stream()
                .filter(lt -> lt.getId().equals(response.leagueTier().id()))
                .findFirst()
                .orElse(null);

        BuilderBaseLeague builderBaseLeague = builderBaseLeagueService.syncBuilderBaseLeagues()
                .stream()
                .filter(bbl -> bbl.getId().equals(response.builderBaseLeague().id()))
                .findFirst()
                .orElse(null);

        Player player = toPlayer(
                response,
                clan,
                leagueTier,
                builderBaseLeague,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        player.getAchievements()
                .addAll(toAchievements(player, response.achievements()));

        player.getHouseElements()
                .addAll(toHouseElements(player, response.playerHouse().elements()));

        List<LabelResponse> labelsResponse = response.labels();
        List<PlayerLabelLink> playerLabels = playerLabelService.syncPlayerLabels()
                .stream()
                .filter(label -> labelsResponse.stream()
                        .anyMatch(labelResponse ->
                                labelResponse.id().equals(label.getId())))
                .map(label -> new PlayerLabelLink(player, label))
                .toList();
        player.getLabelLinks().addAll(playerLabels);

        player.getTroops().addAll(toTroops(player, response.troops()));

        player.getHeroEquipments()
                .addAll(toHeroEquipments(player, response.heroEquipments()));

        player.getHeroes()
                .addAll(toHeroes(player, response.heroes()));

        player.getSpells()
                .addAll(toSpells(player, response.spells()));

        playerRepository.save(player);
    }

    private Player toPlayer(
            PlayerResponse response,
            Clan clan,
            LeagueTier leagueTier,
            BuilderBaseLeague builderBaseLeague,
            List<Achievement> achievements,
            List<HouseElement> houseElements,
            List<PlayerLabelLink> playerLabelLinks,
            List<Troop> troops,
            List<Hero> heroes,
            List<HeroEquipment> heroEquipments,
            List<Spell> spells) {
        return new Player(
                response.tag(),
                response.name(),
                response.townHallLevel(),
                response.townHallWeaponLevel(),
                response.expLevel(),
                response.trophies(),
                response.bestTrophies(),
                response.warStars(),
                response.attackWins(),
                response.defenseWins(),
                response.builderHallLevel(),
                response.builderBaseTrophies(),
                response.bestBuilderBaseTrophies(),
                response.role(),
                response.warPreference(),
                response.donations(),
                response.donationsReceived(),
                response.clanCapitalContributions(),
                clan,
                leagueTier,
                builderBaseLeague,
                response.currentLeagueGroupTag(),
                response.currentLeagueSeasonId(),
                response.previousLeagueGroupTag(),
                response.previousLeagueSeasonId(),
                achievements,
                houseElements,
                playerLabelLinks,
                troops,
                heroes,
                heroEquipments,
                spells);
    }

    private List<Achievement> toAchievements(
            Player player,
            List<AchievementResponse> response) {
        return response
                .stream()
                .map(achievement -> toAchievement(player, achievement))
                .toList();
    }

    private Achievement toAchievement(
            Player player,
            AchievementResponse response) {
        return new Achievement(
                player,
                response.name(),
                response.stars(),
                response.value(),
                response.target(),
                response.info(),
                response.completionInfo(),
                response.village());
    }

    private List<HouseElement> toHouseElements(
            Player player,
            List<HouseElementResponse> response) {
        return response
                .stream()
                .map(houseElement -> toHouseElement(player, houseElement))
                .toList();
    }

    private HouseElement toHouseElement(
            Player player,
            HouseElementResponse response) {
        return new HouseElement(
                player, response.id(), response.type());
    }

    private List<Troop> toTroops(Player player, List<TroopResponse> response) {
        return response
                .stream()
                .map(troop -> toTroop(player, troop))
                .toList();
    }

    private Troop toTroop(Player player, TroopResponse response) {
        return new Troop(
                player,
                response.name(),
                response.level(),
                response.maxLevel(),
                response.village());
    }

    private List<Hero> toHeroes(Player player, List<HeroResponse> response) {
        return response
                .stream()
                .map(hero -> toHero(player, hero))
                .toList();
    }

    private Hero toHero(Player player, HeroResponse response) {
        Hero hero = new Hero(
                player,
                response.name(),
                response.level(),
                response.maxLevel(),
                new ArrayList<>(),
                response.village());
        hero.getHeroEquipmentLinks()
                .addAll(toHeroEquipmentLinks(
                        hero, response.heroEquipmentsResponse()));
        return hero;
    }

    private List<HeroEquipmentLink> toHeroEquipmentLinks(
            Hero hero, List<HeroEquipmentResponse> response) {
        return response
                .stream()
                .map(heroEquipmentResponse -> {
                    HeroEquipment heroEquipment = hero
                            .getPlayer()
                            .getHeroEquipments()
                            .stream()
                            .filter(item -> item.getName().equals(heroEquipmentResponse.name()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Hero equipment not found" + heroEquipmentResponse.name()));
                    return new HeroEquipmentLink(hero, heroEquipment);
                })
                .toList();
    }

    private List<HeroEquipment> toHeroEquipments(
            Player player, List<HeroEquipmentResponse> response) {
        return response
                .stream()
                .map(heroEquipment -> toHeroEquipment(player, heroEquipment))
                .toList();
    }

    private HeroEquipment toHeroEquipment(
            Player player,
            HeroEquipmentResponse response) {
        return new HeroEquipment(
                player,
                response.name(),
                response.level(),
                response.maxLevel(),
                response.village());
    }

    private List<Spell> toSpells(Player player, List<SpellResponse> response) {
        return response
                .stream()
                .map(spell -> toSpell(player, spell))
                .toList();
    }

    private Spell toSpell(Player player, SpellResponse response) {
        return new Spell(
                player,
                response.name(),
                response.level(),
                response.maxLevel(),
                response.village());
    }

}
