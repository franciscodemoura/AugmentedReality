package augmented_reality.markers;

import android.graphics.Canvas;

public abstract class MarkerImage {
    public abstract void draw(Canvas canvas, float x, float y, float width_scale, float height_scale);
    public abstract float getWidth();
    public abstract float getHeight();
}
