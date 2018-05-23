package augmented_reality.markers;

import android.graphics.Canvas;
import android.location.Location;

import augmented_reality.interfaces.LocationToScreenCoordinatesTranslator;
import augmented_reality.coordinate_transformation.ScreenCoordinates;

//Your marker will be accessed by more than one thread through the "onDraw" and the "touched" methods,
//so remember to serialize accesses to mutable fields of tour markers using the synchronized statement.

public abstract class Marker{
    private final String id;
    private LocationToScreenCoordinatesTranslator translator;

    public void SetTranslator(final LocationToScreenCoordinatesTranslator translator){
        this.translator = translator;
    }

    // May return null if the coordinate translation is not initialized yet or the produced screen coordinates are far outside the screen.
    protected ScreenCoordinates getScreenCoordinatesFromLocation(final Location location){
        return translator.getScreenCoordinatesFromLocation(location);
    }

    // May return null if the coordinate translation is not initialized yet or the produced screen coordinates are far outside the screen.
    protected ScreenCoordinates getScreenCoordinatesFromDirection(final float angle_from_north_towards_east, final float elevation_angle){
        return translator.getScreenCoordinatesFromDirection(angle_from_north_towards_east,elevation_angle);
    }

    protected int getWindowWidth(){
        return translator.getWindowWidth();
    }

    protected int getWindowHeight(){
        return translator.getWindowHeight();
    }

    public abstract void onDraw(final Canvas canvas);

    public abstract boolean touched(float x, float y);

    public Marker(final String id){
        this.id = id == null ? "" : id;
    }

    public Marker(){
        id = "";
    }

    public String getId(){
        return id;
    }
}
