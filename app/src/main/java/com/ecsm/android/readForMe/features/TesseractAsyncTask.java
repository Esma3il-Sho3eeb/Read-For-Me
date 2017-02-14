package com.ecsm.android.readForMe.features;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.ecsm.android.readForMe.R;
import com.ecsm.android.readForMe.util.OutputListener;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class TesseractAsyncTask extends AsyncTask<Bitmap, String, String> {
    private static final String TAG = "DBG_" + TesseractAsyncTask.class.getName();
    private Context mContext;
    private OutputListener mOutputListener;

    private View mCaller;
    private String dataPath = "";
    private TessBaseAPI mTess;

    public TesseractAsyncTask(@NonNull Context context) {
        mContext = context;
        mOutputListener = OutputListener.DEFAULT;
        PDFBoxResourceLoader.init(mContext);
    }

    public TesseractAsyncTask(@NonNull Context context, @NonNull OutputListener outputListener) {
        mContext = context;
        mOutputListener = outputListener;
        PDFBoxResourceLoader.init(mContext);
    }

    public void setCaller(View caller) {
        mCaller = caller;
    }

    public void setOutputListener(@NonNull OutputListener outputListener) {
        mOutputListener = outputListener;
    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        if (bitmaps.length == 1) {
            dataPath = mContext.getFilesDir() + "/tesseract/";
            mTess = new TessBaseAPI();
            checkFile(new File(dataPath + "tessdata/"));
            mTess.init(dataPath, "eng");
            return processImage(bitmaps[0]);
        }
        return null;
    }

    /// check for training data
    private void checkFile(File dir) {
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        if (dir.exists()) {
            String dataFilePath = dataPath + "/tessdata/eng.traineddata";
            File datafile = new File(dataFilePath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = dataPath + "/tessdata/eng.traineddata";
            InputStream inputStream = mContext.getResources().openRawResource(R.raw.eng_traineddata);
            OutputStream outputStream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    /// start tess-two engine
    private String processImage(Bitmap image) {
        mTess.setImage(image);
        return mTess.getUTF8Text();
    }


    @Override
    protected void onPreExecute() {
        if (mCaller != null)
            mCaller.setEnabled(false);
        super.onPreExecute();
    }

    protected void onPostExecute(String s) {
        mTess.end();
        if (mCaller != null)
            mCaller.setEnabled(true);
        mOutputListener.onResult(s);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        mOutputListener.onOutput(values);
    }


}