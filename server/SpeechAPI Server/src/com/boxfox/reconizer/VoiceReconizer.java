package com.boxfox.reconizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;

import com.darkprograms.speech.recognizer.Recognizer;
import com.gearcode.stt.capture.CaptureVoice;
import com.gearcode.stt.capture.VoiceClipListener;
import com.gearcode.stt.capture.VoiceLevelListener;
import com.gearcode.stt.flac.FlacEncoder;


import com.darkprograms.speech.recognizer.GoogleResponse;
import javaFlacEncoder.FLACStreamOutputStream;

public class VoiceReconizer extends Thread {
	private String API_KEY;
	private STTListener listener;
	private File file;
	private Recognizer recognizer;

	public VoiceReconizer(String apikey) {
		this.API_KEY = apikey;
		file = new File("tmp/testfile2.flac");
		new File(file.getParent()).mkdirs();
		recognizer = new Recognizer(Recognizer.Languages.KOREAN, API_KEY);
	}

	public VoiceReconizer(String apikey, int delay) {
		this.API_KEY = apikey;
		file = new File("tmp/testfile2.flac");
		new File(file.getParent()).mkdirs();
		recognizer = new Recognizer(Recognizer.Languages.KOREAN, API_KEY);
	}

	public void setSTTListener(STTListener listener) {
		this.listener = listener;
	}

	@Override
	public void run() {
		while (true) {
			String text = cognize();
			if (text != null && listener != null) {
				listener.OnVoiceReconized(text);
			}
		}
	}

	public String doRecognize() {
		return cognize();
	}

	private String cognize() {
		try {
			final CaptureVoice captureVoice = new CaptureVoice();
			ByteArrayOutputStream flacOS = new ByteArrayOutputStream();
			FlacEncoder flacEncoder = new FlacEncoder();
			AudioInputStream clipAIS;
			do {
				clipAIS = captureVoice.getAIS();
			} while (clipAIS == null);
			try {
				flacEncoder.convertWaveToFlac(clipAIS, new FLACStreamOutputStream(flacOS));
				FileOutputStream fout = new FileOutputStream(file);
				fout.write(flacOS.toByteArray());
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return requestGoogle();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private String requestGoogle() {
		String str = null;
		try {
			int maxNumOfResponses = 4;
			GoogleResponse response = recognizer.getRecognizedDataForFlac(file, maxNumOfResponses,
					(int) CaptureVoice.format.getSampleRate());
			str = response.getResponse();
		} catch (Exception ex) {
			System.out.println("ERROR: Google cannot be contacted");
			ex.printStackTrace();
		}
		file.delete();
		return str;
	}

	private static void addClassPath(final URL url) throws IOException {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { url });
		} catch (final Throwable t) {
			t.printStackTrace();
			throw new IOException("Error adding " + url + " to system classloader");
		}
	}
}