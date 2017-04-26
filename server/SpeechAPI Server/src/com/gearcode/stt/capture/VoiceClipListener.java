package com.gearcode.stt.capture;

import javax.sound.sampled.AudioInputStream;

public interface VoiceClipListener {
	public void captureClip(AudioInputStream clipAIS);
}
