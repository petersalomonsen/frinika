// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.distort;

import java.awt.Color;

import org.tritonus.share.sampled.TVolumeUtils;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.Control;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

import static uk.org.toot.misc.Localisation.getString;

/**
 * @author st
 */
public class Distort1Controls extends AudioControls implements Distort1Process.Variables
{
	private static final int GAIN = 0;
	private static final int BIAS = 1;
	
	protected final static LinearLaw GAIN_LAW = new LinearLaw(0, 30, "dB");
	protected final static LinearLaw BIAS_LAW = new LinearLaw(-0.5f, 0.5f, "");
	
	private FloatControl gainControl, biasControl;
	
	private float gain = 1f;
	private float bias = -0.33f;
	
	public Distort1Controls() {
		this(DistortionIds.DISTORT1, "Drive");
	}
	
	public Distort1Controls(int id, String name) {
		super(id, name);
		ControlColumn col = new ControlColumn();
		col.add(biasControl = createBiasControl());
		col.add(gainControl = createGainControl());
		add(col);
	}
	
	protected FloatControl createBiasControl() {
		FloatControl control = new FloatControl(BIAS, getString("Bias"), BIAS_LAW, 0.01f, bias);
		control.setInsertColor(Color.DARK_GRAY);
		return control;
	}
	
	protected FloatControl createGainControl() {
		FloatControl control = new FloatControl(GAIN, getString("Drive"), GAIN_LAW, 0.1f, 0);
        control.setInsertColor(Color.MAGENTA.darker());
		return control;
	}
	
	@Override
	protected void derive(Control c) {
        switch ( c.getId() ) {
        case GAIN: gain = deriveGain(); break;
        case BIAS: bias = deriveBias(); break;
        }
	}
	
	protected float deriveGain() {
		return (float)TVolumeUtils.log2lin(gainControl.getValue());
	}
	
	protected float deriveBias() {
		return biasControl.getValue();
	}
	
	public float getGain() {
		return gain;
	}
	
	public float getBias() {
		return bias;
	}
}
