/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
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
package com.frinika.sequencer.model;

import com.frinika.audio.toot.AudioPeakMonitor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.sound.sampled.AudioFormat;
import javax.swing.Icon;

import rasmus.midi.provider.RasmusSynthesizer;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.mixer.MixControls;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.IOAudioProcess;


import com.frinika.project.FrinikaAudioSystem;
import com.frinika.global.FrinikaConfig;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.SequencerListener;
import com.frinika.audio.io.AudioWriter;
import static com.frinika.localization.CurrentLocale.getMessage;

public class AudioLane extends Lane implements RecordableLane,
		SequencerListener {

	transient AudioProcess audioInProcess = null; // audio input
	protected transient AudioProcess audioInsert = null;
	static Icon icon = new javax.swing.ImageIcon(RasmusSynthesizer.class
			.getResource("/icons/audiolane.png"));

	// THIS IS TEMPORARY - have to be able to add mixerslots dynamically
	// we still need a unique id. But maybe move this up to Lane ?
	public static int stripNo = 1;

	/**
	 * Audio Process to be connected to the project mixer
	 */
	transient AudioProcess audioProcess;

	transient AudioPeakMonitor peakMonitor;

	transient boolean armed = false; // armed for recording

	transient boolean isRecording = false; // is sequencer running && armed //
											// recording

	transient boolean hasRecorded = false; // true if any data has been saved

	transient AudioWriter writer = null; // direct to disk writer

	transient private long recordStartTimeInMicros;

	transient private FrinikaSequencer sequencer;

	transient private MixControls mixerControls = null;
	transient int stripInt=-1;
	
	private static final long serialVersionUID = 1L;
	protected transient File clipFile;
	
	static int nameCount = 0;

	public AudioLane(ProjectContainer project) {
		super("Audio " + nameCount++, project);
		attachAudioProcessToMixer();
	}

	public void dispose() {
		project.getSequencer().removeSequencerListener(this);
		writer.discard();
	}

	public void removeFromModel() {
		project.removeStrip(stripInt+"");
		super.removeFromModel();
	}
	
	private void attachAudioProcessToMixer() {

		peakMonitor = new AudioPeakMonitor();

		audioProcess = new AudioProcess() {
			public void close() {
			}

			public void open() {
			}

			public int processAudio(AudioBuffer buffer) {
				// Process audio of all parts in this lane
				// do we need to zero the buffer here ?

				if (armed) {
					audioInProcess.processAudio(buffer);
					peakMonitor.processAudio(buffer);
					if (audioInsert != null ) audioInsert.processAudio(buffer);
					if (isRecording) {
						// TODO handle DISCONNECT
						writer.processAudio(buffer);
						hasRecorded = true;
					}
					if (FrinikaConfig.getDirectMonitoring()) {
						buffer.makeSilence();
					}
				} else {
					if (project.getSequencer().isRunning()) {
						buffer.setChannelFormat(ChannelFormat.STEREO);
						buffer.makeSilence();
						for (Part part : getParts()) {
							if (((AudioPart) part).getAudioProcess() != null)
								((AudioPart) part).getAudioProcess()
										.processAudio(buffer);
						}
						peakMonitor.processAudio(buffer);
					} else {
						buffer.makeSilence();
					}
				}

				buffer.setMetaInfo(channelLabel);
				return AUDIO_OK;
			}
		};

		try {
			mixerControls = project.addMixerInput(audioProcess, (stripInt=stripNo++)
					+ "");
			
			// project.getMixer().getStrip((stripNo++) + "").setInputProcess(
			// audioProcess);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sequencer = project.getSequencer();
		sequencer.addSequencerListener(this);
	}

	public void restoreFromClone(EditHistoryRecordable object) {
		System.out.println("AudioLane restroeFromClone");
	}

	public Selectable deepCopy(Selectable parent) {

		return null;
	}

	public void deepMove(long tick) {
		// TODO Auto-generated method stub

	}

	public boolean isRecording() {
		return armed;
	}

	public boolean isMute() {
		return mixerControls.isMute();
	}

	public boolean isSolo() {
		return mixerControls.isSolo();
	}

	public void setRecording(boolean b) {
		if (b && audioInProcess == null) {
			armed = false;
			project.message(getMessage("recording.please_select_audio_input"));
			return;
		}

		armed = b;

	}

	public void setMute(boolean b) {
		mixerControls.getMuteControl().setValue(b);
	}

	public void setSolo(boolean b) {
		mixerControls.getSoloControl().setValue(b);
	}

	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {

		in.defaultReadObject();
		attachAudioProcessToMixer();
	}

	public AudioProcess getAudioInDevice() {
		return audioInProcess;
	}

	public void setAudioInDevice(AudioProcess handle) {
		audioInProcess = handle;
		if (writer != null)
			writer.close();
		writer = newAudioWriter();
	}

	public double getMonitorValue() {
		return peakMonitor.getPeak();
	}

	/**
	 * 
	 * Creates a new audio file handle to save a clip.
	 * 
	 */
	public AudioWriter newAudioWriter() {

		clipFile = newFilename();

		AudioFormat format = new AudioFormat(
				FrinikaConfig.sampleRate,
				16,
				((IOAudioProcess) audioInProcess).getChannelFormat().getCount(),
				true, false);

		try {
			return new AudioWriter(clipFile, format);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public File newFilename() {
		ProjectContainer proj = getProject();
		
		File audioDir = proj.getAudioDirectory();
		String audioFileName = getName() + ".wav";
		File clipFile = new File(audioDir, audioFileName);
		int cnt = 1;
		while (clipFile.exists()) {
			audioFileName = getName() + "_" + (cnt++) + ".wav";
			clipFile = new File(audioDir, audioFileName);
		}
		return clipFile;
	}

	public void beforeStart() {
	}

	public void start() {
		isRecording = project.getSequencer().isRecording();
		if (isRecording) {
			recordStartTimeInMicros = sequencer.getMicrosecondPosition();
		}
	}

	public void stop() {
		isRecording = false;
		if (hasRecorded) {
			project.getEditHistoryContainer().mark(
					getMessage("sequencer.audiolane.record"));

			writer.close();
			hasRecorded = false;
			AudioServer server = project.getAudioServer();
			int latencyInframes = project.getAudioServer().getTotalLatencyFrames();

			System.out.println(" latency in frames is " + latencyInframes);
			double latencyInMicros = latencyInframes * 1000000.0
					/ server.getSampleRate();

			// shift record time back in time because we play along with a delay
			// output.
			recordStartTimeInMicros -= latencyInMicros;
			// TODO Latency compensation
			AudioPart part;
			try {
				part = new AudioPart(this, writer.getFile(),
						recordStartTimeInMicros);
				part.onLoad();
				writer = newAudioWriter();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			
			}
			project.getEditHistoryContainer().notifyEditHistoryListeners();
		}
	}

	public MixControls getMixerControls() {
		return mixerControls;
	}

	/**
	 * 
	 */
	@Override
	public Part createPart() {
		try {
			throw new Exception(" Attempt to create an AudiPart");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}
}
