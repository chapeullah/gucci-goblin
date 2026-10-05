package com.chapeullah.guccigoblin.player.service;

import com.chapeullah.guccigoblin.player.dto.HouseElementResponse;
import com.chapeullah.guccigoblin.player.model.HouseElement;
import com.chapeullah.guccigoblin.player.model.Player;
import com.chapeullah.guccigoblin.player.repository.HouseElementRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseElementService {

    private final HouseElementRepository houseElementRepository;

    @Transactional
    public List<HouseElement> syncPlayerHouseElements(
            @NonNull Player player,
            @NonNull List<HouseElementResponse> houseElementsResponse) {
        List<HouseElement> houseElements = houseElementRepository.findAllByPlayerTag(player.getTag());
        List<HouseElement> newHouseElements = toHouseElements(player, houseElementsResponse);
        Map<Integer, HouseElement> houseElementsMap = new HashMap<>();
        Set<Integer> houseElementsSet = new HashSet<>();
        houseElements.forEach(item -> houseElementsMap.put(item.getElementId(), item));
        for (HouseElement houseElement : newHouseElements) {
            Integer id = houseElement.getElementId();
            HouseElement existingHouseElement = houseElementsMap.get(id);
            if (existingHouseElement != null) {
                existingHouseElement.updateFrom(houseElement);
            } else {
                houseElementsMap.put(id, houseElement);
            }
            houseElementsSet.add(id);
        }
        List<HouseElement> savedHouseElements = houseElementRepository
                .saveAll(houseElementsSet.stream().map(houseElementsMap::get).toList());
        for (HouseElement houseElement : houseElementsMap.values()) {
            if (!houseElementsSet.contains(houseElement.getElementId())) {
                houseElementRepository.delete(houseElement);
            }
        }
        log.debug("Player house elements synchronized {} ({}), count={}",
                player.getName(),
                player.getTag(),
                savedHouseElements.size());
        return savedHouseElements;
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

}
