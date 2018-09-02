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
import uk.org.toot.control.LogLaw;

import static uk.org.toot.misc.Localisation.getString;

/**
 * @author st
 */
public class BarrControls extends AudioControls implements BarrProcess.Variables
{
	private final static int MAX_PRE_DELAY_MS = 200;
	private final static ControlLaw PRE_DELAY_LAW = new LinearLaw(0, MAX_PRE_DELAY_MS, "ms");
    private final static ControlLaw DIFFUSION_LAW = new LinearLaw(0, 0.99f, "");
    private final static ControlLaw SIZE_LAW = new LogLaw(0.2f, 1f, "");
    
	private final static int PRE_DELAY = 0;
	private final static int BANDWIDTH = 1;
	private final static int INPUT_DIFFUSION = 2;
	private final static int DAMPING = 4;
	private final static int DECAY = 5;
	private final static int DECAY_DIFFUSION = 6;
    private final static int SIZE = 7;
    
	
	private FloatControl preDelayControl;
	private FloatControl bandwidthControl;
	private FloatControl inputDiffusionControl;
	private FloatControl dampingControl;
	private FloatControl decayControl;
	private FloatControl decayDiffusionControl;
    private FloatControl sizeControl;
	
	private int preDelaySamples;
	private float bandwidth;
	private float inputDiffusion;
	private float damping, decay;
	private float decayDiffusion;
    private float size;
	
	public BarrControls() {
		super(ReverbIds.BARR_ID, getString("Barr.Reverb"));
		createControls();
		deriveControls();
	}

	@Override
	protected void derive(Control c) {
        switch ( c.getId() ) {
        case PRE_DELAY: preDelaySamples = derivePreDelaySamples(); break;
        case BANDWIDTH: bandwidth = deriveBandwidth(); break;
        case INPUT_DIFFUSION: inputDiffusion = deriveInputDiffusion(); break;
        case DAMPING: damping = deriveDamping(); break;
        case DECAY: decay = deriveDecay(); break;
        case DECAY_DIFFUSION: decayDiffusion = deriveDecayDiffusion(); break;
        case SIZE: size = deriveSize(); break;
        }		
	}
	
	protected void createControls() {
		ControlColumn col1 = new ControlColumn();
		col1.add(preDelayControl = createPreDelayControl());
		col1.add(bandwidthControl = createBandwidthControl());
		add(col1);
		ControlColumn col2 = new ControlColumn();
        col2.add(inputDiffusionControl = createInputDiffusionControl());
        col2.add(sizeControl = createSizeControl());
		add(col2);
		ControlColumn col3 = new ControlColumn();
		col3.add(dampingControl = createDampingControl());
		col3.add(decayControl = createDecayControl());
		add(col3);
//		ControlColumn col4 = new ControlColumn();
/*		col4.add(decayDiffusionControl = createDecayDiffusionControl()); */
//		add(col4);
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
	
	protected FloatControl createInputDiffusionControl() {
		FloatControl control = new FloatControl(INPUT_DIFFUSION, "Diff", DIFFUSION_LAW, 0.01f, 0.75f);
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
	
	protected FloatControl createDecayDiffusionControl() {
		FloatControl control = new FloatControl(DECAY_DIFFUSION, "Diff", DIFFUSION_LAW, 0.01f, 0.7f);
		control.setInsertColor(Color.DARK_GRAY);
		return control;
	}
	
    protected FloatControl createSizeControl() {
        FloatControl control = new FloatControl(SIZE, "Size", SIZE_LAW, 0.01f, 1f);
        control.setInsertColor(Color.RED.darker());
        return control;        
    }
    
	protected void deriveControls() {
		preDelaySamples = derivePreDelaySamples();
		bandwidth = deriveBandwidth();
		inputDiffusion = deriveInputDiffusion();
		damping = deriveDamping();
		decay = deriveDecay();
		decayDiffusion = deriveDecayDiffusion();
        size = deriveSize();
	}
	
	protected int derivePreDelaySamples() {
		return Math.max(1, (int)(preDelayControl.getValue() * 44100 / 1000)); // !!! TODO SR
	}
	
	protected float deriveBandwidth() {
		return bandwidthControl.getValue();
	}
	
	protected float deriveInputDiffusion() {
		return inputDiffusionControl.getValue();
	}
	
	protected float deriveDamping() {
		return dampingControl.getValue();
	}
	
	protected float deriveDecay() {
		return decayControl.getValue();
	}
	
	protected float deriveDecayDiffusion() {
        if ( decayDiffusionControl == null ) return 0.5f;
		return decayDiffusionControl.getValue();
	}
	
    protected float deriveSize() {
        return sizeControl.getValue();
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

	public float getInputDiffusion() {
		return inputDiffusion;
	}

	public float getDamping() {
		return damping;
	}

	public float getDecay() {
		return decay;
	}

	public float getDecayDiffusion() {
		return decayDiffusion;
	}

    public float getSize() {
        return size;
    }
    
    public boolean canBypass() { return false; }

    // KB keeps block diffuser length much longer than input diffuser lengths
    // KB keeps delay length a bit less than sum of dif1 and dif2 lengths
    // converted to 44100 from 29761Hz sample rate for original Griesinger literals
    private int[][] sizes = {{ 996, 2667, 6598 }, 
                             { 1777, 3579, 4687 },
                             { 1345, 3936, 6249 },
                             { 1173, 4167, 5512 },
                             { 210, 159, 562, 410 }}; // input diffusers
    
    private int[][] leftTaps = {{ 253, 5211 },
                                { 1745, 4111 },
                                { 634, 5873},
                                { 1423, 5173 }};

    private int[][] rightTaps = {{ 1111, 3576 },
                                 { 568, 4666 },
                                 { 1964, 4236 },
                                 { 47, 3533 }}; 
    
    public int[][] getSizes() {
        return sizes;
    }
    
    public int[][] getLeftTaps() {
        return leftTaps;
    }
    
    public int[][] getRightTaps() {
        return rightTaps;
    }
}
