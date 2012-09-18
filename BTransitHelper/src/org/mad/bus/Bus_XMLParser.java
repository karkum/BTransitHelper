package org.mad.bus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.android.maps.GeoPoint;

/**
 * Parses a given XML document for different requests.
 * @author karthik
 *
 */
public class Bus_XMLParser {

    public ArrayList<Bus_Bus> parseCurrentBusInfo(String XMLResult) {
        ArrayList<Bus_Bus> result = new ArrayList<Bus_Bus>();
        //        String str = XMLResult.replace("null", "");
        InputStream stream = new ByteArrayInputStream(XMLResult.getBytes());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        Document doc;
        try {
            doc = db.parse(stream);
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Element root = doc.getDocumentElement();
        NodeList items = root.getElementsByTagName("RTFInfo");
        result = new ArrayList<Bus_Bus>(items.getLength());
        for (int i = 0; i < items.getLength(); i++) {
            Node rtfinfo = items.item(i);
            NodeList attributes = rtfinfo.getChildNodes();
            String shortName = attributes.item(3).getTextContent();
            double lat = Double.valueOf(attributes.item(17).getTextContent());
            double lon = Double.valueOf(attributes.item(19).getTextContent());
            String longName = Bus_Constants.STOL_ROUTE_NAMES.get(shortName);
            if (longName == null)
                return result;
            Bus_Route route = new Bus_Route(shortName, longName);
            Bus_Bus bus = new Bus_Bus(route, new GeoPoint((int)( lat
                    * Math.pow(10, 6)), (int)( lon * Math.pow(10, 6))));
            result.add(bus);
        }
        return result;
    }

    public ArrayList<Bus_Route> parseRoutesAtStop(String XMLResult) {
        ArrayList<Bus_Route> result;
        InputStream stream = new ByteArrayInputStream(XMLResult.getBytes());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        Document doc;
        try {
            doc = db.parse(stream);
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Element root = doc.getDocumentElement();
        NodeList items = root.getElementsByTagName("ScheduledRoutes");
        result = new ArrayList<Bus_Route>(items.getLength());
        for (int i = 0; i < items.getLength(); i++) {
            Node rtfinfo = items.item(i);
            NodeList attributes = rtfinfo.getChildNodes();
            String stop = attributes.item(1).getTextContent();
            Bus_Route route = new Bus_Route(stop, Bus_Constants.STOL_ROUTE_NAMES.get(stop));
            result.add(route);
        }
        return result;
    }

    public ArrayList<Bus_Stop> parseStopsOnRoute(String XMLResult) {
        ArrayList<Bus_Stop> result;
        //        String newStr = XMLResult.replace("null<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

        InputStream stream = new ByteArrayInputStream(XMLResult.getBytes());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        Document doc;
        try {
            doc = db.parse(stream);
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Element root = doc.getDocumentElement();
        NodeList items = root.getElementsByTagName("ScheduledStops");
        result = new ArrayList<Bus_Stop>(items.getLength());
        for (int i = 0; i < items.getLength(); i++) {
            Node rtfinfo = items.item(i);
            NodeList attributes = rtfinfo.getChildNodes();
            int stopCode = Integer.valueOf(attributes.item(1).getTextContent());
            String longName = attributes.item(3).getTextContent();
            Bus_Stop stop = new Bus_Stop(stopCode, longName);
            result.add(stop);
        }
        return result;
    }

    public ArrayList<Bus_Time> parseNextDepartures(String XMLResult) {
        ArrayList<Bus_Time> result;
        InputStream stream = new ByteArrayInputStream(XMLResult.getBytes());
        //        byte[] re = new String("<DocumentElement><NextDepartures><RouteShortName>HDG</RouteShortName><TripPointID>36bb9a2e-a1e3-4372-aaa5-2a09265c2620</TripPointID><PatternPointName>Harding/Apperson Wbnd</PatternPointName><AdjustedDepartureTime>6/14/2012 8:02:33 PM</AdjustedDepartureTime></NextDepartures><NextDepartures><RouteShortName>HDG</RouteShortName><TripPointID>e7658582-e803-4f0c-be2f-739d10e54b93</TripPointID><PatternPointName>Harding/Apperson Wbnd</PatternPointName><AdjustedDepartureTime>6/14/2012 9:01:56 PM</AdjustedDepartureTime></NextDepartures><NextDepartures><RouteShortName>HDG</RouteShortName><TripPointID>bbe0385f-8f48-4a70-a0ee-46389a5a9a28</TripPointID><PatternPointName>Harding/Apperson Wbnd</PatternPointName><AdjustedDepartureTime>6/14/2012 10:01:38 PM</AdjustedDepartureTime><TripNotes>Last Departure from Stop</TripNotes></NextDepartures></DocumentElement>").getBytes();
        //        InputStream stream = new ByteArrayInputStream(re);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        Document doc;
        try {
            doc = db.parse(stream);
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Element root = doc.getDocumentElement();
        NodeList items = root.getElementsByTagName("NextDepartures");
        result = new ArrayList<Bus_Time>(items.getLength());
        for (int i = 0; i < items.getLength(); i++) {
            Node rtfinfo = items.item(i);
            NodeList attributes = rtfinfo.getChildNodes();
            String time = attributes.item(5).getTextContent();
            DateFormat format = new SimpleDateFormat("M/dd/yyyy h:mm:ss a");
            Date date = null;
            Calendar actualTime = null;
            try {
                date = (Date)format.parse(time);
                actualTime = Calendar.getInstance();
                actualTime.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Bus_Time stop = new Bus_Time(actualTime); 
            result.add(stop);
            if (attributes.item(7) != null) {
                System.out.println("LAST STOP");
            }
        }
        return result;
    }

}
