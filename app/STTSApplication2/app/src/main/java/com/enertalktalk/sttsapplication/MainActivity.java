package com.enertalktalk.sttsapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.enertalktalk.sttsapplication.voice.capture.Recorder;
import com.enertalktalk.sttsapplication.voice.capture.VoiceCaptureListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements VoiceCaptureListener {
    private final int REQUEST_CODE = 222;
    private Recorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recorder = new Recorder(this, this);
        checkPermission();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            boolean hasPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE);
                return;
            } else {
                recorder.start();
            }
        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                recorder.start();
            } else {
                Toast.makeText(MainActivity.this, "You need permissions!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void capture(String str) {
        if (str != null) {
            ((TextView) findViewById(R.id.textView)).setText(str);
            Log.e("Recognizer", str);
        } else {
            ((TextView) findViewById(R.id.textView)).setText("인식 실패");
            return;
        }
    }

    @Override
    public Context getContext() {
        return this;
    }
}
