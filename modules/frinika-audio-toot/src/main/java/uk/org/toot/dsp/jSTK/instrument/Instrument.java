package uk.org.toot.dsp.jSTK.instrument;

public interface Instrument
{
	public void setSampleRate(int rate);

	public void clear();

	/** 
	 * Allows pitch-bend, slides etc.
	 * @param frequency - change the frequency
	 */
	public void setFrequency(float frequency);

	/**
	 * @param frequency - Hz
	 * @param amplitude - 0..1, the required amplitude, possibly compressed
	 * @param excitation - 0..1, the uncompressed amplitude to use for timbre
	 * @param other
	 */
	public void noteOn(float frequency, float amplitude, float excitation, float other);

	public void noteOff(float amplitude);

	public float getSample();

}