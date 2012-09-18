package org.mad.bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

// -------------------------------------------------------------------------
/**
 *  The widget that shows when the next departure is for a user selected stop.
 *
 *  @author Wilson
 *  @version Aug 21, 2012
 */
public class Widget extends AppWidgetProvider {

	private boolean isLoading = false;
    private URL url = null;
    @SuppressWarnings("unused")
    private HttpURLConnection conn;
    private BufferedReader buffer;
    private Bus_XMLParser parser = new Bus_XMLParser();
    private final String PREFIX = "http://www.bt4u.org/BT4U_WebService.asmx";
    String line;

	/**
	 * Name for shared preferences used by the widget.
	 */
	public static final String SHARED_PREFS = "hokiehelperwidget";

	@Override
	public void onUpdate(final Context context, AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		if (!isLoading) {
			isLoading = true;

			new Thread(new Runnable() {

				public void run() {
					for (int appWidgetId : appWidgetIds) {
						RemoteViews views = getView(context, appWidgetId);
						if (views != null) {
							AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
						}
					}

					isLoading = false;
				}
			}).start();
		}
	}

	private RemoteViews getView(Context context, int appWidgetId) {
		SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
		String routeShortName = sharedPrefs.getString(String.valueOf(appWidgetId) + "routeShortName", null);
		String stopCode = sharedPrefs.getString(String.valueOf(appWidgetId) + "stopCode", null);

		RemoteViews views = null;
		if (routeShortName != null && stopCode != null) {
			views = new RemoteViews(context.getPackageName(), R.layout.widget);
			
//			if (isNetworkAvailable()) {


				ArrayList<Bus_Time> times = getNextDepartures(routeShortName, Integer.valueOf(stopCode));

				if (times != null && times.size() > 0) {
					String departure = times.get(0).toString();
					views.setTextViewText(R.id.departureTimeText, departure);
					views.setTextViewText(R.id.routeText, routeShortName + ": " + stopCode);
				}
				else {
					views.setTextViewText(R.id.departureTimeText, "Failed to load");
				}

				// Update widget if clicked.
				Intent intent = new Intent();
				intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {appWidgetId});
				intent.setData(Uri.withAppendedPath(Uri.parse(SHARED_PREFS + "://widget/id/"), String.valueOf(appWidgetId)));

				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				views.setOnClickPendingIntent(R.id.widgetMainWrapper, pendingIntent);
			}
//		}

		return views;
	}

	private ArrayList<Bus_Time> getNextDepartures(String routeShortName,
			Integer stopCode) {
		ArrayList<Bus_Time> result = new ArrayList<Bus_Time>();
        String XMLResult = null;
        String request = PREFIX + "/GetNextDepartures?routeShortName=" + routeShortName
                + "&stopCode=" + stopCode;
        XMLResult = (String) get(request);
        
        if (XMLResult != null) {
            result = parser.parseNextDepartures(XMLResult);
        } else {
            return null;
        }
        return result;
	}

	private String get(String request) {
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
	
}
