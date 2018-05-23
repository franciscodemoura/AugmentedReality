package augmented_reality.parameters;

import android.os.Handler;

import java.util.ArrayList;
import java.util.TreeMap;

public class ParameterManager <T> {

    public interface DataChangedCallback<T>{
        void onDataChanged(T key, Object data, Object old_data);
    }

    private static class Parameter{

        public static class Callback{
            public final DataChangedCallback mCallback;
            public final Handler mHandler;

            public Callback(final DataChangedCallback callback, final Handler handler){
                mCallback = callback;
                mHandler = handler;
            }
        }

        public volatile Object mData;
        public final ArrayList<Callback> mCallbacks = new ArrayList<>();

        public Parameter(final Object data){
            mData = data;
        }
    }

    private final TreeMap<T,Parameter> mMap = new TreeMap<>();

    public void setParameter(final T key, final Object data){
        Object old_data = null;
        Parameter.Callback[] callbacks = null;
        boolean do_callbacks = false;

        synchronized (this) {
            final Parameter parameter = mMap.get(key);
            if (parameter == null) {
                mMap.put(key, new Parameter(data));
            }
            else {
                if(parameter.mCallbacks.size() > 0) {
                    old_data = parameter.mData;
                    callbacks = parameter.mCallbacks.toArray(new Parameter.Callback[0]);
                    do_callbacks = true;
                }
                parameter.mData = data;
            }
        }

        if(do_callbacks){
            dispatchCallbacks(key, data, old_data, callbacks);
        }
    }

    public synchronized Object getParameter(final T key){
        return mMap.get(key).mData;
    }

    public synchronized Object registerCallback(final T key, final DataChangedCallback callback, final Handler handler){
        final Parameter parameter = mMap.get(key);
        if(parameter == null){
            return null;
        }
        else{
            final Parameter.Callback temp = new Parameter.Callback(callback,handler);
            parameter.mCallbacks.add(temp);
            return temp;
        }
    }

    public Object registerCallback(final T key, final DataChangedCallback callback){
        return registerCallback(key,callback,null);
    }

    public synchronized boolean removeCallback(final T key, final Object id){
        final Parameter parameter = mMap.get(key);
        return parameter != null && parameter.mCallbacks.remove(id);
    }

    private void dispatchCallbacks(final T key, final Object data, final Object old_data, final Parameter.Callback[] callbacks){
        for(final Parameter.Callback callback : callbacks){
            if(callback.mHandler == null){
                callback.mCallback.onDataChanged(key, data, old_data);
            }
            else{
                callback.mHandler.post(new CallbackRunnable<>(callback,key,data,old_data));
            }
        }
    }

    private static class CallbackRunnable<T> implements Runnable{

        final Parameter.Callback mCallback;
        final T mKey;
        final Object mData;
        final Object mOldData;

        public CallbackRunnable(
                final Parameter.Callback callback,
                final T key,
                final Object data,
                final Object old_data
        ) {
            mCallback = callback;
            mKey = key;
            mData = data;
            mOldData = old_data;
        }

        @Override
        public void run() {
            mCallback.mCallback.onDataChanged(mKey, mData, mOldData);
        }
    }
}
