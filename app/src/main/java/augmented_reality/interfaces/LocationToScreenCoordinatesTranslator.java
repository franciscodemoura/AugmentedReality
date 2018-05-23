package augmented_reality.interfaces;

import android.location.Location;

import augmented_reality.coordinate_transformation.ScreenCoordinates;

public interface LocationToScreenCoordinatesTranslator {
    ScreenCoordinates getScreenCoordinatesFromLocation(Location location);
    ScreenCoordinates getScreenCoordinatesFromDirection(float angle_from_north_towards_east, float elevation_angle);
    int getWindowWidth();
    int getWindowHeight();
}
