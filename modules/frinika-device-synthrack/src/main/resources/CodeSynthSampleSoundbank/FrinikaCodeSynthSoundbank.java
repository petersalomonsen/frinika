
/*
 *
 * Copyright (c) 2016 Peter Johan Salomonsen ( http://petersalomonsen.com )
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
            floatBuffer[bufferPos] += (float) ((Math.sin(radians) * (velocity/127f) * att));
            floatBuffer[bufferPos+1] += (float)((Math.sin(radians) * (velocity/127f) * att));
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
    }
       
}
