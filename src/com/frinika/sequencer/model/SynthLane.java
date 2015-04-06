/*
 * Created on Mar 2, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

import com.frinika.audio.toot.AudioPeakMonitor;
import static com.frinika.localization.CurrentLocale.getMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioBuffer.MetaInfo;
import uk.org.toot.audio.mixer.MixControls;
import uk.org.toot.audio.server.AudioServer;


import com.frinika.project.FrinikaAudioSystem;
import com.frinika.global.FrinikaConfig;
import com.frinika.project.MidiDeviceDescriptor;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.gui.mixer.MidiDeviceIconProvider;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.audio.io.AudioWriter;

public class SynthLane extends Lane implements RecordableLane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	MidiDeviceDescriptor midiDeviceDescriptor = null;

	private boolean isRendered = false;

	// transient private SynthWrapper synthWrapper;

	transient AudioProcess audioSynthProcess; // audio input

	transient AudioProcess audioProcess;

	transient AudioPeakMonitor peakMonitor;

	transient File clipFile = null;

	static int stripNo = 0;

	transient private MixControls mixerControls = null;

	transient private boolean isRendering = false;

	transient boolean isInstalled = false;

	public SynthLane() {

	}

	public SynthLane(ProjectContainer project, MidiDeviceDescriptor desc) {
		super(null, project);
		install(desc);
		channelLabel = new MetaInfo(getName());
	}

	public void attachAudioProcessToMixer() {

	//	System .out.println(" ATTACHING TO MIXER ");
		peakMonitor = new AudioPeakMonitor();

		audioProcess = new AudioProcess() {
			public void close() {
			}

			public void open() {
			}

			public int processAudio(AudioBuffer buffer) {
				if (!isInstalled)
					return AUDIO_OK;
				// audioSynthProcess.processAudio(buffer);
				if (!isRendered)
					audioSynthProcess.processAudio(buffer);
				else {
					buffer.makeSilence();
					if (!parts.isEmpty() && parts.get(0) != null) {
						((AudioPart) (parts.get(0))).getAudioProcess()
								.processAudio(buffer);
					}
				}

				peakMonitor.processAudio(buffer);

				buffer.setMetaInfo(channelLabel);
				return AUDIO_OK;
			}
		};

		try {
			mixerControls = project.addMixerInput(audioProcess, (stripNo++)
					+ "X");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.frinika.sequencer.model.Lane#getName() @
	 */
	public String getName() {
		return midiDeviceDescriptor.getProjectName();
		// return "SYnthLane"; // TODO synthWrapper.getName();
	}

	public void setName(String name) {
		if (name != null && midiDeviceDescriptor != null) // evasive null
			// pointer if
			midiDeviceDescriptor.setProjectName(name);

		ProjectFrame frame = ProjectFrame.findFrame(project);
		if (frame != null)
			frame.getMidiDevicesPanel().updateDeviceTabs();

		channelLabel = new MetaInfo(name);

	}

	public void removeFromModel() {

		ProjectFrame frame = ProjectFrame.findFrame(project);
		if (frame != null) {
			frame.getMidiDevicesPanel().remove(
					getMidiDescriptor().getMidiDevice());
			frame.getMidiDevicesPanel().updateDeviceTabs();
		}

		super.removeFromModel();
	}

	public Selectable deepCopy(Selectable parent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deepMove(long tick) {
		// TODO Auto-generated method stub

	}

	public void restoreFromClone(EditHistoryRecordable object) {
		// TODO Auto-generated method stub

	}

	public boolean install(MidiDeviceDescriptor desc) {

		if (midiDeviceDescriptor != null && midiDeviceDescriptor != desc) {
			try {
				throw new Exception(
						" Synthlane already attached to a different device ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;

			}
		}

		midiDeviceDescriptor = desc;

		if (midiDeviceDescriptor.getMidiDevice() instanceof SynthWrapper) {

			audioSynthProcess = ((SynthWrapper) midiDeviceDescriptor
					.getMidiDevice()).getAudioProcess();

		} else {
			// System.err.println(" Whatever TODO");
			// audioSynthProcess = new AudioProcess() {
			//
			// public void close() {
			// // TODO Auto-generated method stub
			//					
			// }
			//
			// public void open() {
			// // TODO Auto-generated method stub
			//					
			// }
			//
			// public int processAudio(AudioBuffer buffer) {
			// System. out.println(" ... "); // TODO Auto-generated method stub
			// return AUDIO_OK;
			// }
			//				
			// };

			try {
				throw new Exception(" SynthLane can not attach to "
						+ midiDeviceDescriptor.getMidiDevice());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		isInstalled = true;

		if (!parts.isEmpty())
			try {
				parts.get(0).onLoad();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return true;
	}

	private File getAudioFile() {

		if (clipFile != null)
			return clipFile;
		if (!parts.isEmpty()) {
			AudioPart part = (AudioPart) parts.get(0);
			clipFile = part.getAudioFile();
			return clipFile;
		}

		ProjectContainer proj = getProject();
		File audioDir = proj.getAudioDirectory();
		String audioFileName = getName() + ".wav";
		clipFile = new File(audioDir, audioFileName);
		int cnt = 1;
		while (clipFile.exists()) {
			audioFileName = getName() + "_" + (cnt++) + ".wav";
			clipFile = new File(audioDir, audioFileName);
		}
		return clipFile;
	}

	private void setAudioFile(File clipFile, double startTimeInMicros) {

		if (!parts.isEmpty()) {
			remove(parts.get(0));
		}
		if (clipFile != null) {
			AudioPart newPart;
			try {
				newPart = new AudioPart(this, clipFile, startTimeInMicros);
				newPart.onLoad();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		project.getEditHistoryContainer().notifyEditHistoryListeners();
	}

	public double getMonitorValue() {
		if (peakMonitor == null)
			return 0;
		return peakMonitor.getPeak();
	}

	public void setMute(boolean b) {
		if (mixerControls == null)
			return;
		mixerControls.getMuteControl().setValue(b);
	}

	public void setSolo(boolean b) {
		if (mixerControls == null)
			return;
		mixerControls.getSoloControl().setValue(b);
	}

	public boolean isRecording() {
		if (mixerControls == null)
			return false;
		return isRendered;
	}

	public boolean isSynthesizer() {
		return mixerControls != null;
	}

	/**
	 * Steal the recording logic (recording == rendered)
	 */
	public void setRecording(boolean b) {
		if (mixerControls == null)
			return;

		if (isRendering)
			return;
		if (b == isRendered)
			return;

		if (b) {
			isRendering = true;
			SynthRenderer renderer = new SynthRenderer();
			Thread t = new Thread(renderer);
			t.start();
		} else {
			isRendered = false;
			setAudioFile(null, 0);
		}
	}

	public MixControls getMixerControls() {
		return mixerControls;
	}

	public boolean isMute() {
		if (mixerControls == null)
			return false;
		return mixerControls.getMuteControl().getValue();
	}

	public boolean isSolo() {
		if (mixerControls == null)
			return false;
		return mixerControls.getSoloControl().getValue();
	}

	class SynthRenderer implements Runnable {

		/**
		 * 
		 */

		private static final long serialVersionUID = 1L;

		FrinikaSequencer sequencer;

		FrinikaSequence sequence;

		JProgressBar bar;

		JFrame frame;

		double sampleRate;

		long currentPos;

		long startTick;

		long stopTick;

		public void run() {

			sequencer = project.getSequencer();
			sequence = project.getSequence();
			int ticksPerbeat=sequence.getResolution();
			currentPos = sequencer.getTickPosition();
			startTick = sequencer.getLoopStartPoint();
			stopTick = sequencer.getLoopEndPoint();

			frame = new JFrame();
			bar = new JProgressBar(0, (int) (stopTick - startTick));
			frame.setContentPane(bar);
			frame.pack();
			frame.setVisible(true);

			project.getEditHistoryContainer().mark(
					getMessage("sequencer.project.render_synth"));
			sampleRate = FrinikaConfig.sampleRate;

			double samplesPerTick = samplesPerTick();

			int samplesPerFrame = 128;

			AudioFormat format = new AudioFormat(FrinikaConfig.sampleRate, 16,
					2, true, false);

			AudioServer server = FrinikaAudioSystem.getAudioServer();

			File clipFile = getAudioFile();

			AudioWriter writer;
			try {
				writer = new AudioWriter(clipFile, format);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (frame != null)
					frame.dispose();
				return;
			}

			final AudioBuffer buffer = new AudioBuffer("renderbuf", 2,
					samplesPerFrame, (float) sampleRate);

			samplesPerFrame = buffer.getSampleCount();
			sequencer.setTickPosition(startTick);

			// Stop the server calling the mixer.

			project.getAudioServer().stealAudioServer(this, null);
			server.stop();

			sequencer.setRealtime(false);
			sequencer.start();

			double samplesRendered = 0.0;
			double samplesTarget = 0.0;

			do {
				sequencer.nonRealtimeNextTick();
				samplesTarget += samplesPerTick();  // samplesPerTick;
				while (samplesRendered < samplesTarget) {
					audioSynthProcess.processAudio(buffer);
					writer.processAudio(buffer);
					samplesRendered += samplesPerFrame;
				}
				bar.setValue((int) (sequencer.getTickPosition() - startTick));
			} while (sequencer.getTickPosition() < stopTick);

			sequencer.stop();
			sequencer.setRealtime(true);
			sequencer.setTickPosition(currentPos);
			writer.close();

			double startTime=project.getTempoList().getTimeAtTick(startTick);
			
//			setAudioFile(clipFile, (long) (startTick * 1000000.0
//					* samplesPerTick / sampleRate));
			
			setAudioFile(clipFile, startTime*1000000.0);

			project.getEditHistoryContainer().notifyEditHistoryListeners();
			server.start();
			project.getAudioServer().returnAudioServer(this);
			isRendering = false;
			isRendered = true;

			if (frame != null)
				frame.dispose();

		}

		double samplesPerTick() {

			double ticksPerSecond = (sequence.getResolution() * sequencer
					.getTempoInBPM()) / 60.0;
			double seconds = 1.0 / ticksPerSecond;
			return (seconds * sampleRate);
		}
	}

	public MidiDeviceDescriptor getMidiDescriptor() {

		return midiDeviceDescriptor;
	}
	
	@Override
	public Part createPart() {
		try {
			throw new Exception(" Attempt to create an SynthlanePart");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Icon getIcon() {
		return ((MidiDeviceIconProvider) getMidiDescriptor().getMidiDevice()).getIcon();
	}
}