package com.gearcode.stt.flac;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javaFlacEncoder.FLACEncoder;
import javaFlacEncoder.FLACFileOutputStream;
import javaFlacEncoder.FLACOutputStream;
import javaFlacEncoder.StreamConfiguration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class FlacEncoder {

    public FlacEncoder() {}
    
    public void convertWaveToFlac(File inputFile, File outputFile) {
    	try {
			convertWaveToFlac(AudioSystem.getAudioInputStream(inputFile), new FLACFileOutputStream(outputFile));
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void convertWaveToFlac(String inputFile, String outputFile) {
        convertWaveToFlac(new File(inputFile), new File(outputFile));
    }

    public void convertWaveToFlac(AudioInputStream audioInputStream, FLACOutputStream flacOutputStream) {
        AudioFormat format = audioInputStream.getFormat();
        StreamConfiguration streamConfiguration = new StreamConfiguration();
        streamConfiguration.setSampleRate((int)format.getSampleRate());
        streamConfiguration.setBitsPerSample(format.getSampleSizeInBits());
        streamConfiguration.setChannelCount(format.getChannels());
        try {
            int frameSize = format.getFrameSize();
            FLACEncoder flacEncoder = new FLACEncoder();
            flacEncoder.setStreamConfiguration(streamConfiguration);
            flacEncoder.setOutputStream(flacOutputStream);
            flacEncoder.openFLACStream();

            int[] sampleData = new int[(int) audioInputStream.getFrameLength()];
            byte[] samplesIn = new byte[frameSize];

            int i = 0;
            while (audioInputStream.read(samplesIn, 0, frameSize) != -1) {
                if (frameSize != 1) {
                    ByteBuffer bb = ByteBuffer.wrap(samplesIn);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    short shortVal = bb.getShort();
                    sampleData[i] = shortVal;
                } else {
                    sampleData[i] = samplesIn[0];
                }
                i++;
            }
            flacEncoder.addSamples(sampleData, i);
            flacEncoder.encodeSamples(i, false);
            flacEncoder.encodeSamples(flacEncoder.samplesAvailableToEncode(), true);
            audioInputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
