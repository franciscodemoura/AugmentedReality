package augmented_reality.markers;

public interface MarkerManager {
    Marker[] getMarkerArray(Marker[] markers);
    void updateARTransform();
    void stopListeningToParameters();
}
