package augmented_reality.kernel;

import augmented_reality.interfaces.ARStatusInterface;
import augmented_reality.markers.Marker;
import augmented_reality.markers.MarkerManagerInterface;

public interface AugmentedRealityKernelInterface extends MarkerManagerInterface, ARStatusInterface {

    interface MarkerViewEventListener{
        void onClick(Marker marker);
    }

    void setMarkerViewEventListener(final MarkerViewEventListener listener);
    void setGravityFilteringCoefficient(final float coefficient);
    float getGravityFilteringCoefficient();
    void setMagneticFieldFilteringCoefficient(final float coefficient);
    float getMagneticFieldFilteringCoefficient();
    void setSleepTimeBetweenFrames(final long time);
    long getSleepTimeBetweenFrames();
    void setTimeBetweenLocationReads(final long time);
    long getTimeBetweenLocationReads();
}
