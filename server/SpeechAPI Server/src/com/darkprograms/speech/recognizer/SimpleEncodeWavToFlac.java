package com.darkprograms.speech.recognizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;


public final class SimpleEncodeWavToFlac {

	public static void encodeFile(InputStream in, BitOutputStream out) throws IOException, DataFormatException {
		// Read and parse WAV file headers
		if (!readString(in, 4).equals("RIFF"))
			throw new DataFormatException("Invalid RIFF file header");
		readLittleInt(in, 4);
		if (!readString(in, 4).equals("WAVE"))
			throw new DataFormatException("Invalid WAV file header");
		
		if (!readString(in, 4).equals("fmt "))
			throw new DataFormatException("Unrecognized WAV file chunk");
		if (readLittleInt(in, 4) != 16)
			throw new DataFormatException("Unsupported WAV file type");
		if (readLittleInt(in, 2) != 0x0001)
			throw new DataFormatException("Unsupported WAV file codec");
		int numChannels = readLittleInt(in, 2);
		if (numChannels < 0 || numChannels > 8)
			throw new RuntimeException("Too many (or few) audio channels");
		int sampleRate = readLittleInt(in, 4);
		if (sampleRate <= 0 || sampleRate >= (1 << 20))
			throw new RuntimeException("Sample rate too large or invalid");
		readLittleInt(in, 4);
		readLittleInt(in, 2);
		int sampleDepth = readLittleInt(in, 2);
		if (sampleDepth == 0 || sampleDepth > 32 || sampleDepth % 8 != 0)
			throw new RuntimeException("Unsupported sample depth");
		
		if (!readString(in, 4).equals("data"))
			throw new DataFormatException("Unrecognized WAV file chunk");
		int sampleDataLen = readLittleInt(in, 4);
		if (sampleDataLen <= 0 || sampleDataLen % (numChannels * (sampleDepth / 8)) != 0)
			throw new DataFormatException("Invalid length of audio sample data");
		
		// Start writing FLAC file header and stream info metadata block
		out.writeInt(32, 0x664C6143);
		out.writeInt(1, 1);
		out.writeInt(7, 0);
		out.writeInt(24, 34);
		out.writeInt(16, BLOCK_SIZE - 1);
		out.writeInt(16, BLOCK_SIZE - 1);
		out.writeInt(24, 0);
		out.writeInt(24, 0);
		out.writeInt(20, sampleRate);
		out.writeInt(3, numChannels - 1);
		out.writeInt(5, sampleDepth - 1);
		int numSamples = sampleDataLen / (numChannels * (sampleDepth / 8));
		out.writeInt(18, numSamples >>> 18);
		out.writeInt(18, numSamples >>>  0);
		for (int i = 0; i < 16; i++)
			out.writeInt(8, 0);
		
		// Read raw samples and encode FLAC audio frames
		for (int i = 0; numSamples > 0; i++) {
			int blockSize = Math.min(numSamples, BLOCK_SIZE);
			encodeFrame(in, i, numChannels, sampleDepth, sampleRate, blockSize, out);
			numSamples -= blockSize;
		}
	}
	
	
	private static final int BLOCK_SIZE = 4096;
	
	
	private static String readString(InputStream in, int len) throws IOException {
		byte[] temp = new byte[len];
		for (int i = 0; i < temp.length; i++) {
			int b = in.read();
			if (b == -1)
				throw new EOFException();
			temp[i] = (byte)b;
		}
		return new String(temp, StandardCharsets.UTF_8);
	}
	
	
	private static int readLittleInt(InputStream in, int n) throws IOException {
		int result = 0;
		for (int i = 0; i < n; i++) {
			int b = in.read();
			if (b == -1)
				throw new EOFException();
			result |= b << (i * 8);
		}
		return result;
	}
	
	
	private static void encodeFrame(InputStream in, int frameIndex, int numChannels, int sampleDepth, int sampleRate, int blockSize, BitOutputStream out) throws IOException {
		int[][] samples = new int[numChannels][blockSize];
		int bytesPerSample = sampleDepth / 8;
		for (int i = 0; i < blockSize; i++) {
			for (int ch = 0; ch < numChannels; ch++) {
				int val = 0;
				for (int j = 0; j < bytesPerSample; j++) {
					int b = in.read();
					if (b == -1)
						throw new EOFException();
					val |= b << (j * 8);
				}
				if (sampleDepth == 8)
					samples[ch][i] = val - 128;
				else
					samples[ch][i] = (val << (32 - sampleDepth)) >> (32 - sampleDepth);
			}
		}
		
		out.resetCrcs();
		out.writeInt(14, 0x3FFE);
		out.writeInt(1, 0);
		out.writeInt(1, 0);
		out.writeInt(4, 7);
		out.writeInt(4, sampleRate % 10 == 0 ? 14 : 13);
		out.writeInt(4, numChannels - 1);
		switch (sampleDepth) {
			case  8:  out.writeInt(3, 1);  break;
			case 16:  out.writeInt(3, 4);  break;
			case 24:  out.writeInt(3, 6);  break;
			case 32:  out.writeInt(3, 0);  break;
			default:  throw new IllegalArgumentException();
		}
		out.writeInt(1, 0);
		out.writeInt(8, 0xFC | (frameIndex >>> 30));
		for (int i = 24; i >= 0; i -= 6)
			out.writeInt(8, 0x80 | ((frameIndex >>> i) & 0x3F));
		out.writeInt(16, blockSize - 1);
		out.writeInt(16, sampleRate / (sampleRate % 10 == 0 ? 10 : 1));
		out.writeInt(8, out.crc8);
		
		for (int[] chanSamples : samples)
			encodeSubframe(chanSamples, sampleDepth, out);
		out.alignToByte();
		out.writeInt(16, out.crc16);
	}
	
	
	private static void encodeSubframe(int[] samples, int sampleDepth, BitOutputStream out) throws IOException {
		out.writeInt(1, 0);
		out.writeInt(6, 1);  // Verbatim coding
		out.writeInt(1, 0);
		for (int x : samples)
			out.writeInt(sampleDepth, x);
	}
	
}
