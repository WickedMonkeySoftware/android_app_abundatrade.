package com.abundatrade.android_app_abundatrade;

import java.io.*;

import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView;
import org.codehaus.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

public class LookupAndAdd extends Activity {

	TextView UPC;
	public String upcStore;
	public String jsonResponse;
	public String url;
	HttpClient client;
	JSONObject json;
	String syncKey;
	boolean loggedIn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lookup_and_add);
		client = new DefaultHttpClient();
		Bundle upcBundle = getIntent().getExtras();

		upcStore = upcBundle.getString("UPC");
		loggedIn = upcBundle.getBoolean("loggedIn");
		if (loggedIn) {
			syncKey = upcBundle.getString("synckey");
		}

		// url for initial lookup
		url = "http://abundatrade.com/trade/process/request.php?mobile_scan=t&"
				+ "mobile_type=android&product_code=" + upcStore
				+ "&action=lookup_item&product_qty=0";

		// Initial Object lookup
		new connection().execute("text");

		UPC = (TextView) findViewById(R.id.upcResult);

		UPC.setText(upcStore);

		Button addItem = (Button) findViewById(R.id.addButt);

		addItem.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				setResult(RESULT_OK);

				//Add item to list if logged in
				if (loggedIn) {
					url = "http://abundatrade.com/trade/process/request.php?mobile_scan=t&"
							+ "mobile_type=android&sync_key="
							+ syncKey
							+ "&product_code="
							+ upcStore
							+ "&action=lookup_item&product_qty=1";
					new connection().execute("text");
				} else {
					System.out.println("Not Logged IN!!!!");
				}

			}
		});

		Button contScan = (Button) findViewById(R.id.contScan);

		contScan.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				setResult(RESULT_OK);
				Intent i = new Intent(LookupAndAdd.this, CameraScan.class);
				startActivity(i);
				i.putExtra("loggedIn", loggedIn);
				if (loggedIn) {
					i.putExtra("synckey", syncKey);
				}
				finish();
			}
		});
	}

	public void setUpc(String UPC) {
		upcStore = UPC;
	}

	private class connection extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			System.out.println("Entering new thread");

			try {
				json = getResponse(url);
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
