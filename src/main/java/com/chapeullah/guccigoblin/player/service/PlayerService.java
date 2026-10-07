package com.chapeullah.guccigoblin.player.service;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeagueService;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.label.dto.LabelResponse;
import com.chapeullah.guccigoblin.label.player.PlayerLabelService;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.player.repository.PlayerRepository;
import com.chapeullah.guccigoblin.player.dto.*;
import com.chapeullah.guccigoblin.player.model.*;
import jakarta.transaction.Transactional;
import lombok.NonNull;
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
    private final PlayerAchievementService playerAchievementService;
    private final PlayerHouseElementService playerHouseElementService;
    private final PlayerTroopService playerTroopService;
    private final PlayerHeroEquipmentService playerHeroEquipmentService;
    private final PlayerHeroEquipmentLinkService playerHeroEquipmentLinkService;
    private final PlayerHeroService playerHeroService;
    private final PlayerSpellService playerSpellService;

    @Transactional
    public Player syncPlayer(@NonNull String playerTag) {
        PlayerResponse response = client.getPlayer(playerTag);

        Player existingPlayer = playerRepository.findById(playerTag).orElse(null);

        Clan clan = existingPlayer == null ? null : existingPlayer.getClan(); // TODO ?

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
                    .filter(item -> item.getId().equals(response.builderBaseLeague().id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Builder base league not found"));
        }

        Player incomingPlayer = toPlayer(
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

        Player player;
        if (existingPlayer == null) {
            player = playerRepository.save(incomingPlayer);
        } else {
            existingPlayer.updateFrom(incomingPlayer);
            player = existingPlayer;
        }

        List<PlayerAchievement> playerAchievements = playerAchievementService.syncPlayerAchievements(
                player,
                response.achievements() == null ? List.of() : response.achievements());

        player.getPlayerAchievements().clear();
        player.getPlayerAchievements().addAll(playerAchievements);

        List<PlayerHouseElementResponse> elements =
                response.playerHouse() == null || response.playerHouse().elements() == null
                        ? List.of()
                        : response.playerHouse().elements();

        List<PlayerHouseElement> playerHouseElements =
                playerHouseElementService.syncPlayerHouseElements(player, elements);

        player.getPlayerHouseElements().clear();
        player.getPlayerHouseElements().addAll(playerHouseElements);

        List<LabelResponse> labelsResponse =
                response.labels() == null ? List.of() : response.labels();

        Set<Integer> labelIds = new HashSet<>();
        labelsResponse.forEach(label -> labelIds.add(label.id()));

        player.getLabelLinks().removeIf(
                link -> !labelIds.contains(link.getPlayerLabel().getId()));

        if (!labelsResponse.isEmpty()) {
            playerLabelService.syncPlayerLabels().stream()
                    .filter(label -> labelIds.contains(label.getId()))
                    .filter(label -> player.getLabelLinks().stream()
                            .noneMatch(link ->
                                    link.getPlayerLabel().getId().equals(label.getId())))
                    .forEach(label ->
                            player.getLabelLinks().add(new PlayerLabelLink(player, label)));
        }

        List<PlayerTroop> playerTroops = playerTroopService.syncTroops(
                player,
                response.troops() == null ? List.of() : response.troops());

        player.getPlayerTroops().clear();
        player.getPlayerTroops().addAll(playerTroops);

        List<PlayerHeroEquipmentResponse> equipmentResponse =
                response.heroEquipments() == null ? List.of() : response.heroEquipments();

        Set<String> equipmentNames = new HashSet<>();
        equipmentResponse.forEach(equipment -> equipmentNames.add(equipment.name()));

        boolean linksRemoved = false;
        for (PlayerHero playerHero : player.getPlayerHeroes()) {
            linksRemoved |= playerHero.getPlayerHeroEquipmentLinks().removeIf(
                    link -> !equipmentNames.contains(link.getPlayerHeroEquipment().getName()));
        }

        if (linksRemoved) {
            playerRepository.flush();
        }

        List<PlayerHeroEquipment> playerHeroEquipments =
                playerHeroEquipmentService.syncHeroEquipments(player, equipmentResponse);

        player.getPlayerHeroEquipments().clear();
        player.getPlayerHeroEquipments().addAll(playerHeroEquipments);

        List<PlayerHeroResponse> heroesResponse =
                response.heroes() == null ? List.of() : response.heroes();

        List<PlayerHero> playerHeroes = playerHeroService.syncHeroes(player, heroesResponse);

        player.getPlayerHeroes().clear();
        player.getPlayerHeroes().addAll(playerHeroes);

        for (PlayerHero playerHero : player.getPlayerHeroes()) {
            PlayerHeroResponse playerHeroResponse = heroesResponse.stream()
                    .filter(item -> item.name().equals(playerHero.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Hero response not found: " + playerHero.getName()));

            List<PlayerHeroEquipmentResponse> equipment =
                    playerHeroResponse.heroEquipmentsResponse() == null
                            ? List.of()
                            : playerHeroResponse.heroEquipmentsResponse();

            List<PlayerHeroEquipmentLink> links =
                    playerHeroEquipmentLinkService.syncHeroEquipmentLinks(playerHero, equipment);

            playerHero.getPlayerHeroEquipmentLinks().clear();
            playerHero.getPlayerHeroEquipmentLinks().addAll(links);
        }

        List<PlayerSpell> playerSpells = playerSpellService.syncPlayerSpells(
                player,
                response.spells() == null ? List.of() : response.spells());

        player.getPlayerSpells().clear();
        player.getPlayerSpells().addAll(playerSpells);

        Player savedPlayer = playerRepository.save(player);

        log.info("Player synchronized: {} ({})",
                savedPlayer.getName(), savedPlayer.getTag());

        return savedPlayer;
    }

    private Player toPlayer(
            PlayerResponse response,
            Clan clan,
            LeagueTier leagueTier,
            BuilderBaseLeague builderBaseLeague,
            List<PlayerAchievement> playerAchievements,
            List<PlayerHouseElement> playerHouseElements,
            List<PlayerLabelLink> playerLabelLinks,
            List<PlayerTroop> playerTroops,
            List<PlayerHero> playerHeroes,
            List<PlayerHeroEquipment> playerHeroEquipments,
            List<PlayerSpell> playerSpells) {
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
                playerAchievements,
                playerHouseElements,
                playerLabelLinks,
                playerTroops,
                playerHeroes,
                playerHeroEquipments,
                playerSpells);
    }

}
