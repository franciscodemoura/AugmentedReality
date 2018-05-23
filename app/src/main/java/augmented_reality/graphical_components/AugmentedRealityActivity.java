package augmented_reality.graphical_components;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import augmented_reality.interfaces.AugmentedRealityContainer;
import augmented_reality.kernel.AugmentedRealityKernel;
import augmented_reality.kernel.AugmentedRealityKernelInterface;

public class AugmentedRealityActivity extends Activity implements AugmentedRealityContainer {

    private final boolean mFullscreen;
    private final boolean mLockCamera;
    private final long mTimeBetweenLocationReads;
    private AugmentedRealityKernel mARKernel;
    private final boolean mOptimizeAspectRatio;

    public AugmentedRealityActivity(final long time_between_location_reads){
        this(
                false,
                time_between_location_reads,
                true,
                true
        );
    }

    public AugmentedRealityActivity(final boolean fullscreen, final long time_between_location_reads){
        this(
                fullscreen,
                time_between_location_reads,
                true,
                true
        );
    }

    public AugmentedRealityActivity(
            final boolean fullscreen,
            final long time_between_location_reads,
            final boolean lock_camera,
            final boolean optimize_aspect_ratio
    ) {
        mFullscreen = fullscreen;
        mLockCamera = lock_camera;
        mTimeBetweenLocationReads = time_between_location_reads;
        mOptimizeAspectRatio = optimize_aspect_ratio;
    }

    @Override
    protected void onCreate(final Bundle savedInstance) {

        super.onCreate(savedInstance);

        if(mFullscreen){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        mARKernel = new AugmentedRealityKernel(
                this,
                this,
                mLockCamera,
                mOptimizeAspectRatio
        );
        mARKernel.setTimeBetweenLocationReads(mTimeBetweenLocationReads);
    }

    @Override
    public void setRootViews(final FrameLayout main_view, final FrameLayout client_view_container){
        super.setContentView(main_view);
    }

    @Override
    public void setContentView(final int layoutResID){
        mARKernel.setContentView(layoutResID);
    }

    @Override
    public void setContentView(final View view){
        mARKernel.setContentView(view);
    }

    @Override
    public void setContentView(final View view, final ViewGroup.LayoutParams params){
        mARKernel.setContentView(view, params);
    }

    @Override
    public void addContentView(final View view, final ViewGroup.LayoutParams params){
        mARKernel.addContentView(view, params);
    }

    @Override
    protected void onPause(){
        mARKernel.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mARKernel.resume();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        mARKernel.pause();
        super.onConfigurationChanged(newConfig);
        mARKernel.resume();
    }

    @Override
    public AugmentedRealityKernelInterface getARInterface(){
        return mARKernel;
    }
}
