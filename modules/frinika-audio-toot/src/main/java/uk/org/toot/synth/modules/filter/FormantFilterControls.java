// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.misc.Localisation.getString;

import java.awt.Color;

import uk.org.toot.control.BooleanControl;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.control.LogLaw;

import static uk.org.toot.synth.modules.filter.FilterIds.FORMANT_FILTER_ID;

public class FormantFilterControls extends CompoundControl 
	implements FormantFilterVariables
{
	private final static ControlLaw FREQ_LAW = new LogLaw(100, 5000, "Hz");
    private final static ControlLaw SHIFT_LAW = new LogLaw(0.25f, 4f, "");

	private final static int RESONANCE = 0;
	private final static int FREQSHIFT = 1;
	private final static int FREQUENCY = 2; // ..4..6..8 etc.
	private final static int LEVEL     = 3; // ..5..7..9 etc.
	private final static int ENABLE	  = 15;
	
	private FloatControl frequencyControl[];
	private FloatControl levelControl[];
	private FloatControl frequencyShiftControl;
	private FloatControl resonanceControl;
	private BooleanControl enableControl;
	
	private float[] frequency;
	private float[] level;
	private float frequencyShift;
	private float resonance;
	private boolean bypass;
	
	protected int idOffset = 0;
	
	private int sampleRate = 44100;
	private int nBands = 4;
	
	public FormantFilterControls(int instanceIndex, String name, final int idOffset) {
		super(FORMANT_FILTER_ID, instanceIndex, name);
		this.idOffset = idOffset;
		frequencyControl = new FloatControl[nBands];
		levelControl = new FloatControl[nBands];
		frequency = new float[nBands];
		level = new float[nBands];
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
	}

	protected void derive(Control c) {
		int id = c.getId()-idOffset;
		switch ( id ) {
		case RESONANCE: resonance = deriveResonance(); break;
		case FREQSHIFT: frequencyShift = deriveFrequencyShift(); break;
		case ENABLE: bypass = !deriveEnable(); break;
		default:
			int n = (id / 2) - 1;
			if ( (id & 1) == 0 ) {
				frequency[n] = deriveFrequency(n);
			} else {
				level[n] = deriveLevel(n);
			}
		}		
	}
	
	protected void createControls() {
		ControlColumn col;
		for ( int i = 0; i < nBands; i++ ) {
			col = new ControlColumn();
			frequencyControl[i] = createFrequencyControl(i);
			col.add(frequencyControl[i]);
			levelControl[i] = createLevelControl(i);
			col.add(levelControl[i]);
			add(col);
		}
		col = new ControlColumn();
		col.add(enableControl = createPowerControl());
		col.add(frequencyShiftControl = createFrequencyShiftControl());
		col.add(resonanceControl = createResonanceControl());
		add(col);
	}

	protected void deriveSampleRateIndependentVariables() {
		resonance = deriveResonance();
		frequencyShift = deriveFrequencyShift();
		bypass = !deriveEnable();
		for ( int i = 0; i < nBands; i++ ) {
			level[i] = deriveLevel(i);
		}
	}

	protected void deriveSampleRateDependentVariables() {
		for ( int i = 0; i < nBands; i++ ) {
			frequency[i] = deriveFrequency(i);
		}
	}

	protected float deriveFrequency(int n) {
		return 2 * frequencyControl[n].getValue() / sampleRate;
	}
	
	protected float deriveLevel(int n) {
		return levelControl[n].getValue();
	}
	
	protected float deriveResonance() {
		return resonanceControl.getValue();
	}

	protected float deriveFrequencyShift() {
		return frequencyShiftControl.getValue();
	}

	protected boolean deriveEnable() {
		return enableControl.getValue();
	}
	
	protected FloatControl createFrequencyControl(int n) {
        return new FloatControl(n+n+FREQUENCY+idOffset, getString("Frequency"), FREQ_LAW, 1f, 250 * (int)Math.pow(2, n));
	}

	protected FloatControl createLevelControl(int n) {
        return new FloatControl(n+n+LEVEL+idOffset, getString("Level"), LinearLaw.UNITY, 0.01f, 1f);
	}

	protected FloatControl createResonanceControl() {
        return new FloatControl(RESONANCE+idOffset, getString("Resonance"), LinearLaw.UNITY, 0.01f, 0.25f);
	}

	protected FloatControl createFrequencyShiftControl() {
        FloatControl control = new FloatControl(FREQSHIFT+idOffset, getString("Shift"), SHIFT_LAW, 0.1f, 1f);
        control.setInsertColor(Color.yellow);
        return control;		
	}

	protected BooleanControl createPowerControl() {
		BooleanControl control = new BooleanControl(ENABLE+idOffset, "Power", false);
		control.setAnnotation("5010"); // force IEC 5010 power icon
		control.setStateColor(false, Color.DARK_GRAY);
		control.setStateColor(true, Color.GREEN.darker());
		return control;
	}
	
	public void setSampleRate(int rate) {
		if ( sampleRate != rate ) {
			sampleRate = rate;
			deriveSampleRateDependentVariables();
		}
	}

	public float getFreqencyShift() {
		return frequencyShift;
	}

	public float getFrequency(int n) {
		return frequency[n];
	}

	public float getLevel(int n) {
		return level[n];
	}

	public float getResonance() {
		return resonance;
	}

	public boolean isBypassed() {
		return bypass;
	}
	
	public int size() {
		return nBands;
	}

}
