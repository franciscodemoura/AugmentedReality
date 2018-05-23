package augmented_reality.surfaces;

import android.graphics.Rect;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.List;

import augmented_reality.parameters.ParameterManager;
import augmented_reality.parameters.ParameterNames;

class ARCamera {

    private enum CameraStatus {RUNNING,STOPPED,UNLOCKED}

    private CameraStatus mCameraStatus = CameraStatus.STOPPED;
    private Camera mCamera = null;
    private SurfaceHolder mPreviewHolder;
    private final ParameterManager<String> mParameters;

    public ARCamera(final ParameterManager<String> parameters){
        mParameters = parameters;
    }

    private boolean initCamera(){
        if(mCamera == null){
            try {
                mCamera = Camera.open();
                return mCamera != null;
            }
            catch(RuntimeException ignored){
                return false;
            }
        }
        else{
            return true;
        }
    }

    public void releaseCamera(){
        if(mCamera != null){
            if(mCameraStatus == CameraStatus.RUNNING){
                mCamera.stopPreview();
            }
            mCamera.release();
            mCamera = null;
            mCameraStatus = CameraStatus.STOPPED;
        }

        mParameters.setParameter(ParameterNames.VIEW_ANGLES, new float[] {0.0f, 0.0f} );
    }

    public void stopPreview(final boolean lock_camera){
        if(mCamera != null){
            if(mCameraStatus == CameraStatus.RUNNING){
                mCamera.stopPreview();
                mCameraStatus = CameraStatus.STOPPED;
                if(!lock_camera) {
                    mCamera.unlock();
                    mCameraStatus = CameraStatus.UNLOCKED;
                }
            }
            else{
                if(mCameraStatus == CameraStatus.STOPPED  &&  !lock_camera){
                    mCameraStatus = CameraStatus.UNLOCKED;
                }
            }
        }

        mParameters.setParameter(ParameterNames.VIEW_ANGLES, new float[] {0.0f, 0.0f} );
    }

    public boolean startPreview(final SurfaceHolder preview_holder, final int orientation){
        if(!initCamera()){
            return false;
        }

        else{
            if(mCameraStatus == CameraStatus.UNLOCKED) {
                try {
                    mCamera.reconnect();
                    mCameraStatus = CameraStatus.STOPPED;
                } catch (Exception ignored) {
                    return false;
                }
            }

            if(mCameraStatus == CameraStatus.STOPPED){
                try {
                    if(preview_holder != null){
                        mPreviewHolder = preview_holder;
                    }
                    else{
                        if(mPreviewHolder == null){
                            throw(new Exception());
                        }
                    }
                    mCamera.setPreviewDisplay(mPreviewHolder);
                    adjustPreviewAndViewAngles(orientation);
                    mCamera.startPreview();
                    mCameraStatus = CameraStatus.RUNNING;
                }
                catch(Exception ignored){
                    return false;
                }
            }

            return true;
        }
    }

    private void adjustPreviewAndViewAngles(int orientation){

        final Rect surface_size = new Rect(mPreviewHolder.getSurfaceFrame());

        final Camera.CameraInfo camera_info = new Camera.CameraInfo();
        Camera.getCameraInfo(0,camera_info);
        final int camera_offset_orientation = camera_info.orientation;

        switch(orientation){
            case Surface.ROTATION_270 :
                mCamera.setDisplayOrientation( (90 + camera_offset_orientation + 360) % 360);
                break;

            case Surface.ROTATION_0 :
                mCamera.setDisplayOrientation( (0 + camera_offset_orientation + 360) % 360);

                int temp = surface_size.right;
                surface_size.right = surface_size.bottom;
                surface_size.bottom = temp;

                break;

            case Surface.ROTATION_90 :
                mCamera.setDisplayOrientation( (270 + camera_offset_orientation + 360) % 360);
                break;

            case Surface.ROTATION_180 :
                mCamera.setDisplayOrientation( (180 + camera_offset_orientation + 360) % 360);

                temp = surface_size.right;
                surface_size.right = surface_size.bottom;
                surface_size.bottom = temp;

                break;

            default: break;
        }

        final float surface_aspect_ratio = (float) surface_size.bottom / (float) surface_size.right;
        Camera.Size best_size = null;
        float best_grade = 0.0f;
        Camera.Size biggest_size = null;

        final float aspect_ratio_weight = (Float) mParameters.getParameter(ParameterNames.ASPECT_RATIO_OPTIMIZATION_WEIGHT);
        final Camera.Parameters camera_parameters = mCamera.getParameters();
        final List<Camera.Size> supportedSizes = camera_parameters.getSupportedPreviewSizes();

        for (Camera.Size size : supportedSizes) {

            final float preview_aspect_ratio = (float) size.height / (float) size.width;
            final float grade = size.width * size.height / (0.05f + aspect_ratio_weight*Math.abs(preview_aspect_ratio-surface_aspect_ratio));

            if (best_size == null || grade > best_grade) {
                best_size = size;
                best_grade = grade;
            }

            if (biggest_size == null || size.width * size.height > biggest_size.width * biggest_size.height){
                biggest_size = size;
            }
        }

        assert best_size != null;
        camera_parameters.setPreviewSize(best_size.width,best_size.height);
        mCamera.setParameters(camera_parameters);

        final float camera_horizontal_angle = camera_parameters.getHorizontalViewAngle();
        final float camera_vertical_angle = camera_parameters.getVerticalViewAngle();

        if( best_size.width / biggest_size.width  >=  best_size.height / biggest_size.height ) {

            final float height_ratio = ((float) best_size.height * (float) biggest_size.width) / ((float) biggest_size.height * (float) best_size.width);
            final float preview_vertical_angle =
                    (float) (
                            2.0 * Math.atan(
                                    (double) height_ratio *
                                            Math.tan(
                                                    (double) camera_vertical_angle / 180.0 * Math.PI /
                                                            2.0
                                            )
                            ) / Math.PI * 180.0
                    );

            mParameters.setParameter(ParameterNames.VIEW_ANGLES, new float[]{camera_horizontal_angle, preview_vertical_angle});
        }
        else{
            final float width_ratio = ((float) best_size.width * (float) biggest_size.height) / ((float) biggest_size.width * (float) best_size.height);
            final float preview_horizontal_angle =
                    (float) (
                            2.0 * Math.atan(
                                    (double) width_ratio *
                                            Math.tan(
                                                    (double) camera_horizontal_angle / 180.0 * Math.PI /
                                                            2.0
                                            )
                            ) / Math.PI * 180.0
                    );

            mParameters.setParameter(ParameterNames.VIEW_ANGLES, new float[]{preview_horizontal_angle, camera_vertical_angle});
        }
    }
}
