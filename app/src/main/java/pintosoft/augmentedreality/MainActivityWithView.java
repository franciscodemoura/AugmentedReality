package pintosoft.augmentedreality;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import augmented_reality.kernel.AugmentedRealityKernelInterface;
import augmented_reality.graphical_components.AugmentedRealityView;
import augmented_reality.markers.Marker;
import augmented_reality.markers.MarkerImage;
import augmented_reality.markers.MarkerImageFactory;
import augmented_reality.markers.SimpleDirectionMarker;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivityWithView extends Activity{
    private SeekBar mGravityCoefficientView;
    private SeekBar mCompassCoefficientView;
    private AugmentedRealityView mARView;

    private TextView mStatusView;
    private final Handler mHandler = new Handler();
    private Toast mLastToast;

    private final Runnable mStatusUpdateRunnable =
            new Runnable() {
                @Override
                public void run() {
                    final String status = mARView.getARInterface().decodeStatus(mARView.getARInterface().getStatus());
                    if(status != null) {
                        mStatusView.setText(getResources().getString(R.string.status_text) + status);
                    }

                    mHandler.postDelayed(this,1000);
                }
            };

    private static final int BAR_MAX = 10000;
    private static final double FILTER_SCALE = 8.0;


    @Override
    protected void onCreate(Bundle saved_instance) {
        super.onCreate(saved_instance);

        setContentView(R.layout.main_with_fragment);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mARView = new AugmentedRealityView(this);
        ((FrameLayout) findViewById(R.id.fragment_container)).addView(mARView);

        mGravityCoefficientView = (SeekBar) findViewById(R.id.gravity_filter);
        mCompassCoefficientView = (SeekBar) findViewById(R.id.compass_filter);
        mGravityCoefficientView.setMax(BAR_MAX);
        mCompassCoefficientView.setMax(BAR_MAX);

        getLayoutInflater().inflate(R.layout.fragment_layout,mARView.getClientViewContainer(),true);
        mStatusView = (TextView) mARView.getClientViewContainer().findViewById(R.id.status_text);

        mARView.getARInterface().setMarkerViewEventListener(new AugmentedRealityKernelInterface.MarkerViewEventListener() {
            @Override
            public void onClick(Marker marker) {
                if (marker != null) {
                    if (mLastToast != null) {
                        mLastToast.cancel();
                    }
                    mLastToast = Toast.makeText(MainActivityWithView.this, marker.getId(), LENGTH_LONG);
                    mLastToast.show();
                }
            }
        });

        setListeners();

        setMarkers(mARView.getARInterface());

        setGravityFilterFromBar(BAR_MAX / 2, mGravityCoefficientView, true);
        setGravityFilterFromBar(BAR_MAX / 2, mCompassCoefficientView, true);
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

    private void setListeners() {
        mGravityCoefficientView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setGravityFilterFromBar(progress, seekBar, false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mCompassCoefficientView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setCompassFilterFromBar(progress, seekBar, false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setGravityFilterFromBar(final int progress, final SeekBar bar, final boolean update_bar){
        final float filter = (float) (progress) / (float) bar.getMax();
        final float scaled_filter = (float) Math.pow(filter,1.0/FILTER_SCALE);
        mARView.getARInterface().setGravityFilteringCoefficient(scaled_filter);
        if(update_bar){
            bar.setProgress(progress);
        }
    }

    private void setCompassFilterFromBar(final int progress, final SeekBar bar, final boolean update_bar){
        final float filter = (float) (progress) / (float) bar.getMax();
        final float scaled_filter = (float) Math.pow(filter,1.0/FILTER_SCALE);
        mARView.getARInterface().setMagneticFieldFilteringCoefficient(scaled_filter);
        if(update_bar){
            bar.setProgress(progress);
        }
    }

    private void setMarkers(final AugmentedRealityKernelInterface AR_kernel){
        final String[] names =     {"N",    "NE",   "E",    "SE",   "S",    "SW",   "W",   "NW"};
        final float[] directions = {0.0f,   45.0f,  90.0f,  135.0f, 180.0f, 225.0f, 270.0f, 315.0f};
        final float[] sizes =      {200.0f, 100.0f, 150.0f, 100.0f, 150.0f, 100.0f, 150.0f, 100.0f};

        for(int i=0; i<names.length; i++) {
            AR_kernel.addMarker(
                    new SimpleDirectionMarker(
                            names[i],
                            MarkerImageFactory.createMarkerImage(
                                    names[i],
                                    sizes[i],
                                    0xFF008800
                            ),
                            directions[i], 0.0f
                    )
            );
            AR_kernel.addMarker(
                    new SimpleDirectionMarker(
                            names[i],
                            MarkerImageFactory.createMarkerImage(
                                    names[i],
                                    50.0f,
                                    0xFF008800
                            ),
                            directions[i], -80.0f
                    )
            );
        }

        final MarkerImage pipe_character_image = MarkerImageFactory.createMarkerImage(
                "|",
                50.0f,
                0xFF008800
        );

        for(float i=5.0f; i<360.0f; i+=5.0f){
            if(
                    i != 45.0f  &&
                            i != 90.0f  &&
                            i != 135.0f  &&
                            i != 180.0f  &&
                            i != 225.0f  &&
                            i != 270.0f  &&
                            i != 315.0f
                    ) {
                AR_kernel.addMarker(
                        new SimpleDirectionMarker(
                                "I" + i,
                                pipe_character_image,
                                i, 0.0f
                        )
                );
            }
        }

        final MarkerImage ordinal_character_image = MarkerImageFactory.createMarkerImage(
                "º",
                50.0f,
                0xFF008800
        );

        for(float i=-75.0f; i<=80.0f; i+=5.0f){
            if(i != 0.0f){
                for(float j=0.0f; j<360.0f; j+=45.0f){
                    AR_kernel.addMarker(
                            new SimpleDirectionMarker(
                                    "." + i + "_" + j,
                                    ordinal_character_image,
                                    j, i
                            )
                    );
                }
            }
        }

        AR_kernel.addMarker(
                new SimpleDirectionMarker(
                        "Up",
                        MarkerImageFactory.createMarkerImage(
                                "Up",
                                150.0f,
                                0xFF008800
                        ),
                        0.0f, 90.0f
                )
        );

        AR_kernel.addMarker(
                new SimpleDirectionMarker(
                        "Down",
                        MarkerImageFactory.createMarkerImage(
                                "Down",
                                80.0f,
                                0xFF008800
                        ),
                        0.0f, -90.0f
                )
        );
    }
}
