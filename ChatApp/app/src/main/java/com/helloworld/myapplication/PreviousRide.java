package com.helloworld.myapplication;


import java.io.Serializable;
import java.util.Date;

public class PreviousRide implements Serializable {
    String driverID;
    String riderID;
    String toLocation; // dropOfLocation
    String fromLocation; //pickUpLocation
    Date dateAndTime;

    public PreviousRide (){
    }

    public String getDriverID() {
        return driverID;
    }

    public String getRiderID() {
        return riderID;
    }

    public String getToLocation() {
        return toLocation;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public Date getDateAndTime() {
        return dateAndTime;
    }

    public PreviousRide(String driverID, String riderID, String toLocation, String fromLocation, Date dateAndTime) {
        this.driverID = driverID;
        this.riderID = riderID;
        this.toLocation = toLocation;
        this.fromLocation = fromLocation;
        this.dateAndTime = dateAndTime;
    }
}
