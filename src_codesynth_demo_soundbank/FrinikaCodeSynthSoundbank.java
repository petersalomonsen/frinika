/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://petersalomonsen.com) - Licensed under GNU LGPL
 */


import com.frinika.codesynth.CodeSynthInstrument;

import javax.sound.midi.Patch;

import com.frinika.codesynth.CodeSynthSoundbank;
import com.frinika.codesynth.note.ChromaticScaleNote;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class FrinikaCodeSynthSoundbank extends CodeSynthSoundbank {

     public static class SawToothNote extends ChromaticScaleNote {
        @Override
        public void fillFrame(float[] floatBuffer, int bufferPos, int channels) {
            final double radians = getDefaultRadians();
            floatBuffer[bufferPos] += (float) (((radians % (Math.PI*2)) * (velocity/127f) * 0.1f));
            floatBuffer[bufferPos+1] += (float)(((radians % (Math.PI*2)) * (velocity/127f) * 0.1f));
        }
    }
     
    public static class SineWaveNote extends ChromaticScaleNote {

	boolean released = false;
        float att = 0.5f;
        
        @Override
        public void release(int velocity) {
            released = true;
        }

        
        @Override
        public void fillFrame(float[] floatBuffer, int bufferPos, int channels) {
            final double radians = getDefaultRadians();
            floatBuffer[bufferPos] += (float) ((Math.sin(radians) * (0.7+0.3*velocity/127f) * att));
            floatBuffer[bufferPos+1] += (float)((Math.sin(radians) * (0.7+0.3*velocity/127f) * att));
            if(released)
            {
                att-=0.001f;
                if(att<0)
                {
                    super.release(0);
                }
            }
        }
    }
    
  
    public static class FantasyNote extends ChromaticScaleNote {

	boolean released = false;
        float att = 0.5f;
        
        @Override
        public void release(int velocity) {
            released = true;
        }

        
        @Override
        public void fillFrame(float[] floatBuffer, int bufferPos, int channels) {
            final double radians = getDefaultRadians();
            floatBuffer[bufferPos] += (float) ((Math.sin(radians) * (0.7+0.3*velocity/127f) * att));
            floatBuffer[bufferPos+1] += (float)((Math.sin(radians) * (0.7+0.3*velocity/127f) * att));
	    floatBuffer[bufferPos] += (float) ((Math.sin(radians*2) * (Math.sin(radians/2048.0)*0.8+0.3*velocity/127f) * att));
            floatBuffer[bufferPos+1] += (float)((Math.sin(radians*Math.pow(2.0,5.0/12)) * (Math.sin(radians/1024.0)*0.8+0.3*velocity/127f) * att));
            if(released)
            {
                att-=0.001f;
                if(att<0)
                {
                    super.release(0);
                }
            }
        }
    }
    
    public static class Drumkit extends ChromaticScaleNote {

	boolean released = false;
        float att = 0.5f;
        
        @Override
        public void release(int velocity) {
            released = true;
        }

        
        @Override
        public void fillFrame(float[] floatBuffer, int bufferPos, int channels) {
            final double radians = getDefaultRadians();
            floatBuffer[bufferPos] += (float) ((Math.random() * (0.7+0.3*velocity/127f) * att));
            floatBuffer[bufferPos+1] += (float)((Math.random() * (0.7+0.3*velocity/127f) * att));
	    
            if(released)
            {
                att-=0.001f;
                if(att<0)
                {
                    super.release(0);
                }
            }
        }
    }
    
    public FrinikaCodeSynthSoundbank() {
	this.addInstrument(new CodeSynthInstrument(this, new Patch(0,1),"Sawtooth",SawToothNote.class));
	this.addInstrument(new CodeSynthInstrument(this, new Patch(0,0),"Sine",SineWaveNote.class));
	this.addInstrument(new CodeSynthInstrument(this, new Patch(0,2),"Fantasy",FantasyNote.class));
	this.addInstrument(new CodeSynthInstrument(this, new Patch(0,3),"Drumkit",Drumkit.class));
    }
       
}
