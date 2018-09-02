// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.envelope;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.modules.envelope.EnvelopeControlIds.*;

import java.awt.Color;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.control.LogLaw;

public class EnvelopeControls extends CompoundControl
	implements EnvelopeVariables
{
	private FloatControl delayControl;
	private FloatControl attackControl;
	private FloatControl holdControl;
	private FloatControl decayControl;
	private FloatControl sustainControl;
	private FloatControl releaseControl;
	
	private int sampleRate = 44100;

	private int delay = 0, hold; // in samples
	private float attack, decay, sustain, release; // 0.. coefficients

	private int idOffset = 0;
	
	private boolean hasDelay;
	private boolean hasHold;
	private boolean hasSustain;
	
	// mutiplies the max attack, decay and release times
	private float timeMultiplier;
	
	// a default ADSR envelope
	public EnvelopeControls(int instanceIndex, String name, int idOffset) {
		this(instanceIndex, name, idOffset, "S", 1f);
	}
		
	// options are D, H and S, i.e. "S", "DS", "D", "HS" etc.
	public EnvelopeControls(int instanceIndex, String name, int idOffset, String options) {
		this(instanceIndex, name, idOffset, options, 1f);
	}
		
	public EnvelopeControls(int instanceIndex, String name, int idOffset, String options, float timeMultiplier) {
		this(EnvelopeIds.DAHDSR_ENVELOPE_ID, instanceIndex, name, idOffset, options, timeMultiplier);
	}
		
	public EnvelopeControls(int id, int instanceIndex, String name, final int idOffset, String options, float timeMultiplier) {
		super(id, instanceIndex, name);
		this.idOffset = idOffset;
		hasDelay = options.startsWith("D");
		hasHold = options.indexOf("H") > -1;
		hasSustain = options.indexOf("S") > -1;
		this.timeMultiplier = timeMultiplier;
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
	}
	
    @Override
    protected void derive(Control c) {
		switch ( c.getId()-idOffset ) {
		case DELAY:	delay = deriveDelay(); break;
		case ATTACK: attack = deriveAttack(); break;
		case HOLD: hold = deriveHold(); break;
		case DECAY: decay = deriveDecay(); break;
		case SUSTAIN: sustain = deriveSustain(); break;
		case RELEASE: release = deriveRelease(); break;
		}
    }
    
	protected void createControls() {
		float m = timeMultiplier;
		if ( hasDelay ) {
			add(delayControl = createDelayControl(0f, 1000f*m, 0f));	// ms
		}
		add(attackControl = createAttackControl(1f, 10000f*m, 1f)); 	// ms
		if ( hasHold ) {
			add(holdControl = createHoldControl(0, 1000, 10)); 			// ms
		}
		if ( hasSustain ) {
			add(decayControl = createDecayControl(20f, 20000f*m, 200f));// ms
			add(sustainControl = createSustainControl());
		}
		add(releaseControl = createReleaseControl(20, 2000*m, 200f));	// ms
	}

    protected void deriveSampleRateIndependentVariables() {
    	sustain = deriveSustain();
    }

    protected void deriveSampleRateDependentVariables() {
   		delay = deriveDelay();
    	attack = deriveAttack();
    	hold = deriveHold();
    	decay = deriveDecay();
    	release = deriveRelease();
    }
    
    private static float LOG_0_01 = (float)Math.log(0.01);
    // http://www.physics.uoguelph.ca/tutorials/exp/Q.exp.html
	// http://www.musicdsp.org/showArchiveComment.php?ArchiveID=136
    // return k per sample for 99% in specified milliseconds
    protected float deriveTimeFactor(float milliseconds) {
    	float ns = milliseconds * sampleRate / 1000;
        float k = LOG_0_01 / ns ; // k, per sample
        return (float)(1f -Math.exp(k));
    }

	protected int deriveDelay() {
		if ( !hasDelay ) return 0;
		return (int)(delayControl.getValue() * sampleRate / 1000);		
	}

	// atack is linear
    protected float deriveAttack() {
    	float ns = attackControl.getValue() * sampleRate / 1000;
//        return deriveTimeFactor(attackControl.getValue());
    	return 1/ns;
    }

    protected int deriveHold() {
    	if ( !hasHold ) return 0;
        return (int)(holdControl.getValue() * sampleRate / 1000);
    }

	protected float deriveDecay() {
		if ( !hasSustain ) return 0.001f;
		return deriveTimeFactor(decayControl.getValue());
	}

	protected float deriveSustain() {
		if ( !hasSustain ) return 1f;
		return sustainControl.getValue(); // 0..1		
	}

    protected float  deriveRelease() {
        return deriveTimeFactor(releaseControl.getValue());
    }

	protected FloatControl createDelayControl(float min, float max, float init) {
        ControlLaw law = new LinearLaw(min, max, "ms");
        return new FloatControl(DELAY+idOffset, getString("Delay"), law, 1f, init);
	}

    protected FloatControl createAttackControl(float min, float max, float init) {
        ControlLaw law = new LogLaw(min, max, "ms");
        return new FloatControl(ATTACK+idOffset, getString("Attack"), law, 0.1f, init);
    }

    protected FloatControl createHoldControl(float min, float max, float init) {
        ControlLaw law = new LinearLaw(min, max, "ms");
        return new FloatControl(HOLD+idOffset, getString("Hold"), law, 1f, init);
    }

	protected FloatControl createDecayControl(float min, float max, float init) {
        ControlLaw law = new LogLaw(min, max, "ms");
        return new FloatControl(DECAY+idOffset, getString("Decay"), law, 1f, init);
	}

	protected FloatControl createSustainControl() {
        FloatControl sustainControl = new FloatControl(SUSTAIN+idOffset, getString("Sustain"), LinearLaw.UNITY, 0.01f, 0.5f);
        sustainControl.setInsertColor(Color.lightGray);
        return sustainControl;
	}

    protected FloatControl createReleaseControl(float min, float max, float init) {
        ControlLaw law = new LogLaw(min, max, "ms");
        return new FloatControl(RELEASE+idOffset, getString("Release"), law, 1f, init);
    }

	public int getDelayCount() {
		return delay;
	}

	public float getAttackCoeff() {
		return attack;
	}

	public int getHoldCount() {
		return hold;
	}

	public float getDecayCoeff() {
		return decay;
	}

	public float getSustainLevel() {
		return sustain;
	}

	public float getReleaseCoeff() {
		return release;
	}

	public void setSampleRate(int rate) {
		if ( sampleRate != rate ) {
			sampleRate = rate;
			deriveSampleRateDependentVariables();
		}
	}

}
