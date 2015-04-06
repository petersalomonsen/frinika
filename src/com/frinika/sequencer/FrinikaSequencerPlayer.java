/*
 * Created on Jul 3, 2005
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

/*
 * PJL 27/1/06  Fixed null exception BUG in teimerEvent.
 * 
 */
package com.frinika.sequencer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import com.frinika.priority.Priority;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.MidiPlayOptions;
import com.frinika.sequencer.model.Quantization;
import com.frinika.sequencer.model.tempo.TempoList;
import com.frinika.sequencer.model.tempo.TempoList.MyTempoEvent;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

public class FrinikaSequencerPlayer implements Runnable {
	FrinikaSequencer sequencer;

	private boolean running;

	private boolean finished = true;

	/**
	 * Indicating realtime or rendered (export wav)
	 */
	private boolean realtime = true;

	// Instead of flushing NoteOff cache locally, setting this variable to true
	// will cause the player to do it - thus preventing concurrent modification
	// exceptions
	private boolean tickPositionChanged = false;

	private long startTimeMillis;

        /**
         * Previous sequencer tick position
         */
	private long lastTickPosition;

        /**
         * Sequencer tick position when playback was started
         */
	private long startTickPosition;

        /**
         * Sequencer tick position
         */
	private long tickPosition;
	private long ticksLooped;   // how many ticks have been lost by looping
	
	private int loopCount;

	private NoteOnCache noteOnCache = new NoteOnCache();

	Thread playThread;

	SongPositionNotifier songPositionNotifier;

	TempoList tempoList;

	private long timeAtStart;

	// FrinikaSequence sequence;

	private long startTimeNanos;

	// PJL
	int priorityRequested = 0;

	int priority = 0;

	private HashMap<FrinikaTrackWrapper, QuantizeBuffer> quantizeBuffersByTrack = new HashMap<FrinikaTrackWrapper, QuantizeBuffer>();

	public FrinikaSequencerPlayer(FrinikaSequencer sequencer) {
		this.sequencer = sequencer;
		this.songPositionNotifier = new SongPositionNotifier();
		Thread t = new Thread(songPositionNotifier);
		t.start();
		startTimeMillis = System.currentTimeMillis();
		startTimeNanos = System.nanoTime();
		tickPosition = startTickPosition = 0;
		// sequence = (FrinikaSequence) sequencer.getSequence();
	}

