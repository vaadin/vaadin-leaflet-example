package org.vaadin.example.leaflet.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.example.leaflet.data.MapLocation;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;

import elemental.json.Json;
import elemental.json.JsonArray;

/**
 * Simple custom (web) component wrapper for the LeafletJS map.
 * <p>
 * This file, along with leaflet-map.js (found under the frontent/src
 * directory), create a reusable Web Component that allows us to create and
 * control maps from Java code.
 * <p>
 * This file contains the Java API and makes use of existing Vaadin APIs and
 * helpers. The JavaScript file contains the Web Component (front end) and its
 * API.
 * <p>
 * This wrapper is not fully featured, but supports zooming, panning, and
 * markers. Why not try to add some API yourself? The JS file already has a
 * method called removeAllMarkers...
 * <p>
 * LeafletJS documentation can be found here:
 * <p>
 * {@link https://leafletjs.com/reference-1.6.0.html}
 */

// Assign a web component (html) tag name to our custom component since Leaflet doesn't provide it's own (it's not a Web Component)
@Tag("leaflet-map")

// Download the Leaflet JS files using NPM
@NpmPackage(value = "leaflet", version = "1.6.0")

// Include the necessary theme files from the Leaflet package
@CssImport(value = "leaflet/dist/leaflet.css", themeFor = "leaflet-map")

// Finally import our connector JS file (the '.' refers to the 'frontend' folder in the project root)
@JsModule("./src/leaflet-map.js")
public class LeafletMap extends PolymerTemplate<TemplateModel> implements HasSize {

    /**
     * We'll use OpenStreetMap's free (for limited use) map server for this demo.
     */
    private static final String OPEN_STREET_MAP_LAYER = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
    private static final String OPEN_STREET_MAP_ATTRIBUTION = "&copy; <a href='https://www.openstreetmap.org/copyright'>OpenStreetMap</a> contributors";

    /**
     * Internal helper when dealing with clicks on markers.
     *
     * @see InternalMarkerClickEvent
     */
    private Map<Integer, MapLocation> idToMarker = new HashMap<>();
    private int nextMarkerId = 0;

    public LeafletMap() {

        // define what map service we want and initialize the map
        getElement().callJsFunction("addTileLayer", OPEN_STREET_MAP_LAYER, OPEN_STREET_MAP_ATTRIBUTION);

        // add internal client side listener for marker clicks
        addListener(InternalMarkerClickEvent.class, e -> {

            // map id to server-side object
            MapLocation spot = e.getId() == null ? null : idToMarker.get(e.getId());

            // fire real event
            fireEvent(new MarkerClickEvent(this, true, spot));
        });
    }

    /**
     * Add all given markers to the map and zoom/pan the map so that all markers are
     * visible.
     */
    public void addMarkersAndZoom(List<MapLocation> spots) {

        // Add all
        spots.forEach(this::addMarker);

        // find top left and bottom right, then zoom the map
        double lat1 = spots.stream().map(s -> s.getLatitude()).sorted().findFirst().orElse(0d);
        double long1 = spots.stream().map(s -> s.getLongitude()).sorted().findFirst().orElse(0d);

        int size = spots.size();
        double lat2 = spots.stream().map(s -> s.getLatitude()).sorted().skip(size - 1).findFirst().orElse(0d);
        double long2 = spots.stream().map(s -> s.getLongitude()).sorted().skip(size - 1).findFirst().orElse(0d);

        fitBounds(lat1, long1, lat2, long2);
    }

    /**
     * Add a marker to the map.
     */
    public void addMarker(MapLocation spot) {
        // save id for later use in events
        idToMarker.put(nextMarkerId, spot);

        // call client side to actually add marker
        getElement().callJsFunction("addMarker", spot.getLatitude(), spot.getLongitude(), spot.getName(), nextMarkerId++);
    }

    /**
     * Zoom/Pan map to the given rectangle
     *
     * @param lat1  top left corner latitude
     * @param long1 top left corner longitude
     * @param lat2  bottom right corner latitude
     * @param long2 bottom right corner longitude
     */
    public void fitBounds(double lat1, double long1, double lat2, double long2) {

        // the coords need to be JavaScript arrays. We can create them easily using the
        // JSON util class.

        JsonArray coords = Json.createArray();

        JsonArray corner1 = Json.createArray();
        corner1.set(0, lat1);
        corner1.set(1, long1);

        JsonArray corner2 = Json.createArray();
        corner2.set(0, lat2);
        corner2.set(1, long2);

        coords.set(0, corner1);
        coords.set(1, corner2);

        // 'coords' is now a JSON array that looks like this:
        // [[lat1, long1], [lat2, long2]]

        getElement().callJsFunction("fitBounds", coords);
    }

    /**
     * Allow users to be notified when the map in general is clicked.
     */
    public Registration addMapClickListener(ComponentEventListener<MapClickEvent> listener) {
        return addListener(MapClickEvent.class, listener);
    }

    /**
     * Allow users to be notified when a marker is clicked.
     */
    public Registration addMarkerClickListener(ComponentEventListener<MarkerClickEvent> listener) {
        return addListener(MarkerClickEvent.class, listener);
    }

    /**
     * Listener for user clicks on a {@link MapLocation}
     * <p>
     * This is the event received when registering for
     * {@link LeafletMap#addMarkerClickListener(ComponentEventListener)}
     */
    public static class MarkerClickEvent extends ComponentEvent<LeafletMap> {
        private final MapLocation marker;

        public MarkerClickEvent(LeafletMap source, boolean fromClient, MapLocation marker) {
            super(source, fromClient);
            this.marker = marker;
        }

        public MapLocation getMarker() {
            return marker;
        }
    }

    /**
     * Listener for user clicks on the map.
     * <p>
     * This is the event received when registering for
     * {@link LeafletMap#addMapClickListener(ComponentEventListener)}
     */
    @DomEvent("map-click") // Defined in JS method _mapClicked()
    public static class MapClickEvent extends ComponentEvent<LeafletMap> {

        private double latitude;
        private double longitude;

        public MapClickEvent(LeafletMap source, boolean fromClient, @EventData("event.detail.lat") double latitude,
                @EventData("event.detail.lng") double longitude) {
            super(source, fromClient);
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    /**
     * Internal marker click event implementation that is then mapped to a
     * {@link MarkerClickEvent}.
     * <p>
     * We use an internal event because we want to only send an ID to the client and
     * back, and not the actual {@link MapLocation} object. This way, we store
     * {@link MapLocation} objects only on the server.
     * <p>
     * This event is fired client-side, in leaflet-map.js in the method
     * _markerClicked(id) and received in the constructor of the main class
     * {@link LeafletMap#LeafletMap(LeafletMap, boolean, Integer)}
     * <p>
     * This class needs to be public so that Vaadin can use it internally, but isn't
     * meant for public use.
     */
    @DomEvent("marker-click") // Defined in JS method _markerClicked()
    public static class InternalMarkerClickEvent extends ComponentEvent<LeafletMap> {
        private final Integer id;

        public InternalMarkerClickEvent(LeafletMap source, boolean fromClient, @EventData("event.detail.id") Integer id) {
            super(source, fromClient);
            this.id = id;
        }

        public Integer getId() {
            return id;
        }
    }
}
