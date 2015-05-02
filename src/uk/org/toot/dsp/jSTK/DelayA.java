package uk.org.toot.dsp.jSTK;


/***************************************************/
/*! \class DelayA
    \brief STK allpass interpolating delay line class.

    This Delay subclass implements a fractional-length digital
    delay-line using a first-order allpass filter.  A fixed maximum
    length of 4095 and a delay of 0.5 is set using the default
    constructor.  Alternatively, the delay and maximum length can be
    set during instantiation with an overloaded constructor.

    An allpass filter has unity magnitude gain but variable phase
    delay properties, making it useful in achieving fractional delays
    without affecting a signal's frequency magnitude response.  In
    order to achieve a maximally flat phase delay response, the
    minimum delay possible in this implementation is limited to a
    value of 0.5.

    by Perry R. Cook and Gary P. Scavone, 1995 - 2007.
*/
/***************************************************/

public class DelayA extends Delay
{
	protected float alpha_;
	protected float coeff_;
	protected float apInput_;
	protected float nextOutput_;
	protected boolean doNextOut_;

	public DelayA()
	{
	  setDelay( 0.5f );
	  apInput_ = 0f;
	  doNextOut_ = true;
	}

	public DelayA(float delay, int maxDelay)
	{
	  if ( delay < 0.0 || maxDelay < 1 ) {
		  throw new IllegalArgumentException("DelayA::DelayA: delay must be >= 0.0, maxDelay must be > 0!");
	  }

	  if ( delay > (float) maxDelay ) {
		  throw new IllegalArgumentException("DelayA::DelayA: maxDelay must be > than delay argument!");
	  }

	  // Writing before reading allows delays from 0 to length-1. 
	  if ( maxDelay > inputs_.length-1 ) {
	    inputs_ = new float[maxDelay+1];
	    clear();
	  }

	  inPoint_ = 0;
	  setDelay(delay);
	  apInput_ = 0f;
	  doNextOut_ = true;
	}

	public void clear()
	{
	  super.clear();
	  apInput_ = 0f;
	}

	public void setDelay(float delay)  
	{
	  float outPointer;
	  int length = inputs_.length;

	  if ( delay > inputs_.length - 1 ) { // The value is too big.
		  System.err.println("DelayA::setDelay: argument (" + delay + ") too big ... setting to maximum!");

	    // Force delay to maxLength
	    outPointer = inPoint_ + 1f;
	    delay_ = length - 1;
	  }
	  else if (delay < 0.5) {
		  System.err.println("DelayA::setDelay: argument (" + delay + ") less than 0.5 not possible!");
		  
	    outPointer = inPoint_ + 0.4999999999f;
	    delay_ = 0.5f;
	  }
	  else {
	    outPointer = inPoint_ - delay + 1.0f;     // outPoint chases inpoint
	    delay_ = delay;
	  }

	  if (outPointer < 0)
	    outPointer += length;  // modulo maximum length

	  outPoint_ = (int) outPointer;         // integer part
	  if ( outPoint_ == length ) outPoint_ = 0;
	  alpha_ = 1.0f + outPoint_ - outPointer; // fractional part

	  if (alpha_ < 0.5) {
	    // The optimal range for alpha is about 0.5 - 1.5 in order to
	    // achieve the flattest phase delay response.
	    outPoint_ += 1;
	    if (outPoint_ >= length) outPoint_ -= length;
	    alpha_ += (float) 1.0;
	  }

	  coeff_ = ((float) 1.0 - alpha_) / 
	    ((float) 1.0 + alpha_);         // coefficient for all pass
	}

	public float getDelay()
	{
	  return delay_;
	}

	public float nextOut()
	{
	  if ( doNextOut_ ) {
	    // Do allpass interpolation delay.
	    nextOutput_ = -coeff_ * outputs_[0];
	    nextOutput_ += apInput_ + (coeff_ * inputs_[outPoint_]);
	    doNextOut_ = false;
	  }

	  return nextOutput_;
	}

	public float computeSample( float input )
	{
	  inputs_[inPoint_++] = input;

	  // Increment input pointer modulo length.
	  if (inPoint_ == inputs_.length)
	    inPoint_ = 0;

	  outputs_[0] = nextOut();
	  doNextOut_ = true;

	  // Save the allpass input and increment modulo length.
	  apInput_ = inputs_[outPoint_++];
	  if (outPoint_ == inputs_.length)
	    outPoint_ = 0;

	  return outputs_[0];
	}

}
