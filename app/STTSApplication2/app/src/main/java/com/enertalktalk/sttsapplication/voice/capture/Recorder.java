package com.enertalktalk.sttsapplication.voice.capture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.enertalktalk.sttsapplication.voice.Recognizer;

import net.sourceforge.javaflacencoder.FLAC_FileEncoder;

public class Recorder {
    private final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private final int WAVE_CHANNEL_MONO = 1;
    private final int HEADER_SIZE = 0x2c;
    private final int RECORDER_BPP = 16;
    private final int RECORDER_SAMPLERATE = 8000;
    private final int BUFFER_SIZE;
    public final long WORD_GAPS = 1500;
    public final int AUDIO_LEVEL_MIN = 2800;

    public final String API_KEY = "";

    private AudioRecord mAudioRecord;
    private boolean mIsRecording;
    private BufferedInputStream mBIStream;
    private BufferedOutputStream mBOStream;
    private int mAudioLen = 0;
    private boolean running, block;

    private Recognizer recognizer;
    private VoiceCaptureListener listener;

    public Recorder(VoiceCaptureListener listener) {
        super();
        this.listener = listener;
        recognizer = new Recognizer(Recognizer.Languages.KOREAN, API_KEY);
        BUFFER_SIZE = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        mIsRecording = false;
    }

    public void start() {
        running = true;
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Log.e("Recorder", "Start");
                while (running) {
                    if (block) continue;
                    mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                            RECORDER_AUDIO_ENCODING, BUFFER_SIZE);
                    mAudioRecord.startRecording();
                    mIsRecording = true;
                    File file = convertToFlac(writeAudioDataToFile());
                    try {
                        String result = recognizer.request(file).getResponse();
                        if (block) continue;
                        listener.capture(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }.execute();
    }

    public void stop() {
        running = false;
    }

    public void wait(boolean block) {
        this.block = block;
    }

    private File convertToFlac(File wavAudioFile) {
        File flacAudioFile = new File(wavAudioFile.getParentFile(), "voice.flac");
        FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
        flacEncoder.encode(wavAudioFile, flacAudioFile);
        return flacAudioFile;
    }

    private File writeAudioDataToFile() {
        byte[] buffer = new byte[BUFFER_SIZE];
        byte[] data = new byte[BUFFER_SIZE];
        File tempFile = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/test.bak");
        File waveFile = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/waveAudio.wav");
        try {
            mBOStream = new BufferedOutputStream(new FileOutputStream(tempFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int readSize = 0;
        int len = 0;
        long start = -1;
        long gap = -1;
        if (null != mBOStream) {
            try {
                while (mIsRecording) {
                    double sum = 0;
                    readSize = mAudioRecord.read(data, 0, BUFFER_SIZE);
                    if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                        for (int i = 0; i < data.length; i += 2) {
                            short pice = (short) (((data[i + 1] & 0xff) << 8) | (data[i] & 0xff));
                            sum += pice * pice;
                        }
                        int level = 0;
                        if (readSize > 0) {
                            final double amplitude = sum / readSize;
                            level = (int) Math.sqrt(amplitude);
                        }
                        long cur = System.currentTimeMillis();

                        if (level > AUDIO_LEVEL_MIN) {
                            gap = -1;
                            if (start == -1) {
                                start = cur;
                                Log.e("Recording", "Start");
                            }
                            mBOStream.write(data);
                        } else {
                            if (start != -1) {
                                if (gap == -1) {
                                    gap = cur;
                                }
                                mBOStream.write(data);
                                if (cur - gap > WORD_GAPS) {
                                    start = -1;
                                    gap = -1;
                                    Log.e("Recording", "Stop");
                                    stopRecording();
                                }
                            }
                        }
                    }
                }
                mBOStream.flush();
                mAudioLen = (int) tempFile.length();
                mBIStream = new BufferedInputStream(new FileInputStream(tempFile));
                mBOStream.close();
                mBOStream = new BufferedOutputStream(new FileOutputStream(waveFile));
                mBOStream.write(getFileHeader());
                len = HEADER_SIZE;
                while ((readSize = mBIStream.read(buffer)) != -1) {
                    mBOStream.write(buffer);
                }
                mBOStream.flush();
                mBIStream.close();
                mBOStream.close();
                return waveFile;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    private byte[] getFileHeader() {
        byte[] header = new byte[HEADER_SIZE];
        int totalDataLen = mAudioLen + 40;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * WAVE_CHANNEL_MONO / 8;
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = (byte) 1;  // format = 1 (PCM방식)
        header[21] = 0;
        header[22] = WAVE_CHANNEL_MONO;
        header[23] = 0;
        header[24] = (byte) (RECORDER_SAMPLERATE & 0xff);
        header[25] = (byte) ((RECORDER_SAMPLERATE >> 8) & 0xff);
        header[26] = (byte) ((RECORDER_SAMPLERATE >> 16) & 0xff);
        header[27] = (byte) ((RECORDER_SAMPLERATE >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) RECORDER_BPP * WAVE_CHANNEL_MONO / 8;  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (mAudioLen & 0xff);
        header[41] = (byte) ((mAudioLen >> 8) & 0xff);
        header[42] = (byte) ((mAudioLen >> 16) & 0xff);
        header[43] = (byte) ((mAudioLen >> 24) & 0xff);
        return header;
    }

    public void stopRecording() {
        if (null != mAudioRecord) {
            mIsRecording = false;
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }
}

