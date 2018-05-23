package augmented_reality.coordinate_transformation;

public class ScreenCoordinates {
    private final float mX;
    private final float mY;
    private final float mDepth;
    private final float mDistance;

    public ScreenCoordinates(final float x, final float y, final float depth, final float distance){
        mX = x;
        mY = y;
        mDepth = depth;
        mDistance = distance;
    }

    public float getX(){
        return mX;
    }
    public float getY(){
        return mY;
    }
    public float getDepth(){
        return mDepth;
    }
    public float getDistance(){
        return mDistance;
    }
}
