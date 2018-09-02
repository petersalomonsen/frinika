package uk.org.toot.dsp.jSTK;


/***************************************************/
/*! \class Delay
    \brief STK non-interpolating delay line class.

    This protected Filter subclass implements
    a non-interpolating digital delay-line.
    A fixed maximum length of 4095 and a delay
    of zero is set using the default constructor.
    Alternatively, the delay and maximum length
    can be set during instantiation with an
    overloaded constructor.
    
    A non-interpolating delay line is typically
    used in fixed delay-length applications, such
    as for reverberation.

    by Perry R. Cook and Gary P. Scavone, 1995 - 2007.
*/
/***************************************************/
public class Delay extends Filter 
{
	protected int inPoint_;
	protected int outPoint_;
	protected float delay_;

	public Delay()
	{
	  // Default maximum delay length set to 4095.
	  inputs_ = new float[4096];

	  inPoint_ = 0;
	  outPoint_ = 0;
	  delay_ = 0;
	}

	public Delay(int delay, int maxDelay)
	{
	  // Writing before reading allows delays from 0 to length-1. 
	  // If we want to allow a delay of maxDelay, we need a
	  // delay-line of length = maxDelay+1.
	  if ( maxDelay < 1 ) {
		  throw new IllegalArgumentException("Delay::Delay: maxDelay must be > 0!\n");
	  }

	  if ( delay > maxDelay ) {
		  throw new IllegalArgumentException("Delay::Delay: maxDelay must be > than delay argument!\n");
	  }

	  if ( maxDelay > inputs_.length-1 ) {
	    inputs_ = new float[maxDelay+1];
	    clear();
	  }

	  inPoint_ = 0;
	  setDelay( delay );
	}

	public void clear()
	{
	  for (int i=0; i<inputs_.length; i++)
	    inputs_[i] = 0f;
	  outputs_[0] = 0f;
	}

	public void setMaximumDelay(int delay)
	{
	  if ( delay < inputs_.length ) return;

	  if ( delay < 0 ) {
		  throw new IllegalArgumentException("Delay::setMaximumDelay: argument (" + delay + ") less than zero!\n");
	  } else if (delay < delay_ ) {
		  throw new IllegalArgumentException("Delay::setMaximumDelay: argument (" + delay + ") less than current delay setting (" + delay_ + ")!\n");
	  }

	  //inputs_.resize( delay + 1 );
	  inputs_ = new float[delay+1];
	  
	}

	public void setDelay(float delay)
	{
	  if ( delay > inputs_.length - 1 ) { // The value is too big.
	    System.err.println("Delay::setDelay: argument (" + delay + ") too big ... setting to maximum!\n");

	    // Force delay to maximum length.
	    outPoint_ = inPoint_ + 1;
	    if ( outPoint_ == inputs_.length ) outPoint_ = 0;
	    delay_ = inputs_.length - 1;
	  }
	  else if ( delay < 0 ) {
		  System.err.println("Delay::setDelay: argument (" + delay + ") less than zero ... setting to zero!\n");

	    outPoint_ = inPoint_;
	    delay_ = 0;
	  }
	  else { // read chases write
	    if ( inPoint_ >= delay ) outPoint_ = inPoint_ - (int)delay;
	    else outPoint_ = inputs_.length + inPoint_ - (int)delay;
	    delay_ = delay;
	  }
	}

	public float getDelay()
	{
	  return delay_;
	}

	public float energy()
	{
	  int i;
	  float e = 0;
	  if (inPoint_ >= outPoint_) {
	    for (i=outPoint_; i<inPoint_; i++) {
	      float t = inputs_[i];
	      e += t*t;
	    }
	  } else {
	    for (i=outPoint_; i<inputs_.length; i++) {
	      float t = inputs_[i];
	      e += t*t;
	    }
	    for (i=0; i<inPoint_; i++) {
	      float t = inputs_[i];
	      e += t*t;
	    }
	  }
	  return e;
	}

	public float contentsAt(int tapDelay)
	{
	  int i = tapDelay;
	  if (i < 1) {
		  System.err.println("Delay::contentsAt: argument (" + tapDelay + ") too small!");
	    return 0f;
	  }
	  else if (i > delay_) {
		  System.err.println("Delay::contentsAt: argument (" + tapDelay + ") too big!");
	    return 0f;
	  }

	  int tap = inPoint_ - i;
	  if (tap < 0) // Check for wraparound.
	    tap += inputs_.length;

	  return inputs_[tap];
	}

	public float lastOut()
	{
	  return super.lastOut();
	}

	public float nextOut()
	{
	  return inputs_[outPoint_];
	}

	protected float computeSample( float input )
	{
	  inputs_[inPoint_++] = input;

	  // Check for end condition
	  if (inPoint_ == inputs_.length)
	    inPoint_ = 0;

	  // Read out next value
	  outputs_[0] = inputs_[outPoint_++];

	  if (outPoint_ == inputs_.length)
	    outPoint_ = 0;

	  return outputs_[0];
	}

	public float tick( float input )
	{
	  return computeSample( input );
	}

/*	StkFrames& Delay :: tick( StkFrames& frames, unsigned int channel )
	{
	  return Filter::tick( frames, channel );
	} */

}
