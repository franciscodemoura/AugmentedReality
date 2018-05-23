package augmented_reality.coordinate_transformation;

import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.location.Location;

import augmented_reality.kernel.AugmentedRealityKernelInterface;
import augmented_reality.parameters.ParameterNames;

public class ARTransform {

    private static final float[][] mScreenRotationAdjust = {
            {
                    1.0f,0.0f,
                    0.0f,-1.0f
            },
            {
                    0.0f,-1.0f,
                    -1.0f,0.0f
            },
            {
                    -1.0f,0.0f,
                    0.0f,1.0f
            },
            {
                    0.0f,1.0f,
                    1.0f,0.0f
            }
    };


    private Location mLocation;
    private GeomagneticField mGeomagneticField;
    private final float mWindowWidthBy2;
    private final float mWindowHeightBy2;
    private final float[] M;
    private final float[] mRotation = new float[9];
    private boolean mTransformOK = false;

    public ARTransform(
            final int window_width,
            final int window_height,
            final float horizontal_view_angle,
            final float vertical_view_angle,
            final int surface_rotation
    ) {
        mWindowWidthBy2 = window_width / 2.0f;
        mWindowHeightBy2 = window_height / 2.0f;
        M = mScreenRotationAdjust[surface_rotation].clone();

        float ya = (float) Math.sin(horizontal_view_angle/2.0f/180.0f*Math.PI);
        float xa = (float) Math.sin(vertical_view_angle/2.0f/180.0f*Math.PI);

        M[0] *= mWindowWidthBy2 / xa;
        M[1] *= mWindowWidthBy2 / ya;
        M[2] *= mWindowHeightBy2 / xa;
        M[3] *= mWindowHeightBy2 / ya;
    }

    public int update(
            final float[] gravity_vector,
            final float[] magnetic_field_vector,
            final Location location,
            final GeomagneticField geomagnetic_field
    ) {
        mLocation=location;
        mGeomagneticField = geomagnetic_field;

        mTransformOK =
                gravity_vector != null &&
                magnetic_field_vector != null &&
                SensorManager.getRotationMatrix(mRotation,null, gravity_vector, magnetic_field_vector) &&
                mLocation != null &&
                mGeomagneticField != null;

        if(!mTransformOK){

            int status = AugmentedRealityKernelInterface.STATUS_OK_BIT;

            if(gravity_vector == null){
                status |= AugmentedRealityKernelInterface.GRAVITY_SENSOR_FAILED_BIT;
            }

            if(magnetic_field_vector == null){
                status |= AugmentedRealityKernelInterface.MAGNETIC_FIELD_SENSOR_FAILED_BIT;
            }

            if(mLocation == null){
                status |= AugmentedRealityKernelInterface.LOCATION_SENSOR_FAILED_BIT;
            }

            if(mGeomagneticField == null){
                status |= AugmentedRealityKernelInterface.GEOMAGNETIC_FIELD_FAILED_BIT;
            }

            return status;
        }

        else{
            if (location.getExtras().getBoolean(ParameterNames.PRECISE_LOCATION_OK)) {
                return AugmentedRealityKernelInterface.STATUS_OK_BIT;
            } else {
                return AugmentedRealityKernelInterface.LOW_ACCURACY_LOCATION_BIT;
            }
        }
    }

    public ScreenCoordinates getScreenCoordinatesFromLocation(final Location location){

        if(!mTransformOK){
            return null;
        }

        final float distance_meters = mLocation.distanceTo(location);
        final double bearing_from_magnetic_north_radians = (double)(mLocation.bearingTo(location) - mGeomagneticField.getDeclination()) * Math.PI / 180.0;
        final float x = distance_meters * (float) Math.sin(bearing_from_magnetic_north_radians);
        final float y = distance_meters * (float) Math.cos(bearing_from_magnetic_north_radians);
        final float z = (float)(location.getAltitude() - mLocation.getAltitude());

        return getScreenCoordinatesFromGlobalReferenceFrame(x,y,z);
    }

    public ScreenCoordinates getScreenCoordinatesFromDirection(final float angle_from_north_towards_east, final float elevation_angle){

        if(!mTransformOK){
            return null;
        }

        final double bearing_from_magnetic_north_radians = (double)(angle_from_north_towards_east - mGeomagneticField.getDeclination()) * Math.PI / 180.0;
        final double elevation_cosine = Math.cos(elevation_angle * Math.PI / 180.0);
        final float x = (float) (Math.sin(bearing_from_magnetic_north_radians) * elevation_cosine);
        final float y = (float) (Math.cos(bearing_from_magnetic_north_radians) * elevation_cosine);
        final float z = (float) Math.sin(elevation_angle * Math.PI / 180.0);

        return getScreenCoordinatesFromGlobalReferenceFrame(x,y,z);
    }

    private ScreenCoordinates getScreenCoordinatesFromGlobalReferenceFrame(final float x, final float y, final float z){
        final float[] v = new float[3];
        v[0] = mRotation[0] * x + mRotation[3] * y + mRotation[6] * z;
        v[1] = mRotation[1] * x + mRotation[4] * y + mRotation[7] * z;
        v[2] = mRotation[2] * x + mRotation[5] * y + mRotation[8] * z;

        return convertToScreenCoordinates(v);
    }

    private ScreenCoordinates convertToScreenCoordinates(float[] v){
        if(Math.abs(v[2]) < 0.03f * (Math.abs(v[0]) + Math.abs(v[1])) ){
            return null;
        }

        final float X = -v[0] / v[2];
        final float Y = -v[1] / v[2];
        final float xs = mWindowWidthBy2 + M[0]*X + M[1]*Y;
        final float ys = mWindowHeightBy2 + M[2]*X + M[3]*Y;

        return new ScreenCoordinates(xs,ys,-v[2],(float)Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]));
    }
}
