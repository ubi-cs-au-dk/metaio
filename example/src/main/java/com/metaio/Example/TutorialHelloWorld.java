// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.metaio.Example;

import android.util.Log;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

public class TutorialHelloWorld extends ARViewActivity 
{
	private IGeometry mModel;

	
	@Override
	protected int getGUILayout() 
	{
		// Attaching layout to the activity
		return R.layout.tutorial_hello_world; 
	}


	public void onButtonClick(View v)
	{
		finish();
	}
	
	
	@Override
	protected void loadContents() 
	{
		try
		{
			// Getting a file path for tracking configuration XML file
			String trackingConfigFile = AssetsManager.getAssetPath(getApplicationContext(), "TutorialHelloWorld/Assets/TrackingData_MarkerlessFast.xml");
			
			// Assigning tracking configuration
			boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile); 
			MetaioDebug.log("Tracking data loaded: " + result); 
	        
			// Getting a file path for a 3D geometry
			String metaioManModel = AssetsManager.getAssetPath(getApplicationContext(), "TutorialHelloWorld/Assets/metaioman.md2");			
			if (metaioManModel != null) 
			{
				// Loading 3D geometry
				mModel = metaioSDK.createGeometry(metaioManModel);
				if (mModel != null) 
				{
					// Set geometry properties
					mModel.setScale(new Vector3d(4.0f, 4.0f, 4.0f));
					
				}
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+metaioManModel);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	@Override
	protected void onGeometryTouched(IGeometry geometry)
	{
		// Not used in this tutorial
	}


	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
	{
		// No callbacks needed in this tutorial
		return null;
	}	
}
