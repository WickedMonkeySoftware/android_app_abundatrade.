package com.abundatrade.android_app_abundatrade;

import com.abundatrade.android_app_abundatrade.CameraPreview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;

import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Button;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

import android.widget.TextView;
/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

/**
 * Activity that utilizes the camera to scan a barcode.  Supports
 * EAN-13/UPC-A, UPC-E, EAN-8, Code 128, Code 39, Interleaved 2 of 5 and QR Code
 * @author James D.
 * Code provided by zbar library under the lgpl2.0 license.
 */
public class CameraScan extends Activity {

	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;

	public TextView scanText;
	public TextView resultText;
	public Button scanButton;
	public boolean loggedIn;
	public boolean lookupAll;
	public String syncKey;
	public ImageScanner scanner;

	private boolean previewing = true;

	static {
		System.loadLibrary("iconv");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Get login status and synckey if needed
		Bundle passBundle = getIntent().getExtras();
		loggedIn = passBundle.getBoolean("loggedIn");
		if (loggedIn) {
			lookupAll = passBundle.getBoolean("lookupAll");
			syncKey = passBundle.getString("synckey");
		}
		
		
		//Get rid of title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_camera_scan);
	
		//Initial screen state portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();
		
		/* Instantiate barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);
		
		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
		preview.addView(mPreview);
		
		//scanning echo
		scanText = (TextView)findViewById(R.id.scanText);
		//result text
		resultText= (TextView)findViewById(R.id.upcResult);
		
 		
	}

	public void onPause() {
		super.onPause();
		releaseCamera();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				SymbolSet syms = scanner.getResults();
				String passUpc = "No Upc!";
				for (Symbol sym : syms) {
					passUpc = sym.getData();
					scanText.setText("barcode result " + sym.getData());
				}
				
				//Open Lookup
				Intent i = new Intent(CameraScan.this, LookupAndAdd.class);
				i.putExtra("UPC", passUpc);
				i.putExtra("loggedIn", loggedIn);
				if (loggedIn) {
					i.putExtra("synckey", syncKey);
					i.putExtra("lookupAll", lookupAll);
				}
				startActivity(i);
				
				//close original instance of CameraScan
				finish();
				
			}
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	
}
