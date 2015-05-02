// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.envelope;

/**
 * An ASR Envelope Generator.
 * Uses efficient exponential difference equations for good sounding segments.
 * @author st
 *
 */
public class ASREnvelopeGenerator 
{
	public enum State { ATTACK, SUSTAIN, RELEASE, COMPLETE };
	
	private State state = State.ATTACK;
	
	private float attackCoeff;
	private boolean sustain;
	private float releaseCoeff;
	
	private float envelope = 0f;

	public ASREnvelopeGenerator(ASREnvelopeVariables vars) {
		attackCoeff = vars.getAttackCoeff();
		sustain = true; // TODO !!!
		releaseCoeff = vars.getReleaseCoeff();
	}
	
	public float getEnvelope(boolean release) {
		if ( release && state != State.COMPLETE ) state = State.RELEASE; // !!!
		switch ( state ) {
		case ATTACK:
			envelope += attackCoeff * (1f - envelope);
			if ( envelope > 0.99f ) {
				state = sustain ? State.SUSTAIN : State.RELEASE;
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
}
