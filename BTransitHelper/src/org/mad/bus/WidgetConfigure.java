package org.mad.bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

// -------------------------------------------------------------------------
/**
 *  Configuration activity for the bus widget, allows user to pick which stop
 *  they want to see on the widget.
 *
 *  @author Wilson
 *  @version Aug 21, 2012
 */
public class WidgetConfigure extends Activity {

	private int appWidgetId;
	private int currentStep;
	private final String PREFIX = "http://www.bt4u.org/BT4U_WebService.asmx";

	private AsyncTask task;
	private Animation slideOutLeft;
	private Animation slideInLeft;
	private Animation slideOutRight;
	private Animation slideInRight;
	private ListView routeListView;
	private View routeContainer;
	private ListView stopListView;
	private View stopContainer;
	private TextView nextBusTextView;
	private View nextBusContainer;
	private TextView progressBarTextView;
	private View progressBarContainer;
	private ArrayList<Bus_Route> routes;
	private String[] routeNames;
	private ArrayList<Bus_Stop> stopNames;
	private Context mContext;
	private String selectedRoute;
	private int selectedStop;
	private String departure;
	private Bus_XMLParser parser = new Bus_XMLParser();
	private URL url = null;
	@SuppressWarnings("unused")
	private HttpURLConnection conn;
	private BufferedReader buffer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED, getIntent());
		setContentView(R.layout.widget_configure);
		mContext = this;
		currentStep = 0;
		stopNames = new ArrayList<Bus_Stop>();

		Intent launchIntent = getIntent();
		Bundle extras = launchIntent.getExtras();
		if (extras != null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		routeListView = (ListView) findViewById(R.id.routeListView);
		stopListView = (ListView) findViewById(R.id.stopListView);
		//        nextBusTextView = (TextView) findViewById(R.id.nextBusText);
		progressBarTextView = (TextView) findViewById(R.id.progressBarText);
		routeContainer = findViewById(R.id.routeContainer);
		stopContainer = findViewById(R.id.stopContainer);
		//        nextBusContainer = findViewById(R.id.nextBusContainer);
		progressBarContainer = findViewById(R.id.progressBarContainer);

		slideOutLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1.0f, 0, 0f, 0, 0f);
		slideOutLeft.setDuration(500);
		slideInLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f, 0, 0f, 0, 0f);
		slideInLeft.setDuration(500);
		slideOutRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f, 0, 0f, 0, 0f);
		slideOutRight.setDuration(500);
		slideInRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0f, 0, 0f, 0, 0f);
		slideInRight.setDuration(500);

		task = new RouteTask().execute();
	}

	@Override
	public void onBackPressed() {
		task.cancel(true);
		switch (currentStep) {
		case 1:
			slideInRight.setAnimationListener(new AnimationInListener(routeContainer));
			routeContainer.startAnimation(slideInRight);
			slideOutRight.setAnimationListener(new AnimationOutListener(stopContainer));
			stopContainer.startAnimation(slideOutRight);
			currentStep--;
			break;
		case 2:
			slideInRight.setAnimationListener(new AnimationInListener(stopContainer));
			stopContainer.startAnimation(slideInRight);
			slideOutRight.setAnimationListener(new AnimationOutListener(nextBusContainer));
			nextBusContainer.startAnimation(slideOutRight);
			currentStep--;
			break;
		default:
			super.onBackPressed();
			break;
		}
	}



	// ----------------------------------------------------------
	/**
	 * Called when the add favorite button is pressed, adds a favorite to the
	 * favorites xml file.
	 * @param v The view.
	 */
	public void addFavoritePressed(View v) {
		if (selectedRoute != null) {
			Editor editor = getSharedPreferences(Widget.SHARED_PREFS, Context.MODE_PRIVATE).edit();
			editor.putString(String.valueOf(appWidgetId) + "routeShortName", selectedRoute);
			editor.putString(String.valueOf(appWidgetId) + "stopCode", ""+selectedStop);
			editor.commit();

			// Update widget for first time.
			Intent intent = new Intent();
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, Widget.class)));
			intent.setData(Uri.withAppendedPath(Uri.parse(Widget.SHARED_PREFS + "://widget/id/"), String.valueOf(0)));

			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), pendingIntent);

			Intent result = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			setResult(RESULT_OK, result);
			finish();
		}
	}

	private class RouteTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressBarTextView.setText(R.string.loading_routes);
			progressBarContainer.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {

			routeNames = getResources().getStringArray(R.array.routes1);
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			progressBarContainer.setVisibility(View.GONE);

			routeListView.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, routeNames));
			routeListView.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
					Bus_Constants cons = new Bus_Constants();
					//                    selectedRoute = Bus_Constants.LTOS_ROUTE_NAME.get(routeNames[which]);
					selectedRoute = routeNames[which];
					slideOutLeft.setAnimationListener(new AnimationOutListener(routeContainer));
					routeContainer.startAnimation(slideOutLeft);

					currentStep = 1;
					task = new StopTask().execute();
				}
			});
		}

		@Override
		protected void onCancelled() {
			progressBarContainer.setVisibility(View.GONE);
		}
	}

	private class StopTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressBarTextView.setText(R.string.loading_stops);
			progressBarContainer.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			stopNames.clear();
			if (isNetworkAvailable()) {
				Log.d("OLOLOL" , selectedRoute);
				for (Bus_Stop busStop : getStopsForThisRoute(selectedRoute)) {
					Log.d("OLOLOL" , selectedRoute);
					stopNames.add(busStop);
				}
			}
			//            Collections.sort(stopNames);
			return null;
		}

		private ArrayList<Bus_Stop> getStopsForThisRoute(String selectedRoute) {
			ArrayList<Bus_Stop> result = new ArrayList<Bus_Stop>();
			String XMLResult = "";
			String request = PREFIX + "/GetScheduledStopCodes?routeShortName=" + selectedRoute;
			XMLResult = get(request);
			if (XMLResult != null) {
				result = parser.parseStopsOnRoute(XMLResult);
			} else {
				return null;
			}
			return result;
		}
		private String get(final String request) {
			String XMLResult = "";
			String line;
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
		protected void onPostExecute(Void param) {
			progressBarContainer.setVisibility(View.GONE);
			final ArrayList<String> temp = new ArrayList<String>();
			temp.clear();
			for (Bus_Stop s : stopNames) {
				temp.add(s.getStopName());
			}
			stopListView.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, temp));
			slideInLeft.setAnimationListener(new AnimationInListener(stopContainer));
			stopContainer.startAnimation(slideInLeft);

			stopListView.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
					for (Bus_Stop busStop : stopNames) {
						if (busStop.getStopName().equals(temp.get(which))) {
							selectedStop = busStop.getStopCode();
							break;
						}
					}
					if (selectedRoute != null) {
						Editor editor = getSharedPreferences(Widget.SHARED_PREFS, Context.MODE_PRIVATE).edit();
						editor.putString(String.valueOf(appWidgetId) + "routeShortName", selectedRoute);
						editor.putString(String.valueOf(appWidgetId) + "stopCode", ""+selectedStop);
						editor.commit();

						// Update widget for first time.
						Intent intent = new Intent();
						intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
						intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, Widget.class)));
						intent.setData(Uri.withAppendedPath(Uri.parse(Widget.SHARED_PREFS + "://widget/id/"), String.valueOf(0)));

						PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
						AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
						alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), pendingIntent);

						Intent result = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
						setResult(RESULT_OK, result);
						finish();
					}
					//                    selectedStop = Integer.valueOf(stopNames.get(which));
					//                    slideOutLeft.setAnimationListener(new AnimationOutListener(stopContainer));
					//                    stopContainer.startAnimation(slideOutLeft);

					//                    task = new DepartureTask().execute();
					//                    currentStep = 2;
				}
			});
		}

		@Override
		protected void onCancelled() {
			progressBarContainer.setVisibility(View.GONE);
		}
	}

	//    private class DepartureTask extends AsyncTask<Void, Void, Void> {
	//
	//        @Override
	//        protected void onPreExecute() {
	//            progressBarTextView.setText(R.string.loading_departure);
	//            progressBarContainer.setVisibility(View.VISIBLE);
	//        }
	////
	////        @Override
	////        protected Void doInBackground(Void... params) {
	////        	
	////            ArrayList<String> result = BTransitHelper.getNextDepartures(selectedRoute.getShortName(), selectedStop.getStopCode());
	////            if (result != null && result.size() > 0) {
	////                departure = result.get(0);
	////            }
	////            return null;
	////        }
	//
	//        @Override
	//        protected void onPostExecute(Void param) {
	//            progressBarContainer.setVisibility(View.GONE);
	//
	//            if (departure != null) {
	//                nextBusTextView.setText("Next departure is at " + departure);
	//            }
	//            else {
	//                nextBusTextView.setText(R.string.load_failed);
	//            }
	//
	//            slideInLeft.setAnimationListener(new AnimationInListener(nextBusContainer));
	//            nextBusContainer.startAnimation(slideInLeft);
	//        }
	//
	//        @Override
	//        protected void onCancelled() {
	//            progressBarContainer.setVisibility(View.GONE);
	//        }
	//    }

	private class AnimationOutListener implements AnimationListener {

		private View v;

		public AnimationOutListener(View v) {
			super();
			this.v = v;
		}

		public void onAnimationEnd(Animation animation) {
			v.setVisibility(View.GONE);
		}

		public void onAnimationRepeat(Animation animation) {
			// Nothing
		}

		public void onAnimationStart(Animation animation) {
			// Nothing
		}
	}

	private class AnimationInListener implements AnimationListener {

		private View v;

		public AnimationInListener(View v) {
			super();
			this.v = v;
		}

		public void onAnimationEnd(Animation animation) {
			// Nothing
		}

		public void onAnimationRepeat(Animation animation) {
			// Nothing
		}

		public void onAnimationStart(Animation animation) {
			v.setVisibility(View.VISIBLE);
		}
	}
	/**
	 * Checks if the network is available
	 * 
	 * @return
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

}
