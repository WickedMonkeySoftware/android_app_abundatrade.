package com.abundatrade.android_app_abundatrade;

import java.io.*;

import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
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

import java.security.MessageDigest;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;

import android.widget.EditText;

public class Login extends Activity {

	EditText login_edit;
	EditText pw_edit;

	HttpClient client;
	String url;
	JSONObject json;
	final Context context = this;
	
	boolean lookupAll;
	boolean lookup_done;
	boolean loggedIn;
	String login;
	String pw;
	String syncKey;
	String loginStatus;
	String intErrors;
	String jsonString;
	CheckBox add_all;

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
		add_all = (CheckBox) findViewById(R.id.autoadd_check);

		login_edit = (EditText) findViewById(R.id.login_field);
		pw_edit = (EditText) findViewById(R.id.pw_field);

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
				
				//Wait in main thread until lookup is complete
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
					if (loginStatus.equalsIgnoreCase("logged in")) {
						System.out.println("Logged In: Getting SyncKey");
						loggedIn = true;
						syncKey = json.getString("key");
						System.out.println("Sync Key = " + syncKey);
						setResult(RESULT_OK);
						
						//check scan all option
						lookupAll = add_all.isChecked();
						
						// Start Scanner
						Intent i = new Intent(Login.this, CameraScan.class);
						// Pass syncKey and login status
						i.putExtra("synckey", syncKey);
						i.putExtra("loggedIn", loggedIn);
						i.putExtra("lookupAll", lookupAll);
						startActivity(i);
						finish();

					} else {
						System.out.println("Login Failure!");

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								context);

						alertDialogBuilder.setTitle("Login Failure");

						alertDialogBuilder
								.setMessage("Invalid Login/Pass Please Try Again")
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
