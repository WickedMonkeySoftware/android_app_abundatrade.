package com.abundatrade.android_app_abundatrade;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.abundatrade.android_app_abundatrade.CameraPreview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Button;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

import android.widget.TextView;
/* Import ZBar Class files */
//import net.sourceforge.zbar.ImageScanner;
//import net.sourceforge.zbar.Image;
//import net.sourceforge.zbar.Symbol;
//import net.sourceforge.zbar.SymbolSet;
//import net.sourceforge.zbar.Config;
import com.mirasense.*;
import com.mirasense.scanditsdk.LegacyPortraitScanditSDKBarcodePicker;
import com.mirasense.scanditsdk.ScanditSDKAutoAdjustingBarcodePicker;
import com.mirasense.scanditsdk.ScanditSDKBarcodePicker;
import com.mirasense.scanditsdk.interfaces.ScanditSDK;
import com.mirasense.scanditsdk.interfaces.ScanditSDKListener;

/**
 * Activity that utilizes the camera to scan a barcode.  Supports
 * EAN-13/UPC-A, UPC-E, EAN-8, Code 128, Code 39, Interleaved 2 of 5 and QR Code
 * @author James D.
 * Code provided by zbar library under the lgpl2.0 license.
 */
public class CameraScan extends Activity implements ScanditSDKListener {

	//private Camera mCamera;
	//private CameraPreview mPreview;
	//private Handler autoFocusHandler;

	public TextView scanText;
	public TextView resultText;
	public Button scanButton;
	public boolean loggedIn;
	public boolean lookupAll;
	public String syncKey;
	
	@Override
	public void onResume() {
		mPicker.startScanning();
		super.onResume();
	}
	
	@Override
	public void didScanBarcode(String barcode, String symboligy) {
		ShowResult(barcode);
	}
	
	private void ShowResult(String barcode) {
		this.releaseCamera();
		Intent i = new Intent(CameraScan.this, LookupAndAdd.class);
		i.putExtra("UPC", barcode);
		i.putExtra("loggedIn", loggedIn);
		if (loggedIn) {
			i.putExtra("synckey", syncKey);
			i.putExtra("lookupAll", lookupAll);
		}
		startActivity(i);
		
		//close original instance of CameraScan
		finish();
	}
	
	@Override
	public void didManualSearch(String entry) {
		ShowResult(entry);
	}

	@Override
	public void didCancel() {}

	private boolean previewing = true;

	static {
		System.loadLibrary("iconv");
	}
	
	protected ScanditSDK mPicker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//Get login status and synckey if needed
		Bundle passBundle = getIntent().getExtras();
		loggedIn = passBundle.getBoolean("loggedIn");
		if (loggedIn) {
			lookupAll = passBundle.getBoolean("lookupAll");
			syncKey = passBundle.getString("synckey");
		}
		
		if (ScanditSDKBarcodePicker.canRunPortraitPicker()) {
			ScanditSDKAutoAdjustingBarcodePicker picker = new ScanditSDKAutoAdjustingBarcodePicker(this, "OzptRBwZEeODufuirCM1b8SQmwfm7aAzzZGoheZrpNM", 0);
			mPicker = picker;
			setContentView(picker);
		}
		else {
			ScanditSDK picker = new LegacyPortraitScanditSDKBarcodePicker(this, "OzptRBwZEeODufuirCM1b8SQmwfm7aAzzZGoheZrpNM");
			mPicker = picker;
		}
		
		mPicker.getOverlayView().addListener(this);
		
		mPicker.getOverlayView().showSearchBar(true);
		mPicker.set2DScanningEnabled(false);
		mPicker.setInverseRecognitionEnabled(true);
//		
//		
//		//Get rid of title bar
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_camera_scan);
//	
//		//Initial screen state portrait
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		
//		autoFocusHandler = new Handler();
//		mCamera = getCameraInstance();
//		
//		/* Instantiate barcode scanner */
//		scanner = new ImageScanner();
//		scanner.setConfig(0, Config.X_DENSITY, 3);
//		scanner.setConfig(0, Config.Y_DENSITY, 3);
//		
//		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
//		FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
//		preview.addView(mPreview);
//		
//		//scanning echo
//		scanText = (TextView)findViewById(R.id.scanText);
//		//result text
//		resultText= (TextView)findViewById(R.id.upcResult);
//		
// 		
	}

	public void onPause() {
		releaseCamera();
		super.onPause();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera mCamera = null;
		try {
			mCamera = Camera.open();
			
			if (mCamera == null) {
				Class<?> cameraClass = Camera.class;
				Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
				Object cameraInfo = null;
				
				Method getNumCamerasMethod = cameraClass.getMethod("getNumberOfCameras");
				Method getCameraInfoMethod = null;
				
				Field facingField = null;
				
				int cameraCount = 0;
				
				if (getNumCamerasMethod != null && cameraInfoClass != null) {
					cameraCount = (Integer) getNumCamerasMethod.invoke(null, (Object[]) null);
					cameraInfo = cameraInfoClass.newInstance();
					getCameraInfoMethod = cameraClass.getMethod("getCameraInfo", Integer.TYPE, cameraInfoClass);
					
					if (cameraInfo != null) {
						facingField = cameraInfo.getClass().getField("facing");
					}
				}
				
				if (getCameraInfoMethod != null && facingField != null) {
					for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
						getCameraInfoMethod.invoke(null, cameraIndex, cameraInfo);
						
						int facing = facingField.getInt(cameraInfo);
						
						if (facing == 1) {
							Method cameraOpenMethod = cameraClass.getMethod("open", Integer.TYPE);
							
							if (cameraOpenMethod != null) {
								try {
									mCamera = (Camera)cameraOpenMethod.invoke(null, cameraIndex);
								}
								catch (Exception ex) {
									Log.e("err", "Camera failed to open");
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
		}
		return mCamera;
	}

	private void releaseCamera() {
		mPicker.stopScanning();
		//if (mCamera != null) {
		//	previewing = false;
		//	mCamera.setPreviewCallback(null);
		//	mCamera.release();
		//	mCamera = null;
		//}
	}

	/*private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};*/

	/*PreviewCallback previewCb = new PreviewCallback() {
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
	};*/

	/* Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};*/

	
}
