package augmented_reality.kernel;

import android.content.Context;
import android.graphics.PixelFormat;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import augmented_reality.interfaces.AugmentedRealityContainer;
import augmented_reality.parameters.ParameterManager;
import augmented_reality.markers.Marker;
import augmented_reality.surfaces.ARCameraSurface;
import augmented_reality.surfaces.ARMarkersSurface;

import static augmented_reality.parameters.ParameterNames.*;

public class AugmentedRealityKernel implements AugmentedRealityKernelInterface {

    private final Context mContext;
    private final boolean mLockCamera;
    private final FrameLayout mUISpace;
    private final ARCameraSurface mARCameraSurface;
    private final ARMarkersSurface mARMarkersSurface;
    private float mTouchX;
    private float mTouchY;
    private final ParameterManager<String> mParameters = new ParameterManager<>();
    private MarkerViewEventListener mMarkerViewEventListener;

    private static final float DEFAULT_GRAVITY_FILTERING_COEFFICIENT = 0.95f;
    private static final float DEFAULT_MAGNETIC_FIELD_FILTERING_COEFFICIENT = 0.95f;

    public AugmentedRealityKernel(
            AugmentedRealityContainer container,
            Context context,
            final boolean lock_camera,
            final boolean optimize_aspect_ratio
    ){
        mContext = context;
        mLockCamera = lock_camera;

        FrameLayout main_frame = new FrameLayout(mContext);
        mARCameraSurface = new ARCameraSurface(mContext,mParameters);
        mARMarkersSurface = new ARMarkersSurface(
                mContext,
                mParameters
        );
        mARMarkersSurface.setZOrderMediaOverlay(true);
        mARMarkersSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);

        initTouchEventListening();

        mUISpace = new FrameLayout(mContext);

        main_frame.addView(mARCameraSurface);
        main_frame.addView(mARMarkersSurface);
        main_frame.addView(mUISpace);

        container.setRootViews(main_frame, mUISpace);

        setGravityFilteringCoefficient(DEFAULT_GRAVITY_FILTERING_COEFFICIENT);
        setMagneticFieldFilteringCoefficient(DEFAULT_MAGNETIC_FIELD_FILTERING_COEFFICIENT);
        setSleepTimeBetweenFrames(0);

        mParameters.setParameter(ASPECT_RATIO_OPTIMIZATION_WEIGHT, optimize_aspect_ratio ? 1.0f : 0.0f);
    }

    public void setContentView(final int layoutResID){
        mUISpace.removeAllViews();
        LayoutInflater.from(mContext).inflate(layoutResID, mUISpace);
    }

    public void setContentView(final View view){
        mUISpace.removeAllViews();
        mUISpace.addView(view);
    }

    public void setContentView(final View view, final ViewGroup.LayoutParams params){
        mUISpace.removeAllViews();
        mUISpace.addView(view, params);
    }

    public void addContentView(final View view, final ViewGroup.LayoutParams params){
        mUISpace.addView(view, params);
    }

    public void pause(){
        mARCameraSurface.stopCamera(mLockCamera);
    }

    public void resume(){
        mARCameraSurface.restartCamera();
    }

    public Context getContext(){
        return mContext;
    }

    @Override
    public void addMarker(Marker marker){
        mARMarkersSurface.addMarker(marker);
    }

    @Override
    public boolean removeMarker(Marker marker){
        return mARMarkersSurface.removeMarker(marker);
    }

    @Override
    public Marker findFirstMarkerById(final String id) {
        return mARMarkersSurface.findFirstMarkerById(id);
    }

    @Override
    public void clearMarkers(){
        mARMarkersSurface.clearMarkers();
    }

    @Override
    public int getNumberOfMarkers() {
        return mARMarkersSurface.getNumberOfMarkers();
    }

    @Override
    public Marker getMarkerAtPosition(int pos) {
        return mARMarkersSurface.getMarkerAtPosition(pos);
    }

    @Override
    public void setMarkerViewEventListener(final MarkerViewEventListener listener){
        mMarkerViewEventListener = listener;
    }

    @Override
    public Location getLocation(){
        return mARMarkersSurface.getLocation();
    }

    @Override
    public int getStatus(){
        return mARMarkersSurface.getStatus();
    }

    @Override
    public String decodeStatus(int status) {
        return mARMarkersSurface.decodeStatus(status);
    }

    @Override
    public void setGravityFilteringCoefficient(final float coefficient){
        mParameters.setParameter(GRAVITY_FILTER_COEFFICIENT, coefficient);
    }

    @Override
    public float getGravityFilteringCoefficient(){
        return (Float) mParameters.getParameter(GRAVITY_FILTER_COEFFICIENT);
    }

    @Override
    public void setMagneticFieldFilteringCoefficient(final float coefficient){
        mParameters.setParameter(MAGNETIC_FIELD_FILTER_COEFFICIENT, coefficient);
    }

    @Override
    public float getMagneticFieldFilteringCoefficient(){
        return (Float) mParameters.getParameter(MAGNETIC_FIELD_FILTER_COEFFICIENT);
    }

    @Override
    public void setSleepTimeBetweenFrames(final long time){
        mParameters.setParameter(SLEEP_TIME_BETWEEN_FRAMES, time);
    }

    @Override
    public long getSleepTimeBetweenFrames(){
        return (Long) mParameters.getParameter(SLEEP_TIME_BETWEEN_FRAMES);
    }

    @Override
    public void setTimeBetweenLocationReads(final long time){
        mParameters.setParameter(TIME_BETWEEN_LOCATION_READS, time);
    }

    @Override
    public long getTimeBetweenLocationReads(){
        return (Long) mParameters.getParameter(TIME_BETWEEN_LOCATION_READS);
    }

    private void handleClickEvent(){
        if(mMarkerViewEventListener != null){
            final Marker marker = mARMarkersSurface.getTouchedMarker(mTouchX,mTouchY);
            mMarkerViewEventListener.onClick(marker);
        }
    }

    private void initTouchEventListening() {

        mARMarkersSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickEvent();
            }
        });

        mARMarkersSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mTouchX = event.getX();
                    mTouchY = event.getY();
                }
                return false;
            }
        });
    }
}
