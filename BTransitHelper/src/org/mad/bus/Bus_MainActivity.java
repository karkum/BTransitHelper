package org.mad.bus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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
	private static final String FILENAME = "ASKED";
	/** Called when the activity is first created. */
	@SuppressWarnings("unused")
	private final Bus_Constants cont = new Bus_Constants();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		String[] options = { "Departure Times", "Bus Stop Map",
				"Current Buses Map", "Stops Around Me", "Favorites" };
		ArrayAdapter<String> stringAdapter = new ArrayAdapter<String>(this,
				R.layout.custom_list_item, options);

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
		try {
			FileInputStream fis = openFileInput(FILENAME);
		} catch (FileNotFoundException e) {
			try {
				FileOutputStream fos = openFileOutput(FILENAME, MODE_PRIVATE);
				if  (!isHHInstalled()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							Bus_MainActivity.this);
					builder.setTitle("Shameless plug for HokieHelper");
					builder.setMessage("Like this app?");
					builder.setIcon(R.drawable.hh);
					builder.setPositiveButton("Check out our other apps", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface arg0, int arg1) {
							try {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.mad.app.hokiehelper")));
							} catch (android.content.ActivityNotFoundException anfe) {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.mad.app.hokiehelper")));
							}
						}

					}).setNegativeButton("No thanks", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface arg0, int arg1) {
							//do nothing
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}
				try {
					fos.write(0);
				} catch (IOException e1) {
					
				}
				try {
					fos.close();
				} catch (IOException e1) {
				}
			} catch (FileNotFoundException e1) {
			
			}
		}

	
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
	private boolean isHHInstalled() {
		try{
			@SuppressWarnings("unused")
			ApplicationInfo info = getPackageManager().
			getApplicationInfo("org.mad.app.hokiehelper", 0 );
			return true;
		} catch( PackageManager.NameNotFoundException e ){
			return false;
		}
	}
}