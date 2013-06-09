package com.stehno.oldemo.dto;

import java.io.Serializable;

/**
 * A simple object representation of a geo-located point of interest.
 */
public class PointOfInterest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private double latitude;
    private double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
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
}
