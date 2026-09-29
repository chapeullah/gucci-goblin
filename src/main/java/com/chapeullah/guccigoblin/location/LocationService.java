package com.chapeullah.guccigoblin.leaguetier;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.location.Location;
import com.chapeullah.guccigoblin.location.LocationRepository;
import com.chapeullah.guccigoblin.location.dto.LocationResponse;
import com.chapeullah.guccigoblin.location.dto.LocationsResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final ClashOfClansClient client;

    private final LocationRepository locationRepository;

    @Transactional
    public void syncLocations() {
        LocationsResponse response = client.getLocations();
        List<Location> locations = response
                .items()
                .stream()
                .map(this::toLocation)
                .toList();
        locationRepository.saveAll(locations);
    }

    private Location toLocation(LocationResponse response) {
        return new Location(
                response.id(),
                response.name(),
                response.isCountry(),
                response.countryCode());
    }

}
