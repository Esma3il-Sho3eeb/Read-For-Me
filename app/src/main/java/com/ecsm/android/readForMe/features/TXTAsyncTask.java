package com.ecsm.android.readForMe.features;


import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;

import com.ecsm.android.readForMe.util.OutputListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TXTAsyncTask extends AsyncTask<String, String, String> {
    private OutputListener mOutputListener;
    private View mCaller = null;

    public TXTAsyncTask() {
        mOutputListener = OutputListener.DEFAULT;
    }

    public TXTAsyncTask(@NonNull OutputListener outputListener) {
        mOutputListener = outputListener;
    }

    public void setOutputListener(@NonNull OutputListener outputListener) {
        mOutputListener = outputListener;
    }

    public void setCaller(View caller) {
        mCaller = caller;
    }

    @Override
    protected String doInBackground(String... prams) {
        StringBuilder text = new StringBuilder();
        if (prams.length == 1) {
            if (!prams[0].contains(".txt"))
                return null;
            try {
                publishProgress("try to open file");
                BufferedReader reader = new BufferedReader(new FileReader(prams[0]));
                String line;
                publishProgress("try to read file");
                while ((line = reader.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                reader.close();

            } catch (FileNotFoundException e) {
                publishProgress("error open file");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress("error read file");
            }
        }

        return text.toString();
    }


    @Override
    protected void onPreExecute() {
        if (mCaller != null)
            mCaller.setEnabled(false);
        super.onPreExecute();
    }

    protected void onPostExecute(String s) {
        if (mCaller != null)
            mCaller.setEnabled(true);
        mOutputListener.onResult(s);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        mOutputListener.onOutput(values);
    }


}