package augmented_reality.surfaces;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.hardware.GeomagneticField;

import augmented_reality.parameters.ParameterManager;

import static augmented_reality.parameters.ParameterNames.*;

class SensorThread extends HandlerThread implements SensorEventListener, LocationListener {
    private final ARMarkersSurface.SharedData<float[]> mGravityData;
    private final ARMarkersSurface.SharedData<float[]> mMagneticFieldData;
    private final ARMarkersSurface.SharedData<Location> mLocationData;
    private final ARMarkersSurface.SharedData<GeomagneticField> mGeomagneticFieldData;
    private final ParameterManager<String> mParameters;
    private float mGravityFilteringCoefficient;
    private float mMagneticFilteringCoefficient;
    private Handler mHandler;
    private final SensorManager mSensorManager;
    private Object mGravityCoefficientCallbackId;
    private Object mMagneticCoefficientCallbackId;
    private Object mTimeBetweenLocationReadsCallbackId;
    private final LocationManager mLocationManager;
    private long mTimeBetweenLocationReads = 0;
    private long mGPSDelay = 0;
    private long mLastTimeLocationWasCriticized = System.currentTimeMillis();

    public SensorThread(
            final String name,
            final Context context,
            final ARMarkersSurface.SharedData<float[]> gravity_data,
            final ARMarkersSurface.SharedData<float[]> magnetic_field_data,
            final ARMarkersSurface.SharedData<Location> location_data,
            final ARMarkersSurface.SharedData<GeomagneticField> geomagnetic_field_data,
            final ParameterManager<String> parameters
    ) {
        super(name);

        mParameters = parameters;

        mGravityData = gravity_data;
        mMagneticFieldData = magnetic_field_data;
        mLocationData = location_data;
        mGeomagneticFieldData = geomagnetic_field_data;

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        updateLocation(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        mGravityFilteringCoefficient = (Float) mParameters.getParameter(GRAVITY_FILTER_COEFFICIENT);
        mMagneticFilteringCoefficient = (Float) mParameters.getParameter(MAGNETIC_FIELD_FILTER_COEFFICIENT);
    }

    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler();
        initSensorListening();
        initParametersListening();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        final float[] new_data = new float[3];
        System.arraycopy(event.values,0,new_data,0,3);

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] old_data = mGravityData.getData();
            if (old_data != null) {
                new_data[0] = new_data[0] * (1.0f - mGravityFilteringCoefficient) + old_data[0] * mGravityFilteringCoefficient;
                new_data[1] = new_data[1] * (1.0f - mGravityFilteringCoefficient) + old_data[1] * mGravityFilteringCoefficient;
                new_data[2] = new_data[2] * (1.0f - mGravityFilteringCoefficient) + old_data[2] * mGravityFilteringCoefficient;
            }
            mGravityData.setData(new_data);

            criticizeCurrentLocationData();

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float[] old_data = mMagneticFieldData.getData();
            if (old_data != null) {
                new_data[0] = new_data[0] * (1.0f - mMagneticFilteringCoefficient) + old_data[0] * mMagneticFilteringCoefficient;
                new_data[1] = new_data[1] * (1.0f - mMagneticFilteringCoefficient) + old_data[1] * mMagneticFilteringCoefficient;
                new_data[2] = new_data[2] * (1.0f - mMagneticFilteringCoefficient) + old_data[2] * mMagneticFilteringCoefficient;
            }
            mMagneticFieldData.setData(new_data);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onLocationChanged(final Location location) {
        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
            mGPSDelay = System.currentTimeMillis() - location.getTime();
        }

        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public boolean isReady() {
        return mHandler != null;
    }

    private void initSensorListening(){
        Sensor gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (gravity == null) {
            gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        Sensor magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST, mHandler);
        mSensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_FASTEST, mHandler);

