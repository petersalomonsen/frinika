// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.modules.oscillator.OscillatorIds.DSF_OSCILLATOR_ID;

import java.awt.Color;

import org.tritonus.share.sampled.TVolumeUtils;

import uk.org.toot.control.BooleanControl;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

/**
 * @author st
 *
 */
public class HammondOscillatorControls extends CompoundControl implements HammondOscillatorVariables
{
	private final static int CLICK = 12; // leaving room for 2 or 3 extra drawbars
	
	private int idOffset;

	private float[] levels;
	private boolean click;
	
	private static float gain1 = gain(1);
	
	private FloatControl[] levelControls;
	private BooleanControl clickControl;
	
	private final static String[] names = 
		{ "16'", "5 1/3", "8'", "4'", "2 2/3'", "2'", "1 3/5'", "1 1/3'", "1'"};
	
	private final static LinearLaw LEVEL_LAW = new LinearLaw(0, 8, "");
	private final static Color BROWN = new Color(200, 100, 50);
	
	public HammondOscillatorControls(int instanceIndex, String name, final int idOffset) {
		super(DSF_OSCILLATOR_ID, instanceIndex, name);
		this.idOffset = idOffset;
		levels = new float[names.length];
		levelControls = new FloatControl[names.length];
		createControls();
		deriveSampleRateIndependentVariables();
	}
	
    @Override
    protected void derive(Control c) {
		int n = c.getId()-idOffset;
		switch ( n ) {
		case CLICK: click = deriveClick(); break;
		default: levels[n] = deriveLevel(n); break;
		}    	
    }
	private void createControls() {
		Color color;
		SliderColumn cc;
		ControlRow row = new ControlRow();
		for ( int i = 0; i < names.length; i++ ) {
			if ( i < 2 ) color = BROWN;
			else if ( names[i].length() > 2 ) color = Color.DARK_GRAY;
			else color = Color.WHITE;
			levelControls[i] = createLevelControl(i+idOffset, getString("Level"), color);
			cc = new SliderColumn(names[i]);
			cc.add(levelControls[i]);
			row.add(cc);
			derive(levelControls[i]);
		}
/*		ControlColumn col = new ControlColumn();
		col.add(clickControl = createClickControl(CLICK, getString("Click")));
		row.add(col); */
		add(row);
	}
	
	protected FloatControl createLevelControl(int id, String name, Color color) {
        FloatControl control = new FloatControl(id, name, LEVEL_LAW, 0.1f, 0) {
            public boolean isRotary() { return false; }
        };
        control.setInsertColor(color);
		return control;
	}
	
	protected BooleanControl createClickControl(int id, String name) {
		BooleanControl control = new BooleanControl(id, name, true);
		control.setStateColor(true, Color.YELLOW);
		return control;
	}
	
	private void deriveSampleRateIndependentVariables() {
		for ( int i = 0; i < names.length; i++ ) {
			levels[i] = deriveLevel(i);
		}
		click = deriveClick();
	}

	/*
	 * Levels are marked 1..8.
	 * 8 is 0dB, each lower integer is 3dB lower, 0 is -infinity dB i.e. zero level
	 * 8=0, 7=-3, 6=-6, 5=-9, 4=-12, 3=-15, 2=-18, 1=-21, tapers to 0=-infinity
	 */
	protected float deriveLevel(int i) {
		float value = levelControls[i].getValue();
		if ( value < 1f ) {
			return value * gain1; // linear taper to zero
		} 
		return gain(value);		
	}
	
	protected static float gain(float value) {
		return (float)TVolumeUtils.log2lin(3f * (value - 8f));
	}
	
	protected boolean deriveClick() {
		if ( clickControl == null ) return false;
		return clickControl.getValue();
	}
	
	public float[] getLevels() {
		return levels;
	}
	
	public boolean canClick() {
		return click;
	}
	
    protected static class SliderColumn extends CompoundControl
    {
        public SliderColumn(String name) {
            super(0, name);
        }

        public void add(Control c) { // make public
            super.add(c);
        }

        public boolean isAlwaysVertical() { return true; }
    }


}
