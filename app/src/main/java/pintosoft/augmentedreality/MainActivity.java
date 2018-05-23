package pintosoft.augmentedreality;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import augmented_reality.graphical_components.AugmentedRealityActivity;
import augmented_reality.kernel.AugmentedRealityKernelInterface;
import augmented_reality.markers.Marker;
import augmented_reality.markers.MarkerImage;
import augmented_reality.markers.MarkerImageFactory;
import augmented_reality.markers.SimpleDirectionMarker;

import static android.widget.Toast.*;

public class MainActivity extends AugmentedRealityActivity {

    private TextView mStatusView;
    private SeekBar mGravityCoefficientView;
    private SeekBar mCompassCoefficientView;
    private View mMainFrame;
    private final Handler mHandler = new Handler();
    private Toast mLastToast;

    private static final int BAR_MAX = 10000;
    private static final double FILTER_SCALE = 8.0;
    private final String GRAVITY_FILTER_VALUE_KEY = "gravity_filter_value_key";
    private final String COMPASS_FILTER_VALUE_KEY = "compass_filter_value_key";

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

    private final Runnable mHideInterfaceRunnable =
            new Runnable() {
                @Override
                public void run() {
                    hideInterface();
                }
            };


    public MainActivity(){
        super(
                true,
                10000,
                true,
                true
        );
    }

    @Override
    protected void onCreate(final Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMainFrame = findViewById(R.id.main_frame);
        mStatusView = (TextView) findViewById(R.id.status_text);
        mGravityCoefficientView = (SeekBar) findViewById(R.id.gravity_filter);
        mCompassCoefficientView = (SeekBar) findViewById(R.id.compass_filter);
        mGravityCoefficientView.setMax(BAR_MAX);
        mCompassCoefficientView.setMax(BAR_MAX);

        readPreferences();
        setListeners();
        setMarkers();
    }

    @Override
    protected void onResume(){
        super.onResume();

        showInterface();
        scheduleStatusUpdate();
        scheduleHideInterface();
    }

    protected void onPause(){
        savePreferences();

        cancelHideSchedule();
        cancelStatusSchedule();

        super.onPause();
    }

    private void savePreferences(){
        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(GRAVITY_FILTER_VALUE_KEY,mGravityCoefficientView.getProgress());
        editor.putInt(COMPASS_FILTER_VALUE_KEY,mCompassCoefficientView.getProgress());
        editor.apply();
    }

    private void readPreferences(){
        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        final int gravity_filter_bar_value = prefs.getInt(GRAVITY_FILTER_VALUE_KEY, BAR_MAX / 2);
        final int compass_filter_bar_value = prefs.getInt(COMPASS_FILTER_VALUE_KEY, BAR_MAX / 2);

        setGravityFilterFromBar(gravity_filter_bar_value, mGravityCoefficientView, true);
        setGravityFilterFromBar(compass_filter_bar_value, mCompassCoefficientView, true);
    }

    private void showInterface(){
        mMainFrame.setVisibility(View.VISIBLE);
    }

    private void hideInterface(){
        mMainFrame.setVisibility(View.GONE);
    }

    private void scheduleHideInterface(){
        mHandler.postDelayed(mHideInterfaceRunnable,10000);
    }

    private void scheduleStatusUpdate(){
        mStatusUpdateRunnable.run();
    }

    private void cancelHideSchedule(){
        mHandler.removeCallbacks(mHideInterfaceRunnable);
    }

    private void cancelStatusSchedule(){
        mHandler.removeCallbacks(mStatusUpdateRunnable);
    }

    private void setGravityFilterFromBar(final int progress, final SeekBar bar, final boolean update_bar){
        final float filter = (float) (progress) / (float) bar.getMax();
        final float scaled_filter = (float) Math.pow(filter,1.0/FILTER_SCALE);
        getARInterface().setGravityFilteringCoefficient(scaled_filter);
        if(update_bar){
            bar.setProgress(progress);
        }
    }

    private void setCompassFilterFromBar(final int progress, final SeekBar bar, final boolean update_bar){
        final float filter = (float) (progress) / (float) bar.getMax();
        final float scaled_filter = (float) Math.pow(filter,1.0/FILTER_SCALE);
        getARInterface().setMagneticFieldFilteringCoefficient(scaled_filter);
        if(update_bar){
            bar.setProgress(progress);
        }
    }

    private void setListeners() {
        mGravityCoefficientView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setGravityFilterFromBar(progress, seekBar, false);
                cancelHideSchedule();
                scheduleHideInterface();
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
                cancelHideSchedule();
                scheduleHideInterface();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        getARInterface().setMarkerViewEventListener(new AugmentedRealityKernelInterface.MarkerViewEventListener() {
            @Override
            public void onClick(Marker marker) {
                if (marker != null) {
                    if (mLastToast != null) {
                        mLastToast.cancel();
                    }
                    mLastToast = Toast.makeText(MainActivity.this, marker.getId(), LENGTH_LONG);
                    mLastToast.show();
                }
                else {
                    cancelHideSchedule();
                    showInterface();
                    scheduleHideInterface();
                }
            }
        });
    }

    private void setMarkers(){
        final String[] names =     {"N",    "NE",   "E",    "SE",   "S",    "SW",   "W",   "NW"};
        final float[] directions = {0.0f,   45.0f,  90.0f,  135.0f, 180.0f, 225.0f, 270.0f, 315.0f};
        final float[] sizes =      {200.0f, 100.0f, 150.0f, 100.0f, 150.0f, 100.0f, 150.0f, 100.0f};

        for(int i=0; i<names.length; i++) {
            getARInterface().addMarker(
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
            getARInterface().addMarker(
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
                getARInterface().addMarker(
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
                    getARInterface().addMarker(
                            new SimpleDirectionMarker(
                                    "." + i + "_" + j,
                                    ordinal_character_image,
                                    j, i
                            )
                    );
                }
            }
        }

        getARInterface().addMarker(
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

        getARInterface().addMarker(
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
