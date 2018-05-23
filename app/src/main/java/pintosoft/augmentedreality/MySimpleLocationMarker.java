package pintosoft.augmentedreality;

import android.location.Location;

import augmented_reality.markers.MarkerImage;
import augmented_reality.markers.SimpleLocationMarker;

public class MySimpleLocationMarker extends SimpleLocationMarker{
    public MySimpleLocationMarker(
            final String id,
            final MarkerImage marker_image,
            final Location location
    ){
        super(id,marker_image,location);
    }

    @Override
    protected float getScaleFromDistance(final float distance){
        if(distance > 200.0f){
            return 0.0f;
        }
        else{
            final float s = distance/200.0f;
            return 1.0f*(1.0f-s) + 0.1f*s;
        }
    }
}
