/*
 * Created on Mar 6, 2007
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

package com.frinika.sequencer.midi.groovepattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;

/**
 * Groove pattern derived from midi data.
 * 
 * The midi data must at least contain one event every beat, otherwise its size will
 * not be detected properly.
 *  
 * @author Jens Gulden
 */
public class GroovePatternFromSequence implements GroovePattern {

	protected String name;
	protected Sequence sequence;
	protected long length; // rounded as whole beats
	protected int lengthInBeats;
	protected int notesCount;

	public GroovePatternFromSequence() {
		super();
	}

	public GroovePatternFromSequence(String name, Sequence sequence) {
		this();
		setName(name);
		setSequence(sequence);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
		// calculate length in beats and notes count
		Track track = getTrack();
		notesCount = 0;
		long start = -1;
		long end = -1;
		int size = track.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				MidiEvent ev = track.get(i);
				MidiMessage msg = ev.getMessage();
				if ((msg instanceof ShortMessage) && (((ShortMessage)msg).getCommand() == ShortMessage.NOTE_ON)) {
					notesCount++;
					if (start == -1) {
						start = ev.getTick();
					}
					end = ev.getTick();
				}
			}
			int resolution = sequence.getResolution();
			long b1 = (start + (resolution/4)) / resolution; // +res/2: allow notes to start a bit earlier but still be counted to other bar
			long b2 = (end - (resolution/4)) / resolution; // - res/2: ...
			lengthInBeats = (int)(b2 - b1 + 1);
			length = lengthInBeats * resolution;
		} else {
			length = 0;
		}
	}
	
	public long quantize(long tick, int quantizeResolution, float smudge, int[] velocityByRef) {
		long t = tick % length; // tick inside pattern, to be (de-)quantized (length is rounded as whole beats)
		long diff = tick - t; // add again later
		
		long q = findNearest(t, velocityByRef);
		long qDiff = t-q;
		if (qDiff < 0) qDiff = -qDiff;
		if (qDiff > quantizeResolution) { // didn't find a tick close enough in pattern: use original tick without groove (de-)quantization (should it be quantizeResolution/2 ?)
			return tick;
		} else {
			if (smudge != 0.0f) {
				int[] v2 = new int[1];
				long q2 = findFurthest(t, quantizeResolution, v2);
				long qDiff2 = t-q2;
				if (qDiff2 > qDiff) { // alternative possible value
					long d = q2 - q;
					d = Math.round(d * smudge);
					q += d;
					int n = v2[0] - velocityByRef[0];
					velocityByRef[0] += Math.round(n * smudge);
				}
			}
			return q + diff;
		}
	}
	
	private long findNearest(long tick, int[] v) {
		if (notesCount == 0) return tick;
		Track track = getTrack();
		int size = track.size();
		
		int shift = 4 * sequence.getResolution(); // pattern starts at 4 * ppq
		
		long t = tick % length; // tick inside pattern, to be (de-)quantized
		t = tick + shift;
		
		int i = 0;
		long nearest = -1;
		v[0] = 100;
		long tt = t;
		MidiEvent ev;
		boolean isNote;
		do {
			ev = track.get(i++);
			tt = ev.getTick();
			MidiMessage msg = ev.getMessage();
			isNote = (msg instanceof ShortMessage) && (((ShortMessage)msg).getCommand() == ShortMessage.NOTE_ON); 
			if ((isNote) && (  ( tt < t ) || ( (tt-t) < (t-nearest) ) ) ) {
				nearest = tt;
				v[0] = ((ShortMessage)msg).getData2();
			}
		} while ((i < size) && ((!isNote) || (tt < t))); // stops after first note imediately following after (tt<t) no longer is true 
		return nearest - shift;
	}
	
	private long findFurthest(long tick, int q, int[] v) {
		if (notesCount == 0) return tick;
		Track track = getTrack();
		int size = track.size();
		
		int shift = 4 * sequence.getResolution(); // pattern starts at 4 * ppq
		
		long t = tick % length; // tick inside pattern, to be (de-)quantized
		t = tick + shift;
		
		int i = 0;
		long furthest = t;
		v[0] = 100;
		int q2 = q/2;
		long tt;
		MidiEvent ev;
		while ((i < size) && ( (tt =  (ev = track.get(i++)).getTick()) < t+q2)) {
			MidiMessage msg = ev.getMessage();
			if ((msg instanceof ShortMessage) && (((ShortMessage)msg).getCommand() == ShortMessage.NOTE_ON)) {
				long d = t - tt;
				if (d < 0) d = -d;
				if (d <= q2) {
					if (abs(furthest-t) < d) { // found something in allowed range, and even further away 
						furthest = tt;
						v[0] = ((ShortMessage)msg).getData2();
					}
				}
			}
		}
		return furthest - shift;
	}
	
	private static long abs(long a) {
		if (a < 0) {
			return -a;
		} else {
			return a;
		}
	}
	
	/*public int velocity(long tick) {
		Track track = getTrack();
		int size = track.size();
		if (size <= 0) return 100; // 100 is default if not found
		
		int shift = 4 * sequence.getResolution(); // pattern starts at 4 * ppq
		//long length = getTrack().ticks() - shift;
		//int ticksPerBeat = sequence.getResolution();
		//length = ((length / ticksPerBeat) +1) * ticksPerBeat; // length rounded as whole beats
		
		long t = tick % length; // tick inside pattern
		t = tick + shift;
		
		MidiEvent ev = track.get(0);
		long tt = ev.getTick();
		int i = 1;
		long nearest = tt;
		int vel = 100; // 100 is default if not found
		while ((i < size) && (tt <= t)) {
			ev = track.get(i++);
			tt = ev.getTick();
			nearest = tt;
		}
		if (tt >= t) {
			if ((tt - t) < (t - nearest)) {
				nearest = tt;
			}
		}
		return nearest - shift;
	}*/
	
	public void importFromMidiPart(String name, MidiPart part) throws IOException {
		this.sequence = null;
		this.name = GroovePatternManager.normalizeName(name);
		try {
			sequence = new Sequence(Sequence.PPQ, part.getLane().getProject().getSequence().getResolution(), 1);
			normalize(part, sequence);
			setSequence(sequence); // to update properties
		} catch (InvalidMidiDataException imde) {
			imde.printStackTrace();
			throw new IOException("unable to import MIDI part as groove pattern");
		}
	}

	public void importFromMidiFile(String name, InputStream in) throws IOException {
		this.sequence = null;
		this.name = GroovePatternManager.normalizeName(name);
		try {
			Sequence seq = MidiSystem.getSequence(in);
			if (seq == null) {
				throw new IOException("unable to load groove pattern " + name);
			}
			sequence = new Sequence(Sequence.PPQ, seq.getResolution(), 1);
			normalize(seq, sequence);
			setSequence(sequence); // to update properties
		} catch (InvalidMidiDataException imde) {
			imde.printStackTrace();
			throw new IOException("unable to load groove pattern " + name + " - " + imde.getMessage());
		}
	}

	public void importFromMidiFile(File file) throws IOException {
		String name = file.getName();
		int dot = name.lastIndexOf('.');
		if (dot != 1) {
			name = name.substring(0, dot);
		}
		
		FileInputStream in = new FileInputStream(file);
		importFromMidiFile(name, in);
		in.close();
	}
	
	public void saveAsMidiFile(File file) throws IOException {
		MidiSystem.write(sequence, 1, file);
	}
	
	public void openAsOwnProject() throws Exception {
		ProjectContainer newProject = new ProjectContainer(sequence);
		ProjectFrame newProjectFrame = new ProjectFrame(newProject);
	}
	
	/**
	 * Copies all note-events from the MidiPart to track 0, channel 0, starting at offset 4 beats (offset to cleanly capture negative shifts of the very first beat)
	 * @param part
	 * @param sequence empty 1-track sequence to be filled
	 */
	static void normalize(MidiPart part, Sequence sequence) {
		int resolutionPart = part.getLane().getProject().getSequence().getResolution();
		int resolutionSeq = sequence.getResolution();
		Track track = sequence.getTracks()[0];
		boolean firstNote = true;
		long shift = 4 * resolutionPart; // offset start: 4 beats
		for (MultiEvent ev : part.getMultiEvents()) {
			if (ev instanceof NoteEvent) {
				NoteEvent n = (NoteEvent)ev;
				long start = n.getStartTick();
				long end = n.getEndTick();
				int note = n.getNote();
				int vel = n.getVelocity();
				if (firstNote) {
					shift -= ((start + (resolutionPart / 4)) / resolutionPart) * resolutionPart ; // correct offset by leading number of empty beats (so pattern will always start in first beat (or a little earlier for negative groove-shifts, this is why resolution/2 is added))
					firstNote = false;
				}
				start += shift;
				end += shift;
				if (resolutionPart != resolutionSeq) {
					start = translateResolution(start, resolutionPart, resolutionSeq);
					end = translateResolution(end, resolutionPart, resolutionSeq);
				}
				try {
					ShortMessage sm = new ShortMessage();
					sm.setMessage(ShortMessage.NOTE_ON, 0, note, vel);
					MidiEvent event = new MidiEvent(sm, start);
					track.add(event);
					sm = new ShortMessage();
					sm.setMessage(ShortMessage.NOTE_OFF, 0, note, vel);
					event = new MidiEvent(sm, end);
					track.add(event);
				} catch (InvalidMidiDataException imde) {
					imde.printStackTrace();
				}
			}
		}
	}

	/**
	 * Copies all note-events from seq to track 0, channel 0, starting at offset 4 beats
	 *  
	 * (Might fail if multiple tracks are imported and a track other than #0 has an earlier 
	 * note than track #0. However, this method is mainly intended for loading internally 
	 * saved grooved patterns.)
	 * 
	 * @param part
	 * @param sequence empty 1-track sequence to be filled
	 */
	static void normalize(Sequence seq, Sequence sequence) {
		int srcRes = seq.getResolution();
		int dstRes = sequence.getResolution();
		Track track = sequence.getTracks()[0];
		boolean firstNote = true;
		long shift = 4 * srcRes; // offset start: 4 beats
		Track[] srcTracks = seq.getTracks();
		for (int i = 0; i < srcTracks.length; i++) {
			Track srcTrack = srcTracks[i];
			int size = srcTrack.size();
			for (int j = 0; j < size; j++) {
				MidiEvent ev = srcTrack.get(j);
				MidiMessage msg = ev.getMessage();
				if (msg instanceof ShortMessage) {
					ShortMessage sh = (ShortMessage)msg;
					int cmd = sh.getCommand();
					if (cmd == ShortMessage.NOTE_ON || cmd == ShortMessage.NOTE_OFF) {
						int note = sh.getData1();
						int vel = sh.getData2();
						long start = ev.getTick();
						if (firstNote) {
							shift -= ((start + (srcRes / 4)) / srcRes) * srcRes ; // correct offset by leading number of empty beats (so pattern will always start in first beat (or a little earlier for negative groove-shifts, this is why resolution/2 is added))
							firstNote = false;
						}
						start += shift;
						if (srcRes != dstRes) {
							start = translateResolution(start, srcRes, dstRes);
						}
						// insert new event into target sequence
						try {
							ShortMessage sm = new ShortMessage();
							sm.setMessage(cmd, 0, note, vel);
							MidiEvent event = new MidiEvent(sm, start);
							track.add(event);
						} catch (InvalidMidiDataException imde) {
							imde.printStackTrace();
						}
					}
				}
			}
		}
	}

	static long translateResolution(long tick, int srcRes, int destRes) {
		return Math.round( ((double)tick / srcRes) * destRes );
	}
	
	/**
	 * For displaying in GroovePatternManagerDialog's list.
	 */
	public String toString() {
		return getName() + " [" + lengthInBeats +" beats, " + notesCount + " notes]";
	}

	public Track getTrack() {
		return sequence.getTracks()[0];
	}
}
