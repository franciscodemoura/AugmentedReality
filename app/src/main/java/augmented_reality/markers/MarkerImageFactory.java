package augmented_reality.markers;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public abstract class MarkerImageFactory {

    public static MarkerImage createMarkerImage(
            final Bitmap bitmap,
            final float display_width,
            final float display_height
    ){
        return new MarkerBitmapImage(
                bitmap,
                display_width,
                display_height
        );
    }

    public static MarkerImage createMarkerImage(
            final Drawable drawable,
            final float display_width,
            final float display_height
    ){
        return new MarkerDrawableImage(
                drawable,
                display_width,
                display_height
        );
    }

    public static MarkerImage createMarkerImage(
            final String text,
            final float height,
            final int color
    ){
        return new MarkerTextImage(
                text,
                height,
                color
        );
    }
}
