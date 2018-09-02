package uk.org.toot.synth.channels.pluck;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.FloatDenormals;
import uk.org.toot.synth.PolyphonicSynthChannel;

/**
 * from pluck.c - elementary waveguide simulation of plucked strings - JOS 6/6/92
 * @author st
 */
public class PluckSynthChannel extends PolyphonicSynthChannel
{
	private PluckSynthControls controls;

	public PluckSynthChannel(PluckSynthControls controls) {
		super(controls.getName());
		this.controls = controls;
	}

/*	protected void setSampleRate(int rate) {
		super.setSampleRate(rate);
	} */
	
	@Override
	protected Voice createVoice(int pitch, int velocity, int sampleRate) {
		return new Example3Voice(pitch, velocity, sampleRate);
	}

	public class Example3Voice extends AbstractVoice
	{
		private Delay upperDelay;
		private Delay lowerDelay;
		private int railLength;
		private int pickupSample;
		private float filterState = 0f;
		private float loopGain;
				
		private float pickup;
		private float pick;
		
		private float ampT;
		private float level;
		
		public Example3Voice(int pitch, int velocity, int sampleRate) {
			super(pitch, velocity);
			railLength = (int)(sampleRate / frequency / 2 + 1);
			upperDelay = new UpperDelay(railLength);
			lowerDelay = new LowerDelay(railLength);
			
			pickup = controls.getPickup();
			pick = controls.getPick();

			float ampTracking = controls.getVelocityTrack();
			ampT = velocity == 0 ? 0f : (1 - ampTracking * (1 - amplitude));

			/* 
			 * Round pick position to nearest spatial sample.
			 * A pick position at x = 0 is not allowed. 
			 */
		    int pickSample = (int)Math.max(pick * railLength, 1); 
			float upslope = ampT / pickSample;
		    float downslope = ampT / (railLength - pickSample - 1);
		    float initialShape[] = new float[railLength];
		    for ( int i = 0; i < pickSample; i++ )
		    	initialShape[i] = upslope * i;
		    for ( int i = pickSample; i < railLength; i++ )
		    	initialShape[i] = downslope * (railLength - 1 - i);

		    /*
		     * Initial conditions for the ideal plucked string.
		     * "Past history" is measured backward from the end of the array.
		     */
		    lowerDelay.set(initialShape, 0.5f);
		    upperDelay.set(initialShape, 0.5f);
		    
		    pickupSample = (int)(pickup * railLength);
			setSampleRate(sampleRate);
			
			loopGain = Math.min(0.995f + (frequency * 0.000005f), 0.99999f);
		}

		public void setSampleRate(int rate) {
			// we can't change sample rate of a playing voice
			// because the digital waveguide model is set up
			// for the sample rate and the note frequency
			// so we can't pitch bend either!!! TODO
		}
		
		public boolean mix(AudioBuffer buffer) {
			if ( release ) loopGain *= 0.995f;
			level = controls.getLevel() * 4f;
			return super.mix(buffer);
		}
		
		private float bridgeReflection(float insamp) {
		    /* Implement a one-pole lowpass with feedback coefficient = 0.5 */
		    filterState = 0.5f * filterState + 0.5f * insamp;
		    return filterState * loopGain; 	/* ensure useful LF loss? */
		}

		protected float getSample() {
		    float yp0,ym0,ypM,ymM;
		    float outsamp;

		    /* Output at pickup location */
		    outsamp  = upperDelay.access(pickupSample)
		    		 + lowerDelay.access(pickupSample);

		    ym0 = lowerDelay.access(1);     /* Sample traveling into "bridge" */
		    ypM = upperDelay.access(upperDelay.length - 2); /* Sample to "nut" */

		    ymM = -ypM;                    	/* Inverting reflection at rigid nut */
		    yp0 = -bridgeReflection(ym0);  	/* Reflection at yielding bridge */
		    yp0 = FloatDenormals.zeroDenorm(yp0);

		    /* String state update */
		    upperDelay.update(yp0); 		/* Decrement pointer and then update */
		    lowerDelay.update(ymM); 		/* Update and then increment pointer */

		    return level * outsamp;
		}

		protected boolean isComplete() {
			return loopGain < 0.1f; // ??? might be well too late TODO
		}
	}
	
	protected static abstract class Delay
	{
		protected int length;
		protected float data[];
		protected int pointer;
		protected int end;
		
		public Delay(int length) {
			this.length = length;
			data = new float[length];
			pointer = 0;
			end = length - 1;
		}
		
		public void set(float[] values, float scale) {
			for ( int i = 0; i < length; i++ ) {
				data[i] = values[i] * scale;
			}
		}
		
		/*
		 * Returns sample "position" samples into delay-line's past.
		 * Position "0" points to the most recently inserted sample.
		 */
		public float access(int position) {
			int out = pointer + position;
			while ( out < 0 ) out += length;
			while ( out > end ) out -= length;
			return data[out];
		}
		
		public abstract void update(float sample);
	}
	
	protected static class UpperDelay extends Delay
	{
		public UpperDelay(int length) {
			super(length);
		}

		/*
		 * Decrements current upper delay-line pointer position (i.e.
		 * the wave travels one sample to the right), moving it to the
		 * "effective" x = 0 position for the next iteration.  The
		 * "bridge-reflected" sample from lower delay-line is then placed
		 * into this position.
		 */
		@Override
		public void update(float sample) {
			pointer--;
			if ( pointer < 0 ) pointer = end;
			data[pointer] = sample;			
		}
	}
	
	protected static class LowerDelay extends Delay
	{
		public LowerDelay(int length) {
			super(length);
		}

		/*
		 * Places "nut-reflected" sample from upper delay-line into
		 * current lower delay-line pointer location (which represents
		 * x = 0 position).  The pointer is then incremented (i.e. the
		 * wave travels one sample to the left), turning the previous
		 * position into an "effective" x = L position for the next
		 * iteration.
		 */
		@Override
		public void update(float sample) {
			data[pointer] = sample;
			pointer++;
			if ( pointer > end ) pointer = 0; 
		}		
	}
}