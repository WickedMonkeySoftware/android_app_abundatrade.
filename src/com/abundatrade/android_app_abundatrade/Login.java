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

import java.security.MessageDigest;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

import android.widget.EditText;

public class Login extends Activity {

	EditText login_edit;
	EditText pw_edit;

	HttpClient client;
	String url;
	JSONObject json;

	String login;
	String pw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		client = new DefaultHttpClient();
		// Define Buttons
		Button login_but = (Button) findViewById(R.id.but_login);
		Button acct_but = (Button) findViewById(R.id.but_cr_acct);
		Button scan_but = (Button) findViewById(R.id.but_nlogin);

		login_edit = (EditText) findViewById(R.id.login_field);
		pw_edit = (EditText) findViewById(R.id.pw_field);

		/* Login button pressed */
		login_but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				login = login_edit.getText().toString();
				pw = pw_edit.getText().toString();
				
				//System.out.println("Hashed password: " + md5(pw));
				
				/*
				url = "http://abundatrade.com/trade/process/user/login/?user="
						+ login + "&password=" + md5(pw) + "&mobile_scan=t";
				*/
				
				url = "http://abundatrade.com/trade/process/user/login/?user=landers.robert@gmail.com&password=6519f8571452b3004e6f85cbaf3bdfef&mobile_scan=t";
				new connection().execute("text");
				// Echo to log
				Log.v("Login", login);
				Log.v("PW", pw);
			}
		});

		/* Acct create button pressed */
		acct_but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				login = login_edit.getText().toString();
				pw = pw_edit.getText().toString();

				
			}
		});

		/* Just scan button pressed */
		scan_but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				setResult(RESULT_OK);
				Intent i = new Intent(Login.this, CameraScan.class);
				startActivity(i);
				finish();

			}
		});
	}
	
	public static String md5 (String input) {
		String md5 = null;
		
		if (null == input) return null;
		
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
				return temp;
			} else {
				System.out.println("Entering else!");
				// Toast.makeText(LookupAndAdd.this, "error",
				// Toast.LENGTH_SHORT);
				return null;
			}

		}

	}
}