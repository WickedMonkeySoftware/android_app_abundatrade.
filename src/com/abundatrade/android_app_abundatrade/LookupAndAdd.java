package com.abundatrade.android_app_abundatrade;

import java.io.*;

import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

/**
 * Activity that sends user information (sync key) as well as a scanned
 * product code to the abundatrade server and retrieves product information.
 * User can then add the item to their current calculator list on the abundatrade
 * website.
 * @author James D.
 *
 */
public class LookupAndAdd extends Activity {

	public TextView UPC;
	public String upcStore;
	public String jsonResponse;
	public String url;
	public HttpClient client;
	public JSONObject json;
	public String syncKey;
	public boolean loggedIn;
	public boolean lookupAll;
	public boolean lookup_done;

	public String itemTotalQty;
	public String itemTotal;
	public String itemTitle;
	public String itemPrice;
	public String itemImage;
	public String itemCurrency;
	public String itemQuantity;
	public String itemID;

	public static final String PREFS_NAME = "AbundaPrefs";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lookup_and_add);
		client = new DefaultHttpClient();
		Bundle upcBundle = getIntent().getExtras();
		lookup_done = false;

		upcStore = upcBundle.getString("UPC");
		loggedIn = upcBundle.getBoolean("loggedIn");
		if (loggedIn) {
			syncKey = upcBundle.getString("synckey");
			lookupAll = upcBundle.getBoolean("lookupAll");
			System.out.println("Lookup syncKey:" + syncKey);
		}

		// url for initial lookup
		if (lookupAll == false) {
			url = "http://abundatrade.com/trade/process/request.php?mobile_scan=t&"
					+ "mobile_type=android&product_code="
					+ upcStore
					+ "&action=lookup_item&product_qty=0";
		} else {
			url = "http://abundatrade.com/trade/process/request.php?mobile_scan=t&"
					+ "mobile_type=android&sync_key="
					+ syncKey
					+ "&product_code="
					+ upcStore
					+ "&action=lookup_item&product_qty=1";
		}
		// Initial Object lookup
		new connection().execute("text");

		while (lookup_done == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// UPC = (TextView) findViewById(R.id.upcResult);

		// UPC.setText(upcStore);

		/* Pull information from initial lookup */
		try {
			itemTotalQty = json.getString("total_qty");
			itemTotal = json.getString("total");
			itemTitle = json.getString("title");
			itemPrice = json.getString("price");
			itemImage = json.getString("imagel");
			itemCurrency = json.getString("currency_for_total");
			itemQuantity = json.getString("Total");
			itemID = json.getString("id");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("Total Qty: " + itemTotalQty);
		System.out.println("Item Total: " + itemTotal);
		System.out.println("Item Title: " + itemTitle);
		System.out.println("Item Price: " + itemPrice);
		System.out.println("Item Image: " + itemImage);
		System.out.println("Item Currency: " + itemCurrency);
		System.out.println("Item Qty: " + itemQuantity);
		System.out.println("Item ID: " + itemID);

		Button addItem = (Button) findViewById(R.id.addButt);
		
		/* Add item button pressed */
		addItem.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				lookup_done = false;
				setResult(RESULT_OK);

				// Add item to list if logged in
				if (loggedIn) {
					url = "http://abundatrade.com/trade/process/request.php?mobile_scan=t&"
							+ "mobile_type=android&sync_key="
							+ syncKey
							+ "&product_code="
							+ upcStore
							+ "&action=lookup_item&product_qty=1";
					new connection().execute("text");
					while (lookup_done == false) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					System.out.println("Not Logged IN!!!!");
				}

			}
		});

		Button contScan = (Button) findViewById(R.id.contScan);
		
		/*Continue scanning button pressed */
		contScan.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				setResult(RESULT_OK);
				Intent i = new Intent(LookupAndAdd.this, CameraScan.class);
				i.putExtra("loggedIn", loggedIn);
				if (loggedIn) {
					i.putExtra("synckey", syncKey);
					i.putExtra("lookupAll", lookupAll);
				}
				startActivity(i);
				finish();
			}
		});
	}
	
	/**
	 * Creates a menu if the user presses the menu button on their phone.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.lookup_and_add, menu);
		return true;
	}
	
	/**
	 * Displays the menu and defines the program's response to different
	 * buttons being pressed.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.log_out:
			//Remove the stored login information and return to login screen
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
					.putString(PREF_USERNAME, null)
					.putString(PREF_PASSWORD, null).commit();
			startActivity(new Intent(this, Login.class));
			finish();
			return true;
		case R.id.info:
			// startActivity(new Intent(this, Info.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void setUpc(String UPC) {
		upcStore = UPC;
	}

	
	/**
	 * Embedded class that handles the server communications. Extends and
	 * implements AsyncTask to run connections in a separate thread.
	 * 
	 * @author James D.
	 * 
	 */
	private class connection extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			System.out.println("Entering new thread");

			try {
				json = getResponse(url);
				lookup_done = true;
				return json.toString();
			} catch (ClientProtocolException e) {
				System.out.println("CLIENTPROTOCOL EXCEPTION!");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IO EXCEPTION!");
				e.printStackTrace();
			} catch (JSONException e) {
				System.out.println("JSONE EXCEPTION!!");
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * Sends and receives httpget to the abundatrade server.
		 * @param url of the get request
		 * @return JSONObject containing login status and sync-key
		 * @throws ClientProtocolException
		 * @throws IOException
		 * @throws JSONException
		 */
		public JSONObject getResponse(String url)
				throws ClientProtocolException, IOException, JSONException {
			System.out.println("URL: " + url);
			HttpGet get = new HttpGet(url);
			System.out.println("HTTPGET CREATED");
			HttpResponse r = null;
			try {
				r = client.execute(get);
			} catch (Exception e) {
				System.out.println("Error with get");
				e.printStackTrace();
			}
			System.out.println("Executed get Request");
			int status = r.getStatusLine().getStatusCode();
			if (status == 200) {
				HttpEntity e = r.getEntity();
				String data = EntityUtils.toString(e);
				System.out.println("DATA: " + data);
				JSONObject temp = new JSONObject(data);
				System.out.println("Echo: " + temp.toString());
				return temp;
			} else {
				System.out.println("Entering else!");
				// Toast.makeText(LookupAndAdd.this, "error",
				// Toast.LENGTH_SHORT);
				return null;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			System.out.println(result);
		}

		@Override
		protected void onPreExecute() {
			System.out.println("TESTING");
		}

	}

}
