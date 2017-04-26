package com.boxfox.application;

import com.boxfox.reconizer.STTListener;
import com.boxfox.reconizer.VoiceReconizer;
import com.boxfox.tts.NaverAPITTS;
import com.darkprograms.speech.recognizer.BitOutputStream;
import com.darkprograms.speech.recognizer.SimpleEncodeWavToFlac;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.*;
import java.util.zip.DataFormatException;

/**
 * Created by boxfox on 2017-04-07.
 */
public class ApplicationMain {
    //AIzaSyAW_e4aEiAYkd9er21R9j4aUOnFLZx4ZoA
    //AIzaSyBpAUyxdJyK7WfrQ4or_INLKuMMp1J-q3U
    public static void main(String args[]) {
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setUploadsDirectory("upload-files"));
        router.route("/").handler(new ImageUploadRouter());
        router.route().handler(StaticHandler.create());
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);

        /*VoiceReconizer voiceReconizer = new VoiceReconizer("AIzaSyBpAUyxdJyK7WfrQ4or_INLKuMMp1J-q3U");
        voiceReconizer.setSTTListener(new STTListener() {
            @Override
            public void OnVoiceReconized(String arg) {
                System.out.println(arg);
            }
        });
        voiceReconizer.start();*/
        //VoiceReconuzer voiceReconizer = new VoiceReconuzer("API-KEY", 5000);
        //String str = voiceReconizer.doRecognize();*/
    }
}
