package com.boxfox.application;

import com.darkprograms.speech.recognizer.GoogleResponse;
import com.darkprograms.speech.recognizer.Recognizer;
import io.vertx.core.Handler;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.io.*;


public class ImageUploadRouter implements Handler<RoutingContext> {
	//AIzaSyAW_e4aEiAYkd9er21R9j4aUOnFLZx4ZoA
	//AIzaSyBpAUyxdJyK7WfrQ4or_INLKuMMp1J-q3U
    private Recognizer recognizer;

    public ImageUploadRouter() {
        recognizer = new Recognizer(Recognizer.Languages.KOREAN, "AIzaSyBpAUyxdJyK7WfrQ4or_INLKuMMp1J-q3U");
    }


    @Override
    public void handle(RoutingContext context) {
        for (FileUpload upload : context.fileUploads()) {
        	new Thread(new Runnable() {

				@Override
				public void run() {
			        System.out.println("request");
			        String result = "";
					File file = new File(upload.uploadedFileName());
		            File files = new File(file.getParent() + "/test.wav");
		            try {
		                if (files.exists()) {
		                    files.delete();
		                }
		                boolean check = files.createNewFile();
		                FileInputStream fin = new FileInputStream(file);
		                FileOutputStream fout = new FileOutputStream(files);
		                byte[] bytes = new byte[1024];
		                int len = 0;
		                while ((len = fin.read(bytes)) > -1) {
		                    fout.write(bytes, 0, len);
		                }
		                fin.close();
		                fout.flush();
		                fout.close();
		                GoogleResponse response = recognizer.getRecognizedDataForWave(files);
		                result = response.getResponse();
		                System.out.println("Result"+ result);
		                if (result == null) {
		                    context.response().setStatusCode(500);
		                } else
		                    context.response().setStatusCode(200).end(result);
		                context.response().close();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		            file.delete();
					
				}}).start();
        }
    }
}