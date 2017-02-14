package com.ecsm.android.readForMe.util;

import android.util.Log;


public interface OutputListener {
    public static String TAG="DBG_"+OutputListener.class.getName();
    public void onOutput(String... values);
    public void onResult(String... values);
    public static OutputListener DEFAULT=new OutputListener() {
        @Override
        public void onOutput(String... values) {
            Log.d(TAG, "onOutput: "+values[0] );
        }

        @Override
        public void onResult(String... values) {
            Log.d(TAG, "onResult: "+values[0] );
        }
    };
}
