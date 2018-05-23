package augmented_reality.interfaces;

import android.location.Location;

public interface ARStatusInterface {

    int GRAVITY_SENSOR_FAILED_BIT = 0x01;
    int MAGNETIC_FIELD_SENSOR_FAILED_BIT = 0x02;
    int LOCATION_SENSOR_FAILED_BIT = 0x04;
    int GEOMAGNETIC_FIELD_FAILED_BIT = 0x08;
    int LOW_ACCURACY_LOCATION_BIT = 0x10;
    int STATUS_OK_BIT = 0x00;

    String GRAVITY_SENSOR_FAILED_STRING = "Gravity sensor (accelerometer) failed; ";
    String MAGNETIC_FIELD_SENSOR_FAILED_STRING = "Magnetic field sensor (compass) failed; ";
    String LOCATION_SENSOR_FAILED_STRING = "Location sensor (GPS or Network) failed; ";
    String GEOMAGNETIC_FIELD_FAILED_STRING = "Geomagnetic field model failed; ";
    String LOW_ACCURACY_LOCATION_STRING = "Using low accuracy location; ";
    String STATUS_OK_STRING = "OK";

    Location getLocation();
    int getStatus();
    String decodeStatus(int status);
}
