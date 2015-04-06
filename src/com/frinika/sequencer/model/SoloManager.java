/*
 * Created on 11 Feb 2008
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

import java.util.Set;
import java.util.Vector;

import javax.sound.midi.MidiDevice;

import com.frinika.project.MidiDeviceDescriptor;
import com.frinika.project.ProjectContainer;

public class SoloManager {

	ProjectContainer project;

	// lanes that have had mute set.
	Vector<RecordableLane> muted = new Vector<RecordableLane>();

	// lanes that have had solo set.
	Vector<RecordableLane> soloed = new Vector<RecordableLane>();

	// device lanes need not muted because soloed midi lane
	Vector<RecordableLane> soloedParent = new Vector<RecordableLane>();

	public SoloManager(ProjectContainer container) {
		this.project = container;
	}

	private Vector<MidiLane> findMidiLanes(SynthLane sl) {
		Vector<MidiLane> lanes = new Vector<MidiLane>();

		MidiDevice midi = sl.getMidiDescriptor().getMidiDevice();
		for (Lane l : project.getLanes()) {
			if (!(l instanceof MidiLane))
				continue;
			MidiLane ml = (MidiLane) l;
			MidiDevice md = ml.getMidiDevice();
			if (md == midi)
				lanes.add(ml);
		}
		return lanes;
	}

	private SynthLane findSynthLane(MidiLane lane) {
		MidiDevice midi = lane.getMidiDevice();
		for (Lane l : project.getLanes()) {
			if (!(l instanceof SynthLane))
				continue;
			SynthLane sl = (SynthLane) l;
			MidiDeviceDescriptor md = sl.getMidiDescriptor();
			if (md.getMidiDevice() == midi)
				return sl;
		}
		return null;
	}

	public void toggleSolo(RecordableLane lane) {

		boolean yes;

		if (!soloed.contains(lane)) {
			soloed.add(lane);
			yes = true;
		} else {
			soloed.remove(lane);
			yes = false;
		}

		if (yes) {

			// Find the midi lane device lane and make sure it is solo
			if (lane instanceof MidiLane) {
				SynthLane sl = findSynthLane((MidiLane) lane);
				if (sl != null) {
					System.out.println(" Adding midi lane "
							+ sl.getMidiDescriptor().getMidiDeviceName());
					if (!soloedParent.contains(sl))
						soloedParent.add(sl);
				}
			} else if (lane instanceof SynthLane) { // make sure midi lanes are
													// not mute
				for (MidiLane ml : findMidiLanes((SynthLane) lane)) {
					if (ml.isMute())
						ml.setMute(false);
				}
			}

			// if (!soloed.contains(lane))
			// soloed.add((RecordableLane) lane);

		} else {

			// Find the midi lane device lane
			// if no remaining solo midi lanes rmeove for parentSoloed
			if (lane instanceof MidiLane) {
				SynthLane sl = findSynthLane((MidiLane) lane);
				if (sl != null) {
					boolean soloedSib = false;
					for (MidiLane ml : findMidiLanes(sl)) {
						soloedSib = soloed.contains(ml) || soloedSib;
					}
					if (!soloedSib)
						soloedParent.remove(sl);
				}
			}

			// soloed.remove(lane);

		}

		doit();
	}

	public void toggleMute(RecordableLane lane) {
		if (!muted.contains(lane)) {
			muted.add(lane);
		} else {
			muted.remove(lane);
		}

		doit();
	}

	void doit() {

		// Any soloed lanes then use the solo logic
		if (!soloed.isEmpty()) {
			System.out.println(" SOLOED LANES");
			for (Lane l : project.getLanes()) {
				if (!(l instanceof RecordableLane))
					continue;

				if (!soloed.contains(l)) {

					if (l instanceof SynthLane) { // don't mute if dependent
						// MidiLanes
						if (!soloedParent.contains(l)) {
							((RecordableLane) l).setMute(true);
						}
					} else if (l instanceof MidiLane) { // don't mute children
						// of solo synths
						SynthLane sl = findSynthLane((MidiLane) l);
						if (!soloed.contains(sl)) {
							((RecordableLane) l).setMute(true);
						}
					} else if (l instanceof AudioLane) {
						if (!soloed.contains(l))
							((RecordableLane) l).setMute(true);
					}

				} else {
					((RecordableLane) l).setMute(false);
					if (l instanceof MidiLane) {
						SynthLane sl = findSynthLane((MidiLane) l);
						if (sl != null)
							sl.setMute(false);
					}
				}
			}
		} else {

			// just mute them
			System.out.println(" NO SOLOED LANES");

			for (Lane l : project.getLanes()) {
				if (l instanceof RecordableLane) {
					((RecordableLane) l).setMute(muted.contains(l));
				}
			}
		}

	}

	public boolean isMute(RecordableLane rl) {
		return muted.contains(rl);
	}

	public boolean isSolo(RecordableLane rl) {
		return soloed.contains(rl);
	}

	public boolean isParentSolo(RecordableLane rl) {
		return soloedParent.contains(rl);
	}

}
