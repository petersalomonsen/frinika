// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.envelope;

/**
 * A DAHDSR Envelope Generator.
 * Uses efficient exponential difference equations for good sounding segments.
 * Except for attack, which is linear, which helps filter cutoff frequencies and is typical
 * for analog envelope generators.
 * @author st
 *
 */
public class EnvelopeGenerator 
{
	public enum State { DELAY, ATTACK, HOLD, DECAY, SUSTAIN, RELEASE, COMPLETE };
	
	private EnvelopeVariables vars;
	
	private State state = State.COMPLETE;
	
	private int delayCount;
	private float attackCoeff;
	private int holdCount;
	private float decayCoeff;
	private float sustainLevel;
	private float releaseCoeff;
	
	private float envelope = 0f;

	public EnvelopeGenerator(EnvelopeVariables vars) {
		this.vars = vars;
	}
	
	public float getEnvelope(boolean release) {
		if ( release && state != State.COMPLETE ) state = State.RELEASE; // !!!
		switch ( state ) {
		case DELAY:
			if ( --delayCount >= 0 ) break;
			state = State.ATTACK;
			// intentional fall through
		case ATTACK:
			envelope += attackCoeff; // * (1f - envelope);
			if ( envelope > 0.99f ) {
				state = State.HOLD;
			}
			break;
		case HOLD:
			if ( --holdCount >= 0 ) break; 
			state = State.DECAY;
			// intentional fall through
		case DECAY:
			envelope -= decayCoeff * envelope;
			if ( envelope <= sustainLevel + 0.001f ) {
				state = State.SUSTAIN;
			}
			break;
		case SUSTAIN:
			break;
		case RELEASE:
			envelope -= releaseCoeff * envelope;
			if ( envelope < 0.001f ) { // -60dB cutoff !!!
				envelope = 0f;
				state = State.COMPLETE;
			}
			break;
		case COMPLETE:
			break;
		}
		return envelope;
	}
	
	public boolean isComplete() {
		return state == State.COMPLETE;
	}
	
	public void trigger() {
		delayCount = vars.getDelayCount();
		attackCoeff = vars.getAttackCoeff();
		holdCount = vars.getHoldCount();
		decayCoeff = vars.getDecayCoeff();
		sustainLevel = vars.getSustainLevel();
		releaseCoeff = vars.getReleaseCoeff();		
//		envelope = 0;
		state = State.DELAY;
	}
}
