package augmented_reality.surfaces;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;

import augmented_reality.parameters.ParameterManager;
import augmented_reality.markers.Marker;
import augmented_reality.markers.MarkerManager;

import static augmented_reality.parameters.ParameterNames.*;

class DrawerThread extends Thread{

    private final WeakReference<SurfaceHolder> mSurfaceHolder;
    private final WeakReference<MarkerManager> mMarkersManager;
    private final ParameterManager<String> mParameters;
    private final Object mSleepTimeCallbackId;
    private final AtomicLong mSleepTime = new AtomicLong();

    DrawerThread(
            final MarkerManager marker_manager,
            final SurfaceHolder holder,
            final ParameterManager<String> parameters
    ){
        mMarkersManager = new WeakReference<>(marker_manager);
        mSurfaceHolder = new WeakReference<>(holder);
        mParameters = parameters;

        mSleepTime.set((Long) mParameters.getParameter(SLEEP_TIME_BETWEEN_FRAMES));

        mSleepTimeCallbackId =
            mParameters.registerCallback(SLEEP_TIME_BETWEEN_FRAMES,new ParameterManager.DataChangedCallback() {
                @Override
                public void onDataChanged(Object key, Object data, Object old_data) {
                    mSleepTime.set((Long) data);
                }
            });
    }

    @Override
    public void run(){

        Marker[] markers = new Marker[0];

        try {

            while (!isInterrupted()) {

                final MarkerManager marker_manager = mMarkersManager.get();
                final SurfaceHolder surface_holder = mSurfaceHolder.get();

                if(surface_holder == null  ||  marker_manager == null){
                    return;
                }

                markers = marker_manager.getMarkerArray(markers);

                Canvas canvas = surface_holder.lockCanvas();

                if (canvas != null) {

                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    marker_manager.updateARTransform();

                    for (Marker m : markers) {
                        canvas.save();
                        m.onDraw(canvas);
                        canvas.restore();
                    }

                    surface_holder.unlockCanvasAndPost(canvas);
                } else {
                    return;
                }

                try {
                    sleep(mSleepTime.get());
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        }
        finally {
            if(mMarkersManager.get() != null) {
                mMarkersManager.get().stopListeningToParameters();
            }
            mParameters.removeCallback(SLEEP_TIME_BETWEEN_FRAMES,mSleepTimeCallbackId);
        }
    }
}
