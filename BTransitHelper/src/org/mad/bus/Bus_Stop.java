package org.mad.bus;

import com.google.android.maps.GeoPoint;

/**
 * A stop has a stop code, a name and a location where it is.
 * @author karthik
 *
 */
public class Bus_Stop {
    private int stopCode;
    private String stopName;
    private GeoPoint location;
    public Bus_Stop(int sc, String name) {
        stopCode = sc;
        stopName = name;
    }
    public String toString() {
        return "Bus stop: " + stopCode + ", " + stopName;
    }
    /**
     * @return the stopCode
     */
    public int getStopCode() {
        return stopCode;
    }
    /**
     * @param stopCode the stopCode to set
     */
    public void setStopCode(int stopCode) {
        this.stopCode = stopCode;
    }
    /**
     * @return the stopName
     */
    public String getStopName() {
        return stopName;
    }
    /**
     * @param stopName the stopName to set
     */
    public void setStopName(String stopName) {
        this.stopName = stopName;
    }
    /**
     * @return the location
     */
    public GeoPoint getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

}
