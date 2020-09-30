package com.helloworld.myapplication;

import java.io.Serializable;

public class UserCurrentLocation implements Serializable {
    Double latitude;
    Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public UserCurrentLocation() {
    }

    @Override
    public String toString() {
        return "UserCurrentLocation{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
