package com.enertalktalk.sttsapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.enertalktalk.sttsapplication.voice.capture.Recorder;
import com.enertalktalk.sttsapplication.voice.capture.VoiceCaptureListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements VoiceCaptureListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Recorder recorder = new Recorder(this);
        recorder.start();

    }

    @Override
    public void capture(String str) {
        if (str != null) {
            ((TextView)findViewById(R.id.textView)).setText(str);
            Log.e("Recognizer", str);
        } else {
            ((TextView)findViewById(R.id.textView)).setText("인식 실패");
            return;
        }
    }

    @Override
    public Context getContext() {
        return this;
    }
}
