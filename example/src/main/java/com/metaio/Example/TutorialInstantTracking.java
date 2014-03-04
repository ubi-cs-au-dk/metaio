// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.metaio.Example;

import java.io.FileInputStream;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKAndroid;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.tools.io.AssetsManager;

public class TutorialInstantTracking extends ARViewActivity 
{

	/**
	 * Tiger geometry
	 */
	private IGeometry mTiger;
	
	/**
	 * metaio SDK callback handler
	 */
	private MetaioSDKCallbackHandler mCallbackHandler;

	/**
	 * Flag to indicate proximity to the tiger
	 */
	boolean mIsCloseToTiger;
	
	/**
	 * Media Player to play the sound of the tiger
	 */
	MediaPlayer mMediaPlayer;
	
	private View mInstant2DButton;
	private View mInstant3DButton;
	private View mInstant2DRectButton;
	
	/**
	 * The flag indicating a mode of instant tracking
	 * @see {@link IMetaioSDKAndroid#startInstantTracking(String, String, boolean)}
	 */
	boolean mPreview = true;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		try
		{
			mTiger = null;
			mCallbackHandler = new MetaioSDKCallbackHandler();
			mIsCloseToTiger = false;
			mMediaPlayer = new MediaPlayer();
			FileInputStream fis = new FileInputStream(AssetsManager.getAssetPath(getApplicationContext(), "TutorialInstantTracking/Assets/meow.mp3"));
			mMediaPlayer.setDataSource(fis.getFD());
			mMediaPlayer.prepare();
			fis.close();
			
			mInstant2DButton = mGUIView.findViewById(R.id.instant2dButton);
			mInstant2DRectButton = mGUIView.findViewById(R.id.instant2dRectifiedButton);
			mInstant3DButton = mGUIView.findViewById(R.id.instant3dButton);
		}
		catch (Exception e)
		{
			mMediaPlayer = null;
		}
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		mCallbackHandler.delete();
		mCallbackHandler = null;
		try
		{
			mMediaPlayer.release();
		}
		catch (Exception e)
		{
		}
	}
	
	/**
	 * This method is regularly called in the rendering loop. It calculates the distance
	 * between device and the target and performs actions based on the proximity 
	 */
	private void checkDistanceToTarget() 
	{
		// get tracking values for COS 1
		TrackingValues tv = metaioSDK.getTrackingValues(1);
		
		// Note, you can use this mechanism also to detect if something is tracking or not.
		// (e.g. for triggering an action as soon as some target is visible on screen)
		if (tv.isTrackingState())
		{			
			// calculate the distance as sqrt( x^2 + y^2 + z^2 )
			final float distance = tv.getTranslation().norm();
			
			// define a threshold distance
			final float threshold = 200;
			
			// moved close to the tiger
			if (distance < threshold)
			{
				// if not already close to the model
				if (!mIsCloseToTiger)
				{
					MetaioDebug.log("Moved close to the tiger");
					mIsCloseToTiger = true;
					playSound();
					mTiger.startAnimation("tap");
				}
			}
			else
			{
				if (mIsCloseToTiger)
				{
					MetaioDebug.log("Moved away from the tiger");
					mIsCloseToTiger = false;
				}
			}
			
		}
	}

	/**
	 * Play sound that has been loaded
	 */
	private void playSound()
	{
		try
		{
			MetaioDebug.log("Playing sound");
			mMediaPlayer.start();
		}
		catch (Exception e)
		{
			MetaioDebug.log("Error playing sound: "+e.getMessage());
		}
	}
	
	
	@Override
	protected int getGUILayout() 
	{
		return R.layout.tutorial_instant_tracking; 
	}
	
	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
	{
		return mCallbackHandler;
	}

	@Override
	public void onDrawFrame() 
	{
		super.onDrawFrame();
		
		checkDistanceToTarget();
		
	}

	public void onButtonClick(View v)
	{
		finish();
	}
	
	public void onInstant2dButtonClick(View v)
	{
		mTiger.setVisible(false);
		mInstant2DRectButton.setEnabled(!mPreview);
		mInstant3DButton.setEnabled(!mPreview);
		metaioSDK.startInstantTracking("INSTANT_2D", "", mPreview);
		mPreview = !mPreview; 
	}
	
	public void onInstant2dRectifiedButtonClick(View v)
	{
		mTiger.setVisible(false);
		mInstant2DButton.setEnabled(!mPreview);
		mInstant3DButton.setEnabled(!mPreview);
		metaioSDK.startInstantTracking("INSTANT_2D_GRAVITY", "", mPreview);
		mPreview = !mPreview; 
	}
	
	public void onInstant3dButtonClick(View v)
	{
		mTiger.setVisible(false);
		metaioSDK.startInstantTracking("INSTANT_3D");
	}
	
	@Override
	protected void loadContents() 
	{
		try
		{
			// Load tiger model
			String tigerModelPath = AssetsManager.getAssetPath(getApplicationContext(), "TutorialInstantTracking/Assets/tiger.md2");			
			mTiger = metaioSDK.createGeometry(tigerModelPath);
			
			// Set geometry properties and initially hide it
			mTiger.setScale(8f);
			mTiger.setRotation(new Rotation(0f, 0f, (float)Math.PI));
			mTiger.setVisible(false);
			mTiger.setAnimationSpeed(60f);
			mTiger.startAnimation("meow");					
			MetaioDebug.log("Loaded geometry "+tigerModelPath);
		}       
		catch (Exception e)
		{
			MetaioDebug.log(Log.ERROR, "Error loading geometry: "+e.getMessage());
		}
	}
	
  
	@Override
	protected void onGeometryTouched(IGeometry geometry) 
	{
		playSound();
		geometry.startAnimation("tap");
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
		public void onInstantTrackingEvent(boolean success, String file)
		{
			if(success)
			{
				MetaioDebug.log("MetaioSDKCallbackHandler.onInstantTrackingEvent: "+file);
				metaioSDK.setTrackingConfiguration(file);
				mTiger.setVisible(true);
			}
			else 
			{
				MetaioDebug.log(Log.ERROR, "Failed to create instant tracking configuration!");
			}
		}
		
		@Override
		public void onAnimationEnd(IGeometry geometry, String animationName) 
		{
			// Play a random animation from the list
			final String[] animations = {"meow", "scratch", "look", "shake", "clean"};
			final int random = (int)(Math.random()*animations.length);
			geometry.startAnimation(animations[random]);
		}
	}
	
}
