// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.envelope;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.modules.envelope.EnvelopeControlIds.*;

import uk.org.toot.control.BooleanControl;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LogLaw;

public class ASREnvelopeControls extends CompoundControl
	implements ASREnvelopeVariables
{
	private FloatControl attackControl;
	private BooleanControl sustainControl;
	private FloatControl releaseControl;
	
	private int sampleRate = 44100;

	private float attack, release; // 0.. coefficients
	private boolean sustain;

	private int idOffset = 0;
	
	// mutiplies the max attack, decay and release times
	private float timeMultiplier;
	
	public ASREnvelopeControls(int instanceIndex, String name, int idOffset) {
		this(instanceIndex, name, idOffset, 1f);
	}
		
	public ASREnvelopeControls(int instanceIndex, String name, int idOffset, float timeMultiplier) {
		this(EnvelopeIds.DAHDSR_ENVELOPE_ID, instanceIndex, name, idOffset, timeMultiplier);
	}
		
	public ASREnvelopeControls(int id, int instanceIndex, String name, final int idOffset, float timeMultiplier) {
		super(id, instanceIndex, name);
		this.idOffset = idOffset;
		this.timeMultiplier = timeMultiplier;
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
	}
	
    @Override
    protected void derive(Control c) {
		switch ( c.getId()-idOffset ) {
		case ATTACK: attack = deriveAttack(); break;
		case SUSTAIN: sustain = deriveSustain(); break;
		case RELEASE: release = deriveRelease(); break;
		}    	
    }
    
	protected boolean hasDelay() {
		return true;
	}
	
	protected void createControls() {
		float m = timeMultiplier;
		add(attackControl = createAttackControl(1f, 10000f*m, 1f)); 	// ms
		add(sustainControl = createSustainControl());
		add(releaseControl = createReleaseControl(20, 2000*m, 200f));		// ms
	}

    protected void deriveSampleRateIndependentVariables() {
    	sustain = deriveSustain();
    }

    protected void deriveSampleRateDependentVariables() {
    	attack = deriveAttack();
    	release = deriveRelease();
    }
    
	protected boolean deriveSustain() {
		return sustainControl.getValue(); // 0..1		
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

    protected float deriveAttack() {
        return deriveTimeFactor(attackControl.getValue());
    }

    protected float  deriveRelease() {
        return deriveTimeFactor(releaseControl.getValue());
    }

    protected FloatControl createAttackControl(float min, float max, float init) {
        ControlLaw law = new LogLaw(min, max, "ms");
        return new FloatControl(ATTACK+idOffset, getString("Attack"), law, 0.1f, init);
    }

	protected BooleanControl createSustainControl() {
        BooleanControl sustainControl = new BooleanControl(SUSTAIN+idOffset, getString("Sustain"), true, "on", "off");
        return sustainControl;
	}

    protected FloatControl createReleaseControl(float min, float max, float init) {
        ControlLaw law = new LogLaw(min, max, "ms");
        return new FloatControl(RELEASE+idOffset, getString("Release"), law, 1f, init);
    }

	public float getAttackCoeff() {
		return attack;
	}

	public boolean getSustain() {
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
