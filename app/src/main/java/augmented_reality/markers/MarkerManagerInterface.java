package augmented_reality.markers;

public interface MarkerManagerInterface {

    void addMarker(Marker marker);
    boolean removeMarker(Marker marker);
    Marker findFirstMarkerById(String id);
    void clearMarkers();
    int getNumberOfMarkers();
    Marker getMarkerAtPosition(int pos);
}
