package com.ecsm.android.readForMe.features;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;

import com.ecsm.android.readForMe.util.OutputListener;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;

public class PDFAsyncTask extends AsyncTask<String, String, String> {
    private Context mContext;
    private OutputListener mOutputListener;
    private int mCurrentPage = 0;
    private int mTotalPages = -1;
    private View mCaller = null;

    public PDFAsyncTask(@NonNull Context context) {
        mContext = context;
        mOutputListener = OutputListener.DEFAULT;
        PDFBoxResourceLoader.init(mContext);
    }

    public PDFAsyncTask(@NonNull Context context, @NonNull OutputListener outputListener) {
        mContext = context;
        mOutputListener = outputListener;
        PDFBoxResourceLoader.init(mContext);
    }

    public OutputListener getOutputListener() {
        return mOutputListener;
    }

    public void setOutputListener(@NonNull OutputListener outputListener) {
        mOutputListener = outputListener;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setCaller(@NonNull View caller) {
        mCaller = caller;
    }

    @Override
    protected String doInBackground(String... prams) {
        String parsedText = null;
        if (prams.length == 2) {

            if (!prams[0].contains(".pdf"))
                return null;
            parsedText = null;
            PDDocument document = null;
            try {
                publishProgress("trying to open the file");
                document = PDDocument.load(new File(prams[0]));
                mTotalPages = document.getNumberOfPages();
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress(parsedText="can't open the file");

            }
            mCurrentPage = Integer.parseInt(prams[1]);

            if (mCurrentPage < 0)
                publishProgress(parsedText="stop where are you go");
            else if (mCurrentPage <= mTotalPages) {

                try {
                    publishProgress("initialize text engine");

                    PDFTextStripper pdfStripper = new PDFTextStripper();


                    pdfStripper.setStartPage(mCurrentPage);
                    pdfStripper.setEndPage(mCurrentPage + 1);


                    publishProgress("start text extraction");
                    parsedText = pdfStripper.getText(document);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (document != null) document.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                publishProgress(parsedText="stop you reach the limit");
            }
        }
        return parsedText;
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

