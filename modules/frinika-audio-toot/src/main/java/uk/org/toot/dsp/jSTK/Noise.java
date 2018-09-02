package uk.org.toot.dsp.jSTK;

/***************************************************/
/*! \class Noise
    \brief STK noise generator.

    Generic random number generation.

    by Perry R. Cook and Gary P. Scavone, 1995 - 2007.
*/
/***************************************************/
public class Noise
{
	protected float lastOutput_;

	public float tick()
	{
	  lastOutput_ = (float)(2 * Math.random());
	  lastOutput_ -= 1.0f;
	  return lastOutput_;
	}

}
