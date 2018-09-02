// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.distort;

import java.awt.Color;
import java.util.List;

import org.tritonus.share.sampled.TVolumeUtils;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.filter.ToneStackDesigner;
import uk.org.toot.audio.filter.ToneStackSection;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.EnumControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

import static uk.org.toot.audio.distort.DistortionIds.GUITAR_AMP;
import static uk.org.toot.misc.Localisation.getString;

public class GuitarAmpControls extends AudioControls 
	implements GuitarAmpProcess.Variables
{
	private static final int BASS = 0;
	private static final int MIDDLE = 1;
	private static final int TREBLE = 2;
    private static final int TYPE = 3;  // which components
    private static final int BIAS = 4;
    private static final int GAIN1 = 5;    
    private static final int GAIN2 = 6;
    private static final int MASTER = 7;
	
	private static final ControlLaw TEN_LAW = new LinearLaw(0, 10f, "");
    private final static LinearLaw GAIN_LAW = new LinearLaw(0, 30, "dB");
    private final static LinearLaw BIAS_LAW = new LinearLaw(-0.9f, 0.9f, "");
    private final static LinearLaw MASTER_LAW = new LinearLaw(0, 20, "dB");

	private FloatControl bassControl, midControl, trebleControl;
    private EnumControl typeControl;
    private FloatControl biasControl, gain1Control, gain2Control;
    private FloatControl masterControl;
	
    private float gain1 = 1f, gain2 = 1f;
    private float bias = -0.33f;    
    private float master = 1f;
	private float b = 0f, m = 0f, t = 0f;
	private float fs = 44100f;
	private boolean changed = false;

	private ToneStackDesigner stack = new ToneStackDesigner();
	private ToneStackSection.Coefficients coeffs;
	
	public GuitarAmpControls() {
		super(GUITAR_AMP, "Guitar Amp");
        ControlRow row = new ControlRow();
        row.add(typeControl = new TypeControl("Type"));
        add(row);
        row = new ControlRow();
        ControlColumn col = new ControlColumn();
        col.add(biasControl = createBiasControl());
        col.add(gain1Control = createGainControl(GAIN1));
        row.add(col);
		col = new ControlColumn();
		col.add(trebleControl = createControl(TREBLE, "Treble"));
		col.add(midControl = createControl(MIDDLE, "Mid"));
		col.add(bassControl = createControl(BASS, "Bass"));
		row.add(col);
        col = new ControlColumn();
        col.add(gain2Control = createGainControl(GAIN2));
        col.add(masterControl = createMasterControl());
        row.add(col);
        add(row);
        stack.setComponents((ToneStackDesigner.Components)typeControl.getValue());
        coeffs = design();
        changed = true;
	}
	
    public boolean isAlwaysVertical() { return true; }

    protected FloatControl createBiasControl() {
        FloatControl control = new FloatControl(BIAS, getString("Bias"), BIAS_LAW, 0.01f, bias);
        control.setInsertColor(Color.DARK_GRAY);
        return control;
    }
    
    protected FloatControl createGainControl(int id) {
        FloatControl control = new FloatControl(id, getString("Drive"), GAIN_LAW, 0.1f, 0);
        control.setInsertColor(Color.MAGENTA.darker());
        return control;
    }
    
	protected FloatControl createControl(int id, String name) {
		FloatControl control = new FloatControl(id, name, TEN_LAW, 0.01f, 0f);
		control.setInsertColor(Color.WHITE);
		return control;
	}
	
    protected FloatControl createMasterControl() {
        FloatControl control = new FloatControl(MASTER, getString("Level"), MASTER_LAW, 0.01f, 0f);
        return control;
    }
    @Override
    protected void derive(Control c) {
    	switch ( c.getId() ) {
    	case BASS: b = taper(bassControl.getValue()/10, 2.3f); break;
    	case MIDDLE: m = midControl.getValue() / 10; break;
    	case TREBLE: t = taper(trebleControl.getValue()/10, 2.3f); break;
        case TYPE: stack.setComponents((ToneStackDesigner.Components)typeControl.getValue()); break;
        case BIAS: bias = deriveBias(); return;
        case GAIN1: gain1 = deriveGain(gain1Control); return;
        case GAIN2: gain2 = deriveGain(gain2Control); return;
        case MASTER: master = deriveGain(masterControl); return;
    	default: return;
    	}
    	coeffs = design();
    	changed = true;
    }
    
    protected float deriveGain(FloatControl c) {
        return (float)TVolumeUtils.log2lin(c.getValue());
    }
    
    protected float deriveBias() {
        return biasControl.getValue();
    }
    
    public float getBias() {
        return bias;
    }
    
    public float getGain1() {
        return gain1;
    }
    
    public float getGain2() {
        return gain2;
    }
    
    public float getMaster() {
        return master;
    }
    
    // a power of 2.3 to 2.4 gives best match to log pot taper
    // 0.5 should return about 0.18
    // slope at zero is low compared to real pot taper
    protected float taper(float val, float power) {
        return (float)Math.pow(val, power);
    }
    
    protected ToneStackSection.Coefficients design() {
    	return stack.design(b, m, t, fs);    	
    }
    
    public ToneStackSection.Coefficients setSampleRate(float rate) {
    	fs = rate;
    	changed = false; // race condition !!!
    	return design();
    }
    
    public boolean hasChanged() {
    	return changed;
    }
    
    public ToneStackSection.Coefficients getCoefficients() {
    	changed = false; // race condition !!!
    	return coeffs;
    }
    
    protected static class TypeControl extends EnumControl
    {
        private static List<ToneStackDesigner.Components> values =
            new java.util.ArrayList<ToneStackDesigner.Components>();
        
        static {
            values.add(new ToneStackDesigner.Fender59BassmanComponents());
            values.add(new ToneStackDesigner.FenderComponents());
            values.add(new ToneStackDesigner.MarshallComponents());
        }
        
        public TypeControl(String name) {
            super(TYPE, name, values.get(0));
        }

        @Override
        public List getValues() {
            return values;
        }
        
        // permit wider control
        public int getWidthLimit() { return 120; }
    }
}
