package uk.org.toot.dsp.jSTK;

/***************************************************/
/*! \class Filter
    \brief STK filter class.

    This class implements a generic structure that
    can be used to create a wide range of filters.
    It can function independently or be subclassed
    to provide more specific controls based on a
    particular filter type.

    In particular, this class implements the standard
    difference equation:

    a[0]*y[n] = b[0]*x[n] + ... + b[nb]*x[n-nb] -
                a[1]*y[n-1] - ... - a[na]*y[n-na]

    If a[0] is not equal to 1, the filter coefficients
    are normalized by a[0].

    The \e gain parameter is applied at the filter
    input and does not affect the coefficient values.
    The default gain value is 1.0.  This structure
    results in one extra multiply per computed sample,
    but allows easy control of the overall filter gain.

    by Perry R. Cook and Gary P. Scavone, 1995 - 2007.
 */
/***************************************************/
public class Filter 
{
	protected float gain_;
	protected float[] b_;
	protected float[] a_;
	protected float[] outputs_;
	protected float[] inputs_;

	public Filter()
	{
		// The default constructor should setup for pass-through.
		gain_ = 1.0f;
		b_ = new float[] { 1f }; //b_.push_back( 1.0 );
		a_ = new float[] { 1f }; //a_.push_back( 1.0 );

		inputs_ = new float[] { 0f }; //inputs_.push_back( 0.0 );
		outputs_ = new float[] { 0f }; //outputs_.push_back( 0.0 );
	}

	public Filter(float[] bCoefficients, float[] aCoefficients )
	{
		// Check the arguments.
		if ( bCoefficients.length == 0 || aCoefficients.length == 0 ) {
			throw new IllegalArgumentException("Filter: a and b coefficient vectors must both have size > 0!");
		}

		if ( aCoefficients[0] == 0.0 ) {
			throw new IllegalArgumentException("Filter: a[0] coefficient cannot == 0!");
		}

		gain_ = 1f;

		setCoefficients(bCoefficients, aCoefficients, true);
	}

	public void clear()
	{
		int i;
		for (i=0; i<inputs_.length; i++)
			inputs_[i] = 0f;
		for (i=0; i<outputs_.length; i++)
			outputs_[i] = 0f;
	}

	public void setCoefficients(float[] bCoefficients, float[] aCoefficients, boolean clearState )
	{
		// Check the arguments.
		if ( bCoefficients.length == 0 || aCoefficients.length == 0 ) {
			throw new IllegalArgumentException("Filter::setCoefficients: a and b coefficient vectors must both have size > 0!");
		}

		if ( aCoefficients[0] == 0.0 ) {
			throw new IllegalArgumentException("Filter::setCoefficients: a[0] coefficient cannot == 0!");
		}

		setNumerator(bCoefficients, false);
		setDenominator(aCoefficients, true);
	}

	void setNumerator( float[] bCoefficients, boolean clearState )
	{
		// Check the argument.
		if ( bCoefficients.length == 0 ) {
			throw new IllegalArgumentException("Filter::setNumerator: coefficient vector must have size > 0!");
		}

		b_ = bCoefficients;
		inputs_ = new float[b_.length];

		if ( clearState ) clear();
	}

	public void setDenominator( float[] aCoefficients, boolean clearState )
	{
		// Check the argument.
		if ( aCoefficients.length == 0 ) {
			throw new IllegalArgumentException("Filter::setDenominator: coefficient vector must have size > 0!");
		}

		if ( aCoefficients[0] == 0.0 ) {
			throw new IllegalArgumentException("Filter::setDenominator: a[0] coefficient cannot == 0!");
		}

		a_ = aCoefficients;
		outputs_ = new float[a_.length];

		if ( clearState ) clear();

		// Scale coefficients by a[0] if necessary
		if ( a_[0] != 1.0 ) {
			int i;
			for ( i=0; i<b_.length; i++ ) b_[i] /= a_[0];
			for ( i=1; i<a_.length; i++ )  a_[i] /= a_[0];
		}
	}

	public void setGain(float gain)
	{
		gain_ = gain;
	}

	public float getGain()
	{
		return gain_;
	}

	public float lastOut()
	{
		return outputs_[0];
	}

	public float tick( float input )
	{
		int i;

		outputs_[0] = 0f;
		inputs_[0] = gain_ * input;
		for (i=b_.length-1; i>0; i--) {
			outputs_[0] += b_[i] * inputs_[i];
			inputs_[i] = inputs_[i-1];
		}
		outputs_[0] += b_[0] * inputs_[0];

		for (i=a_.length-1; i>0; i--) {
			outputs_[0] += -a_[i] * outputs_[i];
			outputs_[i] = outputs_[i-1];
		}

		return outputs_[0];
	}


	/*	StkFrames& Filter :: tick( StkFrames& frames, unsigned int channel )
	{
	  if ( channel >= frames.channels() ) {
	    errorString_ << "Filter::tick(): channel and StkFrames arguments are incompatible!";
	    handleError( StkError::FUNCTION_ARGUMENT );
	  }

	  if ( frames.channels() == 1 ) {
	    for ( unsigned int i=0; i<frames.frames(); i++ )
	      frames[i] = tick( frames[i] );
	  }
	  else if ( frames.interleaved() ) {
	    unsigned int hop = frames.channels();
	    unsigned int index = channel;
	    for ( unsigned int i=0; i<frames.frames(); i++ ) {
	      frames[index] = tick( frames[index] );
	      index += hop;
	    }
	  }
	  else {
	    unsigned int iStart = channel * frames.frames();
	    for ( unsigned int i=0; i<frames.frames(); i++, iStart++ )
	      frames[iStart] = tick( frames[iStart] );
	  }

	  return frames;
	} */

}