	void timerEvent() {
		FrinikaSequence sequence = (FrinikaSequence) sequencer.getSequence();
		if (sequence.getDivisionType() == FrinikaSequence.PPQ) {
                        /**
                         * CurrentTick is the time in ticks - regardless of looping - since the playback was started
                         * WARNING (PJS): This is not the same as the sequencer tick position
                         */
                        long currentTick;

			/**
			 * This is a check whether to use real time or rendered (export wav)
                         * 
                         * Calculate the time in ticks since playback was started
			 */
			
            double ticksPerMilli=0.0;            
            if (realtime) {
                ticksPerMilli=sequence
								.getResolution() * (sequencer.getTempoInBPM() / 60000);
				currentTick = startTickPosition
						+ (long) ((System.currentTimeMillis() - startTimeMillis) * ticksPerMilli) ;
            }
			else
				currentTick = lastTickPosition;

			/**
			 * Note that this loop will always try to catch up if any ticks were
			 * missing.
			 */
			for (long playTick = lastTickPosition; playTick <= currentTick; playTick++) {

				if (currentTick >= sequencer.getLoopStartPoint()
						// Do not loop more than number of loops specified
						&& (loopCount < sequencer.getLoopCount() || sequencer
								.getLoopCount() == Sequencer.LOOP_CONTINUOUSLY)
						&& startTickPosition < sequencer.getLoopEndPoint()) {
					// Calculate real play tick regarding loop settings
					tickPosition = ((playTick - sequencer.getLoopStartPoint()) % (sequencer
							.getLoopEndPoint() - sequencer.getLoopStartPoint()))
							+ sequencer.getLoopStartPoint();
	
				} else {
					tickPosition = playTick;
				}

				// Detect loop point and increase counter;
				if (tickPosition == (sequencer.getLoopEndPoint() - 1)
				// Do not increase loop counter more
						// than number of loops specified
						&& (loopCount < sequencer.getLoopCount() || sequencer
								.getLoopCount() == Sequencer.LOOP_CONTINUOUSLY)) {
					loopCount++;
					ticksLooped += sequencer.getLoopEndPoint()
							- sequencer.getLoopStartPoint();
				}
				// If we're starting a new loop or moving tickPosition, then
				// stop hanging notes and rechase controllers
				if ((loopCount > 0 && tickPosition == (sequencer
						.getLoopStartPoint()))
						|| tickPositionChanged) {
					noteOnCache.releasePendingNoteOffs();
					chaseControllers();
					quantizeBuffersByTrack.clear(); // will force to rebuffer
					// on-the-fly-quantization,
					// if active
					tickPositionChanged = false;
				}

				Collection<FrinikaTrackWrapper> ftws;
				if (sequencer.getSoloFrinikaTrackWrappers().size() > 0)
					ftws = sequencer.getSoloFrinikaTrackWrappers();
				else
					ftws = sequence.getFrinikaTrackWrappers();

				boolean first = true;

				
				for (FrinikaTrackWrapper trackWrapper : ftws) {
				
					if (first) { // first track is tempo track
						Vector<MidiEvent> events = trackWrapper
								.getEventsForTick(tickPosition);

						if (events != null)
							handleTempoEvents(events); 
						first = false;
						continue;
					}
					
					
					MidiPlayOptions opt = sequencer
							.getPlayOptions(trackWrapper);
					// Don't play muted and tracks with no midi device
					if (trackWrapper.getMidiDevice() != null && !opt.muted) {


						try {

                            SynthWrapper sw=((SynthWrapper)trackWrapper.getMidiDevice());
                            // long latency=sw.getLatency();
                            // System.out.println("LATENCIES "+latency+" "+latencyCompMillis+sw);

                          

							long t = tickPosition + opt.shiftedTicks;

                            if (!(sw.getRealDevice() instanceof Synthesizer)) {
                              //  System.out.println(" Adjusting tick for real device");
                                // we are using a real device so no latency due to audio buffer
                                // adjust (delay) tick to compensate
                                // (We need to fix  all the SYnths getLatency ... this is a mess )
                                t= (long) (t - ticksPerMilli * latencyCompMillis);
                            }

                            // if it's a looped track, we may need to 'fake' an
							// earlier tickPosition for this track
							if (opt.looped) {
								// TODO: chase controllers
								t = getLoopedTick(t, trackWrapper, opt);
                                                                
							}

							Vector<MidiEvent> events;

							// on-the-fly quantization?
							if (opt.quantizationActive) {

								int d = (int) (t % opt.quantization.interval);
								long bufferStart = t - d;
								QuantizeBuffer quantizeBuffer = quantizeBuffersByTrack
										.get(trackWrapper);
								if ((quantizeBuffer != null)
										&& (quantizeBuffer.startTick == (bufferStart - quantizeBuffer.data.length))) { // just
									// reached
									// the
									// next
									// slice
									// of
									// buffer:
									// "rotate
									// over",
									// reuse
									// existing
									// buffer
									// but
									// adjust
									// start
									// tick
									quantizeBuffer.startTick = bufferStart; // equiv.:
									// quantizeBuffer.startTick
									// +=
									// quantizeBuffer.data.length
								} // no 'else', to catch the last or-branch of
								// the following:
								if ((quantizeBuffer == null)
										|| (quantizeBuffer.startTick != bufferStart)
										|| (quantizeBuffer.data.length != opt.quantization.interval)) {
									quantizeBuffer = new QuantizeBuffer(
											opt.quantization.interval,
											bufferStart);
									quantizeBuffersByTrack.put(trackWrapper,
											quantizeBuffer);
									rebufferQuantization(trackWrapper, t,
											opt.quantization, quantizeBuffer);
								}
								int quantizeLookahead = opt.quantization.interval / 2;
								Vector<MidiEvent> eventsToBeQuantized = trackWrapper
										.getEventsForTick(t + quantizeLookahead);
								if (eventsToBeQuantized != null) {
									quantizeOnTheFly(eventsToBeQuantized,
											opt.quantization, quantizeBuffer);
								}
								events = quantizeBuffer.data[d]; // actual
								// events to
								// be played
								// now (had
								// been
								// buffered
								// before)
								quantizeBuffer.data[d] = null; // invalidate
								// (may already
								// be filled
								// with new data
								// while still
								// in this
								// buffer slice,
								// but near the
								// end
								// ("rotating"
								// buffer))

							} else { // normal playing
								// Now find all midi messages for the given tick
								events = trackWrapper.getEventsForTick(t);
							}

							if (events == null)
								continue;
							for (MidiEvent evt : events) {
								// Handle tempomessages seperately
								byte[] msgBytes = evt.getMessage().getMessage();
								if (msgBytes[0] == -1 && msgBytes[1] == 0x51
										&& msgBytes[2] == 3) {
								
									System.err.println(" tempo event found on a track pther then the first ");
									
								} else { // 'normal' event

									if (!opt.preRenderedUsed) { // Skip if track
										// is
										// pre-rendered

										if ((msgBytes.length > 2)
												&& (((msgBytes[0] & 0xf0) == ShortMessage.NOTE_OFF || (msgBytes[0] & 0xf0) == ShortMessage.NOTE_ON) && (opt.drumMapped
														|| (opt.transpose != 0)
														|| (opt.velocityOffset != 0) || (opt.velocityCompression != 0.0f)))) {
											// need to do some on-the-fly
											// modifications of the event
											int note = msgBytes[1];
											note = applyPlayOptionsNote(opt,
													note);

											int vel = msgBytes[2];
											vel = applyPlayOptionsVelocity(opt,
													vel);

											ShortMessage shm = new ShortMessage();
											shm.setMessage(msgBytes[0], note,
													vel);
											sendMidiMessage(shm, trackWrapper);
										} else { // normal send
											sendMidiMessage(evt.getMessage(),
													trackWrapper);
										}

									}
								}
							}
						} catch (Exception e) {
						}
					}
				}

				// Notify listeners that requires notification on each tick
				sequencer.notifyIntenseSongPositionListeners(tickPosition);
				// Otherwise give the job to another thread.
				songPositionNotifier.setNextTick(tickPosition);
			}

			lastTickPosition = currentTick + 1;
		}
	}

