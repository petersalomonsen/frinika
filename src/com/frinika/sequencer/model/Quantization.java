/*
 * Created on Jun 10, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

package com.frinika.sequencer.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import com.frinika.sequencer.midi.groovepattern.GroovePattern;
import com.frinika.sequencer.midi.groovepattern.GroovePatternManager;

/**
 * Encapsulates quantization by storing quatization options and bundling
 *  corresponding quantization methods.
 *  
 * @author Jens Gulden
 */
public class Quantization implements Serializable {

	private static final long serialVersionUID = 1L;

	public int interval = 128 / 2; // default 1/8 note, TODO make dependent on sequencer's resolution
	public float intensity = 1.0f;
	public float swing = 0f;
	public boolean quantizeNoteStart = true;
	public boolean quantizeNoteLength = false;
	public float smudge = 0.5f; // general groove-pattern-intensity parameter, interpretation dependent on GroovePattern 
	public float velocity = 0f; // intensity of adopting groove pattern's velocity
	public transient GroovePattern groovePattern = null; // null for hard quantization
	private String groovePatternName; // for serialization  
	
	/**
	 * Quantize a tick as requested by options.
	 * 
	 * @param tick
	 * @param quant
	 * @param strength
	 * @param swing
	 * @param groovePattern
	 * @param grooveSmudge
	 * @param velocityByRef
	 * @return
	 */
	public long quantize(long tick, int[] velocityByRef) {
		long t = tick + (interval/2);
		long rest = t % interval ;
		long target = t - rest; // hard-quantized to selected resolution
		long swingShift = 0;
		
		if (swing != 0.0f) {
			if (((target / interval) % 2) == 1) { // every odd position in quantization-raster
				swingShift +=  Math.round(interval * swing);
			}
		}
		
		if (groovePattern != null) {
			target = groovePattern.quantize(target, interval, smudge, velocityByRef); // de-quantize to feel (but remain at selected resolution)
		}
		
		target += swingShift;
		
		int diff = (int)(target - tick);
		int sDiff = Math.round(diff * intensity);
		return tick + sDiff;
    }
	
	
	/**
	 * Quantize a single MidiEvent as requested by options. Either the original instance is returned unchanged
	 * (if no quantization has been applied), or a new MidiEvent object with a modified timestamp (and possibly
	 * changed other valued like e.g. velocity) is returned. 
	 */
	public MidiEvent quantize(MidiEvent event) {
		byte[] data = event.getMessage().getMessage();
		int status = data[0] & 0xf0;
		int vel = data[2];
		if (  (quantizeNoteStart && (status == ShortMessage.NOTE_ON) && (vel != 0) )
		    || (quantizeNoteLength && ( (status == ShortMessage.NOTE_OFF) || ( (vel == 0) && (status == ShortMessage.NOTE_ON) ) ) ) ) {
			// (some devices use note_on with velocity 0 for note_off)
			// quantize note length does not behave exactly the same when quantizing on-the-fly and offline
			long tick = event.getTick();
			int[] velocityByRef = { -1 };
			long qTick = quantize(tick, velocityByRef);
			MidiMessage message;
			int targetVel = velocityByRef[0];
			if ( (vel != 0) && (targetVel != -1) && (velocity != 0f) && (groovePattern != null)) { // change velocity value
				int d = Math.round((targetVel - vel) * velocity);
				vel += d;
				if (vel < 1 ) vel = 1; else if (vel > 127) vel = 127;
				message = new ShortMessage();
				try {
					((ShortMessage)message).setMessage(data[0], data[1], vel);
				} catch (InvalidMidiDataException imde) {
					System.err.println("something went wrong while quantizing a MidiEvent");
					return event;
				}
			} else {
				message = event.getMessage();
			}
			if ( ( (status == ShortMessage.NOTE_OFF) || (vel == 0) ) && ( qTick != tick ) ) { // note-off has been quantized
				if (qTick > 0) qTick--; // end one earlier
			}
			MidiEvent newEvent = new MidiEvent(message, qTick);
			return newEvent;
		} else {
			return event;
		}
	}
	
	private final static int[] DUMMY = new int[1];
	
	/**
	 * Quantize a NoteEvent.
	 * 
	 * @param note
	 */
	public void quantize(NoteEvent note) {
		if ( quantizeNoteStart ) {
			long tick = note.getStartTick();
			int[] velocityByRef = { -1 };
			long qTick = quantize(tick, velocityByRef);
			note.setStartTick(qTick);
			int targetVel = velocityByRef[0];
			if ((targetVel != -1) && (velocity != 0) && (groovePattern != null)) {
				int vel = note.getVelocity();
				vel = Math.round(vel + ( (targetVel - vel) * velocity ));
				if (vel > 127) vel  = 127; else if (vel < 1) vel = 1;
				note.setVelocity(vel);
			}
			
		}
		if ( quantizeNoteLength ) {
			long len = note.getDuration();
			long qLen = quantize(len, DUMMY);
			if (qLen == 0) {
				qLen = interval;
			}
			qLen--;
			note.setDuration(qLen);
		}
	}

	/**
	 * customize serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		if (groovePattern != null) {
			groovePatternName = groovePattern.getName();
		} else {
			groovePatternName = null; 
		}
		out.defaultWriteObject();
	}

	/**
	 * customize serialization
	 * @param out
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream in)	throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		if (groovePatternName != null) {
			groovePattern = GroovePatternManager.getInstance().getGroovePattern(groovePatternName);
		} else {
			groovePatternName = null; 
		}
	}
	
}
