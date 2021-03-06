package com.abundatrade.android_app_abundatrade;

import java.io.*;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.Toast;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.app.AlertDialog;

import android.widget.EditText;

/**
 * Activity class that handles user login. Users will login using their
 * abundatrade.com credentials and will then be able to add items via other
 * activities in the program.
 * 
 * @author James D.
 * @version 1.0
 */
public class Login extends Activity {

	EditText login_edit;
	EditText pw_edit;

	public HttpClient client;
	public String url;
	public JSONObject json;
	final Context context = this;

	public static final String PREFS_NAME = "AbundaPrefs";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";

	private boolean rememberMe;
	private boolean lookupAll;
	private boolean lookup_done;
	private boolean loggedIn;
	private String login;
	private String pw;
	private String syncKey;
	private String loginStatus;
	private CheckBox add_all;
	private CheckBox remember_me;
	private AlertDialog no_internet;

	private void CheckInternet() {
		ConnectivityManager cm =
		        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = false;
		
		if (activeNetwork != null) {
			isConnected = activeNetwork.isConnectedOrConnecting();
		}
		
		if (!isConnected) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);

			alertDialogBuilder.setTitle("Not Connected");

			alertDialogBuilder
					.setMessage(
							"Please connect your device to the internet to continue")
					.setCancelable(false);

			// create alert dialog
			no_internet = alertDialogBuilder.create();

			// show it
			no_internet.show();
		}
		else if (isConnected && no_internet != null) {
			no_internet.hide();
		}
	}
	
	@Override
	protected void onResume(){
		CheckInternet();
		super.onResume();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		lookupAll = false;

		json = new JSONObject();
		client = new DefaultHttpClient();

		// Define Buttons
		Button login_but = (Button) findViewById(R.id.but_login);
		Button acct_but = (Button) findViewById(R.id.but_cr_acct);
		Button scan_but = (Button) findViewById(R.id.but_nlogin);
		//add_all = (CheckBox) findViewById(R.id.autoadd_check);
		//add_all = false;

		remember_me = (CheckBox) findViewById(R.id.remember_check);

		login_edit = (EditText) findViewById(R.id.login_field);
		pw_edit = (EditText) findViewById(R.id.pw_field);

		// Get login and pw if stored
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String prefUserName = pref.getString(PREF_USERNAME, null);
		String prefPw = pref.getString(PREF_PASSWORD, null);

		// Fill the fields with stored login information
		/*
		if (prefUserName != null && prefPw != null) {
			remember_me.setChecked(true);
			login_edit.setText(prefUserName, TextView.BufferType.EDITABLE);
			pw_edit.setText(prefPw, TextView.BufferType.EDITABLE);
			login_but.performClick();
		}
		*/
		/* Login button pressed */
		login_but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				lookup_done = false;
				login = login_edit.getText().toString();
				pw = pw_edit.getText().toString();

				// System.out.println("Hashed password: " + md5(pw));

				url = "http://abundatrade.com/trade/process/user/login/?user="
						+ login + "&password=" + md5(pw) + "&mobile_scan=t";

				// url =
				// "http://abundatrade.com/trade/process/user/login/?user=landers.robert@gmail.com&password=6519f8571452b3004e6f85cbaf3bdfef&mobile_scan=t";
				new connection().execute("text");

				// Wait in main thread until lookup is complete
				while (lookup_done == false) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				System.out.println("Json: " + json.toString());

				try {
					loginStatus = json.getString("status");
					System.out.println("Login Status: " + loginStatus);

					/*
					 * The following code is only used if the user successfully
					 * logs in.
					 */
					if (loginStatus.equalsIgnoreCase("logged in")) {

						rememberMe = remember_me.isChecked();

						// Store Login and Password
						if (rememberMe) {
							getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
									.edit().putString(PREF_USERNAME, login)
									.putString(PREF_PASSWORD, pw).commit();
						}
						// Erase any information stored
						else {
							getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
									.edit().putString(PREF_USERNAME, null)
									.putString(PREF_PASSWORD, null).commit();
						}

						System.out.println("Logged In: Getting SyncKey");
						loggedIn = true;
						syncKey = json.getString("key");
						System.out.println("Sync Key = " + syncKey);
						setResult(RESULT_OK);

						// check scan all option
						//lookupAll = add_all.isChecked();

						// Start Scanner
						Bundle passBundle = getIntent().getExtras();
						Boolean returnTo = false;
						String upc = "";
						if (passBundle != null) {
							returnTo = passBundle.getBoolean("returnTo");
							upc = (String)passBundle.get("UPC");
						}
						
						Class<?> next;
						if (returnTo) {
							next = LookupAndAdd.class;
						}
						else {
							next = CameraScan.class;
						}
						
						Intent i = new Intent(Login.this, next);
						// Pass syncKey and login status
						i.putExtra("synckey", syncKey);
						i.putExtra("loggedIn", loggedIn);
						i.putExtra("lookupAll", lookupAll);
						i.putExtra("UPC", upc);
						startActivity(i);
						finish();

					} else {
						System.out.println("Login Failure!");

						// Create and open an error dialog box
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								context);

						alertDialogBuilder.setTitle("Login Failure");

						alertDialogBuilder
								.setMessage(
										"Invalid Login/Pass Please Try Again")
								.setCancelable(false)
								.setPositiveButton("Change Password",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												Intent browserIntent = new Intent(
														Intent.ACTION_VIEW,
														Uri.parse("http://abundatrade.com/trade/user/profile/"));
												startActivity(browserIntent);

											}
										})
								.setNegativeButton("Ok",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												// if this button is clicked,
												// just close
												// the dialog box and do nothing
												dialog.cancel();
											}
										});

						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();

						// show it
						alertDialog.show();

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// Echo to log
				Log.v("Login", login);
				Log.v("PW", pw);
			}
		});
		
		// Fill the fields with stored login information

				if (prefUserName != null && prefPw != null) {
					remember_me.setChecked(true);
					login_edit.setText(prefUserName, TextView.BufferType.EDITABLE);
					pw_edit.setText(prefPw, TextView.BufferType.EDITABLE);
					login_but.performClick();
				}
		/* Create account button pressed */
		acct_but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// Open Browser to abundatrade acct creation
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://abundatrade.com/trade/user/create/"));
				startActivity(browserIntent);

			}
		});

		/* Just scan button pressed */
		scan_but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				setResult(RESULT_OK);
				Intent i = new Intent(Login.this, CameraScan.class);
				// pass login status
				i.putExtra("loggedIn", loggedIn);
				startActivity(i);
				finish();

			}
		});
	}

	/**
	 * Implementation of md5 hashing for the password.
	 * 
	 * @param input
	 * @return the md5 hashed version of the inputed string
	 */
	public static String md5(String input) {
		String md5 = null;

		if (null == input)
			return null;

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			digest.update(input.getBytes(), 0, input.length());

			md5 = new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return md5;
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
			System.out.println("Status code: " + status);
			if (status == 200) {
				HttpEntity e = r.getEntity();
				String data = EntityUtils.toString(e);
				System.out.println("DATA: " + data);
				JSONObject temp = new JSONObject(data);
				System.out.println("Echo: " + temp.toString());
				lookup_done = true;
				return temp;
			} else {
				System.out.println("Entering else!");
				// Toast.makeText(LookupAndAdd.this, "error",
				// Toast.LENGTH_SHORT);
				lookup_done = true;
				return null;
			}

		}

	}
}
