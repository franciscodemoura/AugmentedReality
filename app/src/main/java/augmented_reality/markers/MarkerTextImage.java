package augmented_reality.markers;

import android.graphics.Canvas;
import android.graphics.Paint;

class MarkerTextImage extends MarkerImage {

    private final String mText;
    private final float mHeight;
    private final float mWidth;
    private final Paint mPaint = new Paint();

    public MarkerTextImage(
            final String text,
            final float height,
            final int color
    ){
        mText = text;
        mHeight = height;
        mPaint.setColor(color);
        mPaint.setTextSize(mHeight);
        mWidth = mPaint.measureText(mText);
    }

    public void draw(final Canvas canvas, final float x, final float y, final float width_scale, final float height_scale) {
        canvas.save();

        canvas.translate(x,y);
        canvas.scale(width_scale,height_scale);

        canvas.drawText(
                mText,
                -mWidth/2.0f,
                mHeight/2.0f,
                mPaint
        );

        canvas.restore();
    }

    public float getWidth(){
        return mWidth;
    }

    public float getHeight(){
        return mHeight;
    }
}
