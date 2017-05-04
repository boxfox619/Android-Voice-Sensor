package com.enertalktalk.sttsapplication.capture;

import net.sourceforge.javaflacencoder.FLACEncoder;
import net.sourceforge.javaflacencoder.FLACFileOutputStream;
import net.sourceforge.javaflacencoder.FLAC_FileEncoder;
import net.sourceforge.javaflacencoder.StreamConfiguration;

import java.io.File;

/**
 * Created by boxfox on 2017-05-04.
 */

public class FlacEncorder {
    public void convertWaveToFlac(File inputFile, File outputFile) {
        FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
        flacEncoder.encode(inputFile, outputFile);
    }
}
