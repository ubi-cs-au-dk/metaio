// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.metaio.Example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.SystemInfo;
import com.metaio.tools.io.AssetsManager;

import java.io.FileOutputStream;

public class TutorialLocationBasedAR extends ARViewActivity {
    private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;

    private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;

    /**
     * Geometries
     */
    private IGeometry mLondonGeo;
    private IGeometry mMunichGeo;
    private IGeometry mRomeGeo;
    private IGeometry mTokyoGeo;
    private IGeometry mParisGeo;

    private IRadar mRadar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set GPS tracking configuration
        boolean result = metaioSDK.setTrackingConfiguration("GPS", false);
        MetaioDebug.log("Tracking data loaded: " + result);
    }

    @Override
    protected void onDestroy() {
        // Break circular reference of Java objects
        if (mAnnotatedGeometriesGroup != null) {
            mAnnotatedGeometriesGroup.registerCallback(null);
        }

        if (mAnnotatedGeometriesGroupCallback != null) {
            mAnnotatedGeometriesGroupCallback.delete();
            mAnnotatedGeometriesGroupCallback = null;
        }

        super.onDestroy();
    }

    @Override
    public void onDrawFrame() {
        if (metaioSDK != null && mSensors != null) {
            SensorValues sensorValues = mSensors.getSensorValues();

            float heading = 0.0f;
            if (sensorValues.hasAttitude()) {
                float m[] = new float[9];
                sensorValues.getAttitude().getRotationMatrix(m);

                Vector3d v = new Vector3d(m[6], m[7], m[8]);
                v = v.normalize();

                heading = (float) (-Math.atan2(v.getY(), v.getX()) - Math.PI / 2.0);
            }

            IGeometry geos[] = new IGeometry[]{mLondonGeo, mParisGeo, mRomeGeo, mTokyoGeo};
            Rotation rot = new Rotation((float) (Math.PI / 2.0), 0.0f, -heading);
            for (IGeometry geo : geos) {
                if (geo != null) {
                    geo.setRotation(rot);
                }
            }
        }

        super.onDrawFrame();
    }

    public void onButtonClick(View v) {
        finish();
    }

    @Override
    protected int getGUILayout() {
        return R.layout.tutorial_location_based_ar;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    protected void loadContents() {
        mAnnotatedGeometriesGroup = metaioSDK.createAnnotatedGeometriesGroup();
        mAnnotatedGeometriesGroupCallback = new MyAnnotatedGeometriesGroupCallback();
        mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);

        // Clamp geometries' Z position to range [5000;200000] no matter how close or far they are away.
        // This influences minimum and maximum scaling of the geometries (easier for development).
        metaioSDK.setLLAObjectRenderingLimits(5, 200);

        // Set render frustum accordingly
        metaioSDK.setRendererClippingPlaneLimits(10, 220000);

        // let's create LLA objects for known cities
        LLACoordinate munich = new LLACoordinate(48.142573, 11.550321, 0, 0);
        LLACoordinate london = new LLACoordinate(51.50661, -0.130463, 0, 0);
        LLACoordinate tokyo = new LLACoordinate(35.657464, 139.773865, 0, 0);
        LLACoordinate rome = new LLACoordinate(41.90177, 12.45987, 0, 0);
        LLACoordinate paris = new LLACoordinate(48.85658, 2.348671, 0, 0);

        // Load some POIs. Each of them has the same shape at its geoposition. We pass a string
        // (const char*) to IAnnotatedGeometriesGroup::addGeometry so that we can use it as POI title
        // in the callback, in order to create an annotation image with the title on it.
        mLondonGeo = createPOIGeometry(london);
        mAnnotatedGeometriesGroup.addGeometry(mLondonGeo, "London");

        mParisGeo = createPOIGeometry(paris);
        mAnnotatedGeometriesGroup.addGeometry(mParisGeo, "Paris");

        mRomeGeo = createPOIGeometry(rome);
        mAnnotatedGeometriesGroup.addGeometry(mRomeGeo, "Rome");

        mTokyoGeo = createPOIGeometry(tokyo);
        mAnnotatedGeometriesGroup.addGeometry(mTokyoGeo, "Tokyo");

        String metaioManModel = AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/metaioman.md2");
        if (metaioManModel != null) {
            mMunichGeo = metaioSDK.createGeometry(metaioManModel);
            if (mMunichGeo != null) {
                mMunichGeo.setTranslationLLA(munich);
                mMunichGeo.setLLALimitsEnabled(true);
                mMunichGeo.setScale(500);
            } else {
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + metaioManModel);
            }
        }

        // create radar
        mRadar = metaioSDK.createRadar();
        mRadar.setBackgroundTexture(AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/radar.png"));
        mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/yellow.png"));
        mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

        // add geometries to the radar
        mRadar.add(mLondonGeo);
        mRadar.add(mMunichGeo);
        mRadar.add(mTokyoGeo);
        mRadar.add(mParisGeo);
        mRadar.add(mRomeGeo);
    }

    private IGeometry createPOIGeometry(LLACoordinate lla) {
        String path = AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/ExamplePOI.obj");
        if (path != null) {
            IGeometry geo = metaioSDK.createGeometry(path);
            geo.setTranslationLLA(lla);
            geo.setLLALimitsEnabled(true);
            geo.setScale(100);
            return geo;
        } else {
            MetaioDebug.log(Log.ERROR, "Missing files for POI geometry");
            return null;
        }
    }

    private String getAnnotationImageForTitle(String title) {
        Bitmap billboard = null;

        try {
            final String texturepath = getCacheDir() + "/" + title + ".png";
            Paint mPaint = new Paint();

            // Load background image and make a mutable copy

            float dpi = SystemInfo.getDisplayDensity(getApplicationContext());
            int scale = dpi > 240 ? 2 : 1;
            String filepath = AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/POI_bg" + (scale == 2 ? "@2x" : "") + ".png");
            Bitmap mBackgroundImage = BitmapFactory.decodeFile(filepath);

            billboard = mBackgroundImage.copy(Bitmap.Config.ARGB_8888, true);

            Canvas c = new Canvas(billboard);

            mPaint.setColor(Color.WHITE);
            mPaint.setTextSize(24);
            mPaint.setTypeface(Typeface.DEFAULT);
            mPaint.setTextAlign(Paint.Align.CENTER);

            float y = 40 * scale;
            float x = 30 * scale;

            // Draw POI name
            if (title.length() > 0) {
                String n = title.trim();

                final int maxWidth = 160 * scale;

                int i = mPaint.breakText(n, true, maxWidth, null);

                int xPos = (c.getWidth() / 2);
                int yPos = (int) ((c.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));
                c.drawText(n.substring(0, i), xPos, yPos, mPaint);

                // Draw second line if valid
                if (i < n.length()) {
                    n = n.substring(i);
                    y += 20 * scale;
                    i = mPaint.breakText(n, true, maxWidth, null);

                    if (i < n.length()) {
                        i = mPaint.breakText(n, true, maxWidth - 20 * scale, null);
                        c.drawText(n.substring(0, i) + "...", x, y, mPaint);
                    } else {
                        c.drawText(n.substring(0, i), x, y, mPaint);
                    }
                }
            }

            // Write texture file
            try {
                FileOutputStream out = new FileOutputStream(texturepath);
                billboard.compress(Bitmap.CompressFormat.PNG, 90, out);
                MetaioDebug.log("Texture file is saved to " + texturepath);
                return texturepath;
            } catch (Exception e) {
                MetaioDebug.log("Failed to save texture file");
                e.printStackTrace();
            }
        } catch (Exception e) {
            MetaioDebug.log("Error creating annotation texture: " + e.getMessage());
            MetaioDebug.printStackTrace(Log.DEBUG, e);
            return null;
        } finally {
            if (billboard != null) {
                billboard.recycle();
                billboard = null;
            }
        }

        return null;
    }

    @Override
    protected void onGeometryTouched(final IGeometry geometry) {
        MetaioDebug.log("Geometry selected: " + geometry);

        mSurfaceView.queueEvent(new Runnable() {

            @Override
            public void run() {
                mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/yellow.png"));
                mRadar.setObjectTexture(geometry, AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/red.png"));
            }
        });
    }

    final class MyAnnotatedGeometriesGroupCallback extends AnnotatedGeometriesGroupCallback {

        @Override
        public IGeometry loadUpdatedAnnotation(IGeometry geometry, Object userData,
                                               IGeometry existingAnnotation) {
            if (userData == null) {
                return null;
            }

            if (existingAnnotation != null) {
                // We don't update the annotation if e.g. distance has changed
                return existingAnnotation;
            }

            String title = (String) userData; // as passed to addGeometry
            String texturePath = getAnnotationImageForTitle(title);

            return metaioSDK.createGeometryFromImage(texturePath, true, false);
        }
    }
}
