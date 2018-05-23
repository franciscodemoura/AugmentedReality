package augmented_reality.markers;

import android.graphics.Canvas;

import augmented_reality.markers.Marker;

public abstract class ScreenCanvasMarker extends Marker {

    @Override
    public void onDraw(Canvas canvas) {
        onDrawToScreen(canvas, getWindowWidth(), getWindowHeight());
    }

    @Override
    public boolean touched(float x, float y) {
        return false;
    }

    public abstract void onDrawToScreen(Canvas canvas, int screen_width, int screen_height);
}
