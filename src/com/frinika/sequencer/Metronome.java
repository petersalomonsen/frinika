package com.frinika.sequencer;


import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.synth.envelope.MidiVolume;

public class Metronome implements AudioProcess, SongPositionListener {
 //   MyTracker tracker;
    float[] sampleData;
    float level = 0f;
    boolean active = false;
    ProjectContainer project;
    int metSamplePos = 0;
    
    public Metronome(ProjectContainer project) throws Exception
    {
//		super(project.getAudioServer().openAudioOutput(project.getAudioServer().getAvailableOutputNames().get(0),null));
		this.project=project;
    	
      //  this.tracker = tracker;
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(ClassLoader.getSystemResource("sounds/metronome1.wav"));
            sampleData = new float[(int)stream.getFrameLength()];
            
            int index = 0;
            
            byte[] frame = new byte[2];
            int b = stream.read(frame);
            while(b!=-1)
            {
                sampleData[index++] = (((frame[1] * 256)+(frame[0] & 0xff)) / 32768f);
                b = stream.read(frame);
            }            
        } catch (Exception e) {
        	e.printStackTrace();
        	
        }

  //      project.getMixer().getMainBus().setOutputProcess(this);
        
    }
    
    public void setVelocity(int velocity)
    {
        if(velocity>0)
        {
            if(!active)
            {
                project.getSequencer().addSongPositionListener(this); //new SwingSongPositionListenerWrapper(this));
                active = true;
            }
            level = MidiVolume.midiVolumeToAmplitudeRatio(velocity);            
        }
        else
        {
            project.getSequencer().removeSongPositionListener(this);
            active = false;
        }
    }
    
    public void notifyTickPosition(long tick) {
    	// Reset metronome sample pos on each metronome beat
    	if(tick%project.getSequence().getResolution()==0)
        	metSamplePos = 0;
    }

    public boolean requiresNotificationOnEachTick() {
        return true;
    }

    
  
    public int processAudio(AudioBuffer buffer) {
    	float left[] = buffer.getChannel(0);
    	float right[] = buffer.getChannel(1);
    	
    	for(int n=0;n<buffer.getSampleCount() && metSamplePos<sampleData.length;n++)
    	{
    		 left[n] += sampleData[metSamplePos] * level;
    		 right[n] += sampleData[metSamplePos++] * level;
    	}
    	return AUDIO_OK; // super.processAudio(buffer);
    }

	public void open() {
		// TODO Auto-generated method stub
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}
}
