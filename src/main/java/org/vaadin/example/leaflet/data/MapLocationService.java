package org.vaadin.example.leaflet.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

/**
 * Demo backend that accepts up to 100 fishing spots. Data is shared with all
 * users.
 */
@Service
@ApplicationScope
public class MapLocationService {

    private List<MapLocation> spots = new ArrayList<MapLocation>();

    @PostConstruct
    private void init() {

        // Add some demo data

        spots.add(new MapLocation(60.465071, 22.302923, "Halistenkoski"));
        spots.add(new MapLocation(60.479928, 21.328347, "Kustavi"));
        spots.add(new MapLocation(60.124169, 21.906335, "Kirjais"));
    }

    public List<MapLocation> getAll() {

        return Collections.unmodifiableList(spots);
    }

    public void addSpot(MapLocation spot) {

        // protect concurrent access since MapLocationService is a singleton
        synchronized (spots) {

            spots.add(spot);

            if (spots.size() > 100) {
                spots.remove(0);
            }
        }
    }
}
