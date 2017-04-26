package com.enertalktalk.sttsapplication.capture;

import android.content.Context;

/**
 * Created by boxfox on 2017-04-08.
 */

public interface VoiceCaptureListener {
    public void capture(String str);
    public Context getContext();
}
