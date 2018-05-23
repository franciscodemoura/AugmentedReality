package augmented_reality.surfaces;

import android.content.Context;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import augmented_reality.parameters.ParameterManager;

public class ARCameraSurface extends SurfaceView implements SurfaceHolder.Callback{

    private final ARCamera mARCamera;
    private final Context mContext;
    private int mSurfaceRotation;

    {
        getHolder().addCallback(this);
    }

    public ARCameraSurface(final Context context, final ParameterManager<String> parameters){
        super(context);
        mContext = context;
        mARCamera = new ARCamera(parameters);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        updateSurfaceRotation();
        mARCamera.startPreview(holder, mSurfaceRotation);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mARCamera.stopPreview(true);
        updateSurfaceRotation();
        mARCamera.startPreview(holder, mSurfaceRotation);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mARCamera.releaseCamera();
    }

    public void stopCamera(final boolean lock_camera){
        mARCamera.stopPreview(lock_camera);
    }

    public void restartCamera(){
        if(getHolder().getSurface().isValid()) {
            updateSurfaceRotation();
            mARCamera.startPreview(getHolder(), mSurfaceRotation);
        }
    }

    private void updateSurfaceRotation(){
        final WindowManager window_manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        final Display display = window_manager.getDefaultDisplay();
        mSurfaceRotation = display.getRotation();
    }
}
