package uk.org.toot.dsp.jSTK;

/***************************************************/
/*! \class OnePole
    \brief STK one-pole filter class.

    This protected Filter subclass implements
    a one-pole digital filter.  A method is
    provided for setting the pole position along
    the real axis of the z-plane while maintaining
    a constant peak filter gain.

    by Perry R. Cook and Gary P. Scavone, 1995 - 2007.
 */
/***************************************************/
public class OnePole extends Filter
{
	public OnePole()
	{
		float[] b = { 0.1f };
		float[] a = { 1.0f, 1.0f };
		a[1] = -0.9f;
		super.setCoefficients( b, a , true);
	}

	public OnePole(float thePole)
	{
		float[] b = new float[1];
		float[] a = { 1.0f, 1.0f };
		a[1] = -thePole;

		// Normalize coefficients for peak unity gain.
		if (thePole > 0.0)
			b[0] = 1.0f - thePole;
		else
			b[0] = 1.0f + thePole;

		super.setCoefficients( b, a, true );
	}

	public void setB0(float b0)
	{
		b_[0] = b0;
	}

	public void setA1(float a1)
	{
		a_[1] = a1;
	}

	public void setPole(float thePole)
	{
		// Normalize coefficients for peak unity gain.
		if (thePole > 0.0)
			b_[0] = 1.0f - thePole;
		else
			b_[0] = 1.0f + thePole;

		a_[1] = -thePole;
	}

	public float tick( float input )
	{
		inputs_[0] = gain_ * input;
		outputs_[0] = b_[0] * inputs_[0] - a_[1] * outputs_[1];
		outputs_[1] = outputs_[0];

		return outputs_[0];
	}

	/*	StkFrames& OnePole :: tick( StkFrames& frames, unsigned int channel )
	{
	  return Filter::tick( frames, channel );
	} */

}
