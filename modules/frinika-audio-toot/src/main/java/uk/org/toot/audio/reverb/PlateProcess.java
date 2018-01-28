// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.reverb;

/**
 * A literal implementation of the network diagram from Jon Dattorro's Effect Design Part 1,
 * Reverberator and Other Filters. Allegedly this is based on Griesinger's work, as were
 * Lexicon hardware reverberators.
 * Constants are for the design sample rate, not 44.1kHz!
 * @author st
 */
public class PlateProcess extends AbstractReverbProcess
{
	private Variables vars;
	private float tank1zm1 = 0f;
	private Tank tank1, tank2;
	
	private Delay ipd;
	private Filter bw;
	private Diffuser id1a, id1b, id2a, id2b;
	
	private int preDelaySamples;
	private float bandwidth;
	private float inputDiffusion1, inputDiffusion2;
	private float damping, decay;
	private float decayDiffusion1, decayDiffusion2;
	
	public PlateProcess(Variables vars) {
		this.vars = vars;
		tank1 = new Tank(true);
		tank2 = new Tank(false);
		ipd = new Delay(1+vars.getMaxPreDelaySamples());
		bw = new Filter();
		id1a = new Diffuser(142);
		id1b = new Diffuser(107);
		id2a = new Diffuser(379);
		id2b = new Diffuser(277);
	}
	
	protected void cacheVariables() {
		preDelaySamples = vars.getPreDelaySamples();
		bandwidth = vars.getBandwidth();
		inputDiffusion1 = vars.getInputDiffusion1();
		inputDiffusion2 = vars.getInputDiffusion2();
		damping = vars.getDamping();
		decay = vars.getDecay();
		decayDiffusion1 = vars.getDecayDiffusion1();
		decayDiffusion2 = vars.getDecayDiffusion2();
	}
	
	protected void reverb(float left, float right) {
		float sample = 0.3f * idiffuse(left + right);
		tank1zm1 = tank1.tick(sample + tank2.tick(sample + tank1zm1));
	}
	
    protected float left() { return tank1.output(0) + tank2.output(0); }
    
    protected float right() { return tank1.output(1) + tank2.output(1); }
    
	private float idiffuse(float sample) {
		// pre delay
		ipd.delay(sample);
		// bandwidth
		// input diffusion 1 x 2
		// input diffusion 2 x 2
		return id2b.diffuse(
				id2a.diffuse(
					id1b.diffuse(
						id1a.diffuse(
							bw.filter(
								ipd.tap(preDelaySamples), 1-bandwidth), 
								inputDiffusion1), 
								inputDiffusion1), 
								inputDiffusion2), 
								inputDiffusion2);
	}
	
	private class Tank
	{
		private boolean first;
		
		private Diffuser dif1, dif2;
		private Delay del1, del2;
		private Filter filter;
		
		public Tank(boolean first) {
			this.first = first;
			dif1 = new Diffuser(first ? 672 : 908);
			del1 = new Delay(first ? 4453 : 4217);
			filter = new Filter();
			dif2 = new Diffuser(first ? 1800 : 2656);
			del2 = new Delay(first ? 3720 : 3163);
		}
		
		public float tick(float sample) {
			// decay diffusion 1, note sign, and delay
			// damping
			// decay diffusion 2 and delay
			return decay * del2.delay(
				dif2.diffuse(
					filter.filter(
						del1.delay(
							dif1.diffuse(sample, -decayDiffusion1)), 
							damping) * decay, decayDiffusion2));
		}
		
		public float output(int chan) {
			if ( first ) {
				if ( chan == 0 ) {
					return -del2.tap(1066) - del1.tap(1990) - dif2.tap(187);					
				} else {
					return del1.tap(353) + del1.tap(3627) - dif2.tap(1228) + del2.tap(2673);
				}
			} else {
				if ( chan == 0 ) {
					return del1.tap(266) + del1.tap(2974) - dif2.tap(1913) + del2.tap(1996);
				} else {
					return -del1.tap(2111) - dif2.tap(335) - del2.tap(121); 
				}
				
			}
		}
	}
    
    public interface Variables
    {
        boolean isBypassed();
        int getMaxPreDelaySamples();
        int getPreDelaySamples();
        float getBandwidth();       // 0..1
        float getInputDiffusion1(); // 0..1
        float getInputDiffusion2(); // 0..1
        float getDecayDiffusion1(); // 0..1
        float getDecayDiffusion2(); // 0..1
        float getDamping();         // 0..1
        float getDecay();           // 0..1
    }

}
