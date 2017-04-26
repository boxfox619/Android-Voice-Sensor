package com.gearcode.stt.capture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class CaptureVoice {
	public enum CaptureState {
		CAPTURING, STOP
	}

	public static final AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);

	public static final float MAX_8_BITS_SIGNED = Byte.MAX_VALUE;
	public static final float MAX_8_BITS_UNSIGNED = 0xff;
	public static final float MAX_16_BITS_SIGNED = Short.MAX_VALUE;
	public static final float MAX_16_BITS_UNSIGNED = 0xffff;

	public final long WORD_GAPS = 1500;

	public final int AUDIO_LEVEL_MIN = 8;

	public CaptureState state = CaptureState.STOP;

	public VoiceLevelListener levelListener;

	private TargetDataLine targetDataLine;

	public AudioInputStream getAIS() throws LineUnavailableException {
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, CaptureVoice.format);
		targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
		state = CaptureState.CAPTURING;
		AudioInputStream audioInputStream = null;
		int frameSizeInBytes = format.getFrameSize();
		int bufferLengthInFrames = targetDataLine.getBufferSize() / 8;
		int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		byte[] bytes = new byte[bufferLengthInBytes];
		@SuppressWarnings("unused")
		int numBytesRead;

		long start = -1;
		long gap = -1;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			targetDataLine.open(CaptureVoice.format);
			targetDataLine.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		while (state.equals(CaptureState.CAPTURING)) {
			if ((numBytesRead = targetDataLine.read(bytes, 0, bytes.length)) == -1) {
				break;
			}

			int level = (int) (CaptureVoice.calculateLevel(bytes, 0, 0) * 100);
			if (levelListener != null) {
				levelListener.captureLevel(level);
			}

			long cur = System.currentTimeMillis();

			if (level > AUDIO_LEVEL_MIN) {
				gap = -1;
				if (start == -1) {
					start = cur;
					out = new ByteArrayOutputStream();
				}
				try {
					out.write(bytes);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				if (start != -1) {
					if (gap == -1) {
						gap = cur;
					}

					if (cur - gap > WORD_GAPS) {
						start = -1;
						gap = -1;
						byte[] byteArray = out.toByteArray();
						audioInputStream = new AudioInputStream(new ByteArrayInputStream(byteArray),
								CaptureVoice.format, byteArray.length / CaptureVoice.format.getFrameSize());
						state = CaptureState.STOP;
						targetDataLine.stop();
						targetDataLine.close();
						return audioInputStream;
					}
				}
			}
		}
		state = CaptureState.STOP;
		targetDataLine.stop();
		targetDataLine.close();
		return null;
	}

	public static float calculateLevel(byte[] buffer, int readPoint, int leftOver) {
		int max = 0;
		float level;
		boolean use16Bit = (CaptureVoice.format.getSampleSizeInBits() == 16);
		boolean signed = (CaptureVoice.format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED);
		boolean bigEndian = (CaptureVoice.format.isBigEndian());
		if (use16Bit) {
			for (int i = readPoint; i < buffer.length - leftOver; i += 2) {
				int value = 0;
				int hiByte = (bigEndian ? buffer[i] : buffer[i + 1]);
				int loByte = (bigEndian ? buffer[i + 1] : buffer[i]);
				if (signed) {
					short shortVal = (short) hiByte;
					shortVal = (short) ((shortVal << 8) | (byte) loByte);
					value = shortVal;
				} else {
					value = (hiByte << 8) | loByte;
				}
				max = Math.max(max, value);
			}
		} else {
			for (int i = readPoint; i < buffer.length - leftOver; i++) {
				int value = 0;
				if (signed) {
					value = buffer[i];
				} else {
					short shortVal = 0;
					shortVal = (short) (shortVal | buffer[i]);
					value = shortVal;
				}
				max = Math.max(max, value);
			}
		}
		if (signed) {
			if (use16Bit) {
				level = (float) max / MAX_16_BITS_SIGNED;
			} else {
				level = (float) max / MAX_8_BITS_SIGNED;
			}
		} else {
			if (use16Bit) {
				level = (float) max / MAX_16_BITS_UNSIGNED;
			} else {
				level = (float) max / MAX_8_BITS_UNSIGNED;
			}
		}
		return level;
	}

}
