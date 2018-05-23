package augmented_reality.interfaces;

import android.widget.FrameLayout;

import augmented_reality.kernel.AugmentedRealityKernelInterface;

public interface AugmentedRealityContainer {
    void setRootViews(FrameLayout main_view, FrameLayout client_view_container);

    //getARInterface may return null. This means that the ARFramework is not ready yet.
    AugmentedRealityKernelInterface getARInterface();
}
