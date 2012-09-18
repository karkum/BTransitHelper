package org.mad.bus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 * A class that works as a wrapper for Calendar
 * @author karthik
 *
 */
public class Bus_Time {
    
    private Calendar departureTime;
    public Bus_Time(Calendar time) {
        departureTime = time;
    }
    
    public String toString() {

        DateFormat format = new SimpleDateFormat("hh:mm:ss a");
        return format.format(departureTime.getTime());
    }
    /**
     * @return the departureTime
     */
    public Calendar getDepartureTime() {
        return departureTime;
    }

    /**
     * @param departureTime the departureTime to set
     */
    public void setDepartureTime(Calendar departureTime) {
        this.departureTime = departureTime;
    }
}
