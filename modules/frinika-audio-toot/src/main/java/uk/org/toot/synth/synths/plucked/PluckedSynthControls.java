// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.synths.plucked;

import static uk.org.toot.misc.Localisation.getString;

import java.awt.Color;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.synth.SynthControls;
import uk.org.toot.synth.modules.amplifier.AmplifierControls;

abstract public class PluckedSynthControls extends SynthControls
{
	// OFFSETS MUST NOT BE CHANGED TO PRESERVE PERSISTENCE PORTABILITY
	// OFFSETS ARE SLIGHTLY SPARSE TO ALLOW EXTENSION OF EXISTING MODULES
//	private final static int LFOVIB_OFFSET 	= 0x18;
	private final static int STRING_OFFSET  = 0x00;
	private final static int AMP_OFFSET 	= 0x38;

	private StringControls stringControls;
	private AmplifierControls amplifierControls;

	private int stringCount;
	
	public PluckedSynthControls(int id, String name, int nstrings) {
		super(id, name);
		stringCount = nstrings;
		ControlRow row = new ControlRow();
		stringControls = new StringControls();
		row.add(stringControls);		
		amplifierControls = new AmplifierControls(0, getString("Amplifier"), AMP_OFFSET);
		row.add(amplifierControls);
		add(row);
	}
	
	public int getStringCount() {
		return stringCount;
	}

	public abstract float getLowestFrequency(int string);
	
	public float getPickup() {
		return stringControls.getPickup();
	}
	
	public float getPick() {
		return stringControls.getPick();
	}
	
	public float getVelocityTrack() {
		return amplifierControls.getVelocityTrack();
	}
	
	public float getLevel() {
		return amplifierControls.getLevel();
	}
	
	protected class StringControls extends CompoundControl
	{
		private FloatControl pickupControl;
		private FloatControl pickControl;
		
		public StringControls() {
			super(STRING_OFFSET, "String");
			// TODO Auto-generated constructor stub
			pickupControl = createControl(0, "P/up");
			pickControl = createControl(1, "Pick");
			add(pickupControl);
			add(pickControl);
		}

		private FloatControl createControl(int id, String name) {
			FloatControl control = new FloatControl(id, name, LinearLaw.UNITY, 0.001f, 0.2f);
			control.setInsertColor(Color.WHITE);
			return control;
		}
		
		public float getPickup() {
			return pickupControl.getValue();
		}
		
		public float getPick() {
			return pickControl.getValue();
		}
	}

}
