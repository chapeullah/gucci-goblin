package com.chapeullah.guccigoblin.player.service;

import com.chapeullah.guccigoblin.player.dto.AchievementResponse;
import com.chapeullah.guccigoblin.player.model.Achievement;
import com.chapeullah.guccigoblin.player.model.Player;
import com.chapeullah.guccigoblin.player.repository.AchievementRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;

    @Transactional
    public List<Achievement> syncPlayerAchievements(
            @NonNull Player player,
            @NonNull List<AchievementResponse> achievementsResponse) {
        List<Achievement> achievements = achievementRepository.findAllByPlayerTag(player.getTag());
        List<Achievement> newAchievements = toAchievements(player, achievementsResponse);
        Map<String, Achievement> achievementsMap = new HashMap<>();
        Set<String> achievementsSet = new HashSet<>();
        achievements.forEach(item -> achievementsMap.put(item.getName(), item));
        for (Achievement achievement : newAchievements) {
            String name = achievement.getName();
            Achievement existingAchievement = achievementsMap.get(name);
            if (existingAchievement != null) {
                existingAchievement.updateFrom(achievement);
            } else {
                achievementsMap.put(name, achievement);
            }
            achievementsSet.add(name);
        }
        List<Achievement> savedAchievements = achievementRepository
                .saveAll(achievementsSet.stream().map(achievementsMap::get).toList());
        for (Achievement achievement : achievementsMap.values()) {
            if (!achievementsSet.contains(achievement.getName())) {
                achievementRepository.delete(achievement);
            }
        }
        log.debug("Player achievements synchronized {} ({}), count={}",
                player.getName(),
                player.getTag(),
                savedAchievements.size());
        return savedAchievements;
    }

    private List<Achievement> toAchievements(
            Player player,
            List<AchievementResponse> achievementsResponse) {
        return achievementsResponse
                .stream()
                .map(item -> toAchievement(player, item))
                .toList();
    }

    private Achievement toAchievement(
            Player player,
            AchievementResponse achievementResponse) {
        return new Achievement(
                player,
                achievementResponse.name(),
                achievementResponse.stars(),
                achievementResponse.value(),
                achievementResponse.target(),
                achievementResponse.info(),
                achievementResponse.completionInfo(),
                achievementResponse.village());
    }

}
