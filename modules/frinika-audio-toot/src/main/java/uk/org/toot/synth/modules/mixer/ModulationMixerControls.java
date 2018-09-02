// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.mixer;

import static uk.org.toot.misc.Localisation.getString;

import java.awt.Color;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class ModulationMixerControls extends CompoundControl implements ModulationMixerVariables
{
	public final static int DEPTH = 0;
	
	private final ControlLaw law;
	private FloatControl[] depthControl;
	private float[] depth;
	
	private int idOffset = 0;
	
	private int count;
	
	public ModulationMixerControls(int instanceIndex, String name, int idOffset, String[] labels, boolean bipolar) {
		this(instanceIndex, name, idOffset, labels, bipolar, 1f, "");
	}
	
	public ModulationMixerControls(int instanceIndex, String name, final int idOffset, String[] labels, boolean bipolar, float range, String units) {
		super(MixerIds.MODULATION_MIXER_ID, instanceIndex, name);
		this.idOffset = idOffset;
		this.count = labels.length;
		law = new LinearLaw(bipolar ? -range : 0f, range, units);
		depth = new float[count];
		createControls(labels);
		deriveSampleRateIndependentVariables();
	}

    @Override
    protected void derive(Control c) {
		int n = c.getId() - idOffset - DEPTH;
		depth[n] = deriveDepth(n);    	
    }
    
	protected void createControls(String[] labels) {
		depthControl = new FloatControl[count];
		for ( int i = 0; i < count; i++ ) {
			add(depthControl[i] = createDepthControl(i, labels[i]));
		}
	}

	protected void deriveSampleRateIndependentVariables() {
		for ( int i = 0; i < count; i++ ) {
			depth[i] = deriveDepth(i);
		}
	}

	protected float deriveDepth(int i) {
		return depthControl[i].getValue();
	}

	protected FloatControl createDepthControl(int i, String label) {
		FloatControl control = new FloatControl(i+DEPTH+idOffset, label, law, 0.01f, 0f) {
			private final String[] presetNames = { getString("Off") };

			public String[] getPresetNames() {
				return presetNames;
			}

			public void applyPreset(String presetName) {
				if ( presetName.equals(getString("Off")) ) {
					setValue(0f);
				}
			}        	
		};
		control.setInsertColor(Color.DARK_GRAY);
		return control;				
	}

	public int getCount() {
		return count;
	}
	
	public float getDepth(int n) {
		return depth[n];
	}
	
	public float[] getDepths() {
		return depth;
	}
}
