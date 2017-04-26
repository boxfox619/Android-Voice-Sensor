package com.boxfox.tts;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class NaverAPITTS {
    private static boolean isPlaying;

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static void play(Object obj) {
        StringBuilder builder = new StringBuilder();
        if (obj instanceof String) {
            builder.append((String) obj);
        }
        play(builder.toString());
    }

    private synchronized static void play(String text) {
        while (isPlaying) {
        }
        String clientId = "D5nVnYPzkEEBJpkeaAhM";
        String clientSecret = "PY43k4UHM5";
        try {
            text = URLEncoder.encode(text, "UTF-8"); // 13자
            String apiURL = "https://openapi.naver.com/v1/voice/tts.bin";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            String postParams = "speaker=jinho&speed=0&text=" + text;
            con.setDoOutput(true);
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
                File f = new File(tempname + ".mp3");
                f.createNewFile();
                OutputStream outputStream = new FileOutputStream(f);
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                is.close();
                AdvancedPlayer player = new AdvancedPlayer(new FileInputStream(f));
                isPlaying = true;
                player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent playbackEvent) {
                        super.playbackFinished(playbackEvent);
                        isPlaying = false;
                    }
                });
                player.play();
                f.delete();
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
            }
        } catch (Exception e) {
        }
    }
}