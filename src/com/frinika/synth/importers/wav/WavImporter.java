package com.frinika.synth.importers.wav;

import java.io.File;

import com.frinika.swing.ProgressBarDialog;
import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;
import com.frinika.synth.synths.sampler.settings.sampledsoundsettingversions.SampledSound20050403;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WavImporter {    
    short[] leftData;
    short[] rightData;

    public final SampledSoundSettings importWav(File file) throws Exception
    {
        final AudioInputStream ais = AudioSystem.getAudioInputStream(file);

        if(ais.getFormat().getFrameSize()!=2 && 
                ais.getFormat().getFrameSize()!=4 )
            throw new Exception("Only 16 bit mono or stereo samples supported..");
        
        leftData = new short[(int)ais.getFrameLength()];
        
        if(ais.getFormat().getFrameSize()==4)
            // If stereo samples
            rightData = new short[(int)ais.getFrameLength()];
    
        final ProgressBarDialog pbd = new ProgressBarDialog(null, "Importing wav", ais.available());
        new Thread() {
            public void run()
            {
                try
                {
                    int sCount = 0;
                    while(ais.available()>0)
                    {
                        byte[] bytes = new byte[ais.getFormat().getFrameSize()];
                        ais.read(bytes);
                        leftData[sCount] = (short)((0xff & bytes[0]) + ((0xff & bytes[1]) * 256));  
                        // If stereo samples
                        if(ais.getFormat().getFrameSize()==4)
                            rightData[sCount] = (short)((0xff & bytes[2]) + ((0xff & bytes[3]) * 256));
                        sCount++;
                        pbd.setProgressValue(sCount*bytes.length);
                    }
                } catch(Exception e)
                {
                    
                }
                pbd.dispose();
            }
        }.start();

        pbd.setVisible(true);
        
        SampledSoundSettings sampledSound = (SampledSoundSettings) new SampledSound20050403();

        sampledSound.setLeftSamples(leftData);
        sampledSound.setRightSamples(rightData);
        
        sampledSound.setSampleMode(0);
        sampledSound.setLoopStart(0);
        sampledSound.setLoopEnd(0);

        sampledSound.setFineTune(0);
        sampledSound.setScaleTune(100);
        sampledSound.setSampleRate((int)ais.getFormat().getFrameRate());
        sampledSound.setRelease((short)0);
        sampledSound.setSampleName(file.getName());
        sampledSound.setExclusiveClass(0);                
        
        return sampledSound;
    }
}
