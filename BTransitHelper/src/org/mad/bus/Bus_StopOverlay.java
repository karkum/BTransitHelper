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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Represents a stop overlay. It shows next departures from a stop and gives an
 * option to set alerts.
 * 
 * @author karthik
 * 
 */
public class Bus_StopOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private Bus_Time selected1;
	private static Bus_Time selected2;
	private Bus_Route route_selected;
	private Bus_Record rec;
	private boolean aroundme;
	private static int chosen1;
	private static int chosen2;
	public Bus_StopOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public Bus_StopOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	public Bus_StopOverlay(Drawable defaultMarker, Context context, boolean a) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		aroundme = a;
	}

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		GeoPoint point = item.getPoint();
		int lat = point.getLatitudeE6();
		String[] arr = new String[20];
		int lon = point.getLongitudeE6();

		rec = (Bus_Record) Bus_Constants.DB.get(lat, lon);
		if (rec == null) {
			Toast.makeText(mContext, "Stop not found", Toast.LENGTH_SHORT)
			.show();
			return true;
		} else {
			String stopName = Bus_Constants.STOPCODES.get(rec.getStopCode());
			final Bus_BTConnection conn = new Bus_BTConnection(mContext);
			if (isNetworkAvailable()) {
				if (aroundme) {
					final ArrayList<Bus_Route> routesForThisStop = conn
							.getRoutesAtStop(rec.getStopCode());
					if (routesForThisStop != null && routesForThisStop.size() != 0) {
						String[] array = new String[routesForThisStop.size()];
						for (int k = 0; k < routesForThisStop.size(); k++) {
							array[k] = routesForThisStop.get(k).getLongName();
						}
						int checked = 0;
						route_selected = routesForThisStop.get(0);
						AlertDialog.Builder builder1 = new AlertDialog.Builder(
								mContext);
						builder1.setTitle(stopName);
						builder1.setSingleChoiceItems(array, checked,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int item) {
								route_selected = routesForThisStop
										.get(item);
							}
						}).setPositiveButton("Find Times",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								final ArrayList<Bus_Time> listOfNextDepartures = conn.getNextDepartures(
										route_selected.getShortName(),
										rec.getStopCode());
								if (listOfNextDepartures.size() != 0) {
									String[] arra = new String[listOfNextDepartures
									                           .size()];
									for (int i = 0; i < listOfNextDepartures
											.size(); i++) {
										arra[i] = listOfNextDepartures
												.get(i).toString();
									}
									selected1 = listOfNextDepartures
											.get(0);
									final int checkedItem = 0;
									AlertDialog.Builder builder2 = new AlertDialog.Builder(
											mContext);
									builder2.setTitle(route_selected
											.getShortName()
											+ " at "
											+ rec.getStopCode());
									builder2.setSingleChoiceItems(
											arra,
											checkedItem,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int item) {
													selected1 = listOfNextDepartures
															.get(item);
												}
											})
											.setPositiveButton(
													"Set Alert",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int whichButton) {
															final String[] nums = {
																	"1",
																	"5",
																	"10",
																	"15",
															"20" };

															AlertDialog.Builder builder3 = new AlertDialog.Builder(
																	mContext);
															builder3.setTitle("Choose alert time");
															int checked = 0;
															builder3.setSingleChoiceItems(
																	nums,
																	checked,
																	new DialogInterface.OnClickListener() {
																		public void onClick(
																				DialogInterface dialog,
																				int item) {
																			chosen1 = Integer
																					.valueOf(nums[item]);
																		}
																	})
																	.setPositiveButton(
																			"Set Alert",
																			new DialogInterface.OnClickListener() {
																				public void onClick(
																						DialogInterface dialog,
																						int whichButton) {

																					int timeBefore = chosen1;
																					dialog.dismiss();
																					Calendar leaving = selected1
																							.getDepartureTime();
																					leaving.add(
																							Calendar.MINUTE,
																							-1
																							* timeBefore);
																					DateFormat format = new SimpleDateFormat(
																							"hh:mm:ss a");
																					String finalTime = format
																							.format(leaving
																									.getTime());
																					Toast.makeText(
																							mContext,
																							"Alert set for "
																									+ finalTime,
																									Toast.LENGTH_LONG)
																									.show();

																					Intent intent = new Intent(
																							mContext,
																							Bus_AlarmReceiver.class);
																					Bundle extras = new Bundle();
																					extras.putString(
																							"ROUTE",
																							route_selected
																							.getShortName());
																					extras.putString(
																							"STOP",
																							""
																									+ rec.getStopCode());
																					extras.putString(
																							"TIME",
																							selected1
																							.toString());
																					extras.putInt(
																							"BEFORE",
																							timeBefore);
																					intent.putExtras(extras);
																					AlarmManager alarmManager = (AlarmManager) mContext
																							.getSystemService(Context.ALARM_SERVICE);
																					int requestID = (int) System.currentTimeMillis();
																					PendingIntent pendingIntent = PendingIntent
																							.getActivity(
																									mContext,
																									requestID,
																									intent,
																									0);

																					alarmManager
																					.set(AlarmManager.RTC_WAKEUP,
																							leaving.getTimeInMillis(),
																							pendingIntent);

																				}
																			})
																			.setNegativeButton(
																					"Cancel",
																					new DialogInterface.OnClickListener() {
																						public void onClick(
																								DialogInterface dialog,
																								int whichButton) {

																							/*
																							 * User
																							 * clicked
																							 * No
																							 * so
																							 * do
																							 * some
																							 * stuff
																							 */
																						}
																					});
															AlertDialog alert = builder3
																	.create();
															alert.show();

														}
													})
													.setNegativeButton(
															"Cancel",
															new DialogInterface.OnClickListener() {
																public void onClick(
																		DialogInterface dialog,
																		int whichButton) {

																	/*
																	 * User
																	 * clicked
																	 * No
																	 * so
																	 * do
																	 * some
																	 * stuff
																	 */
																}
															});
									AlertDialog alert = builder2
											.create();
									alert.show();


								} else {
									Toast.makeText(
											mContext,
											"Sorry, no busses are running at that stop.",
											Toast.LENGTH_SHORT).show();

								}
							}
						}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								//do nothing
							}

						});
						AlertDialog alert = builder1
								.create();
						alert.show();
					} else {
						Toast.makeText(mContext, "Could not find busses.",
								Toast.LENGTH_SHORT).show();
					}

				} else {
					final ArrayList<Bus_Time> listOfNextDepartures = conn
							.getNextDepartures(Bus_StopMap.chosenRoute,
									rec.getStopCode());
					if (listOfNextDepartures!= null && listOfNextDepartures.size() != 0) {
						selected2 = listOfNextDepartures.get(0);
						arr = new String[listOfNextDepartures.size()];
						for (int i = 0; i < listOfNextDepartures.size(); i++) {
							arr[i] = listOfNextDepartures.get(i).toString();
						}
						final int checkedItem = 0;
						AlertDialog.Builder builder = new AlertDialog.Builder(
								mContext);
						builder.setTitle(stopName);
						builder.setSingleChoiceItems(arr, checkedItem,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int item) {
								selected2 = listOfNextDepartures
										.get(item);
							}
						})
						.setPositiveButton("Set Alert",
								new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int whichButton) {
								final String[] nums = {
										"1",
										"5",
										"10",
										"15",
								"20" };

								AlertDialog.Builder builder3 = new AlertDialog.Builder(
										mContext);
								builder3.setTitle("Choose alert time");
								int checked = 0;
								builder3.setSingleChoiceItems(
										nums,
										checked,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int item) {
												chosen2 = Integer
														.valueOf(nums[item]);
											}
										})
										.setPositiveButton(
												"Set Alert",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int whichButton) {

														int timeBefore = chosen2;
														dialog.dismiss();
														Calendar leaving = selected2
																.getDepartureTime();
														leaving.add(
																Calendar.MINUTE,
																-1
																* timeBefore);
														DateFormat format = new SimpleDateFormat(
																"hh:mm:ss a");
														String finalTime = format
																.format(leaving
																		.getTime());
														Toast.makeText(
																mContext,
																"Alert set for "
																		+ finalTime,
																		Toast.LENGTH_LONG)
																		.show();

														Intent intent = new Intent(
																mContext,
																Bus_AlarmReceiver.class);
														Bundle extras = new Bundle();
														extras.putString(
																"ROUTE",
																Bus_StopMap.chosenRoute
																);
														extras.putString(
																"STOP",
																""
																		+ rec.getStopCode());
														extras.putString(
																"TIME",
																selected2
																.toString());
														extras.putInt(
																"BEFORE",
																timeBefore);
														intent.putExtras(extras);
														AlarmManager alarmManager = (AlarmManager) mContext
																.getSystemService(Context.ALARM_SERVICE);
														int requestID = (int) System.currentTimeMillis();
														PendingIntent pendingIntent = PendingIntent
																.getActivity(
																		mContext,
																		requestID,
																		intent,
																		0);

														alarmManager
														.set(AlarmManager.RTC_WAKEUP,
																leaving.getTimeInMillis(),
																pendingIntent);

													}
												})
												.setNegativeButton(
														"Cancel",
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int whichButton) {

																/*
																 * User
																 * clicked
																 * No
																 * so
																 * do
																 * some
																 * stuff
																 */
															}
														});
								AlertDialog alert = builder3
										.create();
								alert.show();

							}
						})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int whichButton) {

								/*
								 * User clicked No so do some
								 * stuff
								 */
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
					} 

























				}
			} else {
				Toast.makeText(mContext, "No Network Connection.",
						Toast.LENGTH_LONG).show();
			}
		}
		return true;
	}

	// /*
	// protected boolean onTap1(int index) {
	// OverlayItem item = mOverlays.get(index);
	// GeoPoint point = item.getPoint();
	// int lat = point.getLatitudeE6();
	// String[] arr = new String[20];
	// int lon = point.getLongitudeE6();
	//
	// rec = (Bus_Record) Bus_Constants.DB.get(lat, lon);
	// if (rec == null) {
	// Toast.makeText(mContext, "Stop not found", Toast.LENGTH_SHORT)
	// .show();
	// return true;
	// } else {
	// String stopName = Bus_Constants.STOPCODES.get(rec.getStopCode());
	// final Bus_BTConnection conn = new Bus_BTConnection();
	// if (isNetworkAvailable()) {
	// if (aroundme) {
	// final ArrayList<Bus_Route> routesForThisStop = conn
	// .getRoutesAtStop(rec.getStopCode());
	// if (routesForThisStop.size() != 0) {
	// String[] array = new String[routesForThisStop.size()];
	// for (int k = 0; k < routesForThisStop.size(); k++) {
	// array[k] = routesForThisStop.get(k).getLongName();
	// }
	// int checked = 0;
	// route_selected = routesForThisStop.get(0);
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// mContext);
	// builder.setTitle(stopName);
	// builder.setSingleChoiceItems(array, checked,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int item) {
	// route_selected = routesForThisStop
	// .get(item);
	// }
	// })
	// .setPositiveButton("Find Times",
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog,
	// int whichButton) {
	//
	// final ArrayList<Bus_Time> listOfNextDepartures = conn.getNextDepartures(
	// route_selected
	// .getShortName(),
	// rec.getStopCode());
	// if (listOfNextDepartures.size() != 0) {
	// String[] arra = new String[listOfNextDepartures
	// .size()];
	// for (int i = 0; i < listOfNextDepartures
	// .size(); i++) {
	// arra[i] = listOfNextDepartures
	// .get(i)
	// .toString();
	// }
	// final int checkedItem = 0;
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// mContext);
	// builder.setTitle(route_selected
	// .getShortName()
	// + " at "
	// + rec.getStopCode());
	// builder.setSingleChoiceItems(
	// arra,
	// checkedItem,
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog,
	// int item) {
	// selected1 = listOfNextDepartures
	// .get(item);
	// }
	// })
	// .setPositiveButton(
	// "Set Alert",
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog,
	// int whichButton) {
	//
	//
	// final String[] nums = { "1", "5", "10", "15", "20" };
	//
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// Bus_StopOverlay.this);
	// builder.setTitle("Choose alert time");
	// int checked = 0;
	// builder.setSingleChoiceItems(nums, checked,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int item) {
	// chosen = Integer.valueOf(nums[item]);
	// }
	// })
	// .setPositiveButton("Set Alert",
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	// int timeBefore = chosen == 1 ? 1
	// : (chosen - 1) * 5;
	// dialog.dismiss();
	// Calendar leaving = selected
	// .getDepartureTime();
	// leaving.add(Calendar.MINUTE, -1
	// * timeBefore);
	// DateFormat format = new SimpleDateFormat(
	// "hh:mm:ss a");
	// String finalTime = format
	// .format(leaving.getTime());
	// Toast.makeText(
	// Bus_NextDeparturesActivity.this,
	// "Alert set for " + finalTime
	// + " minutes",
	// Toast.LENGTH_LONG).show();
	//
	// Intent intent = new Intent(
	// Bus_NextDeparturesActivity.this,
	// Bus_AlarmReceiver.class);
	// Bundle extras = new Bundle();
	// extras.putString("ROUTE", route);
	// extras.putString("STOP", "" + stop);
	// extras.putString("TIME",
	// selected.toString());
	// extras.putInt("BEFORE", timeBefore);
	// intent.putExtras(extras);
	// AlarmManager alarmManager = (AlarmManager)
	// getSystemService(ALARM_SERVICE);
	// PendingIntent pendingIntent = PendingIntent
	// .getActivity(getBaseContext(),
	// 0, intent, 0);
	//
	// alarmManager.set(
	// AlarmManager.RTC_WAKEUP,
	// leaving.getTimeInMillis(),
	// pendingIntent);
	//
	// }
	// })
	// .setNegativeButton("Cancel",
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	//
	// /*
	// * User clicked No so do some stuff
	// */
	// /*
	// }
	// });
	// AlertDialog alert = builder.create();
	// alert.show();
	// }
	//
	// });
	//
	//
	//
	//
	//
	//
	//
	//
	// }
	// })
	// .setNegativeButton(
	// "Cancel",
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog,
	// int whichButton) {
	//
	// /*
	// * User
	// * clicked
	// * No
	// * so
	// * do
	// * some
	// * stuff
	// */
	// /*
	// }
	// });
	// AlertDialog alert = builder
	// .create();
	// alert.show();
	// } else {
	// Toast.makeText(
	// mContext,
	// "Sorry, no busses are running at that stop.",
	// Toast.LENGTH_SHORT)
	// .show();
	//
	// }
	//
	// }
	// })
	// .setNegativeButton("Cancel",
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog,
	// int whichButton) {
	//
	// /*
	// * User clicked No so do some
	// * stuff
	// */
	// }
	// });
	// AlertDialog alert = builder.create();
	// alert.show();
	// } else {
	// Toast.makeText(mContext,
	// "No routes running at that stop",
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// } else {
	// final ArrayList<Bus_Time> listOfNextDepartures = conn
	// .getNextDepartures(Bus_StopMap.chosenRoute,
	// rec.getStopCode());
	// if (listOfNextDepartures.size() != 0) {
	// arr = new String[listOfNextDepartures.size()];
	// for (int i = 0; i < listOfNextDepartures.size(); i++) {
	// arr[i] = listOfNextDepartures.get(i).toString();
	// }
	// final int checkedItem = 0;
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// mContext);
	// builder.setTitle(stopName);
	// builder.setSingleChoiceItems(arr, checkedItem,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int item) {
	// selected2 = listOfNextDepartures
	// .get(item);
	// }
	// })
	// .setPositiveButton("Set Alert",
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog,
	// int whichButton) {
	//
	// setAlertDialog = new Dialog(
	// mContext);
	//
	// setAlertDialog
	// .setContentView(R.layout.custom_dialog);
	// setAlertDialog
	// .setTitle("Set alert");
	//
	// np = (NumberPicker) setAlertDialog
	// .findViewById(R.id.number_picker);
	//
	// String[] nums = { "1", "5",
	// "10", "15", "20" };
	// np.setMinValue(1);
	// np.setMaxValue(5);
	// np.setWrapSelectorWheel(false);
	// np.setDisplayedValues(nums);
	// np.setValue(1);
	//
	// Button setButton = (Button) setAlertDialog
	// .findViewById(R.id.set_alert);
	// setButton
	// .setOnClickListener(new OnClickListener() {
	// public void onClick(
	// View v) {
	// int chosen = np
	// .getValue();
	// int timeBefore = chosen == 1 ? 1
	// : (chosen - 1) * 5;
	// setAlertDialog
	// .dismiss();
	// Calendar leaving = null;
	// if (selected2 == null) {
	// selected2 = listOfNextDepartures
	// .get(0);
	// }
	// leaving = selected2
	// .getDepartureTime();
	//
	// leaving.add(
	// Calendar.MINUTE,
	// -1
	// * timeBefore);
	// DateFormat format = new SimpleDateFormat(
	// "hh:mm:ss a");
	// String finalTime = format
	// .format(leaving
	// .getTime());
	// Toast.makeText(
	// mContext,
	// "Alert set for "
	// + finalTime
	// + " minutes",
	// Toast.LENGTH_LONG)
	// .show();
	//
	// Intent intent = new Intent(
	// mContext,
	// Bus_AlarmReceiver.class);
	// Bundle extras = new Bundle();
	// extras.putString(
	// "ROUTE",
	// Bus_StopMap.chosenRoute);
	// extras.putString(
	// "STOP",
	// ""
	// + rec.getStopCode());
	// extras.putString(
	// "TIME",
	// selected2.toString());
	// extras.putInt(
	// "BEFORE",
	// timeBefore);
	// intent.putExtras(extras);
	// AlarmManager alarmManager = (AlarmManager) mContext
	// .getSystemService(Context.ALARM_SERVICE);
	// PendingIntent pendingIntent = PendingIntent
	// .getActivity(
	// mContext,
	// 0,
	// intent,
	// 0);
	//
	// alarmManager
	// .set(AlarmManager.RTC_WAKEUP,
	// leaving.getTimeInMillis(),
	// pendingIntent);
	// }
	// });
	// setAlertDialog.show();
	//
	// }
	// })
	// .setNegativeButton("Cancel",
	// new DialogInterface.OnClickListener() {
	// public void onClick(
	// DialogInterface dialog,
	// int whichButton) {
	//
	// /*
	// * User clicked No so do some
	// * stuff
	// */
	// }
	// });
	// AlertDialog alert = builder.create();
	// alert.show();
	// } else {
	// Toast.makeText(mContext,
	// "Sorry, no busses are running at that stop.",
	// Toast.LENGTH_SHORT).show();
	//
	// }
	// }
	// } else {
	// Toast.makeText(mContext, "No Network Connection.",
	// Toast.LENGTH_LONG).show();
	// }
	//
	// // Toast.makeText(mContext, stopName, Toast.LENGTH_SHORT).show();
	// // AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	// // dialog.setTitle(stopName);
	// // dialog.setMessage(item.getSnippet());
	// // dialog.show();
	// return true;
	// }
	// }

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addOverlay(OverlayItem overlayitem) {
		mOverlays.add(overlayitem);
		setLastFocusedIndex(-1);
		populate();

	}

	/**
	 * Checks if the network is available
	 * 
	 * @return
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
}