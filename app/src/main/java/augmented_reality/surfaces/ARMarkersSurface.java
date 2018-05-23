package augmented_reality.surfaces;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Build;
import android.os.HandlerThread;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import augmented_reality.interfaces.ARStatusInterface;
import augmented_reality.coordinate_transformation.ARTransform;
import augmented_reality.interfaces.LocationToScreenCoordinatesTranslator;
import augmented_reality.parameters.ParameterManager;
import augmented_reality.parameters.ParameterNames;
import augmented_reality.coordinate_transformation.ScreenCoordinates;
import augmented_reality.markers.Marker;
import augmented_reality.markers.MarkerManager;
import augmented_reality.markers.MarkerManagerInterface;

public class ARMarkersSurface extends SurfaceView implements
        SurfaceHolder.Callback,
        LocationToScreenCoordinatesTranslator,
        MarkerManager,
        MarkerManagerInterface,
        ARStatusInterface {

    public static class SharedData<T> {
        private final AtomicReference<T> data = new AtomicReference<>();

        T getData() {
            return data.get();
        }

        void setData(T data) {
            this.data.set(data);
        }
    }

    private final List<Marker> mMarkers = new LinkedList<>();
    private final SharedData<float[]> mGravityData = new SharedData<>();
    private final SharedData<float[]> mMagneticFieldData = new SharedData<>();
    private final SharedData<Location> mLocationData = new SharedData<>();
    private final SharedData<float[]> mViewAngles = new SharedData<>();
    private final SharedData<GeomagneticField> mGeomagneticFieldData = new SharedData<>();
    private final SharedData<Integer> mStatus = new SharedData<>();
    private volatile int mSurfaceRotation;
    private volatile int mWindowWidth;
    private volatile int mWindowHeight;
    private boolean mCallbackSet = false;
    private Object mViewAnglesCallbackID;

    private DrawerThread mDrawerThread;
    private SensorThread mSensorThread;
    private final Context mContext;
    private final ParameterManager<String> mParameters;
    private volatile ARTransform mARTransform;

    {
        getHolder().addCallback(this);
    }

    public ARMarkersSurface(
            final Context context,
            final ParameterManager<String> parameters
    ) {
        super(context);
        mContext = context;
        mParameters = parameters;
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        final Rect rect = holder.getSurfaceFrame();
        mWindowWidth = rect.right;
        mWindowHeight = rect.bottom;
        updateSurfaceRotation();
        mARTransform = null;

        startThreads(holder);
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        stopThreads();

        mWindowWidth = width;
        mWindowHeight = height;
        updateSurfaceRotation();
        mARTransform = null;

        startThreads(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThreads();
    }

    private void updateSurfaceRotation() {
        final WindowManager window_manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        final Display display = window_manager.getDefaultDisplay();
        mSurfaceRotation = display.getRotation();
    }

    @Override
    public void addMarker(final Marker marker) {
        marker.SetTranslator(this);
        synchronized (mMarkers) {
            mMarkers.add(marker);
        }
    }

    @Override
    public boolean removeMarker(final Marker marker) {
        synchronized (mMarkers) {
            return mMarkers.remove(marker);
        }
    }

    @Override
    public Marker findFirstMarkerById(String id) {
        if(id == null){
            id = "";
        }

        final Marker[] markers = getMarkerArray(new Marker[0]);

        for(Marker marker : markers){
            if(marker.getId().equals(id)){
                return marker;
            }
        }

        return null;
    }

    @Override
    public void clearMarkers() {
        synchronized (mMarkers) {
            mMarkers.clear();
        }
    }

    @Override
    public int getNumberOfMarkers() {
        synchronized (mMarkers) {
            return mMarkers.size();
        }
    }

    @Override
    public Marker getMarkerAtPosition(final int pos) {
        synchronized (mMarkers) {
            return mMarkers.get(pos);
        }
    }

    @Override
    public int getStatus(){
        if(mStatus.getData() == null)
            return STATUS_OK_BIT;
        else {
            return mStatus.getData();
        }
    }

    @Override
    public String decodeStatus(int status) {
        String status_string = "";
        if(status == STATUS_OK_BIT){
            return STATUS_OK_STRING;
        }
        else{
            if((status & GRAVITY_SENSOR_FAILED_BIT) != 0){
                status_string += GRAVITY_SENSOR_FAILED_STRING;
            }
            if((status & MAGNETIC_FIELD_SENSOR_FAILED_BIT) != 0){
                status_string += MAGNETIC_FIELD_SENSOR_FAILED_STRING;
            }
            if((status & LOCATION_SENSOR_FAILED_BIT) != 0){
                status_string += LOCATION_SENSOR_FAILED_STRING;
            }
            if((status & GEOMAGNETIC_FIELD_FAILED_BIT) != 0){
                status_string += GEOMAGNETIC_FIELD_FAILED_STRING;
            }
            if((status & LOW_ACCURACY_LOCATION_BIT) != 0){
                status_string += LOW_ACCURACY_LOCATION_STRING;
            }

            return status_string;
        }
    }

    @Override
    public Location getLocation(){
        return new Location(mLocationData.getData());
    }

    public Marker getTouchedMarker(final float x, final float y){
        final Marker[] markers = getMarkerArray(new Marker[0]);
        for(int i=markers.length-1; i>=0; i--){
            final Marker marker = markers[i];
            if(marker.touched(x,y)){
                return marker;
            }
        }
        return null;
    }

    @Override
    public Marker[] getMarkerArray(final Marker[] markers) {
        synchronized (mMarkers)
        {
            return mMarkers.toArray(markers);
        }
    }

    @Override
    public void updateARTransform() {

        if (mARTransform == null) {

            if(!mCallbackSet) {
                while(
                        null == (mViewAnglesCallbackID =
                            mParameters.registerCallback(
                                ParameterNames.VIEW_ANGLES,
                                new ParameterManager.DataChangedCallback() {
                                    @Override
                                    public void onDataChanged(Object key, Object data, Object old_data) {
                                        mViewAngles.setData((float[]) data);
                                    }
                                }
                            ))
                ) {}
                mCallbackSet = true;
                mViewAngles.setData((float[]) mParameters.getParameter(ParameterNames.VIEW_ANGLES));
            }

            final float[] view_angles = mViewAngles.getData();

            mARTransform = new ARTransform(
                    mWindowWidth,
                    mWindowHeight,
                    view_angles[0],
                    view_angles[1],
                    mSurfaceRotation
            );
        }

        final int status =
            mARTransform.update(
                    mGravityData.getData(),
                    mMagneticFieldData.getData(),
                    mLocationData.getData(),
                    mGeomagneticFieldData.getData()
            );

        mStatus.setData(status);
    }

    @Override
    public void stopListeningToParameters(){
        mParameters.removeCallback(ParameterNames.VIEW_ANGLES,mViewAnglesCallbackID);
        mCallbackSet = false;
    }

    @Override
    public ScreenCoordinates getScreenCoordinatesFromLocation(final Location location) {
        return mARTransform.getScreenCoordinatesFromLocation(location);
    }

    @Override
    public ScreenCoordinates getScreenCoordinatesFromDirection(final float angle_from_north_towards_east, final float elevation_angle) {
        return mARTransform.getScreenCoordinatesFromDirection(angle_from_north_towards_east, elevation_angle);
    }

    @Override
    public int getWindowWidth() {
        return mWindowWidth;
    }

    @Override
    public int getWindowHeight() {
        return mWindowHeight;
    }

    private void startThreads(final SurfaceHolder holder){
        mDrawerThread = new DrawerThread(
                this,
                holder,
                mParameters
        );
        mDrawerThread.start();

        mSensorThread = new SensorThread(
                "Sensor thread",
                mContext,
                mGravityData,
                mMagneticFieldData,
                mLocationData,
                mGeomagneticFieldData,
                mParameters
        );
        mSensorThread.start();
    }

    private void stopThreads(){
        if (mDrawerThread != null) {
            mDrawerThread.interrupt();
            try {
                mDrawerThread.join();
            } catch (InterruptedException ignored) {
            }
            mDrawerThread = null;
        }

        if (mSensorThread != null) {
            while(!mSensorThread.isReady()){}
            mSensorThread.stopListeningToSensorsAndParameters();

            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                quitHandlerThread(mSensorThread);
            }
            else{
                mSensorThread.quit();
            }

            try {
                mSensorThread.join();
            } catch (InterruptedException ignored) {
            }
            mSensorThread = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void quitHandlerThread(HandlerThread t){
        t.quitSafely();
    }
}
