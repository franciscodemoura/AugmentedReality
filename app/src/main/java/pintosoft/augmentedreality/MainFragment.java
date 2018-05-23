package pintosoft.augmentedreality;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import augmented_reality.graphical_components.AugmentedRealityFragment;
import augmented_reality.kernel.AugmentedRealityKernelInterface;
import augmented_reality.markers.Marker;
import augmented_reality.markers.MarkerImageFactory;

import static android.widget.Toast.LENGTH_LONG;

public class MainFragment extends AugmentedRealityFragment{

    private TextView mStatusView;
    private Button mSaveLocationButton;
    private final Handler mHandler = new Handler();
    private Toast mLastToast;

    private final Runnable mStatusUpdateRunnable =
            new Runnable() {
                @Override
                public void run() {
                    final String status = getARInterface().decodeStatus(getARInterface().getStatus());
                    if(status != null) {
                        mStatusView.setText(getResources().getString(R.string.status_text) + status);
                    }

                    mHandler.postDelayed(this,1000);
                }
            };


    public MainFragment() {
        super(
                1000,
                true,
                false
        );
    }

    @Override
    public void onCreate(final Bundle saved_instance){
        super.onCreate(saved_instance);
        setRetainInstance(true);
    }

    @Override
    public View onCreateClientView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved_instance) {
        return inflater.inflate(R.layout.fragment_layout,container,false);
    }

    @Override
    public void onClientViewCreated(final FrameLayout client_view, final Bundle saved_instance) {
        mStatusView = (TextView) client_view.findViewById(R.id.status_text);
        mSaveLocationButton = (Button) client_view.findViewById(R.id.save_location);

        setListeners();
    }

    @Override
    public void onResume(){
        super.onResume();
        scheduleStatusUpdate();
    }

    public void onPause(){
        cancelStatusSchedule();
        super.onPause();
    }

    private void scheduleStatusUpdate(){
        mStatusUpdateRunnable.run();
    }

    private void cancelStatusSchedule(){
        mHandler.removeCallbacks(mStatusUpdateRunnable);
    }

    private void setListeners(){
        getARInterface().setMarkerViewEventListener(new AugmentedRealityKernelInterface.MarkerViewEventListener() {
            @Override
            public void onClick(Marker marker) {
                if (marker != null) {
                    if (mLastToast != null) {
                        mLastToast.cancel();
                    }
                    mLastToast = Toast.makeText(MainFragment.this.getActivity(), marker.getId(), LENGTH_LONG);
                    mLastToast.show();
                }
            }
        });

        mSaveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Location location = getARInterface().getLocation();
                if (location != null  &&  location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    getARInterface().addMarker(
                            new MySimpleLocationMarker(
                                    "Me",
                                    MarkerImageFactory.createMarkerImage(
                                            ContextCompat.getDrawable(getActivity(),R.drawable.boy),
                                            128,
                                            128
                                    ),
                                    location
                            )
                    );
                }
            }
        });

    }
}