	private void sendMessageToAll(MidiMessage msg) {

		for (FrinikaTrackWrapper trackWrapper : ((FrinikaSequence) sequencer
				.getSequence()).getFrinikaTrackWrappers()) {
			if (trackWrapper.midiDevice != null) {
				try {
					trackWrapper.getMidiDevice().getReceiver().send(msg, -1);
				} catch (MidiUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	private void handleTempoEvents(Vector<MidiEvent> events) {
		for (MidiEvent evt : events) {
			// Handle tempomessages seperately
			byte[] msgBytes = evt.getMessage().getMessage();
			if (msgBytes[0] == -1 && msgBytes[1] == 0x51 && msgBytes[2] == 3) 
                        {
                            int mpq = ((msgBytes[3] & 0xff) << 16)
						| ((msgBytes[4] & 0xff) << 8) | (msgBytes[5] & 0xff);

                            // PJL removed cast to int
                            sequencer.setTempoInBPM((60000000f / mpq));

                            // Send tempo messages to all MidiDevice we
                            // send a message
                            sendMessageToAll(evt.getMessage());

                            if (realtime) {
                                    /**
                                     * (PJS) Since the sequencer player calculates the time in ticks based on the
                                     * startTickPosition and the tempo, the startTickPosition has to be altered
                                     * to be the same as the tick position when the tempo change occured.
                                     */
                                    startTickPosition = sequencer.getTickPosition();
                                    startTimeMillis = System.currentTimeMillis();
                            }
			}
		}
	}

	static int applyPlayOptionsNote(MidiPlayOptions opt, int note) {

		if (opt.drumMapped) {
			note = opt.noteMap[note];
		} else if (opt.transpose != 0) {
			note += opt.transpose;
			if (note < 0) {
				note = 0;
			} else if (note > 127) {
				note = 127;
			}
		}
		return note;
	}

	static int applyPlayOptionsVelocity(MidiPlayOptions opt, int vel) {
		if (vel != 0) {
			if (opt.velocityCompression != 0.0f) {
				float diff = (64 - vel) * opt.velocityCompression;
				vel += diff;
			}
			if (opt.velocityOffset != 0) {
				vel += opt.velocityOffset;
				if (vel < 1) {
					vel = 1;
				} else if (vel > 127) {
					vel = 127;
				}
			}
		}
		return vel;
	}

	private static void quantizeOnTheFly(Vector<MidiEvent> eventsToBeQuantized,
			Quantization q, QuantizeBuffer quantizeBuffer) {
		for (MidiEvent event : eventsToBeQuantized) {
			event = q.quantize(event);
			long tick = event.getTick();
			long d = tick - quantizeBuffer.startTick;
			if (d < 0) {
				d = 0; // cannot do better
			} else {
				d %= quantizeBuffer.data.length; // if the data is cached at
				// the beginning of the
				// buffer (thus reaches over
				// the end of the current
				// buffer), this part of the
				// buffer that has already
				// been played and will be
				// reused later, "rotating"
			}
			Vector<MidiEvent> v = quantizeBuffer.data[(int) d];
			if (v == null) {
				v = new Vector<MidiEvent>();
				quantizeBuffer.data[(int) d] = v;
			}
			v.add(event);
		}
	}

	private static void rebufferQuantization(FrinikaTrackWrapper trackWrapper,
			long t, Quantization q, QuantizeBuffer quantizeBuffer) {
		int lookahead = q.interval / 2;
		long from = t - lookahead + 1;
		if (from < 0) { // case when start playing at very begnning
			from = 0;
		}
		long to = t + lookahead;
		for (long tick = from; tick < to; tick++) {
			Vector<MidiEvent> events = trackWrapper.getEventsForTick(tick);
			if (events != null) {
				quantizeOnTheFly(events, q, quantizeBuffer);
			}
		}
	}

	/**
	 * 
	 * Set the tempoList to get the tempo when the time is warped.
	 * 
	 * @param tempoList
	 */
	public void setTempoList(TempoList tempoList) {
//		System.out
//				.println("************************************************  TempoList has been set "
//						+ tempoList);
		this.tempoList = tempoList;
	}

	/*
	 * private static String bytesout(byte[] b) { StringBuffer sb = new
	 * StringBuffer("["); for (int i = 0; i < b.length; i++) {
	 * //sb.append(hexDigit(b[i]>>4)+hexDigit(b[i]&0xf)+", ");
	 * sb.append(Integer.toHexString(b[i])+", "); } sb.append("]"); return
	 * sb.toString(); }
	 */

	private final void chaseControllers() {

	//	System.out.println(" Chase controllers ");
		if (tempoList != null) {
			MyTempoEvent evt = tempoList.getTempoEventAt(tickPosition);
			float bpm = (float) evt.getBPM();
			sequencer.setTempoInBPM(bpm);
			sendMessageToAll(evt.getTempoEvent().getMessage());
		}

		for (FrinikaTrackWrapper trackWrapper : ((FrinikaSequence) sequencer
				.getSequence()).getFrinikaTrackWrappers()) {
			if (trackWrapper.midiDevice != null) {
				for (MidiMessage msg : trackWrapper
						.getControllerStateAtTick(tickPosition)) {
					try {
						sendMidiMessage(msg, trackWrapper);
					} catch (InvalidMidiDataException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					} catch (MidiUnavailableException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Takes into account that a track might be set to "loop-mode", meaning that
	 * the very last part of it will be repeated endlessly until the song ends.
	 * (This has nothing to do with the overall loop-mode from a start-point to
	 * an end-point. "Loop-mode" of a track is set together with "solo"/ "mute"
	 * settings.)
	 * 
	 * @param tick
	 * @return adopted tick time for the corresponding track, "faking" the last
	 *         part is still playing
	 * @author Jens Gulden
	 */
	private long getLoopedTick(long tick, FrinikaTrackWrapper trackWrapper,
			MidiPlayOptions opt) {
		int s = trackWrapper.track.size();
		if (s <= 1)
			return tick;
		long end = trackWrapper.lastTickUsed();
                // PJS: Changed from tick > end to tick>=end so that first tick of loop isn't skipped, also removed t=t-1
		if (tick >= end) { // yes, loop
			long t = end - opt.loopedTicks + ((tick - end) % opt.loopedTicks);
			return t; // "faked" tick position before/insed last midi part of
			// track
		} else { // end of track not yet reached: normal operation
			return tick;
		}
	}

	/*
	 * public long lastTickUsed() { // TODO: could already cache this in
	 * contructor, right? int s = track.size(); if (s > 1) { MidiEvent
	 * lastMidiEvent = track.get( s - 2 ); long tick = lastMidiEvent.getTick();
	 * return tick; } else { return 0; } }
	 */

	public void start() {
		// Ensure that there is no other running thread
		running = false;
		while (!finished)
			Thread.yield();

		running = true;
		loopCount = 0;
		ticksLooped=0;
		startTimeMillis = System.currentTimeMillis();
		startTimeNanos = System.nanoTime();
		tickPosition = startTickPosition;
		FrinikaSequence sequence = (FrinikaSequence) sequencer.getSequence();
		timeAtStart = (long) (1000 * startTickPosition / (sequence
				.getResolution() * (sequencer.getTempoInBPM() / 60000)));
		chaseControllers();

		// If not real time the timerEvents has to be sent manually
		if (realtime) {
			playThread = new Thread(this);
			// playThread.setPriority(Thread.MAX_PRIORITY-1);
			playThread.start();
		}
	}

	/**
	 * Returns the current time relative to the time at tick=0
	 * 
	 * @return
	 */

	transient long microSecondCache;

	transient long tickPosCache;

	private double latencyCompMillis;

	public long getMicroSecondPosition() {

		/**
		 * if(running) { // This returns time relative to the play start
		 * position which is wrong long timeSinceStart = (System.nanoTime() -
		 * startTimeNanos)/1000; return timeAtStart+timeSinceStart; } else {
		 */

		if (tickPosition == tickPosCache) // avoid duplicate work
			return microSecondCache;

		tickPosCache = tickPosition;

		microSecondCache = (long) (1000000.0 * tempoList
				.getTimeAtTick(tickPosition));

	//	System.out.println(" getMicroSec =" + microSecondCache);

		return microSecondCache;

		/*
		 * FrinikaSequence sequence = (FrinikaSequence) sequencer.getSequence();
		 * 
		 * 
		 * return (long) ((tickPosition * 60000000) / (sequence.getResolution() *
		 * sequencer .getTempoInBPM()));
		 */
		// }
	}

	public void stop() {
		running = false;
		notesOff(true);
		// noteOnCache.releasePendingNoteOffs();
	}

	ShortMessage createShortMessage(int command, int channel, int data1,
			int data2) throws InvalidMidiDataException {
		ShortMessage shm = new ShortMessage();
		shm.setMessage(command, channel, data1, data2);
		return shm;
	}

	/**
	 * Send note off to mididevices and reset all controllers
	 * 
	 * @param doControllers
	 */
	void notesOff(boolean doControllers) {
		try {
			int done = 0;
			for (int ch = 0; ch < 16; ch++) {
				int channelMask = (1 << ch);
				for (int i = 0; i < 128; i++) {
					sendMidiMessage(createShortMessage(ShortMessage.NOTE_ON,
							ch, i, 0));
					done++;
				}
				/* all notes off */
				sendMidiMessage(createShortMessage(ShortMessage.CONTROL_CHANGE,
						ch, 123, 0));
				/* sustain off */
				sendMidiMessage(createShortMessage(ShortMessage.CONTROL_CHANGE,
						ch, 64, 0));
				if (doControllers) {
					/* reset all controllers */
					sendMidiMessage(createShortMessage(
							ShortMessage.CONTROL_CHANGE, ch, 121, 0));
					done++;
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Send a MidiMessage to using the device and channel specs of a
	 * FrinikaTrackWrapper
	 * 
	 * @param msg
	 * @param trackWrapper
	 * @throws InvalidMidiDataException
	 * @throws MidiUnavailableException
	 */
	final void sendMidiMessage(MidiMessage msg, FrinikaTrackWrapper trackWrapper)
			throws InvalidMidiDataException, MidiUnavailableException {
		if (msg instanceof ShortMessage
				&& trackWrapper.getMidiChannel() != FrinikaTrackWrapper.CHANNEL_FROM_EVENT) {
			ShortMessage smsg = ((ShortMessage) msg);
			smsg.setMessage(smsg.getCommand(), trackWrapper.getMidiChannel(),
					smsg.getData1(), smsg.getData2());
			sendMidiMessage(smsg, trackWrapper.getMidiDevice().getReceiver());
		} else
			sendMidiMessage(msg, trackWrapper.getMidiDevice().getReceiver());
	}

	/**
	 * Send a MidiMessage to one transmitter
	 * 
	 * @param msg
	 * @param trans
	 */
	final void sendMidiMessage(MidiMessage msg, Receiver receiver) {
		noteOnCache.interceptMessage(msg, receiver);
		receiver.send(msg, -1);
	}

	/**
	 * Send MidiMessage to all transmitters
	 * 
	 * @param msg
	 */
	final void sendMidiMessage(MidiMessage msg) {
		for (Transmitter transmitter : sequencer.getTransmitters()) {
			sendMidiMessage(msg, transmitter.getReceiver());
		}
	}

	// Note that this thread should do nothing but sending the timerEvents
	public void run() {
		finished = false;
		priority = 0;
		while (running) {
			if (priorityRequested != priority) {
				System.out.println(" PLayer priority requested "
						+ priorityRequested);
				Priority.setPriorityRR(priorityRequested);
				priority = priorityRequested;
			}
			try {
				Thread.sleep(1);
				// wait(1);
				timerEvent();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		finished = true;
		startTickPosition = tickPosition;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * 
	 * 
	 * @return ticks lost because of looping
	 */
	public long getTicksLooped() {
		return ticksLooped;
	}
	
	
	public long getTickPosition() {
		return tickPosition;
	}

	/**
	 * Return number of loops played
	 * 
	 * @return
	 */
	public int getLoopCount() {
		return loopCount;
	}
	
	/**
	 * The delay due to audio latency which must be subtracted when recording midi events
     *
	 * 
	 * @param latencyCompMillis
	 */

	public void setLatencyCompensationInMillis(double latencyCompMillis) {
		this.latencyCompMillis=latencyCompMillis;	
	}
	
	/**
	 * 
	 * Used for recording to time stamp incoming midi events
	 * @return
	 */
	public long getRealTimeTickPosition() {
		FrinikaSequence sequence = (FrinikaSequence) sequencer.getSequence();
		long currentTick = startTickPosition
				+ (long) ((System.currentTimeMillis()-latencyCompMillis - startTimeMillis) * (sequence
						.getResolution() * (sequencer.getTempoInBPM() / 60000)));

		if (sequencer.getLoopCount() == FrinikaSequencer.LOOP_CONTINUOUSLY)
			currentTick = ((currentTick - sequencer.getLoopStartPoint()) % (sequencer
					.getLoopEndPoint() - sequencer.getLoopStartPoint()))
					+ sequencer.getLoopStartPoint();

		return (currentTick);
	}

	public void setTickPosition(long tickPosition) {
		startTickPosition = tickPosition;
		startTimeMillis = System.currentTimeMillis();
		startTimeNanos = System.nanoTime();
		this.lastTickPosition = tickPosition;
		this.tickPosition = tickPosition;

		if (running) {
			tickPositionChanged = true;
		}

		sequencer.notifyAllSongPositionListeners(tickPosition);
	}

	class SongPositionNotifier implements Runnable {

		long tickLast = -1;

		long tickNext = -1;

		long intervalInMillis = 50;

		public void run() {
			while (true) {
				try {
					Thread.sleep(intervalInMillis);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (tickLast != tickNext) {

					sequencer.notifySongPositionListeners(tickNext);
					// TODO sequencer.notifyGUISongPositionListeners(tickNext);
					tickLast = tickNext;
				}
			}
		}

		void setNextTick(long tick) {
			// TODO sequencer.notifyRealTimePositionListeners(tick);
			tickNext = tick;
		}

	}

	/**
	 * Set whether to play in realtime or if rendering (e.g. export wav)
	 * 
	 * @param realtime
	 */
	void setRealtime(boolean realtime) {
		this.realtime = realtime;
	}

	/**
	 * Returns whether to play in realtime or if rendering (e.g. export wav)
	 */
	boolean getRealtime() {
		return realtime;
	}

	// --- inner class ---

	private class QuantizeBuffer {

		long startTick;

		Vector<MidiEvent>[] data;

		public QuantizeBuffer(int bufferSize, long startTick) {
			this.startTick = startTick;
			this.data = new Vector[bufferSize];
		}

	}
}
