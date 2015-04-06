/*
 * Created on Sep 30, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth.synths;

import java.io.Serializable;

import com.frinika.synth.*;
import com.frinika.synth.soundbank.SynthRackInstrumentIF;

/**
 * @author peter
 * 
 */
public class Organ extends Synth {

        public class OrganSettings implements SynthRackInstrumentIF,Serializable
        {
            public static final long SerialVersionUID = 1L;
            
            public String getInstrumentName() {
                return "Organ";
            }

            public void setInstrumentName(String instrumentName) {
                throw new UnsupportedOperationException("Not supported yet.");
            }                    
        }
        
        OrganSettings organSettings = new OrganSettings();
	/**
	 * @param voiceServer
	 */
	public Organ(SynthRack synth) {
		super(synth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiChannel#noteOn(int, int)
	 */
	public void noteOn(int noteNumber, int velocity) {
		Oscillator osc = new OrganOscillator(this);
		osc.setNoteNumber(noteNumber);
		osc.setVelocity(velocity);
		addOscillator(noteNumber, osc);
	}

	class OrganOscillator extends Oscillator {

		/**
		 * @param synth
		 */
		public OrganOscillator(Synth synth) {
			super(synth);
		}

		public void fillBuffer(int startBufferPos, int endBufferPos,
				float[] buffer) {
			for (int n = startBufferPos; n < endBufferPos;) {
				float res = (float) ((((float) (position += increment)
						% (2.0 * Math.PI) + ((float) (position * 2.0)
						% (2.0 * Math.PI) / 2.0))
						+ ((float) (position * 4.0) % (2.0 * Math.PI) / 3.0)
						+ ((float) (position * 6.0) % (2.0 * Math.PI) / 4.0)
						+ ((float) (position * 8.0) % (2.0 * Math.PI) / 5.0)
						+ ((float) (position * 10.0) % (2.0 * Math.PI) / 6.0)
						+ ((float) (position * 12.0) % (2.0 * Math.PI) / 7.0)
						+ ((float) (position * 14.0) % (2.0 * Math.PI) / 8.0)
						+ ((float) (position * 16.0) % (2.0 * Math.PI) / 9.0)
						+ ((float) (position * 18.0) % (2.0 * Math.PI) / 10.0) + ((float) (position * 20.0)
						% (2.0 * Math.PI) / 11.0)) * (float) 0.01);

				buffer[n++] += res;
				buffer[n++] += res;

				if (level > 0) {
					if (release) {
						level -= 0.001;
					}
				} else {
					getAudioOutput().removeTransmitter(this);
					break;
				}
			}
		}
	}

	/**
	 * 
	 */
	public String toString() {
		//TODO count "ORGAN 1" ETC
		return "Organ";
	}

	@Override
	public Serializable getSettings() {
            return organSettings;
        }

	@Override
	public void loadSettings(Serializable settings) {
		// TODO Auto-generated method stub
		
	}
}