        requestLocationUpdates((Long) mParameters.getParameter(TIME_BETWEEN_LOCATION_READS));
    }

    private void initParametersListening(){

        mTimeBetweenLocationReadsCallbackId =
                mParameters.registerCallback(
                        TIME_BETWEEN_LOCATION_READS,
                        new ParameterManager.DataChangedCallback<String>() {
                            @Override
                            public void onDataChanged(String key, Object data, Object old_data) {
                                cancelLocationUpdates();
                                requestLocationUpdates((Long) data);
                            }
                        },
                        mHandler
                );

        mGravityCoefficientCallbackId =
                mParameters.registerCallback(
                        GRAVITY_FILTER_COEFFICIENT,
                        new ParameterManager.DataChangedCallback<String>() {
                            @Override
                            public void onDataChanged(String key, Object data, Object old_data) {
                                mGravityFilteringCoefficient = (Float) data;
                            }
                        },
                        mHandler
                );

        mMagneticCoefficientCallbackId =
                mParameters.registerCallback(
                        MAGNETIC_FIELD_FILTER_COEFFICIENT,
                        new ParameterManager.DataChangedCallback<String>() {
                            @Override
                            public void onDataChanged(String key, Object data, Object old_data) {
                                mMagneticFilteringCoefficient = (Float) data;
                            }
                        },
                        mHandler
                );
    }

    public void stopListeningToSensorsAndParameters() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSensorManager.unregisterListener(SensorThread.this);
                cancelLocationUpdates();
                mParameters.removeCallback(GRAVITY_FILTER_COEFFICIENT, mGravityCoefficientCallbackId);
                mParameters.removeCallback(MAGNETIC_FIELD_FILTER_COEFFICIENT, mMagneticCoefficientCallbackId);
                mParameters.removeCallback(TIME_BETWEEN_LOCATION_READS, mTimeBetweenLocationReadsCallbackId);
            }
        });
    }

    private void cancelLocationUpdates() {
        mLocationManager.removeUpdates(this);
    }

    private void requestLocationUpdates(long time) {
        mTimeBetweenLocationReads = time;
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                time,
                0.0f,
                this
        );

        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                time,
                0.0f,
                this
        );
    }

    private void updateLocation(final Location location) {

        if (location != null) {

            Location last_good_location = mLocationData.getData();

            if(
                    last_good_location == null ||
                    location.getProvider().equals(LocationManager.GPS_PROVIDER) ||
                    location.getTime() - last_good_location.getTime() > 2*60*1000
            ) {

                Location temp = new Location(location);
                mLocationData.setData(temp);

                final GeomagneticField geomagnetic_field =
                        new GeomagneticField(
                                (float) location.getLatitude(),
                                (float) location.getLongitude(),
                                (float) location.getAltitude(),
                                location.getTime()
                        );
                mGeomagneticFieldData.setData(geomagnetic_field);
            }
        }
    }

    private void criticizeCurrentLocationData(){

        final long current_time = System.currentTimeMillis();
        if (current_time - mLastTimeLocationWasCriticized < mTimeBetweenLocationReads) {
            return;
        }
        mLastTimeLocationWasCriticized = current_time;

        final Location current_location = mLocationData.getData();

        if(current_location != null) {
            final boolean precise_and_valid =
                    System.currentTimeMillis() - current_location.getTime() - mGPSDelay < 3L * mTimeBetweenLocationReads &&
                    current_location.getProvider().equals(LocationManager.GPS_PROVIDER);
            if (
                    current_location.getExtras() == null ||
                    current_location.getExtras().getBoolean(PRECISE_LOCATION_OK,!precise_and_valid) != precise_and_valid
            ) {
                final Location updated_location = new Location(current_location);

                if(updated_location.getExtras() == null){
                    updated_location.setExtras(new Bundle());
                }
                updated_location.getExtras().putBoolean(PRECISE_LOCATION_OK,precise_and_valid);

                mLocationData.setData(updated_location);
            }
        }
    }
}
