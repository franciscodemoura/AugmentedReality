package pintosoft.augmentedreality;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SeekBar;

import augmented_reality.graphical_components.AugmentedRealityFragment;
import augmented_reality.kernel.AugmentedRealityKernelInterface;
import augmented_reality.markers.MarkerImage;
import augmented_reality.markers.MarkerImageFactory;
import augmented_reality.markers.SimpleDirectionMarker;

public class MainActivityWithFragment extends Activity{

    private AugmentedRealityFragment mMainFragment;
    private SeekBar mGravityCoefficientView;
    private SeekBar mCompassCoefficientView;

    private static final int BAR_MAX = 10000;
    private static final double FILTER_SCALE = 8.0;


    @Override
    protected void onCreate(Bundle saved_instance) {
        super.onCreate(saved_instance);

        setContentView(R.layout.main_with_fragment);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FragmentManager fragment_manager = getFragmentManager();
        mMainFragment = (MainFragment) fragment_manager.findFragmentByTag("ARFragment");
        if (mMainFragment == null) {
            mMainFragment = new MainFragment();
        }
        if (!mMainFragment.isAdded()){
            FragmentTransaction fragment_transaction = fragment_manager.beginTransaction();
            fragment_transaction.add(R.id.fragment_container, mMainFragment, "ARFragment");
            fragment_transaction.commit();
        }

        mGravityCoefficientView = (SeekBar) findViewById(R.id.gravity_filter);
        mCompassCoefficientView = (SeekBar) findViewById(R.id.compass_filter);
        mGravityCoefficientView.setMax(BAR_MAX);
        mCompassCoefficientView.setMax(BAR_MAX);

        setListeners();
    }

    @Override
    protected void onStart(){
        super.onStart();

        setGravityFilterFromBar(BAR_MAX / 2, mGravityCoefficientView, true);
        setGravityFilterFromBar(BAR_MAX / 2, mCompassCoefficientView, true);

        setMarkers(mMainFragment.getARInterface());
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
        mMainFragment.getARInterface().setGravityFilteringCoefficient(scaled_filter);
        if(update_bar){
            bar.setProgress(progress);
        }
    }

    private void setCompassFilterFromBar(final int progress, final SeekBar bar, final boolean update_bar){
        final float filter = (float) (progress) / (float) bar.getMax();
        final float scaled_filter = (float) Math.pow(filter,1.0/FILTER_SCALE);
        mMainFragment.getARInterface().setMagneticFieldFilteringCoefficient(scaled_filter);
        if(update_bar){
            bar.setProgress(progress);
        }
    }

    private void setMarkers(final AugmentedRealityKernelInterface AR_kernel){
        if(AR_kernel == null  ||  AR_kernel.getNumberOfMarkers() > 0){
            return;
        }

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
