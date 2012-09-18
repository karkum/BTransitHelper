package org.mad.bus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Class to notify user when an alarm goes off. It gets all the information 
 * about an alarm and sends it to the user at the correct time.
 * @author karthik
 *
 */
public class Bus_AlarmReceiver extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notify_view);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Alert!");
		TextView txtview = (TextView) findViewById(R.id.notification);
		// Bus_Constants cons = new Bus_Constants();
		Bundle extras = getIntent().getExtras();
		String stop = extras.getString("STOP");
		String route = extras.getString("ROUTE");
		//		String time = extras.getString("TIME");
		int timeBefore = extras.getInt("BEFORE");
		if (stop != null && route != null) {
			//		Toast.makeText(this, "Alarm went off", Toast.LENGTH_SHORT).show();
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			Intent intent = new Intent(
					this,
					Bus_AlarmReceiver.class);
			Bundle ex = new Bundle();
			ex.putString("ROUTE", route);
			ex.putString("STOP", "" + stop);
			ex.putInt("BEFORE", timeBefore);
			intent.putExtras(ex);
			PendingIntent pendingIntent = PendingIntent
					.getActivity(getBaseContext(),
							0, intent, 0);
			builder.setContentTitle(
					route + " bus arrives in " + timeBefore + " minute(s)!")
					.setSmallIcon(R.drawable.stop)
					.setOnlyAlertOnce(true)
					.setContentIntent(pendingIntent)
					.setDefaults( 
							Notification.DEFAULT_ALL
							| Notification.FLAG_AUTO_CANCEL);

			Notification notification = builder.getNotification();
			
			notificationManager.notify(0, notification);

			extras = getIntent().getExtras();
			stop = extras.getString("STOP");
			route = extras.getString("ROUTE");
			//		String time = extras.getString("TIME");
			timeBefore = extras.getInt("BEFORE");
			@SuppressWarnings("unused")
			Bus_Constants cons = new Bus_Constants();
			txtview.setText("The " + Bus_Constants.STOL_ROUTE_NAMES.get(route)
					+ " bus will arrive at the " + Bus_Constants.STOPCODES.get(Integer.valueOf(stop))
					+ " stop in " + timeBefore + " minute(s)");
		}
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
