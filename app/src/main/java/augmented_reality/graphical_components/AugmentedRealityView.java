package augmented_reality.graphical_components;

import android.content.Context;
import android.widget.FrameLayout;

import augmented_reality.interfaces.AugmentedRealityContainer;
import augmented_reality.kernel.AugmentedRealityKernel;
import augmented_reality.kernel.AugmentedRealityKernelInterface;

public class AugmentedRealityView extends FrameLayout implements AugmentedRealityContainer {
    private FrameLayout mClientViewContainer;
    private final AugmentedRealityKernel mARKernel;

    public AugmentedRealityView(final Context context) {
        this(
                context,
                1000,
                true,
                true
        );
    }

    public AugmentedRealityView(final Context context,final long time_between_location_reads){
        this(
                context,
                time_between_location_reads,
                true,
                true
        );
    }

    public AugmentedRealityView(
            final Context context,
            final long time_between_location_reads,
            final boolean lock_camera,
            final boolean optimize_aspect_ratio
    ) {
        super(context);
        mARKernel = new AugmentedRealityKernel(
                this,
                context,
                lock_camera,
                optimize_aspect_ratio
        );
        mARKernel.setTimeBetweenLocationReads(time_between_location_reads);
    }

    @Override
    public void setRootViews(final FrameLayout main_view, final FrameLayout client_view_container) {
        mClientViewContainer = client_view_container;
        this.addView(main_view);
    }

    @Override
    public AugmentedRealityKernelInterface getARInterface() {
        return mARKernel;
    }

    public FrameLayout getClientViewContainer(){
        return mClientViewContainer;
    }
}
