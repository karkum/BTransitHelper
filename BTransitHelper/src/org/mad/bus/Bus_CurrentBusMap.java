package org.mad.bus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Bus_CurrentBusMap extends SherlockMapActivity {
	private MapView mapView;
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private Bus_BusOverlay itemizedOverlay;
	public static String chosenRoute;
	private static final int centerLat = (int)( 37.2277 * 1E6 );
    private static final int centerLng = (int)( -80.422037 * 1E6 );
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maps);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setLogo(R.drawable.ic_launcher);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setTitle("Current Buses");
		
		refreshMap();
	}

	private void refreshMap() {
		if (isNetworkAvailable()) {
			mapView = (MapView) findViewById(R.id.mapview);
			mapView.invalidate();
			MapController mc = mapView.getController();
	        mc.setZoom( 17 ); // Zoom 1 is world view   
	        GeoPoint mapCenter = new GeoPoint( centerLat, centerLng );
	        mc.setCenter( mapCenter );
			mapView.setBuiltInZoomControls(true);
			mapOverlays = mapView.getOverlays();
			mapOverlays.clear();
			drawable = this.getResources().getDrawable(R.drawable.stop);
			itemizedOverlay = new Bus_BusOverlay(drawable, this);
			Bus_BTConnection conn = new Bus_BTConnection(Bus_CurrentBusMap.this);
			ArrayList<Bus_Bus> busses = new ArrayList<Bus_Bus>();
			busses = conn.getCurentBusInfo();
			if (busses != null) {
				for (Bus_Bus b : busses) {
					GeoPoint point = new GeoPoint(b.getLocation().getLatitudeE6(), b.getLocation().getLongitudeE6());
					OverlayItem overlayitem = new OverlayItem(point, b.getRoute().getShortName(), 
							b.getRoute().getLongName());
					
					itemizedOverlay.addOverlay(overlayitem);
					mapOverlays.add(itemizedOverlay);
				}
			}
		} else {
			Toast.makeText(this, "No Network Connection Available", Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}


	/**
	 * Handles the clicking of action bar icons.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionbar_refresh:
			refreshMap();
			return true;
		case android.R.id.home:
			Intent intent = new Intent(this, Bus_MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		default:
			return super.onOptionsItemSelected(item);
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
	/**
	 * Creates the action bar icons.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.refresh_actionbar, menu);
		return true;
	}
}
