package com.ecsm.android.readForMe.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecsm.android.readForMe.R;
import com.ecsm.android.readForMe.features.PDFAsyncTask;
import com.ecsm.android.readForMe.features.TXTAsyncTask;
import com.ecsm.android.readForMe.features.TesseractAsyncTask;
import com.ecsm.android.readForMe.util.OutputListener;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    //@formatter:off

    //    private static final String TAG = "DBG_" + MainActivity.class.getName();
    private static final int REQUEST_IMAGE_CAPTURE  = 0x00000000;
    private static final int REQUEST_FILE_SELECT    = 0x00000001;

    @BindView(R.id.mTakeCaptureButton)     ImageButton takeCaptureButton;
    @BindView(R.id.mCapturedImageView)     ImageView mCapturedImageView;
    @BindView(R.id.mCapturedTextView)      TextView mCapturedTextView;
    @BindView(R.id.mBrowsButton)           ImageButton browsButton;
    @BindView(R.id.mPrevBtn)               ImageButton mPrev;
    @BindView(R.id.mNextBtn)               ImageButton mNext;
    @BindView(R.id.mPlay)                  ImageButton mPlay;

    private String          mFilePath     = null  ;
    private PDFAsyncTask    mPDFAsyncTask         ;
    private boolean         mFromBDF      = false ;
    private TextToSpeech    textToSpeech          ;
    private String          mSpeak        = ""    ;
//@formatter:on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initiateResources();
        startServices();
    }

    public void TextToSpeechFunction() {
        //noinspection deprecation
        textToSpeech.speak(mSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    ///initiation section
    private void initiateResources() {

        mPrev.setEnabled(false);
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFromBDF) {
                    int p = mPDFAsyncTask.getCurrentPage() - 1;
                    mPDFAsyncTask = new PDFAsyncTask(getApplicationContext(), mPDFAsyncTask.getOutputListener());
                    mPDFAsyncTask.execute(mFilePath, p + "");
                }
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFromBDF) {
                    int p = mPDFAsyncTask.getCurrentPage() + 1;
                    mPDFAsyncTask = new PDFAsyncTask(getApplicationContext(), mPDFAsyncTask.getOutputListener());
                    mPDFAsyncTask.execute(mFilePath, p + "");
                }
            }
        });


        textToSpeech = new TextToSpeech(MainActivity.this, MainActivity.this);

        browsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        takeCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureByIntent();
            }
        });
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextToSpeechFunction();
            }
        });


    }


    private void startServices() {
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            mFilePath = getIntent().getData().getPath();
            selectTask(mFilePath);
        }
    }

    private void selectTask(String filePath) {
        mFilePath = filePath;

        //pdf
        if (mFilePath.contains(".pdf")) {
            fileIsPDF();
        }
        //txt
        else if (mFilePath.contains(".txt")) {
            fileIsTEXT();
        } else if (mFilePath.contains(".png") || mFilePath.contains(".jpg")) {

            fileIsImage();
        }
    }

    private void fileIsImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(mFilePath);
        mCapturedImageView.setImageBitmap(bitmap);
        TesseractAsyncTask tesseractAsyncTask = new TesseractAsyncTask(this);
        tesseractAsyncTask.setOutputListener(new OutputListener() {
            @Override
            public void onOutput(String... values) {
                mCapturedTextView.setText(values[0]);
            }

            @Override
            public void onResult(String... values) {
                mFromBDF = false;
                mCapturedTextView.setText(values[0]);
                mSpeak = values[0];
            }
        });
        tesseractAsyncTask.execute(bitmap);
    }

    private void fileIsTEXT() {
        mFromBDF = false;
        TXTAsyncTask txtAsyncTask = new TXTAsyncTask();
        txtAsyncTask.setOutputListener(new OutputListener() {
            @Override
            public void onOutput(String... values) {
                mCapturedTextView.setText(values[0]);
            }

            @Override
            public void onResult(String... values) {
                mFromBDF = false;
                mCapturedTextView.setText(values[0]);
                mSpeak = values[0];
            }
        });
        txtAsyncTask.execute(mFilePath);
    }

    private void fileIsPDF() {
        mFromBDF = true;
        mPDFAsyncTask = new PDFAsyncTask(getApplicationContext());
        mPDFAsyncTask.setOutputListener(new OutputListener() {
            @Override
            public void onOutput(String... values) {
                mCapturedTextView.setText(values[0]);
            }

            @Override
            public void onResult(String... values) {
                mFromBDF = true;
                mCapturedTextView.setText(values[0]);
                mSpeak = values[0];
            }
        });
        mPDFAsyncTask.execute(mFilePath, "1");
    }

    ///send file request
    private void showFileChooser() {
        Intent i = new Intent(this, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(i, REQUEST_FILE_SELECT);

    }

    /// send intent camera request
    private void takePictureByIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onDestroy() {

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    /// get the result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE: {
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    mCapturedImageView.setImageBitmap(imageBitmap);
                    break;
                }
            }
            case REQUEST_FILE_SELECT: {


                Uri uri = data.getData();
                File file = com.nononsenseapps.filepicker.Utils.getFileForUri(uri);
                Uri fileUri = Uri.fromFile(file);
                selectTask(fileUri.getPath());

                break;
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int Text2SpeechCurrentStatus) {

        if (Text2SpeechCurrentStatus == TextToSpeech.SUCCESS) {

            textToSpeech.setLanguage(Locale.US);


        }

    }


}
