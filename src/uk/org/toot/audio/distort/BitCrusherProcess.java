package uk.org.toot.audio.distort;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

public class BitCrusherProcess extends SimpleAudioProcess
{
	private Variables vars;
	private float precision;
	
	public BitCrusherProcess(Variables vars) {
		this.vars = vars;
	}

	public int processAudio(AudioBuffer buffer) {
		if ( vars.isBypassed() ) return AUDIO_OK;
		precision = vars.getPrecision();
		int ns = buffer.getSampleCount();
		int nc = buffer.getChannelCount();
		for ( int c = 0; c < nc; c++ ) {
			float[] samples = buffer.getChannel(c);
			for ( int s = 0; s < ns; s++ ) {
				samples[s] = crush(samples[s]);
			}
		}
		return AUDIO_OK;
	}

	private final float crush(float val) {
		return (float)(int)(val*precision)/precision;
	}
	
	public interface Variables
	{
		boolean isBypassed();
		float getPrecision();
	}
}
