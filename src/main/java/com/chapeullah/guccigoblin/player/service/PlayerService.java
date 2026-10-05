package com.chapeullah.guccigoblin.player.service;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeagueService;
import com.chapeullah.guccigoblin.clan.Clan;
import com.chapeullah.guccigoblin.label.dto.LabelResponse;
import com.chapeullah.guccigoblin.label.player.PlayerLabelService;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.player.PlayerRepository;
import com.chapeullah.guccigoblin.player.dto.*;
import com.chapeullah.guccigoblin.player.model.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final ClashOfClansClient client;

    private final PlayerRepository playerRepository;

    private final LeagueTierService leagueTierService;
    private final BuilderBaseLeagueService builderBaseLeagueService;
    private final PlayerLabelService playerLabelService;
    private final AchievementService achievementService;
    private final HouseElementService houseElementService;

    @Transactional
    public Player syncPlayer(String playerTag) {
        PlayerResponse response = client.getPlayer(playerTag);
        return playerRepository
                .findById(playerTag)
                .map(player -> updatePlayer(player, response))
                .orElseGet(() -> createPlayer(response));

    }

    @Transactional
    private Player updatePlayer(Player player, PlayerResponse response) {
        if (!player.getTag().equals(response.tag())) {
            throw new IllegalStateException("Player tag mismatch");
        }

        LeagueTier leagueTier = null;
        if (response.leagueTier() != null) {
            leagueTier = leagueTierService.syncLeagueTiers()
                    .stream()
                    .filter(item -> item.getId().equals(response.leagueTier().id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("League tier not found"));
        }

        BuilderBaseLeague builderBaseLeague = null;
        if (response.builderBaseLeague() != null) {
            builderBaseLeague = builderBaseLeagueService.syncBuilderBaseLeagues()
                    .stream()
                    .filter(leagueTierItem -> leagueTierItem.getId().equals(response.builderBaseLeague().id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Builder base league not found"));
        }

        Player updatedPlayer = toPlayer(
                response,
                player.getClan(),
                leagueTier,
                builderBaseLeague,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());

        player.updateFrom(updatedPlayer);

        achievementService.syncPlayerAchievements(player, response.achievements());



        Player savedPlayer = playerRepository.save(player);
        log.info("Player updated: {} ({})",
                savedPlayer.getName(), savedPlayer.getTag());
        return savedPlayer;
    }

    private record AchievementKey(String name, String village) {}

    @Transactional
    private Player createPlayer(PlayerResponse response) {
        Clan clan = null;

        LeagueTier leagueTier = null;
        if (response.leagueTier() != null) {
            leagueTier = leagueTierService.syncLeagueTiers()
                    .stream()
                    .filter(leagueTierItem -> leagueTierItem.getId().equals(response.leagueTier().id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("League tier not found"));
        }

        BuilderBaseLeague builderBaseLeague = null;
        if (response.builderBaseLeague() != null) {
            builderBaseLeague = builderBaseLeagueService.syncBuilderBaseLeagues()
                    .stream()
                    .filter(bbl -> bbl.getId().equals(response.builderBaseLeague().id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Builder base league not found"));
        }

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

        List<LabelResponse> labelsResponse = response.labels();
        if (labelsResponse != null) {
            List<PlayerLabelLink> playerLabels = playerLabelService.syncPlayerLabels()
                    .stream()
                    .filter(label -> labelsResponse.stream()
                            .anyMatch(labelResponse ->
                                    labelResponse.id().equals(label.getId())))
                    .map(label -> new PlayerLabelLink(player, label))
                    .toList();

            player.getLabelLinks().addAll(playerLabels);
        }

        player.getTroops().addAll(toTroops(player, response.troops()));

        player.getHeroEquipments()
                .addAll(toHeroEquipments(player, response.heroEquipments()));

        player.getHeroes()
                .addAll(toHeroes(player, response.heroes()));

        player.getSpells()
                .addAll(toSpells(player, response.spells()));

        Player savedPlayer = playerRepository.save(player);

        savedPlayer.getAchievements().addAll(
                achievementService.syncPlayerAchievements(
                        savedPlayer, response.achievements() == null ? List.of() : response.achievements()));

        List<HouseElementResponse> elements =
                response.playerHouse() == null || response.playerHouse().elements() == null
                        ? List.of()
                        : response.playerHouse().elements();
        savedPlayer.getHouseElements().addAll(
                houseElementService.syncPlayerHouseElements(
                        savedPlayer, elements));

        // LABEL LINKS

        savedPlayer.getTroops().addAll(
                achievementService.syncPlayerAchievements(
                        savedPlayer, response.achievements() == null ? List.of() : response.achievements()));

        log.info("Player created: {} ({})", savedPlayer.getName(), savedPlayer.getTag());

        return savedPlayer;
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

    private List<Troop> toTroops(Player player, List<TroopResponse> response) {
        if (response == null) {
            return List.of();
        }
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
        if (response == null) {
            return List.of();
        }
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
        if (response == null) {
            return List.of();
        }
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
        if (response == null) {
            return List.of();
        }
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
        if (response == null) {
            return List.of();
        }
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
