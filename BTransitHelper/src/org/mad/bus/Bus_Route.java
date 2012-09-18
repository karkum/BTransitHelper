package org.mad.bus;

/**
 * A single route has a short name, a long name and a list of stops.
 * @author karthik
 *
 */
public class Bus_Route {
    private String shortName;
    private String longName;
    private Bus_Stop [] stops;
    
    public Bus_Route(String sName, String lName) {
        shortName = sName;
        longName = lName;
    }
    
    public String toString() {
        return shortName + "=" + longName;
    }
    
    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public String getLongName() {
        return longName;
    }
    public void setLongName(String longName) {
        this.longName = longName;
    }
    public Bus_Stop[] getStops() {
        return stops;
    }
    public void setStops(Bus_Stop[] stops) {
        this.stops = stops;
    }
    
}
