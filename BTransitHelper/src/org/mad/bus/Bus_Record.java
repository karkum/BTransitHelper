package org.mad.bus;


/**
 * This class represents a record that we are storing. It has a latitute, longitude
 * and stop code.
 * 
 * @author Karthik Kumar (kkumar91)
 */
public class Bus_Record {
	//~ Instance/static variables .........................................

	// ----------------------------------------------------------
	/* The lat coordinate we are trying to store*/
	private int x;

	/* The long coordinate we are trying to store*/
	private int y;

	/* The city name coordinate we are trying to store*/
	private int stopCode;

	//~ Constructors .......................................................

	// ----------------------------------------------------------
	/**
	 * Constructor. Creates a new record with the given parameters. Encodes these 
	 * variables into a byte array (message) and sets the size.
	 * @param int the x variable to set
	 * @param int the y variable to set
	 * @param int the stopcode  to set
	 */
	public Bus_Record (int x, int y, int stopCode) {
		this.setX(x);
		this.setY(y);
		this.setStopCode(stopCode);
	}

	//~ Public Methods ........................................................

	// ----------------------------------------------------------
	/**
	 * 
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param cityName the cityName to set
	 */
	public void setStopCode(int cityName) {
		this.stopCode = cityName;
	}

	/**
	 * @return the cityName
	 */
	public int getStopCode() {
		return stopCode;
	}
	
	public String toString() {
		return "(" + x +", " + y + "), " + stopCode; 
	}
}