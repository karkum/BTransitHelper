package org.mad.bus;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Main Bus activity, simply give a list of options to chose from for navigating to the 
 * different features.
 * @author karthik
 *
 */
public class Bus_MainActivity extends ListActivity {
	/** Called when the activity is first created. */
	@SuppressWarnings("unused")
	private final Bus_Constants cont = new Bus_Constants();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		String[] options = { "Departure Times", "Bus Stop Map",
				"Current Buses Map", "Stops Around Me", "Favorites" };
		ArrayAdapter<String> stringAdapter = new ArrayAdapter<String>(this,
				R.layout.real_list_item, options);

		setListAdapter(stringAdapter);
		ListView lv = getListView();

		lv.setTextFilterEnabled(true);

		View parent = (View) lv.getParent();
		parent.setBackgroundResource(R.drawable.bg_tan);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				createNextActivity(position);
			}
		});
		lv.setCacheColorHint(0);
	}

	protected void createNextActivity(int position) {
		Intent i;
		try {
			if (position == 0) {
				i = new Intent(Bus_MainActivity.this,
						Bus_NextDeparturesActivity.class);
				startActivity(i);
			} else if (position == 1) {
				i = new Intent(Bus_MainActivity.this, Bus_StopMap.class);
				startActivity(i);
			} else if (position == 2) {
				i = new Intent(Bus_MainActivity.this, Bus_CurrentBusMap.class);
				startActivity(i);
			} else if (position == 3) {
				i = new Intent(Bus_MainActivity.this, Bus_AroundMe.class);
				startActivity(i);
			}
			else if (position == 4) {
				i = new Intent(Bus_MainActivity.this, Bus_Favorites.class);
				startActivity(i);
			}
		} 
		catch (Exception e) {
			Toast.makeText(Bus_MainActivity.this, "Sorry, unknown Error", Toast.LENGTH_LONG).show();
		}
	}
}