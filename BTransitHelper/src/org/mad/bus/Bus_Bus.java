package org.mad.bus;

import com.google.android.maps.GeoPoint;

/**
 * Represents a bus, only holds the route and the location of the bus. Can be
 * extended to include more things.
 * @author karthik
 *
 */
public class Bus_Bus {
    private Bus_Route route;
    private GeoPoint location;
    
    public String toString() {
        return route.toString() + " " +location.toString();
    }
    
    public Bus_Bus(Bus_Route rout, GeoPoint loc) {
        route = rout;
        location = loc;
    }
    /**
     * @return the route
     */
    public Bus_Route getRoute() {
        return route;
    }
    /**
     * @param route the route to set
     */
    public void setRoute(Bus_Route route) {
        this.route = route;
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
