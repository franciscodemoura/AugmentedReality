package augmented_reality.graphical_components;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import augmented_reality.interfaces.AugmentedRealityContainer;
import augmented_reality.kernel.AugmentedRealityKernel;
import augmented_reality.kernel.AugmentedRealityKernelInterface;
import augmented_reality.markers.Marker;

public abstract class AugmentedRealityFragment extends Fragment implements AugmentedRealityContainer {

    private final long mTimeBetweenLocationReads;
    private final boolean mLockCamera;
    private final boolean mOptimizeAspectRatio;

    private AugmentedRealityKernel mARKernel;
    private FrameLayout mMainView;
    private FrameLayout mClientViewContainer;

    public AugmentedRealityFragment(final long time_between_location_reads){
        this(
                time_between_location_reads,
                true,
                true
        );
    }

    protected AugmentedRealityFragment(
            final long time_between_location_reads,
            final boolean lock_camera,
            final boolean optimize_aspect_ratio
    ) {
        mLockCamera = lock_camera;
        mTimeBetweenLocationReads = time_between_location_reads;
        mOptimizeAspectRatio = optimize_aspect_ratio;
    }

    @Override
    public void onAttach(final Activity activity){
        super.onAttach(activity);

        Marker[] markers = null;
        if(mARKernel != null) {
            if( activity == mARKernel.getContext() ){
                return;
            }
            markers = new Marker[mARKernel.getNumberOfMarkers()];
            for(int i=0; i<mARKernel.getNumberOfMarkers(); i++){
                markers[i] = mARKernel.getMarkerAtPosition(i);
            }
        }

        mARKernel = new AugmentedRealityKernel(
                this,
                activity,
                mLockCamera,
                mOptimizeAspectRatio
        );
        mARKernel.setTimeBetweenLocationReads(mTimeBetweenLocationReads);

        if(markers != null){
            for(Marker marker : markers){
                mARKernel.addMarker(marker);
            }
        }
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved_instance){
        final View client_view = onCreateClientView(inflater, mClientViewContainer, saved_instance);
        if(client_view != null) {
            mClientViewContainer.addView(client_view);
        }

        return mMainView;
    }

    @Override
    public final void onViewCreated(final View view, final Bundle saved_instance){
        onClientViewCreated(mClientViewContainer,saved_instance);
    }

    protected abstract View onCreateClientView(LayoutInflater inflater, ViewGroup container, Bundle saved_instance);

    protected abstract void onClientViewCreated(FrameLayout client_view, Bundle saved_instance);

    @Override
    public void setRootViews(final FrameLayout main_view, final FrameLayout client_view_container){
        mMainView = main_view;
        mClientViewContainer = client_view_container;
    }

    @Override
    public View getView(){
        return mClientViewContainer;
    }

    @Override
    public void onPause(){
        mARKernel.pause();
        super.onPause();
    }

    @Override
    public void onResume(){
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
