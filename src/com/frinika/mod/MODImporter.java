/*
 * Created on 12.3.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

package com.frinika.mod;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import rasmus.midi.provider.RasmusSynthesizer;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.midi.message.TempoMessage;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.MetaEvent;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.PitchBendEvent;
import com.frinika.sequencer.model.ProgramChangeEvent;
import com.vwp.sound.mod.modplay.loader.InvalidFormatException;
import com.vwp.sound.mod.modplay.loader.ModuleLoader;
import com.vwp.sound.mod.modplay.module.Instrument;
import com.vwp.sound.mod.modplay.module.Module;
import com.vwp.sound.mod.modplay.module.Pattern;
import com.vwp.sound.mod.modplay.module.Sample;
import com.vwp.sound.mod.modplay.module.Track;
import com.vwp.sound.mod.modplay.player.Mixer;
import com.vwp.sound.mod.modplay.player.ModuleState;
import com.vwp.sound.mod.modplay.player.PlayerException;
import com.vwp.sound.mod.modplay.player.TrackState;

import static com.frinika.localization.CurrentLocale.getMessage;



public class MODImporter {

	private static class ModulesFileFilter extends FileFilter {
		public boolean accept(File f) {	
			if(f.isDirectory()) return true;
			if(!f.isFile()) return false;		
			if(f.getName().toLowerCase().endsWith(".mod")) return true;
			if(f.getName().toLowerCase().endsWith(".xm")) return true;
			if(f.getName().toLowerCase().endsWith(".s3m")) return true;
			if(f.getName().toLowerCase().endsWith(".stm")) return true;
			if(f.getName().toLowerCase().endsWith(".it")) return true;
			if(f.getName().toLowerCase().endsWith(".zip")) return true;
			return false;
		}

		public String getDescription() {
			return "Module Files (*.mod,*.xm,*.s3m,*.stm,*.it,*.zip)";
		}
	}	
	
	public static void load(Component parent)
	{
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(getMessage("project.menu.import_module"));
			chooser.setFileFilter(new ModulesFileFilter());

			if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				File newFile = chooser.getSelectedFile();
				if(newFile.isDirectory()) return;
				if(!newFile.isFile()) return;
				
				ProjectContainer project = new ProjectContainer();
				load(newFile, project);
				ProjectFrame frame = new ProjectFrame(project);
			}
			;
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(parent, ex.toString());
			ex.printStackTrace();
		}		
	}

	class XNoteEvent
	{
		long tick = 0;
		MidiPart part;
		int note = 0;
		int instrument = 0;
		double volume = 1.0;
		double pitch = 1.0;
		double pan = 0.5;
	}
	class XNoteEvents
	{	
		ArrayList<XNoteEvent> events = new ArrayList<XNoteEvent>();
		int track;
		boolean note_active = false;
		public void processEvent(int note, int instrument, double pitch, double volume, double pan)
		{
			if(note == -1) // Note End
			{				
				commit();
				return;
			}
			
			if(note != -2) // New Note 
			{
				commit();
				note_active = true;
			}
			
			if(!note_active) return;
			XNoteEvent event = new XNoteEvent();
			event.tick = tickpos;
			event.part = getPart(track);
			event.note = note;
			event.instrument = instrument;
			event.pitch = pitch;
			event.volume = volume;
			event.pan = pan;
			events.add(event);
		}
			
		public void commit()
		{			
			if(events.size() == 0) return;
			
			XNoteEvent firstevent = events.get(0);
			
			Iterator<XNoteEvent> iter = events.iterator();
			double maxvol = firstevent.volume;;
			while (iter.hasNext()) {
				XNoteEvent event = iter.next();
				if(event.volume > maxvol) maxvol = event.volume;
				
			}
			
			if(maxvol == 0) // Volume is always 0, skip this events
			{
				events.clear();
				return;
			}
			
			// Set instrument first !!!
			if(firstevent.instrument >= 0)
			if(selected_instrument[track] != firstevent.instrument)
			{
				selected_instrument[track] = firstevent.instrument;
				int program = firstevent.instrument;
				if(program < 0) program = 0;
				if(program > 127) program = 127;
				
				ProgramChangeEvent programEvent = new ProgramChangeEvent(firstevent.part, firstevent.tick, program, 0, 0);
				programEvent.setProgram(program,0,0);
				firstevent.part.add(programEvent);
			}			
								
			// Find out last tick pos, by looking at volume
			long lasttick = tickpos;
			for (int i = events.size() - 1; i >= 0 ; i--) {
				if(events.get(i).volume == 0)
				{
					lasttick = events.get(i).tick;
				}
				else
				{
					break;
				}
			}

			int pitchRange = 2;
			// Check for pitch range need
			iter = events.iterator();
			while (iter.hasNext()) {
				XNoteEvent event = iter.next();				
				if(event.tick >= lasttick) break;				
				double set_pitch = event.pitch;
				double pitchN = (Math.log(set_pitch)/Math.log(2))*12.0;				
				if(Math.abs(pitchN) > 2)
				{
					int new_range = (int)(Math.abs(pitchN)+1);
					if(new_range > pitchRange) pitchRange = new_range;
				}

			}
			
			// Update pitch range if needed
			if(pitchRange != control_pitch_range[track])
			{
				control_pitch_range[track] = pitchRange;
				
				if(pitchRange > 127) pitchRange = 127;
				// Select RPN : Pitch Bend Range
				firstevent.part.add(new ControllerEvent(firstevent.part, firstevent.tick, 0x65,  0));
				firstevent.part.add(new ControllerEvent(firstevent.part, firstevent.tick, 0x64,  0));
				// Data Entry coarse										
				firstevent.part.add(new ControllerEvent(firstevent.part, firstevent.tick, 0x06,  pitchRange));
				firstevent.part.add(new ControllerEvent(firstevent.part, firstevent.tick, 0x26,  pitchRange));
			}				
			
			// Add Control events
			iter = events.iterator();
			while (iter.hasNext()) {
				XNoteEvent event = iter.next();
				
				if(event.tick >= lasttick) break;
				
				double set_vol = event.volume / maxvol;
				double set_pitch = event.pitch;
				double set_pan = event.pan;
				
				if(set_vol != control_vol[track])
				{
					control_vol[track] = set_vol;
					int val = (int)(127.0 * set_vol);
					if(val > 127) val = 127;
					if(val < 0) val = 0;							
					event.part.add(new ControllerEvent(event.part, event.tick, 7,  val));
					
				}
				if(set_pan != control_pan[track])
				{
					control_pan[track] = set_pan;		
					int val = (int)(127.0 * set_pan);
					if(val > 127) val = 127;
					if(val < 0) val = 0;							
					event.part.add(new ControllerEvent(event.part, event.tick, 10,  val));					
				}
				if(set_pitch != control_pitch[track])
				{
					control_pitch[track] = set_pitch;
					
					// Convert pitch to log					
					double pitchN = (Math.log(set_pitch)/Math.log(2))*12.0;
                    /*
					int pitchRange = 2;
					
					if(Math.abs(pitchN) > 2)
						pitchRange = (int)(Math.abs(pitchN)+1);
					
					if(pitchRange != control_pitch_range[track])
					{
						control_pitch_range[track] = pitchRange;
						
						if(pitchRange > 127) pitchRange = 127;
						// Select RPN : Pitch Bend Range
						event.part.add(new ControllerEvent(event.part, event.tick, 0x65,  0));
						event.part.add(new ControllerEvent(event.part, event.tick, 0x64,  0));
						// Data Entry coarse						
						event.part.add(new ControllerEvent(event.part, event.tick, 0x06,  pitchRange));
						
					}						*/
										
					if(pitchN > pitchRange) pitchN = pitchRange;
					if(pitchN < -pitchRange) pitchN = -pitchRange;
					
					int val = (int)((pitchN + pitchRange)* (8192.0 / pitchRange));
					
					if(val > 16256) val = 16256;
					if(val < 0) val = 0;
					
					event.part.add(new PitchBendEvent(event.part, event.tick, val));
					
				}				
			}						
			
			// Add Note Event
			
			int vel = (int)(maxvol * 127.0); 
			if(vel > 127) vel = 127;
			if(vel < 0) vel = 0;							
			
			// public NoteEvent(MidiPart part, long startTick, int note, int velocity, int channel, long duration)
			firstevent.part.add(new NoteEvent(firstevent.part, firstevent.tick, firstevent.note, vel, 0, lasttick - firstevent.tick));
			

			
			events.clear();
			
			note_active = false;
		}
	}

	Module module;
	MidiLane[] lanes;
	MidiPart[] parts;
	XNoteEvents[] events;
	
	public MidiPart getPart(int track)
	{
		if(parts[track] != null) return parts[track];		
		parts[track] = new MidiPart(lanes[track]);
		parts[track].setStartTick(lastPatternSeperator);		
		return parts[track];
	}
	
	double[] control_pan;
	double[] control_vol;
	double[] control_pitch;
	int[] control_pitch_range;
	int[] selected_instrument;
	double current_tempo;
	
	double MIN_PITCH_RANGE = 2;
	long lastPatternSeperator = -1;
	long tickpos = 0;
	long tickpos_major = 0;	
	long tickstep;  // ticks per divition(in modules)
	long part_startpos = 0;
	boolean patternActive = false;
	private void endPattern()
	{
		for (int c = 0; c < lanes.length; c++) 
		{
			if(parts[c] != null)
			{
				parts[c].setEndTick(tickpos);
				parts[c] = null;
			}
		}
		patternActive = false;
	}
	private void startPattern()
	{
		if(patternActive)
		if(lastPatternSeperator == tickpos) return;
		
		endPattern();		
		part_startpos = tickpos;
		for (int c = 0; c < lanes.length; c++) {			
			parts[c] = null;
		}					
		patternActive = true;
		lastPatternSeperator = tickpos;
	}
	
	public void processEvent(int track, int note, int instrument, double pitch, double volume, double pan)
	{		
		events[track].processEvent(note,instrument,pitch,volume,pan);
	}
	public void finish()
	{
		for (int i = 0; i < events.length; i++) {
			events[i].commit();
		}


		// Remove empty parts
		for (int i = 0; i < lanes.length; i++) {			
			Part[] parts = new Part[lanes[i].getParts().size()];
			lanes[i].getParts().toArray(parts);
			for (int j = 0; j < parts.length; j++) {
				if(parts[j] instanceof MidiPart)
				{
					MidiPart mpart = (MidiPart)parts[j];
					if(mpart.getMultiEvents().isEmpty())
					{
						mpart.removeFromModel();
					}
				}
			}
		}
		
		// Remove lanes not used
		for (int i = 0; i < lanes.length; i++) {
			if(lanes[i].getParts().size() == 0) lanes[i].removeFromModel();
		}		
		
	}
	
	int trackcount;
	
	ProjectContainer project;
	private MODImporter(File file, ProjectContainer project) throws InvalidFormatException, IOException
	{
		
		this.project = project;
		
		tickstep = project.getSequence().getResolution()/4;
		
		ModuleLoader modloader = ModuleLoader.getModuleLoader(file);
		module = modloader.getModule();
		
		// Count how many tracks are really used
		trackcount = 0;		
		for (int i = 0; i < module.getNumberOfPositions(); i++) {
			Pattern pattern = module.getPatternAtPos(i);
			for (int j = 0; j < pattern.getTrackCount(); j++) {
				Track track = pattern.getTrack(j);
				for (int k = 0; k < pattern.getDivisions(); k++) {
					if(track.getNote(k) > 0)
					{
						if(trackcount < (j + 1)) trackcount = j + 1;							
					}
				}
			}
		}
	
		lanes = new MidiLane[trackcount];
		parts = new MidiPart[trackcount];
		events = new XNoteEvents[trackcount];

		control_pan = new double[trackcount];
		control_vol = new double[trackcount];
		control_pitch = new double[trackcount];
		control_pitch_range = new int[trackcount];
		selected_instrument = new int[trackcount];
		
		
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("tostereo <- function(input)\n");
		buffer.append("{ \n");
		buffer.append("  <- channelmux(input, input); \n");
		buffer.append("}\n");
		buffer.append("\n");
		
		Instrument[] instruments = module.getInstruments();
		for (int i = 0; i < instruments.length; i++) {
			Instrument ins = instruments[i];
								
			
			if(ins.getNumberOfSamples() == 1)
			{				
				Sample sample = ins.getSampleByNum(0);
				File tempfile = getSampleFile(sample);
				
				if(tempfile != null)
				{
					String sampleid = "sample_" + i ;
					if(sample.getLoopType() != 0)
						buffer.append(sampleid + " <- sample('" + tempfile.getPath() + "', loopstart=" + sample.getLoopStart()*2 + ", loopend=" + (sample.getLoopStart() + sample.getLoopLength())*2 + ");\n");
					else
						buffer.append(sampleid + " <- sample('" + tempfile.getPath() + "');\n"); 
					buffer.append("instruments <- instrument(" + i  + ",0,'" + ins.getName().replace('\'', '"')+ "')\n");
					buffer.append("            <- function(note, velocity, pitch = 1, gate)\n");
					buffer.append("{\n");
					
					float rate = (float)sample.getUnits().note2rate(sample.getFineTune() + sample.getRelativeNote());
					buffer.append("  rate <- " + rate + " * pow(2, note / 12) * pitch; \n");
					buffer.append("  <- tostereo() <- gain(velocity*adsr(gate,0.01,0.01,1,0.01)) <- resamplei(rate/srate()) <- " + sampleid + "; \n");
					buffer.append("} \n");
					buffer.append("\n");
				}
			}			
			
			if(ins.getNumberOfSamples() > 1)
			{
				HashMap<Sample, String> sample_table = new HashMap<Sample, String>();
				for (int j = 0; j < ins.getNumberOfSamples(); j++) {
					
					Sample sample = ins.getSampleByNum(0);
					File tempfile = getSampleFile(sample);
					
					if(tempfile != null)
					{
						String sampleid = "sample_" + j + "_" + i ;
						if(sample.getLoopType() != 0)
							buffer.append(sampleid + " <- sample('" + tempfile.getPath() + "', loopstart=" + sample.getLoopStart()*2 + ", loopend=" + (sample.getLoopStart() + sample.getLoopLength())*2 + ");\n");
						else
							buffer.append(sampleid + " <- sample('" + tempfile.getPath() + "');\n"); 
						buffer.append("instrument_" + j + "_" + i + " <- function(note, velocity, pitch = 1, gate)\n");
						buffer.append("{\n");
						
						float rate = (float)sample.getUnits().note2rate(sample.getFineTune() + sample.getRelativeNote());
						buffer.append("  rate <- " + rate + " * pow(2, note / 12) * pitch; \n");
						buffer.append("  <- tostereo() <- gain(velocity*adsr(gate,0.01,0.01,1,0.01)) <- resamplei(rate/srate()) <- " + sampleid + "; \n");
						buffer.append("} \n");
						buffer.append("\n");
						
						sample_table.put(sample, "instrument_" + j + "_" + i );
					}				
					
				}
				for (int j = 0; j < 128; j++) {
					Sample sample = ins.getSampleByNote(j);
					int firstNote = j;
					if(sample != null)
					{						
						for (; j < 128; j++) {							
							if(ins.getSampleByNote(j + 1) != sample)
							{
								int lastNote = j - 1;
								String s = sample_table.get(sample);
								if(s != null)
									buffer.append("instrument_" + i + " <- registerVoice(" + firstNote + "," + lastNote + ") <- " + s + ";\n");
								break;
							}
						}
					}
				}
				
				buffer.append("instruments <- instrument(" + i  + ",0,'" + ins.getName().replace('\'', '"')+ "') <- instrument_" + i + ";\n");
			}
			
		}
		
		SynthWrapper synthwrap = null;
		for (int i = 0; i < lanes.length; i++) {	
			
			if(i % 16 == 0)
			{
				RasmusSynthesizer midiDevice = new RasmusSynthesizer();
				//midiDevice.open();
				synthwrap = new SynthWrapper(project, midiDevice);
				try {
					project.addMidiOutDevice(synthwrap);
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}		
				midiDevice.setScript(buffer.toString());							
			}
			
			lanes[i] = project.createMidiLane();
			lanes[i].setName("ch"+ (i+1));
			lanes[i].getTrack().setMidiChannel(i % 16);
			lanes[i].getTrack().setMidiDevice(synthwrap);
			events[i] = new XNoteEvents();
			events[i].track = i;

			control_pan[i] = -1; // Not set
			control_vol[i] = -1;
			control_pitch[i] = 1;
			control_pitch_range[i] = -1; // Pitch range (not set), default is 2
			selected_instrument[i] = -1; // No instrument selected;
			
		}		
		
	}
	
	private class ShortInputStream extends InputStream
	{		
		short[] data;
		int lastindex;
		public ShortInputStream(short[] data)
		{
			this.data = data;
			lastindex = (data.length * 2) - 1;
		}
		
		int index = -1;
		public int read() throws IOException {
			if(index == lastindex) return -1;
			index++;
			short val = data[index >> 1];
			if((index & 1) == 0)
				return (val >>> 0 ) & 0xFF ;	
			else			
				return (val >>> 8 ) & 0xFF;						
		}
	}
	
	HashMap<Sample, File> samplestempfiles = new HashMap<Sample, File>();
	private File getSampleFile(Sample sample)
	{
		if(sample == null) return null;
		if(sample.getData() == null) return null;
		if(sample.getData().length == 0) return null;
		
		File file = samplestempfiles.get(sample);
		if(file != null) return file;
				
		try {
			file = File.createTempFile("sample", ".wav");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		short[] data = sample.getData();
		
		ShortInputStream is = new ShortInputStream(data);
				
        float rate = (float)sample.getUnits().note2rate(36 + sample.getFineTune() + sample.getRelativeNote());
		
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, 1, 2, rate*2, false);		
		AudioInputStream audio_inputstream = new AudioInputStream(is, format, data.length * 2);		
		try {
			AudioSystem.write(audio_inputstream, AudioFileFormat.Type.WAVE, file);
		} catch (IOException e) {
			e.printStackTrace();
			if(file.exists()) file.delete();
			return null;
		}
		
		file.deleteOnExit(); // Temp file should be deleted on frinika exit!!!
		samplestempfiles.put(sample, file);		
			
		return file;
	}
	
	static final boolean DEBUG_PRINT_PATTERN_WHILE_LOADING = false;
		
	private void load()
	{
		//hardwired.xm,moscow.mod,odyssey1.mod
		
		/*
		System.out.println();
		System.out.println("Instruments");
		System.out.println("===========");
		
		Instrument[] instruments = module.getInstruments();
		for (int i = 0; i < instruments.length; i++) {
			Instrument ins = instruments[i];
			System.out.println("name: " + ins.getName());
			System.out.println("samples: " + ins.getNumberOfSamples());
			System.out.println();
			
			System.out.println("sample.length: " + ins.getSampleByNum(0).getLength());
			System.out.println("sample.note: " + ins.getSampleByNum(0).getRelativeNote());
			System.out.println("sample.tune: " + ins.getSampleByNum(0).getFineTune());
		}
		
		System.out.println();
		System.out.println("Orderlist");
		System.out.println("=========");
		for (int i = 0; i < module.getNumberOfPositions(); i++) {
			System.out.println("pattern" + module.getPatternIndexAtPos(i));
		}
		
		System.out.println();
		System.out.println("Pattern0");
		System.out.println("========");
		Pattern pattern = module.getPattern(0);		
		System.out.println("tracks: " + pattern.getTrackCount());
		for (int i = 0; i < pattern.getDivisions(); i++) {
			System.out.print(i + "# ");
			for (int j = 0; j < pattern.getTrackCount(); j++) {
				System.out.print(pattern.getTrack(j).getInfo(i)+ " ");
				//System.out.print(pattern.getTrack(j).getInstrumentNumber(i)+ " ");
			}
			System.out.println();
		}
		*/
			
		FRMixer mixer = new FRMixer(trackcount);
		ModuleState ms = new ModuleState(module, mixer);
		
		
		startPattern();
		
		TrackState[] ts = null;
		// trackStates is private variable, so we just cheat a little
		// to access it
		Field field;
		try {
			field = ms.getClass().getDeclaredField("trackStates");
			
			field.setAccessible(true);
			ts = (TrackState[])field.get(ms);
		} catch (Exception e1) {
			e1.printStackTrace();
		}  		
		
        try {
        	System.out.println(ms.getPosition() + ":" + ms.getDivision() + ":" + ms.getTick() + " first");
        	boolean engage = false;
        	boolean ok = true;
        	int last_position = ms.getPosition();
        	
        	TreeSet<String> loop_detection = new TreeSet<String>();
        	
        	String pos_id = ms.getPosition() + ":" + ms.getDivision();
        	loop_detection.add(pos_id);
        	
        	int beat = 0;
        	
			while(ok)
			{		
				
				int tick = ms.getTick();
				
				if(last_position != ms.getPosition())
				{					
					System.out.println();
					System.out.println();
					startPattern();
				}
				last_position = ms.getPosition();
				
				ok = ms.play();
				
				
				//if(tick == 0)
				{
					
				// In midi , let 4 divison be a beat, where one divison is a quarter note
				// Let's calculate BPM from that, lastplaytime is in msec
					
				// beat/ms   ms * 1000 = sec
				// 
				double bpm = 60000.0 / (mixer.lastplaytime*ms.getTicksInDivision()*4.0);
				
				if(tickpos == 0)
				{
					project.setTempoInBPM((float)bpm);
					current_tempo = bpm;
				}
				else
				{
					if(Math.abs(bpm - current_tempo) > 0.0001)
					{
						try {
							MetaEvent event = new MetaEvent(getPart(0), tickpos);
							event.setMessage(new TempoMessage((int)bpm));
							getPart(0).add(event);
						} catch (InvalidMidiDataException e) {
							e.printStackTrace();
						}
						current_tempo = bpm;
					}
				}
				
				//System.out.print((mixer.lastplaytime*ms.getTicksInDivision()) + " ");
				if(DEBUG_PRINT_PATTERN_WHILE_LOADING) if(tick == 0) System.out.print((((int)bpm)+ "bpm " + (beat/4.0) + "       ").substring(0, 16));
				if(DEBUG_PRINT_PATTERN_WHILE_LOADING) if(tick == 0) System.out.print((ms.getPosition() + ":" + ms.getDivision() + ":" + tick+ "             ").substring(0, 10));
				if(DEBUG_PRINT_PATTERN_WHILE_LOADING) if(tick == 0) System.out.print("  ");
				for (int i = 0; i < mixer.tracks.length; i++) {
					
					double baserate = 0;
					Sample sample = ts[i].getSample();
					if(sample != null)
					if(ts[i].getNote() != Instrument.NO_NOTE)
					{
			            baserate = sample.getUnits().note2rate(ts[i].getNote() + sample.getFineTune() + sample.getRelativeNote());
					}
					
					if(baserate == 0)
					{
						processEvent(i, -1,-1,-1,-1,-1); // Note end / No active note
						if(DEBUG_PRINT_PATTERN_WHILE_LOADING) if(tick == 0) System.out.print(("                               ").substring(0, 10));
					}
					else
					{
					if(!mixer.tracks[i].newNoteEvent)
					{
						if(DEBUG_PRINT_PATTERN_WHILE_LOADING) if(tick == 0) System.out.print(("  -- -" + ("-   ").substring(0, 2) + ""  + (" p"+mixer.tracks[i].rate/baserate).substring(0,5)  + (" v"+mixer.tracks[i].volume).substring(0,5) +"              ").substring(0, 18));
						processEvent(i, -2,-2,mixer.tracks[i].rate/baserate, mixer.tracks[i].volume,mixer.tracks[i].panning);
					}
					else
					{
						if(DEBUG_PRINT_PATTERN_WHILE_LOADING) if(tick == 0) System.out.print(("  " + ts[i].getNote() + ":i" + (ts[i].getInstrument()+"  ").substring(0, 2) + ""  + (" p"+mixer.tracks[i].rate/baserate).substring(0,5)  + (" v"+mixer.tracks[i].volume).substring(0,5) +"           ").substring(0, 18));
						processEvent(i, ts[i].getNote(),ts[i].getInstrument(),mixer.tracks[i].rate/baserate, mixer.tracks[i].volume,mixer.tracks[i].panning);
					}
					}
					
					
				}
				if(DEBUG_PRINT_PATTERN_WHILE_LOADING) if(tick == 0) System.out.println();
				
				
				}
				
				if(tick == ms.getTicksInDivision()-1)
				{
					beat += 1;
					tickpos_major += tickstep;
					tickpos = tickpos_major;
				}
				else
				{
					tickpos += tickstep / ms.getTicksInDivision();
				}
				
				
								
				
				
				if(ms.getTick() == 0)
				{
					pos_id = ms.getPosition() + ":" + ms.getDivision();
					if(loop_detection.contains(pos_id))
					{
						System.out.println("LOOP detected, break");
						break;
					}
					loop_detection.add(pos_id);
				}
				
				 				
				
				/*
				
				if(!engage)
				{
					
				if(first_position != ms.getPosition())
				{
					engage = true;
				}
				
				}
				else
				{

	        	if(first_position == ms.getPosition())
	        	{
	        		System.out.println("We are looping");
	        		break;
	        	}
	        	
				}
	        	*/
				
			}
		} catch (PlayerException e) {
			System.out.println("error in player");
			e.printStackTrace();
		}		

		endPattern();
		
		finish();
	}

	public static void load(File file, ProjectContainer project) throws InvalidFormatException, IOException
	{
		new MODImporter(file, project).load();		
		project.validate();
	}
	
	/*
	public static void load(File file, ProjectContainer project) throws InvalidFormatException, IOException
	{
		
		//Pattern pattern = module.getPattern(0);		
		//System.out.println("tracks: " + pattern.getTrackCount());
		
		
		NoteEvent[] active_noteevents = new NoteEvent[lanes.length];
		
		long tickpos = 0;
		for (int p = 0; p < module.getNumberOfPositions(); p++) {
			
			Pattern pattern = module.getPatternAtPos(p);
			
			// Scan for end of pattern mark
			int pattlen = pattern.getDivisions();
			body:
			for (int r = 0; r < pattern.getDivisions(); r++) {
				System.out.print(r + "# ");
				for (int c = 0; c < pattern.getTrackCount(); c++) {
					System.out.print(pattern.getTrack(c).getInfo(r)+ " ");
				
					int x_count = pattern.getTrack(c).getNumberOfEffects(r);
					for (int k = 0; k < x_count; k++) {
						 if(pattern.getTrack(c).getEffect(r, k) == Effect.MOD_PATTERN_BREAK)
						 {
							 pattlen = r;
							 break body;
						 }
					}
				}				
			}	
			
		
			MidiPart[] parts = new MidiPart[trackcount];
			for (int c = 0; c < lanes.length; c++) {			
				parts[c] = new MidiPart(lanes[c]);
				parts[c].setStartTick(tickpos);
				parts[c].setEndTick(tickpos + res*pattlen);
			}										
		
			for (int r = 0; r < pattlen; r++) {
				System.out.print(r + "# ");
				for (int c = 0; c < pattern.getTrackCount(); c++) {
					System.out.print(pattern.getTrack(c).getInfo(r)+ " ");				
					int note = pattern.getTrack(c).getNote(r);
					if (note != Instrument.NO_NOTE)
					{
						// End previous note:
						if(active_noteevents[c] != null)
						{
							active_noteevents[c].setDuration(tickpos - active_noteevents[c].getStartTick());
							active_noteevents[c].getPart().add(active_noteevents[c]);
						}
						
						if(note > 0)
						{
							// Add new note
							active_noteevents[c] = new NoteEvent(parts[c], tickpos, note, 100, 0, 1*res);
						}						
					}
					/*
					int newNote = getNote(pattern, division, trackNumber);
					int newInstrument = getInstrument(pattern, division, trackNumber);
					if (newNote != Instrument.NO_NOTE
						&& !noteIsArgument(pattern, division, trackNumber)
						&& newInstrument != Track.NO_INSTRUMENT)
						newNoteAndInstrument(newNote, newInstrument);
					else if (
						newNote != Instrument.NO_NOTE 
						&& !noteIsArgument(pattern, division, trackNumber)
						&& newInstrument == Track.NO_INSTRUMENT)
						newNote(newNote);
					else if (
						(newNote == Instrument.NO_NOTE
							|| noteIsArgument(pattern, division, trackNumber))
							&& newInstrument != Track.NO_INSTRUMENT)
						newInstrument(newInstrument);
					/
				//	System.out.print(pattern.getTrack(j).getInstrumentNumber(i)+ " ");
				}
				System.out.println();
				
				tickpos += (long)res;
				
			}				
				
		}
		
		for (int c = 0; c < active_noteevents.length; c++) {
			if(active_noteevents[c] != null)
			{
				active_noteevents[c].setDuration(tickpos - active_noteevents[c].getStartTick());
				active_noteevents[c].getPart().add(active_noteevents[c]);
			}
		}
		
		// Create a lane
/*
		MidiLane lane = project.createMidiLane();

		// Create a MidiPart
		MidiPart part = new MidiPart(lane);

		// Add some notes
		part.add(new NoteEvent(part, 0,   60, 100, 0, 70*127));
		part.add(new NoteEvent(part, 128, 61, 100, 0, 2*127));
		part.add(new NoteEvent(part, 256, 62, 100, 0, 127));
		part.add(new NoteEvent(part, 512, 63, 100, 0, 3*127));
		part.add(new NoteEvent(part, 768, 64, 100, 0, 30*127));
		part.setBoundsFromEvents();

		lane=project.createMidiLane();
		// Create a second part on the same lane
		part = new MidiPart(lane);

		// Add some notes
		part.add(new NoteEvent(part, 1024 + 0, 60, 100, 0, 127));
		part.add(new NoteEvent(part, 1024 + 128, 59, 100, 0, 127));
		part.add(new NoteEvent(part, 1024 + 256, 58, 100, 0, 127));
		part.add(new NoteEvent(part, 1024 + 512, 57, 100, 0, 127));
		part.add(new NoteEvent(part, 1024 + 768, 56, 100, 0, 127));
		part.setBoundsFromEvents();
	*
		project.validate();
		
	} */
	
	static class FRState
	{
		boolean active = false;
		boolean newNoteEvent = false;
        short[] sampleData = null;
        double offset = 0;
        double rate = 0;
        double volume = 0;
        double panning = 0;
        int loopType = 0;
        int loopStart = 0;
        int loopLength = 0;
	}
	static class FRMixer implements Mixer
	{
		
		FRState[] tracks;
		
		public FRMixer(int trackcount)
		{
			tracks = new FRState[trackcount];
			for (int i = 0; i < tracks.length; i++) {
				tracks[i] = new FRState();
			}
		}
		
		public void setTrack(
		        short[] sampleData,
		        double offset,
		        double rate,
		        double volume,
		        double panning,
		        int loopType,
		        int loopStart,
		        int loopLength,
		        int track)
		throws PlayerException {
			if(track >= tracks.length) return;
			tracks[track].newNoteEvent = false;
			if(sampleData != null)
			if(tracks[track].sampleData == null)
			{
				tracks[track].newNoteEvent = true;
				tracks[track].active = true;
			}
			if(tracks[track].offset > offset)
			{
				tracks[track].newNoteEvent = true;
				tracks[track].active = true;
			}
			tracks[track].sampleData = sampleData;			
			tracks[track].offset = offset;
			tracks[track].rate = rate;
			tracks[track].volume = volume;
			tracks[track].panning = panning;
			tracks[track].loopType = loopType;
			tracks[track].loopStart = loopStart;
			tracks[track].loopLength = loopLength;
			
			if(sampleData == null)
			{
				tracks[track].active = false;
				return;
			}
			
			if(tracks[track].active)
			if(tracks[track].loopType == 0)					
			{
				if(tracks[track].offset == sampleData.length)
				{
					tracks[track].active = false;
				}
			}
			
		}

		double lastplaytime = 0;
		public void play(double arg0) throws PlayerException {
			lastplaytime = arg0;	
		}

		public int getNumberOfTracks() {
			return 0;
		}

		public void setAmplification(double arg0) {
		}

		public double getAmplification() {
			return 0;
		}

		public void setVolume(double arg0) {
		}

		public double getVolume() {
			return 0;
		}

		public void setBalance(double arg0) {
		}

		public double getBalance() {
			return 0;
		}

		public void setSeparation(double arg0) {
		}

		public double getSeparation() {
			return 0;
		}

		public void setMute(int arg0, boolean arg1) {
		}

		public boolean isMute(int arg0) {
			return false;
		}
		
	}
	
	public static void main(String[] args) throws Exception
	{
		//File file = new File("S:\\Java\\eclipse\\workspace\\jmod\\www\\testdata\\moscow.mod");
		//File file = new File("C:\\Documents and Settings\\kalli\\My Documents\\spacedeb.mod");
		File file = new File("S:\\Java\\eclipse\\workspace\\jmod\\www\\testdata\\gbcoll.mod");
		
		//hardwired.xm,moscow.mod,odyssey1.mod
		
		ModuleLoader modloader = ModuleLoader.getModuleLoader(file);
		Module module = modloader.getModule();
		System.out.println("Init BPM: " + module.getInitialBpm());
		/*
		System.out.println();
		System.out.println("Instruments");
		System.out.println("===========");
		
		Instrument[] instruments = module.getInstruments();
		for (int i = 0; i < instruments.length; i++) {
			Instrument ins = instruments[i];
			System.out.println("name: " + ins.getName());
			System.out.println("samples: " + ins.getNumberOfSamples());
			System.out.println();
			
			System.out.println("sample.length: " + ins.getSampleByNum(0).getLength());
			System.out.println("sample.note: " + ins.getSampleByNum(0).getRelativeNote());
			System.out.println("sample.tune: " + ins.getSampleByNum(0).getFineTune());
		}
		
		System.out.println();
		System.out.println("Orderlist");
		System.out.println("=========");
		for (int i = 0; i < module.getNumberOfPositions(); i++) {
			System.out.println("pattern" + module.getPatternIndexAtPos(i));
		}
		
		System.out.println();
		System.out.println("Pattern0");
		System.out.println("========");
		Pattern pattern = module.getPattern(0);		
		System.out.println("tracks: " + pattern.getTrackCount());
		for (int i = 0; i < pattern.getDivisions(); i++) {
			System.out.print(i + "# ");
			for (int j = 0; j < pattern.getTrackCount(); j++) {
				System.out.print(pattern.getTrack(j).getInfo(i)+ " ");
				//System.out.print(pattern.getTrack(j).getInstrumentNumber(i)+ " ");
			}
			System.out.println();
		}
		*/
			
		FRMixer mixer = new FRMixer(module.getTrackCount());
		ModuleState ms = new ModuleState(module, mixer);
		
		
		TrackState[] ts = null;
		// trackStates is private variable, so we just cheat a little
		// to access it
		Field field;
		try {
			field = ms.getClass().getDeclaredField("trackStates");
			
			field.setAccessible(true);
			ts = (TrackState[])field.get(ms);
		} catch (Exception e1) {
			e1.printStackTrace();
		}  		
		
        try {
        	System.out.println(ms.getPosition() + ":" + ms.getDivision() + ":" + ms.getTick() + " first");
        	boolean engage = false;
        	boolean ok = true;
        	int last_position = ms.getPosition();
        	
        	TreeSet<String> loop_detection = new TreeSet<String>();
        	
        	String pos_id = ms.getPosition() + ":" + ms.getDivision();
        	loop_detection.add(pos_id);
        	
        	int beat = 0;
        	
			while(ok)
			{		
				
				int tick = ms.getTick();
				
				if(last_position != ms.getPosition())
				{
					System.out.println();
					System.out.println();
				}
				last_position = ms.getPosition();
				
				ok = ms.play();
				
				
				if(tick == 0)
				{
					
				// In midi , let 4 divison be a beat, where one divison is a quarter note
				// Let's calculate BPM from that, lastplaytime is in msec
					
				// beat/ms   ms * 1000 = sec
				// 
				double bpm = 60000.0 / (mixer.lastplaytime*ms.getTicksInDivision()*4.0);
				//System.out.print((mixer.lastplaytime*ms.getTicksInDivision()) + " ");
				System.out.print((((int)bpm)+ "bpm " + (beat/4.0) + "       ").substring(0, 16));
				System.out.print((ms.getPosition() + ":" + ms.getDivision() + ":" + tick+ "             ").substring(0, 10));
				System.out.print("  ");
				for (int i = 0; i < mixer.tracks.length; i++) {
					
					double baserate = 0;
					Sample sample = ts[i].getSample();
					if(sample != null)
					if(ts[i].getNote() != Instrument.NO_NOTE)
					{
			            baserate = sample.getUnits().note2rate(ts[i].getNote() + sample.getFineTune() + sample.getRelativeNote());
					}
					
					if(baserate == 0 || !mixer.tracks[i].active)
					{
						System.out.print(("                               ").substring(0, 10));
					}
					else
					{					
					if(!mixer.tracks[i].newNoteEvent)
						System.out.print("  -- -" + ("-   ").substring(0, 2));
					else
						System.out.print("  " + ts[i].getNote() + ":i" + (ts[i].getInstrument()+"  ").substring(0, 2));
						
					System.out.print(
							(
									
									(" p"+mixer.tracks[i].rate/baserate).substring(0,5)  + 								
									(" v"+mixer.tracks[i].volume).substring(0,5) +
									(" pn"+mixer.tracks[i].panning).substring(0,6) +
									
							"              ").substring(0, 18));
										
					}
					
					
					
					
				}
				System.out.println();
				
				beat += 1;
				}
								
				
				
				if(ms.getTick() == 0)
				{
					pos_id = ms.getPosition() + ":" + ms.getDivision();
					if(loop_detection.contains(pos_id))
					{
						System.out.println("LOOP detected, break");
						break;
					}
					loop_detection.add(pos_id);
				}
				
				 				
				
				/*
				
				if(!engage)
				{
					
				if(first_position != ms.getPosition())
				{
					engage = true;
				}
				
				}
				else
				{

	        	if(first_position == ms.getPosition())
	        	{
	        		System.out.println("We are looping");
	        		break;
	        	}
	        	
				}
	        	*/
				
			}
		} catch (PlayerException e) {
			System.out.println("error in player");
			e.printStackTrace();
		}		
		
	}
}

