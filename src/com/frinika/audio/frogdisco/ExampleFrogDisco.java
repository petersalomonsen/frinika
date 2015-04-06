package com.frinika.audio.frogdisco;

import com.synthbot.frogdisco.CoreAudioRenderAdapter;
import com.synthbot.frogdisco.FrogDisco;
import com.synthbot.frogdisco.SampleFormat;
import java.io.File;
import java.nio.FloatBuffer;

public class ExampleFrogDisco extends CoreAudioRenderAdapter {

  private long sampleIndex = 0;
  private FrogDisco frogDisco;

  public ExampleFrogDisco() {
    frogDisco = new FrogDisco(1, 128, 44100.0, SampleFormat.UNINTERLEAVED_FLOAT, 4, this);
  }

  public void play() {
    frogDisco.play();
  }

  @Override
  public void onCoreAudioFloatRenderCallback(FloatBuffer buffer) {
    /*
     * Put your audio code here.
     * This example produces a 440Hz sine wave.
     */
    int length = buffer.capacity();buffer.put((float) Math.sin(2.0 * Math.PI * 440.0 * sampleIndex / 44100.0));
    for (int i = 0; i < length; i++, sampleIndex++) {
      buffer.put((float) Math.sin(2.0 * Math.PI * 440.0 * sampleIndex / 44100.0));
    }
  }

  public static void main(String[] args) throws Exception {
    FrogDiscoNativeLibInstaller.loadNativeLibs();
    ExampleFrogDisco example = new ExampleFrogDisco();
    example.play();
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("ExampleFrogDisco exiting.");
  }
}