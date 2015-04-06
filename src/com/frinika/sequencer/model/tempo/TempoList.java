/*
 * Created on May 16, 2007
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

package com.frinika.sequencer.model.tempo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.sound.midi.MidiEvent;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.midi.message.TempoMessage;

/**
 * Maintains a list of tempo changes.
 * 
 * 
 * Tempo changes occur at midi ticks ( an tempo changed is fixed to a tick)
 * 
 * Inserting a new tempo change sets a dirty flag.
 * 
 * If need be the real time of the events are reconstructed if the dirty flag is
 * true.
 * 
 * @author pjl
 * 
 */

public class TempoList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	transient private TreeMap<Long, MyTempoEvent> treeSet;

	private Vector<MyTempoEvent> list; // for the list

	transient private boolean dirty = true;

	private double ticksPerBeat;

	ProjectContainer project;

	transient Vector<TempoListListener> listeners;

	public void addTempoListListener(TempoListListener o) {
		listeners.add(o);
	}

	public void removeTempoListListener(TempoListListener o) {
		listeners.remove(o);
	}

	public void notifyListeners() {
		for (TempoListListener o : listeners)
			o.notifyTempoListChange();
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {

		in.defaultReadObject();

		treeSet = new TreeMap<Long, MyTempoEvent>();

		for (MyTempoEvent e : list) {
			treeSet.put(e.tick, e);
		}
		dirty = true;
		listeners = new Vector<TempoListListener>();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		reco();
		out.defaultWriteObject();
	}

	public TempoList(double ticksPerBeat, ProjectContainer project) {
		this.project = project;
		treeSet = new TreeMap<Long, MyTempoEvent>();
		list = new Vector<MyTempoEvent>();
		this.ticksPerBeat = ticksPerBeat;
		listeners = new Vector<TempoListListener>();
	}

	/**
	 * Add a tempo event
	 * 
	 * @param tick
	 * @param bpm
	 */
	public synchronized void add(long tick, double bpm) {

		if (treeSet.remove(tick) != null)
			System.out.println(" DUPLICATE TIME REMOVED ");

		treeSet.put(tick, new MyTempoEvent(bpm));
		dirty = true;
	}

	/**
	 * 
	 * Remove tempo events between tick1 and tick2
	 * 
	 * @param tick1
	 * @param tick2
	 */
	public synchronized void remove(long tick1, long tick2) {
		treeSet.subMap(tick1, tick2).clear();
		dirty = true;
	}

	public synchronized void reco() {
		if (!dirty)
			return;
		list.clear();
		FrinikaTrackWrapper tempoTrack = null;

		if (project != null) {
			tempoTrack = project.getTempoTrack();
			tempoTrack.clear();
		}

		long tick = 0;
		double bpm = 120;
		double time = 0.0;

		for (Map.Entry<Long, MyTempoEvent> e : treeSet.entrySet()) {
			long deltaTick = e.getKey() - tick;
			time += deltaTick * 60.0 / bpm / ticksPerBeat;
			e.getValue().time = time;
			tick = e.getValue().tick = e.getKey();

			bpm = e.getValue().bpm;
			if (tempoTrack != null) {
				try {
					// System.out.println(" Adding tempo event " + e.getKey()
					// + " " + bpm + " " + tempoTrack);
					TempoMessage tempoMsg = new TempoMessage((float) bpm);
					MidiEvent tempoEvent = new MidiEvent(tempoMsg, e.getKey());
					e.getValue().tempoEvent = tempoEvent;
					tempoTrack.add(tempoEvent);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			list.add(e.getValue());
		}

		dirty = false;
	}

	public class MyTempoEvent implements Serializable {
		transient public MidiEvent tempoEvent;

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private double bpm;

		transient private double time; // reconstructed in a dirty way

		private long tick;

		private MyTempoEvent() {
		}

		MyTempoEvent(double bpm) {
			this.bpm = bpm;
		}

		void display() {
			System.out.print(time + ":" + bpm);
		}

		public long getTick() {
			return tick;
		}

		public double getBPM() {
			return bpm;
		}

		public double getTime() {
			if (dirty)
				reco();
			return time;
		}

		public MidiEvent getTempoEvent() {
			return tempoEvent;
		}

	}

	/**
	 * 
	 * get tempo event before tick.
	 * 
	 * @param tick
	 * @return
	 */
	public synchronized MyTempoEvent getTempoEventAt(long tick) {
		if (dirty)
			reco();
		SortedMap<Long, MyTempoEvent> head = treeSet.headMap(tick + 1);
		MyTempoEvent ev = (head.get(head.lastKey()));
		return ev;
	}

	/**
	 * 
	 * Get tempo at tick.
	 * 
	 * @param tick
	 * @return
	 */
	public synchronized float getTempoAt(long tick) {
		if (dirty)
			reco();
		SortedMap<Long, MyTempoEvent> head = treeSet.headMap(tick + 1);
		if (head.isEmpty()) {
			return 120.0f;
		}
		MyTempoEvent ev = (head.get(head.lastKey()));
		return (float) ev.bpm;
	}

	/**
	 * return the tick at the given time
	 * 
	 * Slow (do not use if speed is required)
	 * 
	 * @param time
	 * @return tick
	 */

	public synchronized double getTickAtTime(double time) {

		if (dirty)
			reco();

		MyTempoEvent lastEv = list.firstElement();

		if (time < 0) {
			return ticksPerBeat * time * lastEv.bpm / 60.0;
		}

		for (MyTempoEvent ev : list) {
			assert (ev != null);
			if (ev.time > time)
				break;
			lastEv = ev;
		}

		return lastEv.tick + ticksPerBeat * (time - lastEv.time) * lastEv.bpm
				/ 60.0;

	}

	/**
	 * 
	 * 
	 * @param tick
	 * @return real time at this tick in seconds
	 */
	public synchronized double getTimeAtTick(double tick) {

		// assert(tick >= 0);

		if (dirty)
			reco();
		long itick = (long) tick + 1; // 
		SortedMap<Long, MyTempoEvent> head = treeSet.headMap(itick);
		MyTempoEvent ev;
		Long lastKey;

		if (head.isEmpty()) {

			// PJS thinks this is annyoing so temporarily commented out :)

			// System.out.println(this);
			// Yeh but I put it here because it shouold never happen . . . but
			// it was . . . (PJL)
			// System.out.println(" Tempo list has no head event " + tick +
			// this);
			// try {
			// throw new Exception();
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			// allow negative events for audio parts before the start
			
			lastKey = 0L;
			ev = treeSet.get(treeSet.firstKey());
		} else {
			lastKey = head.lastKey();
			ev = head.get(lastKey);
		}

		return ev.time + (tick - lastKey) * 60.0 / ev.bpm / ticksPerBeat;
	}

	// return (60.0 * 1000000.0 * tick)
	// / (sequence.getResolution() * sequencer.getTempoInBPM());
	// }
	// public double getTickAt(double time) {
	// // TODO Auto-generated method stub
	// return 0;
	// }

	public void display() {
		reco();
		for (Map.Entry<Long, MyTempoEvent> me : treeSet.entrySet()) {
			System.out.println(me.getKey() + " : " + me.getValue().time + " : "
					+ me.getValue().bpm);
			// System.out.println(getTimeAt(me.getKey()+0.5));
		}
	}

	public static void main(String args[]) {

	}

	public int size() {
		return treeSet.size();
	}

	public MyTempoEvent elementAt(int row) {
		if (dirty)
			reco();
		return list.elementAt(row);
	}
}
