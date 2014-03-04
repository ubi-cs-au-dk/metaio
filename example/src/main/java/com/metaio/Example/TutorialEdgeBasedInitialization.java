// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.metaio.Example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ETRACKING_STATE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;

public class TutorialEdgeBasedInitialization extends ARViewActivity 
{

	private enum EState
	{
		INITIALIZATION,
		TRACKING
	};
	
	EState mState = EState.INITIALIZATION;
	
	/**
	 * Geometry
	 */
	private IGeometry mModel = null;
	private IGeometry mVizAidModel = null;

	
	/**
	 * metaio SDK callback handler
	 */
	private MetaioSDKCallbackHandler mCallbackHandler;	

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		mCallbackHandler = new MetaioSDKCallbackHandler();
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		mCallbackHandler.delete();
		mCallbackHandler = null;
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
	{
		return mCallbackHandler;
	}

	public void onButtonClick(View v)
	{
		finish();
	}
	
	public void onResetButtonClick(View v)
	{
		loadTrackingConfig();
	}

	@Override
	protected void loadContents() 
	{
		mModel = loadModel("TutorialEdgeBasedInitialization/Assets/Custom/rim.obj");
		mVizAidModel = loadModel("TutorialEdgeBasedInitialization/Assets/Custom/VizAid.obj");
		
		String envmapPath = AssetsManager.getAssetPath(getApplicationContext(), "TutorialEdgeBasedInitialization/Assets/Custom/env_map.zip");	
		metaioSDK.loadEnvironmentMap(envmapPath);

		if (mModel != null)
			mModel.setCoordinateSystemID(1);
		
		if (mVizAidModel != null)
			mVizAidModel.setCoordinateSystemID(2);
		
		loadTrackingConfig();
	}
	
	void loadTrackingConfig()
	{
		boolean result = setTrackingConfiguration("TutorialEdgeBasedInitialization/Assets/Custom/rim_tracking/Tracking.xml");
		
		if(!result)
			MetaioDebug.log(Log.ERROR, "Failed to load tracking configuration.");

		mState = EState.INITIALIZATION;
	}
	

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback
	{
	
		@Override
		public void onSDKReady() 
		{
			// show GUI
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}
		

		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues) {
			if (trackingValues.size() > 0 &&
					trackingValues.get(0).getState() == ETRACKING_STATE.ETS_REGISTERED)
			{
				mState = EState.TRACKING;
			}
		}
	}
	
	public IGeometry loadModel(final String path)
	{
		IGeometry geometry = null;
		try
		{
			// Load model
			String modelPath = AssetsManager.getAssetPath(getApplicationContext(), path);			
			geometry = metaioSDK.createGeometry(modelPath);
			
			MetaioDebug.log("Loaded geometry "+modelPath);			
		}       
		catch (Exception e)
		{
			MetaioDebug.log(Log.ERROR, "Error loading geometry: "+e.getMessage());
			return geometry;
		}		
		return geometry;
	}
	
	public boolean setTrackingConfiguration(final String path)
	{
		boolean result = false;
		try
		{
			// set tracking configuration
			String xmlPath = AssetsManager.getAssetPath(getApplicationContext(), path);			
			result = metaioSDK.setTrackingConfiguration(xmlPath);
			MetaioDebug.log("Loaded tracking configuration "+xmlPath);			
		}       
		catch (Exception e)
		{
			MetaioDebug.log(Log.ERROR, "Error loading tracking configuration: "+ path + " " +e.getMessage());
			return result;
		}		
		return result;
	}

	@Override
	protected int getGUILayout()
	{
		return R.layout.tutorial_edge_based_initialization; 
	}

	@Override
	protected void onGeometryTouched(IGeometry geometry) 
	{
		
	}
	
}
