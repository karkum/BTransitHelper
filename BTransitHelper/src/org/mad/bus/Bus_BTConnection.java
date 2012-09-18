package org.mad.bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.os.AsyncTask;

/**
 * An API for getting information about BT transit busses.
 * 
 * @author karthik
 * 
 */
public class Bus_BTConnection {

    private URL url = null;
    @SuppressWarnings("unused")
    private HttpURLConnection conn;
    private BufferedReader buffer;
    private Bus_XMLParser parser = new Bus_XMLParser();
    private final String PREFIX = "http://www.bt4u.org/BT4U_WebService.asmx";
    String line;
	@SuppressWarnings("unused")
	private Context mContext;
	public Bus_BTConnection (Context cont) {
		mContext = cont;
	}
    /**
     * Get information about all the busses that are running right now.
     * 
     * @return A list of busses that are running
     */
    public ArrayList<Bus_Bus> getCurentBusInfo() {
        ArrayList<Bus_Bus> result = new ArrayList<Bus_Bus>();
        String request = PREFIX + "/GetCurrentBusInfo";
        Bus_Task task = new Bus_Task("Getting current busses...");
        task.execute(request);
        String XMLResult;
        try {
            XMLResult = (String) task.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            XMLResult = null;
            e.printStackTrace();
        } catch (ExecutionException e) {
            XMLResult = null;
            e.printStackTrace();
        } catch (TimeoutException e) {
            XMLResult = null;
            e.printStackTrace();
        }
        if (XMLResult != null) {
            result = parser.parseCurrentBusInfo(XMLResult);
        } else {
            return null;
        }
        //for (Bus_Bus b : result) {
        //    System.out.println(b);
        //}
        return result;
    }

    /**
     * Get all the routes that go through this stop
     * 
     * @param stopCode
     *            the stop code we are querying
     * @return the list of routes that go through this stop.
     */
    public ArrayList<Bus_Route> getRoutesAtStop(int stopCode) {
        ArrayList<Bus_Route> result = new ArrayList<Bus_Route>();
        String XMLResult = "";
        String request = PREFIX + "/GetScheduledRoutes?stopCode=" + stopCode;
        Bus_Task task = new Bus_Task("Getting Routes...");
        task.execute(request);
        try {
            XMLResult = (String) task.get(45, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            XMLResult = null;
            e.printStackTrace();
        } catch (ExecutionException e) {
            XMLResult = null;
            e.printStackTrace();
        } catch (TimeoutException e) {
            XMLResult = null;
            e.printStackTrace();
        }
        if (XMLResult != null) {
            result = parser.parseRoutesAtStop(XMLResult);
        } else {
            return null;
        }
        return result;
    }

    /**
     * Get all the stops that this route goes through
     * 
     * @param route
     *            the route we are querying
     * @return the list of stops that we this route goes through.
     */
    public ArrayList<Bus_Stop> getStopsOnRoute(String route) {
        ArrayList<Bus_Stop> result = new ArrayList<Bus_Stop>();
        String XMLResult = "";
        String request = PREFIX + "/GetScheduledStopCodes?routeShortName=" + route;
        Bus_Task task = new Bus_Task("Getting stops...");
        task.execute(request);
        try {
            XMLResult = (String) task.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            XMLResult = null;
            e.printStackTrace();
        } catch (ExecutionException e) {
            XMLResult = null;
            e.printStackTrace();
        } catch (TimeoutException e) {
            XMLResult = null;
            e.printStackTrace();
        }
        if (XMLResult != null) {
            result = parser.parseStopsOnRoute(XMLResult);
        } else {
            return null;
        }
        return result;
    }

    /**
     * Get the few departures from this stop that this route goes through
     * 
     * @param route
     *            the route we are querying
     * @return the list of stops that we this route goes through.
     */
    public ArrayList<Bus_Time> getNextDepartures(String route, int stopCode) {
        ArrayList<Bus_Time> result = new ArrayList<Bus_Time>();
        String XMLResult = null;
        String request = PREFIX + "/GetNextDepartures?routeShortName=" + route
                + "&stopCode=" + stopCode;
        Bus_Task task = new Bus_Task("Getting next departures...");
        task.execute(request);
        try {
            XMLResult = (String) task.get(45, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            XMLResult = null;
            e.printStackTrace();
        } catch (ExecutionException e) {
            XMLResult = null;
            e.printStackTrace();
        } catch (TimeoutException e) {
            XMLResult = null;
            e.printStackTrace();
        }
        if (XMLResult != null) {
            result = parser.parseNextDepartures(XMLResult);
        } else {
            return null;
        }
        return result;
    }

    /**
     * Task that perform the network transaction
     * @author karthik
     *
     */
    @SuppressWarnings({ })
    private class Bus_Task extends AsyncTask <String, String, String>
    {
    	@SuppressWarnings("unused")
		private String task;
    	public Bus_Task(String t) {
    		task = t;
    	}
        private String get(final String request) {
            String XMLResult = "";
            try {
            	StringBuffer buff = new StringBuffer();
                url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                buffer = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                while ((line = buffer.readLine()) != null) {
                    buff.append(line);
                }
                XMLResult = buff.toString();
                buffer.close();
            } catch (MalformedURLException e) {
                System.out.println("BAD URL");
                e.printStackTrace();
            } catch (ProtocolException e) {
                System.out.println("Could not connect");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Could not read");
                e.printStackTrace();
            }
            return XMLResult;
        }
        @Override
        protected String doInBackground(String... arg0) {
            return get((String)arg0[0]);
        }
        
//        @Override
//		protected void onPreExecute() {
//        	dialogProgressBar = ProgressDialog.show(mContext, "Fetching information...",
//    				task);
//		}
//        @Override
//		protected void onPostExecute(String param) {
//        	dialogProgressBar.dismiss();
//		}
//        @Override
//        protected void onCancelled() {
//        	dialogProgressBar.dismiss();
//		}
    }
    
}
