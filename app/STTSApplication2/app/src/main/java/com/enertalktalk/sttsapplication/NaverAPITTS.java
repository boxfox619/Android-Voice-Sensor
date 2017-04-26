package com.enertalktalk.sttsapplication;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class NaverAPITTS {
    private static boolean isPlaying;
    private static final String clientId = "D5nVnYPzkEEBJpkeaAhM";
    private static final String clientSecret = "PY43k4UHM5";

    public static String voice = "jinho";

    public static boolean isPlay() {
        return isPlaying;
    }

    public static void play(Context context, Object obj) {
        StringBuilder builder = new StringBuilder();
        if (obj instanceof String) {
            builder.append((String) obj);
        }
        play(context, builder.toString());
    }

    private synchronized static void play(final Context context, String text) {
        while (isPlaying) {
        }
        isPlaying = true;
        try {
            text = URLEncoder.encode(text, "UTF-8"); // 13자
            String apiURL = "https://openapi.naver.com/v1/voice/tts.bin";
            URL url = new URL(apiURL);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            final String postParams = "speaker="+voice+"&speed=2&text=" + text;
            con.setDoOutput(true);
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    try {
                        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                        wr.writeBytes(postParams);
                        wr.flush();
                        wr.close();
                        int responseCode = con.getResponseCode();
                        BufferedReader br;
                        if (responseCode == 200) {
                            InputStream is = con.getInputStream();
                            int read = 0;
                            byte[] bytes = new byte[1024];
                            String tempname = Long.valueOf(new Date().getTime()).toString();
                            File f = new File(Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    + "/" + tempname + ".mp3");
                            f.createNewFile();
                            OutputStream outputStream = new FileOutputStream(f);
                            while ((read = is.read(bytes)) != -1) {
                                outputStream.write(bytes, 0, read);
                            }
                            is.close();
                            MediaPlayer player = MediaPlayer.create(context, Uri.parse(f.getPath()));
                            player.start();
                            while (player.isPlaying()) {
                            }
                            isPlaying = false;
                            f.delete();
                        } else {
                            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();
                            while ((inputLine = br.readLine()) != null) {
                                response.append(inputLine);
                            }
                            isPlaying = false;
                            br.close();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
            isPlaying = false;
        }
    }
}