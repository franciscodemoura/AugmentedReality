package augmented_reality.markers;

import android.graphics.Canvas;
import android.location.Location;

import augmented_reality.coordinate_transformation.ScreenCoordinates;

public class SimpleLocationMarker extends Marker {
    private final Location mLocation;
    private final MarkerImage mMarkerImage;
    private final float mXOffset;
    private final float mYOffset;
    private ScreenCoordinates mLastScreenCoordinate;

    public SimpleLocationMarker(
            final String id,
            final MarkerImage marker_image,
            final Location location,
            final float x_offset,
            final float y_offset
    ){
        super(id);
        mLocation = location;
        mMarkerImage = marker_image;
        mXOffset = x_offset;
        mYOffset = y_offset;
    }

    public SimpleLocationMarker(
            final String id,
            final MarkerImage marker_image,
            final Location location
    ){
        this(
                id,
                marker_image,
                location,
                0.0f,
                0.0f
        );
    }

    @Override
    public final synchronized void onDraw(final Canvas canvas){

        mLastScreenCoordinate = getScreenCoordinatesFromLocation(mLocation);

        if(mLastScreenCoordinate != null && mLastScreenCoordinate.getDepth() > 0.0f){

            final float scale = getScaleFromDistance(mLastScreenCoordinate.getDepth());

            if(scale > 0.0f) {
                mMarkerImage.draw(
                        canvas,
                        mLastScreenCoordinate.getX() + mXOffset * scale,
                        mLastScreenCoordinate.getY() + mYOffset * scale,
                        scale,
                        scale
                );
            }
        }
    }

    @Override
    public boolean touched(final float x, final float y) {

        if(mLastScreenCoordinate != null && mLastScreenCoordinate.getDepth() > 0.0f){

            final float scale = getScaleFromDistance(mLastScreenCoordinate.getDepth());

            if(scale > 0.0f) {
                final float w = mMarkerImage.getWidth();
                final float h = mMarkerImage.getHeight();

                return
                        x >= mLastScreenCoordinate.getX() + (mXOffset - w / 2.0f) * scale &&
                        y >= mLastScreenCoordinate.getY() + (mYOffset - h / 2.0f) * scale &&
                        x <= mLastScreenCoordinate.getX() + (mXOffset + w / 2.0f) * scale &&
                        y <= mLastScreenCoordinate.getY() + (mYOffset + h / 2.0f) * scale;
            }
        }

        return false;
    }

    protected float getScaleFromDistance(final float distance){
        return 1.0f;
    }
}
