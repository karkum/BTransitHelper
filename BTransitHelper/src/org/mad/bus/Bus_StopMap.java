package org.mad.bus;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Bus_StopMap extends SherlockMapActivity {
	private MapView mapView;
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private Bus_StopOverlay itemizedOverlay;
	public static String chosenRoute;
	boolean gps_enabled = false;
	boolean network_enabled = false;
	private static final int centerLat = (int) (37.2277 * 1E6);
	private static final int centerLng = (int) (-80.422037 * 1E6);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maps);
		ActionBar actionBar = getSupportActionBar();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Bus Map");
		chosenRoute = "NONE";
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.just_routes,
				android.R.layout.simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter,
				new ActionBar.OnNavigationListener() {
					// Get the same strings provided for the drop-down's
					// ArrayAdapter
					public boolean onNavigationItemSelected(int position,
							long itemId) {
						// Toast.makeText(Bus_Map.this, "A",
						// Toast.LENGTH_SHORT).show();
						chosenRoute = Bus_Constants.LTOS_ROUTE_NAME
								.get(getResources().getStringArray(
										R.array.just_routes)[position]);
						if (chosenRoute == null) {
//							Toast.makeText(Bus_StopMap.this, "Internal Error",
//									Toast.LENGTH_SHORT).show();
							return true;
						} else {
							refreshMap();
							return true;
						}
					}
				});
	}

	protected void refreshMap() {
		if (!chosenRoute.equals("NONE")) {
			Bus_BTConnection conn = new Bus_BTConnection(Bus_StopMap.this);
			mapView = (MapView) findViewById(R.id.mapview);
			mapView.setBuiltInZoomControls(true);
			MapController mc = mapView.getController();
			mc.setZoom(17); // Zoom 1 is world view
			GeoPoint mapCenter = new GeoPoint(centerLat, centerLng);
			mc.setCenter(mapCenter);
			mapOverlays = mapView.getOverlays();
			mapView.getOverlays().clear();
			drawable = this.getResources().getDrawable(R.drawable.stop);
			itemizedOverlay = new Bus_StopOverlay(drawable, this);
			// get all the stops37151160, -8041452
			// ArrayList<Bus_Record> list = new ArrayList(Bus_Constants.DB.get(
			// 37200000, 38000000, -80500000, -80380000));
//			ArrayList<Bus_Record> list = new ArrayList(Bus_Constants.DB.get(
//					37227582, -80422165, 1000000000));
			ArrayList<Bus_Stop> stops = conn.getStopsOnRoute(chosenRoute);
			ArrayList<Integer> codes = new ArrayList<Integer>();
			for (Bus_Stop s : stops) {
				codes.add(s.getStopCode());
			}
			for (Integer integ : Bus_Constants.STOPCODES.keySet()) {
				if (codes.contains(integ)) {
					GeoPoint point = new GeoPoint(Bus_Constants.STOP_LOCATIONS
							.get(integ).getLatitudeE6(),
							Bus_Constants.STOP_LOCATIONS.get(integ)
									.getLongitudeE6());
					OverlayItem overlayitem = new OverlayItem(point,
							"Hola, Mundo!", "I'm in Mexico City!");
					itemizedOverlay.addOverlay(overlayitem);
					mapOverlays.add(itemizedOverlay);
				}
			}

		}
	}

	/**
	 * Handles the clicking of action bar icons.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
