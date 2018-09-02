package uk.org.toot.dsp.jSTK.instrument;

import uk.org.toot.dsp.jSTK.Delay;
import uk.org.toot.dsp.jSTK.DelayA;
import uk.org.toot.dsp.jSTK.Noise;
import uk.org.toot.dsp.jSTK.OnePole;
import uk.org.toot.dsp.jSTK.OneZero;

/*
    This class implements a simple plucked string
    physical model based on the Karplus-Strong
    algorithm.

    This is a digital waveguide model, making its
    use possibly subject to patents held by
    Stanford University, Yamaha, and others.
    There exist at least two patents, assigned to
    Stanford, bearing the names of Karplus and/or
    Strong.

    by Perry R. Cook and Gary P. Scavone, 1995 - 2007.
	java version by Steve Taylor 2008
 */
public class PluckedString implements Instrument
{
	private DelayA   delayLine_ = new DelayA();
	private OneZero  loopFilter_ = new OneZero();
	private OnePole  pickFilter_ = new OnePole();
	private Noise    noise_ = new Noise();
	private Delay    pickComb = new Delay();
	
	private float loopGain_;
	private int length_;
	private float lowestFrequency = 25;
	private float lastOutput_;
	private int sampleRate;
	private float frequency;

	public PluckedString(float lowF) {
		lowestFrequency = lowF;
		setSampleRate(44100); // provisional
	}
	
	/* (non-Javadoc)
	 * @see uk.org.toot.dsp.jSTK.instrument.Instrument#setSampleRate(int)
	 */
	public void setSampleRate(int rate) {
		sampleRate = rate;
		length_ = (int) (sampleRate / lowestFrequency + 1);
		delayLine_.setMaximumDelay( length_ );
		delayLine_.setDelay( 0.5f * length_ );
		clear();
	}
	
	/* (non-Javadoc)
	 * @see uk.org.toot.dsp.jSTK.instrument.Instrument#clear()
	 */
	public void clear()	{
		delayLine_.clear();
		loopFilter_.clear();
		pickFilter_.clear();
	}
	
	/* (non-Javadoc)
	 * @see uk.org.toot.dsp.jSTK.instrument.Instrument#setFrequency(float)
	 */
	public void setFrequency(float frequency) {
		this.frequency = frequency < lowestFrequency ? lowestFrequency : frequency;
		// Delay = length - approximate filter delay.
		float delay = (sampleRate / frequency) - 0.5f;
		delayLine_.setDelay( delay );

		loopGain_ = 0.995f + (frequency * 0.000005f);
		if ( loopGain_ >= 1.0f ) loopGain_ = 0.99999f;
	}

	public void pluck(float amplitude, float excitation, float position) {
		loopGain_ = 0.999f;
		pickComb.setDelay(position * length_);
		float f = frequency / 1000;
		pickFilter_.setPole(0.999f - (excitation * 0.05f) - (f * 0.1f));
		pickFilter_.setGain(amplitude * 0.5f);
		for ( int i = 0; i < length_; i++ ) {
			// Fill delay with noise additively with current contents.
			delayLine_.tick( 0.6f * delayLine_.lastOut() + pickComb.tick( pickFilter_.tick( noise_.tick() ) ) );
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.toot.dsp.jSTK.instrument.Instrument#noteOn(float, float, float, float)
	 */
	public void noteOn(float frequency, float amplitude, float excitation, float position) {
		setFrequency(frequency);
		pluck(amplitude, excitation, position);
	}

	/* (non-Javadoc)
	 * @see uk.org.toot.dsp.jSTK.instrument.Instrument#noteOff(float)
	 */
	public void noteOff(float amplitude) {
		loopGain_ = 1.0f - amplitude;
		if ( loopGain_ < 0.0 ) {
			loopGain_ = 0.0f;
		} else if ( loopGain_ > 1.0 ) {
			loopGain_ = 0.99999f;
		}
	}


	/* (non-Javadoc)
	 * @see uk.org.toot.dsp.jSTK.instrument.Instrument#getSample()
	 */
	public float getSample() {
		// Here's the whole inner loop of the instrument!!
		lastOutput_ = delayLine_.tick( loopFilter_.tick( delayLine_.lastOut() * loopGain_ ) ); 
	    return 3f * lastOutput_;
	}


}
