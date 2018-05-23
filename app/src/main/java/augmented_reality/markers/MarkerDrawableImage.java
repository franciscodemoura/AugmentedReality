package augmented_reality.markers;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

class MarkerDrawableImage extends MarkerImage{

    private final Drawable mDrawable;
    private final float mWidth;
    private final float mHeight;

    public MarkerDrawableImage(
            final Drawable drawable,
            final float display_width,
            final float display_height
    ){
        mDrawable = drawable;
        mWidth = display_width;
        mHeight = display_height;
    }

    public void draw(final Canvas canvas, final float x, final float y, final float width_scale, final float height_scale){
        mDrawable.setBounds(
                (int)(x - mWidth*width_scale/2.0f + 0.5f),
                (int)(y - mHeight*height_scale/2.0f + 0.5f),
                (int)(x + mWidth*width_scale/2.0f + 0.5f),
                (int)(y + mHeight*height_scale/2.0f + 0.5f)
        );
        mDrawable.draw(canvas);
    }

    public float getWidth(){
        return mWidth;
    }

    public float getHeight(){
        return mHeight;
    }
}
