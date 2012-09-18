package org.mad.bus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
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

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Activity for Bus favorites. It allows the user to add and remove favorites
 * and see the times when favorite buses will depart. I use a textfile "favorites.txt"
 * to store the favorites. It is stored in this format: "ROUTE:STOP",for ex: "HWD:1011".
 * 
 * TO add a favorite, open the file, read the file into a buffer, append the new fav to the 
 * buffer and then write the buffer to the file.
 * to delete a favorite, go through the file, compare each line to the one we want to delete
 * and remove that line, and put back the rest.
 * @author karthik
 *
 */
public class Bus_Favorites extends SherlockListActivity {

	private Spinner fav_route_spinner;
	private Spinner fav_stop_spinner;
	private static int chosen;
	private Dialog dialog;
	private Button fav_add;
	private Bus_BTConnection conn = new Bus_BTConnection(Bus_Favorites.this);
	private ArrayList<Bus_Stop> listOfStops;
	private String route;
	private int stop;
	private ArrayAdapter<String> stringAdapter;
	private final String FILENAME = "favorites.txt";
	private String r;
	private String s;
	private Bus_Time selected;
	ArrayList<String> list = new ArrayList<String>(100);

	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.favorites);
		// deleteFile(FILENAME);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Favorites");

		refreshList();

		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {

			//when a favorite is clicked, give them options to add alerts
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				try {
					FileInputStream fis = openFileInput(FILENAME);
					int iter = 0;
					String[] arr = new String[20];
					Scanner scan = new Scanner(fis);
					while (scan.hasNext()) {
						String line = scan.nextLine();
						if (iter == position) {
							int colon = line.indexOf(":");
							r = line.substring(0, colon);
							s = line.substring(colon + 1);

							if (isNetworkAvailable()) {
								final ArrayList<Bus_Time> listOfNextDepartures = conn
										.getNextDepartures(r,
												Integer.valueOf(s));
								if (listOfNextDepartures.size() != 0) {
									arr = new String[listOfNextDepartures
											.size()];
									for (int i = 0; i < listOfNextDepartures
											.size(); i++) {
										arr[i] = listOfNextDepartures.get(i)
												.toString();
									}
									final int checkedItem = 0;
									selected = listOfNextDepartures.get(0);
									AlertDialog.Builder builder = new AlertDialog.Builder(
											Bus_Favorites.this);
									builder.setTitle("Next Departures");
									builder.setSingleChoiceItems(
											arr,
											checkedItem,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int item) {
													selected = listOfNextDepartures
															.get(item);
												}
											})
											.setPositiveButton(
													"Set Alert",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int whichButton) {

															final String[] nums = { "1", "5", "10", "15", "20" };

															AlertDialog.Builder builder = new AlertDialog.Builder(
																	Bus_Favorites.this);
															builder.setTitle("Choose alert time");
															int checked = 0;
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
																							Bus_Favorites.this,
																							"Alert set for " + finalTime,
																							Toast.LENGTH_LONG).show();

																					Intent intent = new Intent(
																							Bus_Favorites.this,
																							Bus_AlarmReceiver.class);
																					Bundle extras = new Bundle();
																					extras.putString("ROUTE", r);
																					extras.putString("STOP", "" + Integer.valueOf(s));
																					extras.putString("TIME",
																							selected.toString());
																					extras.putInt("BEFORE", timeBefore);
																					intent.putExtras(extras);
																					AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
																					int requestID = (int) System.currentTimeMillis();
																					PendingIntent pendingIntent = PendingIntent
																							.getActivity(Bus_Favorites.this,
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
															
															
															
															
															
															
															
															
															
															
															
															
															
															
															
															
															
															
															
//															
//															setAlertDialog = new Dialog(
//																	Bus_Favorites.this);
//
//															setAlertDialog
//																	.setContentView(R.layout.custom_dialog);
//															setAlertDialog
//																	.setTitle("Set alert");
//															setAlertDialog
//																	.setOwnerActivity(Bus_Favorites.this);
//
//															np = (NumberPicker) setAlertDialog
//																	.findViewById(R.id.number_picker);
//
//															String[] nums = {
//																	"1", "5",
//																	"10", "15",
//																	"20" };
//															np.setMinValue(1);
//															np.setMaxValue(5);
//															np.setWrapSelectorWheel(false);
//															np.setDisplayedValues(nums);
//															np.setValue(1);
//
//															Button setButton = (Button) setAlertDialog
//																	.findViewById(R.id.set_alert);
//															setButton
//																	.setOnClickListener(new OnClickListener() {
//																		public void onClick(
//																				View v) {
//																			int chosen = np
//																					.getValue();
//																			int timeBefore = chosen == 1 ? 1
//																					: (chosen - 1) * 5;
//																			setAlertDialog
//																					.dismiss();
//																			Calendar leaving = selected
//																					.getDepartureTime();
//																			leaving.add(
//																					Calendar.MINUTE,
//																					-1
//																							* timeBefore);
//																			DateFormat format = new SimpleDateFormat(
//																					"hh:mm:ss a");
//																			String finalTime = format
//																					.format(leaving
//																							.getTime());
//																			Toast.makeText(
//																					Bus_Favorites.this,
//																					"Alert set for "
//																							+ finalTime
//																							+ " minutes",
//																					Toast.LENGTH_LONG)
//																					.show();
//
//																			Intent intent = new Intent(
//																					Bus_Favorites.this,
//																					Bus_AlarmReceiver.class);
//																			Bundle extras = new Bundle();
//																			extras.putString(
//																					"ROUTE",
//																					r);
//																			extras.putString(
//																					"STOP",
//																					""
//																							+ s);
//																			extras.putString(
//																					"TIME",
//																					selected.toString());
//																			extras.putInt(
//																					"BEFORE",
//																					timeBefore);
//																			intent.putExtras(extras);
//																			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//																			PendingIntent pendingIntent = PendingIntent
//																					.getActivity(
//																							getBaseContext(),
//																							0,
//																							intent,
//																							0);
//
//																			alarmManager
//																					.set(AlarmManager.RTC_WAKEUP,
//																							leaving.getTimeInMillis(),
//																							pendingIntent);
//																		}
//																	});
//															setAlertDialog
//																	.show();
//
//														}
//													})
//											.setNegativeButton(
//													"Cancel",
//													new DialogInterface.OnClickListener() {
//														public void onClick(
//																DialogInterface dialog,
//																int whichButton) {
//
//															/*
//															 * User clicked No
//															 * so do some stuff
//															 */
														}
													});
									AlertDialog alert = builder.create();
									alert.show();
								} else {
									Toast.makeText(
											Bus_Favorites.this,
											"Sorry, no busses are running at that stop.",
											Toast.LENGTH_SHORT).show();

								}
							} else {
								Toast.makeText(Bus_Favorites.this,
										"No Network Connection.",
										Toast.LENGTH_LONG).show();
							}
						}
						iter++;
					}

					fis.close();
				} catch (FileNotFoundException e) {

				} catch (IOException e) {					
				}
			}
		});
	}

	/**
	 * Handles the clicking of action bar icons.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionbar_new:
			createNewFavorite();
			return true;
		case R.id.actionbar_edit:
			editFavorites();
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Handles deleting favorites.
	 */
	private void editFavorites() {
		// editDialog = new Dialog(this);

		final boolean[] checked = new boolean[list.size()];
		String[] items = new String[list.size()];
		for (int i = 0; i < items.length; i++) {
			items[i] = list.get(i);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Delete favorite")
				.setMultiChoiceItems(items, checked,
						new DialogInterface.OnMultiChoiceClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton, boolean isChecked) {

								/* User clicked on a check box do some stuff */
							}
						})
				.setPositiveButton("Delete",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								for (int j = checked.length - 1; j >= 0; j--) {
									// Toast.makeText(Bus_Favorites.this,
									// checked[j] == true ? "YES" : "NO",
									// Toast.LENGTH_SHORT).show();
									if (checked[j] == true)
										list.remove(j);
								}
								editFile(list, checked);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked No so do some stuff */
							}
						}).create();

		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Given the list of strings and the array of boolean values where each true
	 * means we want to remove that from our list/file
	 * @param list
	 * @param chosen
	 */
	protected void editFile(ArrayList<String> list, boolean[] chosen) {
		String output = "";
		try {
			FileInputStream fis = openFileInput(FILENAME);
			Scanner scan = new Scanner(fis);
			for (int i = 0; i < chosen.length; i++) {
				String line = scan.nextLine();
				if (chosen[i] == false) {
					output += line + "\n";
				}
			}
		} catch (FileNotFoundException e1) {
		}

		deleteFile(FILENAME);
		list.clear();
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(FILENAME, MODE_PRIVATE);
			fos.write(output.getBytes());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		try {
			if (fos != null)
				fos.close();
		} catch (IOException e) {
		
		}
		refreshList();
	}

	private void createNewFavorite() {
		dialog = new Dialog(Bus_Favorites.this);

		dialog.setContentView(R.layout.add_favorite);
		dialog.setTitle("Add favorite");
		dialog.setOwnerActivity(Bus_Favorites.this);

		fav_route_spinner = (Spinner) dialog
				.findViewById(R.id.fav_spinnerroute);

		fav_stop_spinner = (Spinner) dialog.findViewById(R.id.fav_spinnerstop);
		fav_stop_spinner.setEnabled(false);
		fav_stop_spinner.setClickable(false);

		fav_add = (Button) dialog.findViewById(R.id.add_fav);
		fav_add.setEnabled(false);

		String[] arr = getApplicationContext().getResources().getStringArray(
				R.array.just_routes);
		ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(this,
				R.layout.real_list_item, arr);

		routeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fav_route_spinner.setAdapter(routeAdapter);

		fav_route_spinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					/**
					 * Make the other spinner and button gryed out until they
					 * select a route. After the select a route, go online and
					 * get the stops for this route. Then show the other
					 * spinner.
					 */
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						if (pos == 0) {
							route = null;
						} else {
							String temp = getResources().getStringArray(
									R.array.just_routes)[pos];
							route = Bus_Constants.LTOS_ROUTE_NAME.get(temp);
							if (isNetworkAvailable()) {
								listOfStops = conn.getStopsOnRoute(route);
								if (listOfStops.size() == 0) {
									Toast.makeText(
											Bus_Favorites.this,
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
										Bus_Favorites.this,
										// android.R.layout.simple_spinner_item,
										R.layout.real_list_item, arr);
								stopAdapter
										.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								fav_stop_spinner.setAdapter(stopAdapter);
								fav_stop_spinner.setEnabled(true);
								fav_stop_spinner.setClickable(true);
							} else {
								Toast.makeText(Bus_Favorites.this,
										"No Network Connection.",
										Toast.LENGTH_LONG).show();
							}
						}

					}

					public void onNothingSelected(AdapterView<?> arg0) {
						route = null;
					}

				});

		fav_stop_spinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						/**
						 * Once they select a route, show this spinner and the
						 * button.
						 */
						if (listOfStops != null) {
							Bus_Stop s = listOfStops.get(pos);
							stop = s.getStopCode();
							fav_add.setEnabled(true);
							fav_add.setClickable(true);
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
						// do nothing
					}

				});
		fav_add.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				String info = route + ":" + stop + "\n";
				appendInfoToFile(info);
			}
		});
		dialog.show();
	}

	/**
	 * Append new favorite to the file, 
	 * @param info should have the new fav info, in this format: "HWD:1011"
	 */
	protected void appendInfoToFile(String info) {
		FileInputStream fis;
		FileOutputStream fos;
		try {
			String output = "";
			fis = openFileInput(FILENAME);
			Scanner scan = new Scanner(fis);
			while (scan.hasNextLine()) {
				output += scan.nextLine() + "\n";
			}
			output += info;
			fis.close();
			deleteFile(FILENAME);
			list.clear();
			fos = openFileOutput(FILENAME, MODE_PRIVATE);
			fos.write(output.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// file doesn't exit, create it
			try {
				fos = openFileOutput(FILENAME, MODE_PRIVATE);
				fos.write(info.getBytes());
				fos.close();
			} catch (FileNotFoundException e1) {
			} catch (IOException ex) {
			}

		} catch (IOException e) {
		}
		refreshList();

	}

	/**
	 * Creates the action bar icons.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionabar, menu);
		return true;
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
	 * Refresh the listview from the contents of the file.
	 */
	private void refreshList() {
		try {
			FileInputStream fis = openFileInput(FILENAME);
			Scanner scan = new Scanner(fis);
			while (scan.hasNext()) {
				String line = scan.nextLine();
				int colon = line.indexOf(":");
				String r = line.substring(0, colon);
				String s = line.substring(colon + 1);
				list.add(Bus_Constants.STOL_ROUTE_NAMES.get(r) + " at "
						+ Bus_Constants.STOPCODES.get(new Integer(s)));
			}

			fis.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		stringAdapter = new ArrayAdapter<String>(this, R.layout.real_list_item,
				list);
		setListAdapter(stringAdapter);
	}
}
