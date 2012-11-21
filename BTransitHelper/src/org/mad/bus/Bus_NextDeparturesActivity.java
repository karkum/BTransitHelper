package org.mad.bus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Activity to show the page for giving next departures. It has a spinner for
 * choosing the route, a spinner for choosing the stop, which is dynamically
 * filled and a button for getting the times and a list view for setting alerts.
 * Once the route is chosen, the stop is highlighted, once the stop is chosen,
 * the button is availble to be clicked.
 * 
 * @author karthik
 * 
 */
public class Bus_NextDeparturesActivity extends SherlockActivity {
	private static String route;
	private int checked;
	ArrayList<Bus_Stop> listOfStops;
	private ArrayList<Bus_Time> listOfNextDepartures;
	private static int stop;
	private static int chosen;
	private Spinner route_spinner;
	private Spinner stop_spinner;
	private Button button;
	private ListView botList;
	private Bus_BTConnection conn = new Bus_BTConnection(Bus_NextDeparturesActivity.this);

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.departures);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Departures");

		button = (Button) findViewById(R.id.getdepartures);
		button.setClickable(false);
		button.setEnabled(false);
		route_spinner = (Spinner) findViewById(R.id.spinnerroute);
		botList = (ListView) findViewById(R.id.listOfDepartures);

		stop_spinner = (Spinner) findViewById(R.id.spinnerstop);

		String[] arr = getApplicationContext().getResources().getStringArray(
				R.array.just_routes);
		ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(this,
				R.layout.custom_spinner, arr);
		stop_spinner.setEnabled(false);
		stop_spinner.setClickable(false);
		routeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		route_spinner.setAdapter(routeAdapter);

		route_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			/**
			 * Make the other spinner and button gryed out until they select a
			 * route. After the select a route, go online and get the stops for
			 * this route. Then show the other spinner.
			 */
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				if (pos == 0) {
					route = null;
				} else {
					route = Bus_Constants.LTOS_ROUTE_NAME.get((getResources()
							.getStringArray(R.array.just_routes)[pos]));

					if (isNetworkAvailable()) {
						
						listOfStops = conn.getStopsOnRoute(route);
						
						if (listOfStops == null || listOfStops.size() == 0) {
							Toast.makeText(Bus_NextDeparturesActivity.this,
									"Bus Route currently not in service",
									Toast.LENGTH_SHORT).show();
						}
						String[] arr = new String[listOfStops.size()];
						for (int i = 0; i < listOfStops.size(); i++) {
							Bus_Stop st = listOfStops.get(i);
							arr[i] = st.getStopCode() + " - "
									+ st.getStopName();
						}
						ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(
								Bus_NextDeparturesActivity.this,
								// android.R.layout.simple_spinner_item,
								R.layout.custom_spinner, arr);
						stopAdapter
								.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						stop_spinner.setAdapter(stopAdapter);
						stop_spinner.setEnabled(true);
						stop_spinner.setClickable(true);
					} else {
						Toast.makeText(Bus_NextDeparturesActivity.this,
								"No Network Connection.", Toast.LENGTH_LONG)
								.show();
					}
				}

			}

			public void onNothingSelected(AdapterView<?> arg0) {
				route = null;
			}

		});
		stop_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				/**
				 * Once they select a route, show this spinner and the button.
				 */
				if (listOfStops != null) {
					Bus_Stop s = listOfStops.get(pos);
					stop = s.getStopCode();
					button.setEnabled(true);
					button.setClickable(true);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
			}

		});

		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (isNetworkAvailable()) {
					listOfNextDepartures = conn.getNextDepartures(route, stop);
					if (listOfNextDepartures.size() != 0) {

						String[] arr = new String[listOfNextDepartures.size()];
						for (int i = 0; i < listOfNextDepartures.size(); i++) {
							Bus_Time t = listOfNextDepartures.get(i);
							if (t != null) 
								arr[i] = t.toString();
						}
						ArrayAdapter<String> adapt = new ArrayAdapter<String>(
								Bus_NextDeparturesActivity.this,
								R.layout.custom_list_item, arr);
						botList.setAdapter(adapt);
						Toast.makeText(Bus_NextDeparturesActivity.this,
								"Click on a time to set alert",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(Bus_NextDeparturesActivity.this,
								"Sorry, no busses are running at that stops.",
								Toast.LENGTH_SHORT).show();

					}
				} else {
					Toast.makeText(Bus_NextDeparturesActivity.this,
							"No Network Connection.", Toast.LENGTH_LONG).show();
				}
			}
		});

		botList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				final Bus_Time selected = listOfNextDepartures.get(position);
				final String[] nums = { "1", "5", "10", "15", "20" };

				AlertDialog.Builder builder = new AlertDialog.Builder(
						Bus_NextDeparturesActivity.this);
				builder.setTitle("Choose alert time");
				builder.setSingleChoiceItems(nums, checked,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								chosen = Integer.valueOf(nums[item]);
							}
						})
						.setPositiveButton("Set Alert",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										int timeBefore = chosen;
										dialog.dismiss();
										Calendar leaving = selected
												.getDepartureTime();
										leaving.add(Calendar.MINUTE, -1
												* timeBefore);
										DateFormat format = new SimpleDateFormat(
												"hh:mm:ss a");
										String finalTime = format
												.format(leaving.getTime());
										Toast.makeText(
												Bus_NextDeparturesActivity.this,
												"Alert set for " + finalTime,
												Toast.LENGTH_LONG).show();

										Intent intent = new Intent(
												Bus_NextDeparturesActivity.this,
												Bus_AlarmReceiver.class);
										Bundle extras = new Bundle();
										extras.putString("ROUTE", route);
										extras.putString("STOP", "" + stop);
										extras.putString("TIME",
												selected.toString());
										extras.putInt("BEFORE", timeBefore);
										intent.putExtras(extras);
										AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
										int requestID = (int) System.currentTimeMillis();
										PendingIntent pendingIntent = PendingIntent
												.getActivity(Bus_NextDeparturesActivity.this,
														requestID, intent, 0);

										alarmManager.set(
												AlarmManager.RTC_WAKEUP,
												leaving.getTimeInMillis(),
												pendingIntent);

									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

										/*
										 * User clicked No so do some stuff
										 */
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}

		});

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

	/**
	 * Handles the clicking of action bar icons.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
