package uk.org.toot.dsp.jSTK;

/***************************************************/
/*! \class OneZero
    \brief STK one-zero filter class.

    This protected Filter subclass implements
    a one-zero digital filter.  A method is
    provided for setting the zero position
    along the real axis of the z-plane while
    maintaining a constant filter gain.

    by Perry R. Cook and Gary P. Scavone, 1995 - 2007.
*/
/***************************************************/
public class OneZero extends Filter
{
	public OneZero()
	{
		float[] b = { 0.5f, 0.5f };
		float[] a = { 1.0f };
		
		super.setCoefficients( b, a, true );
	}

	public OneZero(float theZero)
	{
		float[] b = new float[2];
		float[] a = new float[1];

	  // Normalize coefficients for unity gain.
	  if (theZero > 0.0)
	    b[0] = 1.0f / ((float) 1.0 + theZero);
	  else
	    b[0] = 1.0f / ((float) 1.0 - theZero);

	  b[1] = -theZero * b[0];
	  super.setCoefficients( b, a, true );
	}

	public void setB0(float b0)
	{
	  b_[0] = b0;
	}

	public void setB1(float b1)
	{
	  b_[1] = b1;
	}

	public void setZero(float theZero)
	{
	  // Normalize coefficients for unity gain.
	  if (theZero > 0.0)
	    b_[0] = 1.0f / (1.0f + theZero);
	  else
	    b_[0] = 1.0f / (1.0f - theZero);

	  b_[1] = -theZero * b_[0];
	}

	public float tick( float input )
	{
	  inputs_[0] = gain_ * input;
	  outputs_[0] = b_[1] * inputs_[1] + b_[0] * inputs_[0];
	  inputs_[1] = inputs_[0];

	  return outputs_[0];
	}

/*	StkFrames& OneZero :: tick( StkFrames& frames, unsigned int channel )
	{
	  return Filter::tick( frames, channel );
	} */

}
