package com.enertalktalk.sttsapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.enertalktalk.sttsapplication.capture.Recorder;
import com.enertalktalk.sttsapplication.capture.VoiceCaptureListener;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements VoiceCaptureListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        gifImageView.setGifImageResource(R.drawable.robot);

        Typeface type = Typeface.createFromAsset(this.getAssets(), "Somatic-Rounded_1.otf");
        TextView titleTV = (TextView) findViewById(R.id.textView3);
        titleTV.setTypeface(type);

        //new VoiceRecorder(this, this).start();
        //NaverAPITTS.play(MainActivity.this,"안녕하세요 기모띠 입니다.");
        Recorder recorder = new Recorder(this);
        new Thread(recorder).start();

    }

    @Override
    public void capture(String str) {
        if (str != null) {
            ((TextView)findViewById(R.id.textView)).setText(str);
            Log.e("Send", str);
        } else {
            ((TextView)findViewById(R.id.textView)).setText("인식 실패");
            Recorder recorder = new Recorder(MainActivity.this);
            new Thread(recorder).start();
            return;
        }
        AQuery aq = new AQuery(this);
        aq.ajax("http://52.78.71.212:81/chatbot?msg=" + str, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                try {
                    if (object != null && new JSONObject(object).getString("message") != null) {
                        Log.e("Test", new JSONObject(object).getString("message"));
                        try {
                            NaverAPITTS.play(MainActivity.this, new JSONObject(object).getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                while (NaverAPITTS.isPlay()) {
                }
                Recorder recorder = new Recorder(MainActivity.this);
                new Thread(recorder).start();
            }
        });
    }

    @Override
    public Context getContext() {
        return this;
    }
}
