package org.vaadin.example.leaflet.data;

/**
 * Simple data object representing a marker on a map.
 */
public class MapLocation {

    private double latitude;
    private double longitude;
    private String name;

    public MapLocation() {

    }

    public MapLocation(double latitude, double longitude, String name) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
