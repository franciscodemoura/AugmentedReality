package augmented_reality.markers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

class MarkerBitmapImage extends MarkerImage{

    private final Bitmap mBitmap;
    private final int mWidth;
    private final int mHeight;
    private final float mWidthScale;
    private final float mHeightScale;
    private final Paint mPaint = new Paint();

    {
        mPaint.setAntiAlias(true);
    }

    public MarkerBitmapImage(
            final Bitmap bitmap,
            final float display_width,
            final float display_height
    ){
        mBitmap = bitmap;
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();
        mWidthScale = display_width / mWidth;
        mHeightScale = display_height / mHeight;
    }

    public void draw(final Canvas canvas, final float x, final float y, final float width_scale, final float height_scale){
        canvas.save();

        canvas.translate(x,y);
        canvas.scale(mWidthScale*width_scale, mHeightScale*height_scale);

        canvas.drawBitmap(mBitmap, -mWidth/2.0f,-mHeight/2.0f, mPaint);

        canvas.restore();
    }

    public float getWidth(){
        return mWidth*mWidthScale;
    }

    public float getHeight(){
        return mHeight*mHeightScale;
    }
}
