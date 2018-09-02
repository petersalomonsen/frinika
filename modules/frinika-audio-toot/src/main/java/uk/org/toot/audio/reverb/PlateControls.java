// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.reverb;

import java.awt.Color;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

import static uk.org.toot.misc.Localisation.getString;

/**
 * @author st
 */
public class PlateControls extends AudioControls implements PlateProcess.Variables
{
	private final static int MAX_PRE_DELAY_MS = 200;
	private final static ControlLaw PRE_DELAY_LAW = new LinearLaw(0, MAX_PRE_DELAY_MS, "ms");
    private final static ControlLaw DIFFUSION_LAW = new LinearLaw(0, 0.99f, "");
	private final static int PRE_DELAY = 0;
	private final static int BANDWIDTH = 1;
	private final static int INPUT_DIFFUSION_1 = 2;
	private final static int INPUT_DIFFUSION_2 = 3;
	private final static int DAMPING = 4;
	private final static int DECAY = 5;
	private final static int DECAY_DIFFUSION_1 = 6;
	private final static int DECAY_DIFFUSION_2 = 7;
	
	private FloatControl preDelayControl;
	private FloatControl bandwidthControl;
	private FloatControl inputDiffusion1Control, inputDiffusion2Control;
	private FloatControl dampingControl;
	private FloatControl decayControl;
	private FloatControl decayDiffusion1Control, decayDiffusion2Control;
	
	private int preDelaySamples;
	private float bandwidth;
	private float inputDiffusion1, inputDiffusion2;
	private float damping, decay;
	private float decayDiffusion1, decayDiffusion2;
	
	public PlateControls() {
		super(ReverbIds.PLATE_ID, getString("Plate.Reverb"));
		createControls();
		deriveControls();
	}

	@Override
	protected void derive(Control c) {
        switch ( c.getId() ) {
        case PRE_DELAY: preDelaySamples = derivePreDelaySamples(); break;
        case BANDWIDTH: bandwidth = deriveBandwidth(); break;
        case INPUT_DIFFUSION_1: inputDiffusion1 = deriveInputDiffusion1(); break;
        case INPUT_DIFFUSION_2: inputDiffusion2 = deriveInputDiffusion2(); break;
        case DAMPING: damping = deriveDamping(); break;
        case DECAY: decay = deriveDecay(); break;
        case DECAY_DIFFUSION_1: decayDiffusion1 = deriveDecayDiffusion1(); break;
        case DECAY_DIFFUSION_2: decayDiffusion2 = deriveDecayDiffusion2(); break;
        }		
	}
	
	protected void createControls() {
		ControlColumn col1 = new ControlColumn();
		col1.add(preDelayControl = createPreDelayControl());
		col1.add(bandwidthControl = createBandwidthControl());
		add(col1);
		ControlColumn col2 = new ControlColumn() {
            public int getVisibility() { return 1; }            
        };
		col2.add(inputDiffusion1Control = createInputDiffusion1Control());
		col2.add(inputDiffusion2Control = createInputDiffusion2Control());
		add(col2);
		ControlColumn col3 = new ControlColumn();
		col3.add(dampingControl = createDampingControl());
		col3.add(decayControl = createDecayControl());
		add(col3);
		ControlColumn col4 = new ControlColumn() {
            public int getVisibility() { return 1; }                        
        };
		col4.add(decayDiffusion1Control = createDecayDiffusion1Control());
		col4.add(decayDiffusion2Control = createDecayDiffusion2Control());
		add(col4);
	}
	
	protected FloatControl createPreDelayControl() {
		FloatControl control = new FloatControl(PRE_DELAY, "Pre", PRE_DELAY_LAW, 0.1f, 20f);
		return control;
	}
	
	protected FloatControl createBandwidthControl() {
		FloatControl control = new FloatControl(BANDWIDTH, "B/W", LinearLaw.UNITY, 0.01f, 0.95f);
		control.setInsertColor(Color.YELLOW);
		return control;
	}
	
	protected FloatControl createInputDiffusion1Control() {
		FloatControl control = new FloatControl(INPUT_DIFFUSION_1, "Diff1", DIFFUSION_LAW, 0.01f, 0.75f);
		control.setInsertColor(Color.DARK_GRAY);
		return control;
	}
	
	protected FloatControl createInputDiffusion2Control() {
		FloatControl control = new FloatControl(INPUT_DIFFUSION_2, "Diff2", DIFFUSION_LAW, 0.01f, 0.625f);
		control.setInsertColor(Color.DARK_GRAY);
		return control;
	}
	
	protected FloatControl createDampingControl() {
		FloatControl control = new FloatControl(DAMPING, "Damp", LinearLaw.UNITY, 0.01f, 0f);
		control.setInsertColor(Color.ORANGE);
		return control;
	}
	
	protected FloatControl createDecayControl() {
		FloatControl control = new FloatControl(DECAY, "Decay", LinearLaw.UNITY, 0.01f, 0.5f);
		control.setInsertColor(Color.RED.darker());
		return control;
	}
	
	protected FloatControl createDecayDiffusion1Control() {
		FloatControl control = new FloatControl(DECAY_DIFFUSION_1, "Diff1", DIFFUSION_LAW, 0.01f, 0.7f);
		control.setInsertColor(Color.DARK_GRAY);
		return control;
	}
	
	protected FloatControl createDecayDiffusion2Control() {
		FloatControl control = new FloatControl(DECAY_DIFFUSION_2, "Diff2", DIFFUSION_LAW, 0.01f, 0.5f);
		control.setInsertColor(Color.DARK_GRAY);
		return control;
	}
	
	protected void deriveControls() {
		preDelaySamples = derivePreDelaySamples();
		bandwidth = deriveBandwidth();
		inputDiffusion1 = deriveInputDiffusion1();
		inputDiffusion2 = deriveInputDiffusion2();
		damping = deriveDamping();
		decay = deriveDecay();
		decayDiffusion1 = deriveDecayDiffusion1();
		decayDiffusion2 = deriveDecayDiffusion2();
	}
	
	protected int derivePreDelaySamples() {
		return (int)(preDelayControl.getValue() * 44100 / 1000); // !!! TODO
	}
	
	protected float deriveBandwidth() {
		return bandwidthControl.getValue();
	}
	
	protected float deriveInputDiffusion1() {
		return inputDiffusion1Control.getValue();
	}
	
	protected float deriveInputDiffusion2() {
		return inputDiffusion2Control.getValue();
	}
	
	protected float deriveDamping() {
		return dampingControl.getValue();
	}
	
	protected float deriveDecay() {
		return decayControl.getValue();
	}
	
	protected float deriveDecayDiffusion1() {
		return decayDiffusion1Control.getValue();
	}
	
	protected float deriveDecayDiffusion2() {
		return decayDiffusion2Control.getValue();
	}
	
	public int getMaxPreDelaySamples() {
		return 44100 * MAX_PRE_DELAY_MS / 1000;
	}
	
	public int getPreDelaySamples() {
		return preDelaySamples;
	}

	public float getBandwidth() {
		return bandwidth;
	}

	public float getInputDiffusion1() {
		return inputDiffusion1;
	}

	public float getInputDiffusion2() {
		return inputDiffusion2;
	}

	public float getDamping() {
		return damping;
	}

	public float getDecay() {
		return decay;
	}

	public float getDecayDiffusion1() {
		return decayDiffusion1;
	}

	public float getDecayDiffusion2() {
		return decayDiffusion2;
	}
        
    public boolean canBypass() { return false; }
    
    // Diffusion is level 1
    public int getMaxVisibility() { return 1; }
    

}
