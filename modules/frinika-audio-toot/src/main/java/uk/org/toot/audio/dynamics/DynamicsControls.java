// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.core.KVolumeUtils;
import uk.org.toot.audio.core.Taps.TapControl;
import uk.org.toot.control.*;

import java.awt.Color;

import org.tritonus.share.sampled.TVolumeUtils;

import static uk.org.toot.audio.dynamics.DynamicsControlIds.*;
import static uk.org.toot.misc.Localisation.*;

abstract public class DynamicsControls extends AudioControls
    implements DynamicsVariables
{
	private final static ControlLaw THRESH_LAW = new LinearLaw(-40f, 20f, "dB");
    private final static ControlLaw RATIO_LAW = new LogLaw(1.5f, 10f, "");
    private final static ControlLaw INVERSE_RATIO_LAW = new LinearLaw(1, -1, "");
    private final static ControlLaw KNEE_LAW = new LinearLaw(0.1f, 20f, "dB");
    private final static ControlLaw ATTACK_LAW = new LogLaw(0.1f, 100f, "ms");
    private final static ControlLaw HOLD_LAW = new LogLaw(1f, 1000f, "ms");
    private final static ControlLaw RELEASE_LAW = new LogLaw(200f, 2000f, "ms");
    private final static ControlLaw DRY_GAIN_LAW = new LinearLaw(-40f, 0f, "dB");
    private final static ControlLaw GAIN_LAW = new LinearLaw(0f, 20f, "dB");
    private final static ControlLaw DEPTH_LAW = new LinearLaw(-80f, 0f, "dB");
    private final static ControlLaw HYSTERESIS_LAW = new LinearLaw(0f, 20f, "dB");

    private GainReductionIndicator gainReductionIndicator;
    private FloatControl thresholdControl;
    private FloatControl ratioControl;
    private FloatControl kneeControl;
    private BooleanControl rmsControl;
    private FloatControl attackControl;
    private FloatControl holdControl;
    private FloatControl releaseControl;
    private FloatControl dryGainControl;
    private FloatControl gainControl;
    private FloatControl depthControl;
    private FloatControl hysteresisControl;
    private TapControl keyControl;
    
    private float sampleRate = 44100;
    private float threshold, inverseThreshold, thresholddB, inverseRatio, kneedB = 10f;
    private float attack, release, gain = 1f, dryGain = 0f, depth = 40f, hysteresis = 0f;
    private boolean rms = false;
    public int hold = 0;
    private AudioBuffer key;

    private int idOffset = 0;

    public DynamicsControls(int id, String name) {
        this(id, name, 0);
    }

    public DynamicsControls(int id, String name, int idOffset) {
        super(id, name, 126-idOffset); // cheap sparse bypass id
        this.idOffset = idOffset;
        if ( hasGainReductionIndicator() ) {
            gainReductionIndicator = new GainReductionIndicator();
        	add(gainReductionIndicator);
        }
        ControlColumn g1 = new ControlColumn();
        if ( hasKey() ) { // only compressors
            keyControl = createKeyControl();
            g1.add(keyControl);
            derive(keyControl);
        }
        if ( hasDepth() ) { // only gates
            depthControl = createDepthControl();
            g1.add(depthControl);
            derive(depthControl);
        }
        if ( hasHysteresis() ) { // only gates
            hysteresisControl = createHysteresisControl();
            g1.add(hysteresisControl);
            derive(hysteresisControl);
        }
        if ( hasKnee() ) {
            kneeControl = createKneeControl();
            g1.add(kneeControl);
            derive(kneeControl);
        }
        if ( hasRatio() ) { // only compressor and expanders
	        ratioControl = createRatioControl();
    	    g1.add(ratioControl);
    	    derive(ratioControl);
        } else if ( hasInverseRatio() ) {
            ratioControl = new InverseRatioControl();
            g1.add(ratioControl);
            derive(ratioControl);
            
        }
        thresholdControl = createThresholdControl();
        g1.add(thresholdControl);
        derive(thresholdControl);
        add(g1);

        ControlColumn g2 = new ControlColumn();
        if ( hasRMS() ) {
            rmsControl = createRMSControl();
            g2.add(rmsControl);
            derive(rmsControl);
        }
        attackControl = createAttackControl();
        g2.add(attackControl);
        derive(attackControl);
        if ( hasHold() ) {
	        holdControl = createHoldControl();
    	    g2.add(holdControl);
    	    derive(holdControl);
        }
        releaseControl = createReleaseControl();
        g2.add(releaseControl);
		add(g2);
		derive(releaseControl);

        ControlColumn g3 = new ControlColumn();
        boolean useg3 = false;
        if ( hasDryGain() ) {
            dryGainControl = createDryGainControl();
            g3.add(dryGainControl);
            useg3 = true;
            derive(dryGainControl);
        }
        if ( hasGain() ) {
            gainControl = createGainControl();
            g3.add(gainControl);
        	useg3 = true;
        	derive(gainControl);
        }
        if ( useg3 ) {
        	add(g3);
        }
    }

    public void update(float sampleRate) {
        this.sampleRate = sampleRate;
        // derive sample rate dependent variables
        deriveAttack();
        deriveHold();
        deriveRelease();
    }

    @Override
    protected void derive(Control c) {
    	switch ( c.getId() - idOffset ) {
    	case THRESHOLD: deriveThreshold(); break;
    	case RATIO: deriveRatio(); break;
        case KNEE: deriveKnee(); break;
    	case ATTACK: deriveAttack(); break;
    	case HOLD: deriveHold(); break;
    	case RELEASE: deriveRelease(); break;
        case DRY_GAIN: deriveDryGain();
    	case GAIN: deriveGain(); break;
    	case DEPTH: deriveDepth(); break;
    	case KEY: deriveKey(); break;
        case HYSTERESIS: deriveHysteresis(); break;
        case RMS: deriveRMS(); break;
    	}
    }
    
    protected void deriveThreshold() {
        thresholddB = thresholdControl.getValue();
        threshold = (float)KVolumeUtils.log2lin(thresholddB);
        inverseThreshold = 1f / threshold;
    }

    protected void deriveRatio() {
        if ( ratioControl == null ) return;
        float val = ratioControl.getValue();
        // ratio control 1.5 .. 10, inverse ratio control 1 .. -1
        inverseRatio = val > 1f ? 1f / val : val;
    }
    
    protected void deriveKnee() {
        if ( kneeControl == null ) return;
        kneedB = kneeControl.getValue();
    }
    
    protected void deriveRMS() {
        rms = rmsControl.getValue();
    }
    
    private static float LOG_0_01 = (float)Math.log(0.01);
    // http://www.physics.uoguelph.ca/tutorials/exp/Q.exp.html
    // http://www.musicdsp.org/showArchiveComment.php?ArchiveID=136
    // return per sample factor for 99% in specified milliseconds
    protected float deriveTimeFactor(float milliseconds) {
        float ns = milliseconds * sampleRate * 0.001f;
        float k = LOG_0_01 / ns ; // k, per sample
        return (float)Math.exp(k);
    }

    protected void deriveAttack() {
        attack = deriveTimeFactor(attackControl.getValue());
    }

    protected void deriveHold() {
        if ( holdControl == null ) return;
        hold = (int)(holdControl.getValue()*sampleRate*0.001f);
    }

    protected void deriveRelease() {
        release = deriveTimeFactor(releaseControl.getValue());
    }

    protected void deriveDryGain() {
        if ( dryGainControl == null ) return;
        dryGain = (float)TVolumeUtils.log2lin(dryGainControl.getValue());
    }

    protected void deriveGain() {
        if ( gainControl == null ) return;
        gain = (float)TVolumeUtils.log2lin(gainControl.getValue());
    }

    protected void deriveDepth() {
        if ( depthControl == null ) return;
        depth = (float)TVolumeUtils.log2lin(depthControl.getValue());
    }

    protected void deriveHysteresis() {
        hysteresis = (float)TVolumeUtils.log2lin(-hysteresisControl.getValue());
    }

    protected void deriveKey() {
        if ( keyControl == null ) return;
        key = keyControl.getBuffer();
    }
    
    @Override
    protected void setParent(CompoundControl parent) {
        super.setParent(parent);
        if ( parent == null && keyControl != null ) {
            keyControl.remove(); // dereferences tap
        }
    }
    
	protected boolean hasGainReductionIndicator() { return false; }

	protected ControlLaw getThresholdLaw() {
		return THRESH_LAW;
	}

	protected FloatControl createThresholdControl() {
        FloatControl control = new FloatControl(THRESHOLD+idOffset, getString("Threshold"), getThresholdLaw(), 0.1f, 0f);
        control.setInsertColor(Color.WHITE);
        return control;
    }

    protected boolean hasInverseRatio() { return false; }
    
    protected boolean hasRatio() { return false; }

    protected FloatControl createRatioControl() {
        FloatControl ratio = new FloatControl(RATIO+idOffset, getString("Ratio"), RATIO_LAW, 0.1f, 2f);
        ratio.setInsertColor(Color.BLUE);
        return ratio;
    }

    protected boolean hasKnee() { return false; }
    
    protected FloatControl createKneeControl() {
        FloatControl ratio = new FloatControl(KNEE+idOffset, getString("Knee"), KNEE_LAW, 0.1f, 10f);
        ratio.setInsertColor(Color.DARK_GRAY);
        return ratio;
    }

    protected boolean hasRMS() { return false; }
    
    protected BooleanControl createRMSControl() {
        BooleanControl c = new BooleanControl(RMS+idOffset, getString("RMS"), false);
        c.setStateColor(true, Color.YELLOW);
        return c;
    }
    
    protected ControlLaw getAttackLaw() {
    	return ATTACK_LAW;
    }
    
    protected FloatControl createAttackControl() {
        ControlLaw law = getAttackLaw();
        return new FloatControl(ATTACK+idOffset, getString("Attack"), law, 0.1f, law.getMinimum());
    }

    protected boolean hasHold() { return false; }

    protected ControlLaw getHoldLaw() {
    	return HOLD_LAW;
    }
    
    protected FloatControl createHoldControl() {
        return new FloatControl(HOLD+idOffset, getString("Hold"), getHoldLaw(), 1f, 10f);
    }

    protected ControlLaw getReleaseLaw() {
    	return RELEASE_LAW;
    }
    
    protected FloatControl createReleaseControl() {
        ControlLaw law = getReleaseLaw();
        return new FloatControl(RELEASE+idOffset, getString("Release"), law, 1f, law.getMinimum());
    }

    protected boolean hasDryGain() { return false; }

    protected ControlLaw getDryGainLaw() {
        return DRY_GAIN_LAW;
    }
    
    protected FloatControl createDryGainControl() {
        return new FloatControl(DRY_GAIN+idOffset, getString("Dry"), getDryGainLaw(), 1f, 0);
    }

    protected boolean hasGain() { return false; }

    protected ControlLaw getGainLaw() {
    	return GAIN_LAW;
    }
    
    protected FloatControl createGainControl() {
        return new FloatControl(GAIN+idOffset, getString("Gain"), getGainLaw(), 1f, 0);
    }

    protected boolean hasDepth() { return false; }

    protected ControlLaw getDepthLaw() {
    	return DEPTH_LAW;
    }
    
    protected FloatControl createDepthControl() {
        FloatControl depthC = new FloatControl(DEPTH+idOffset, getString("Depth"), getDepthLaw(), 1f, -40);
        depthC.setInsertColor(Color.lightGray);
        return depthC;
    }

    protected boolean hasHysteresis() {
        return false;
    }
    
    protected ControlLaw getHysteresisLaw() {
        return HYSTERESIS_LAW;
    }
    
    protected FloatControl createHysteresisControl() {
        FloatControl hystC = new FloatControl(HYSTERESIS+idOffset, getString("Hysteresis"), getHysteresisLaw(), 1f, 0f);
        hystC.setInsertColor(Color.LIGHT_GRAY);
        return hystC;
    }
    
    protected boolean hasKey() { return false; }
    
    protected TapControl createKeyControl() {
    	return new TapControl(KEY+idOffset, "Key");
    }
    
//	implement DynamicsProcess.Variables

    public float getThreshold() {
        return threshold;
    }

    public float getInverseThreshold() {
        return inverseThreshold;
    }

    public float getThresholddB() {
        return thresholddB;
    }
    
    public float getKneedB() {
        return kneedB;
    }

    public float getInverseRatio() {
        return inverseRatio;
    }
    
    public float getAttack() {
        return attack;
    }

    public int getHold() {
        return hold;
    }

    public float getRelease() {
        return release;
    }

    public float getDryGain() {
        return dryGain;
    }
    
    public float getGain() {
        return gain;
    }

    public float getDepth() {
        return depth;
    }

    public float getHysteresis() {
        return hysteresis;
    }
    
    public AudioBuffer getKeyBuffer() {
        return key;
    }

    public boolean isRMS() { return rms; } // !!!
    
    public void setDynamicGain(float dynamicGain) {
        if ( gainReductionIndicator == null ) return;
        // ideally we'd offload the log to another thread !!!
        // and annoyingly sometimes we already know the reduction in dBs
        gainReductionIndicator.setValue((float)(20*Math.log(dynamicGain)));
    }
    
    protected ControlLaw getInverseRatioLaw() { return INVERSE_RATIO_LAW; }
    
    private static final String INFINITY = "Infinity";
    private static String[] ratioPresets2 = { "2", "4", "10", getString(INFINITY), "-10", "-4", "-2" };
    
    // @TODO presets 2, 4, 10, Infinity, -10, -4, -2
    private class InverseRatioControl extends FloatControl
    {
        public InverseRatioControl() {
            super(RATIO+idOffset, getString("Ratio"), getInverseRatioLaw(), 0.1f, 0.5f);
            setInsertColor(Color.BLUE);
        }

        @Override 
        public String getValueString() {
            float ratio = 1f / getValue();
            int dp = Math.abs(ratio) >= 10f ? 0 : 1;
            return String.format("%1$."+dp+"f", ratio);
        }
        
        public String[] getPresetNames() {
            return ratioPresets2;
        }

        public void applyPreset(String name) {
            if ( getString(INFINITY).equals(name) ) {
                setValue(0f);
                return;
            }
            setValue(1f / Integer.parseInt(name));
        }
        
    }
    
}
