package augmented_reality.markers;

import android.graphics.Canvas;

import augmented_reality.coordinate_transformation.ScreenCoordinates;

public class SimpleDirectionMarker extends Marker {

    private final float mAngleNorth;
    private final float mAngleElevation;
    private final MarkerImage mMarkerImage;
    private final float mXOffset;
    private final float mYOffset;
    private ScreenCoordinates mLastScreenCoordinate;

    public SimpleDirectionMarker(
            final String id,
            final MarkerImage marker_image,
            final float angle_from_north_towards_east,
            final float elevation_angle,
            final float x_offset,
            final float y_offset
    ){
        super(id);
        mMarkerImage = marker_image;
        mAngleNorth = angle_from_north_towards_east;
        mAngleElevation = elevation_angle;
        mXOffset = x_offset;
        mYOffset = y_offset;
    }

    public SimpleDirectionMarker(
            final String id,
            final MarkerImage marker_image,
            final float angle_from_north_towards_east,
            final float elevation_angle
    ){
        this(
                id,
                marker_image,
                angle_from_north_towards_east,
                elevation_angle,
                0.0f,
                0.0f
        );
    }

    @Override
    public final synchronized void onDraw(final Canvas canvas){

        mLastScreenCoordinate = getScreenCoordinatesFromDirection(mAngleNorth,mAngleElevation);

        if(mLastScreenCoordinate != null && mLastScreenCoordinate.getDepth() > 0.0f){
            mMarkerImage.draw(
                    canvas,
                    mLastScreenCoordinate.getX() + mXOffset,
                    mLastScreenCoordinate.getY() + mYOffset,
                    1.0f,
                    1.0f
            );
        }
    }

    @Override
    public boolean touched(final float x, final float y) {

        if(mLastScreenCoordinate != null && mLastScreenCoordinate.getDepth() > 0.0f){
            final float w = mMarkerImage.getWidth();
            final float h = mMarkerImage.getHeight();

            return
                    x >= mLastScreenCoordinate.getX() + mXOffset - w/2.0f  &&
                    x <= mLastScreenCoordinate.getX() + mXOffset + w/2.0f  &&
                    y >= mLastScreenCoordinate.getY() + mYOffset - h/2.0f  &&
                    y <= mLastScreenCoordinate.getY() + mYOffset + h/2.0f;
        }
        else {
            return false;
        }
    }
}
