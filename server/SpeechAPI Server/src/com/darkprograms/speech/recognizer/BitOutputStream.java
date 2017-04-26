package com.darkprograms.speech.recognizer;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements AutoCloseable {

    private OutputStream out;
	private long bitBuffer;
	private int bitBufferLen;
	public int crc8;
	public int crc16;
	
	
	public BitOutputStream(OutputStream out) {
		this.out = out;
		bitBuffer = 0;
		bitBufferLen = 0;
		resetCrcs();
	}
	
	
	public void resetCrcs() {
		crc8 = 0;
		crc16 = 0;
	}
	
	
	public void alignToByte() throws IOException {
		writeInt((64 - bitBufferLen) % 8, 0);
	}
	
	
	public void writeInt(int n, int val) throws IOException {
		bitBuffer = (bitBuffer << n) | (val & ((1L << n) - 1));
		bitBufferLen += n;
		while (bitBufferLen >= 8) {
			bitBufferLen -= 8;
			int b = (int)(bitBuffer >>> bitBufferLen) & 0xFF;
			out.write(b);
			crc8 ^= b;
			crc16 ^= b << 8;
			for (int i = 0; i < 8; i++) {
				crc8 = (crc8 << 1) ^ ((crc8 >>> 7) * 0x107);
				crc16 = (crc16 << 1) ^ ((crc16 >>> 15) * 0x18005);
			}
		}
	}
	
	
	public void close() throws IOException {
		out.close();
	}
	
}