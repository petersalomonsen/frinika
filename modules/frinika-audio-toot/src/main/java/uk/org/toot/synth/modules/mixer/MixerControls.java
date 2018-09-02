// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.mixer;

import java.awt.Color;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class MixerControls extends CompoundControl implements MixerVariables
{
	public final static int LEVEL = 0;
	
	private FloatControl[] levelControl;
	private float[] level;
	
	private int idOffset = 0;
	
	private int count;
	
	public MixerControls(int instanceIndex, String name, int idOffset, int count) {
		this(MixerIds.SIMPLE_MIXER_ID , instanceIndex, name, idOffset, count);
	}
	
	public MixerControls(int id, int instanceIndex, String name, final int idOffset, int count) {
		super(id, instanceIndex, name);
		this.idOffset = idOffset;
		this.count = count;
		level = new float[count];
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
	}

    @Override
    protected void derive(Control c) {
		int n = c.getId() - idOffset - LEVEL;
		level[n] = deriveLevel(n);    	
    }
    
	protected void createControls() {
		levelControl = new FloatControl[count];
		for ( int i = 0; i < count; i++ ) {
			add(levelControl[i] = createLevelControl(i));
		}
	}

	protected void deriveSampleRateIndependentVariables() {
		for ( int i = 0; i < count; i++ ) {
			level[i] = deriveLevel(i);
		}
	}

	protected float deriveLevel(int i) {
		return levelControl[i].getValue();
	}

	protected void deriveSampleRateDependentVariables() {
	}

	protected FloatControl createLevelControl(int i) {
        FloatControl control = new FloatControl(i+LEVEL+idOffset, String.valueOf(i+1), LinearLaw.UNITY, 0.01f, i > 0 ? 0f : 1f);
        control.setInsertColor(Color.BLACK);
        return control;				
	}

	public int getCount() {
		return count;
	}
	
	public float getLevel(int n) {
		return level[n];
	}
}
