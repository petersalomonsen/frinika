// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.reverb;

import java.util.Arrays;

/**
 * An implementation of the network diagram from 
 * http://spinsemi.com/knowledge_base/effects.html#Reverberation
 * This is based on Keith Barr's work for later Alesis hardware reverberators.
 * RIP Keith Barr 1949 - 2010
 * @author st
 */
public class BarrProcess extends AbstractReverbProcess
{
    // gain compensation based on square root of number of taps
    private final static float GAIN_8 = 0.35f;
//    private final static float GAIN_4 = 0.5f;
//    private final static float GAIN_2 = 0.71f;
    
	private Variables vars;
	private float zm1 = 0f;
	private Block block1, block2, block3, block4;
    private Filter damp1, damp2;
	
	private Delay ipd;
	private Filter bw;
	private Diffuser id1a, id1b, id2a, id2b;
	
	private int preDelaySamples;
	private float bandwidth;
	private float inputDiffusion;
	private float damping, decay;
	private float decayDiffusion;
    private float size, prevSize = 1f;
    private boolean halfInserts = false;
	
	public BarrProcess(Variables vars) {
		this.vars = vars;
		ipd = new Delay(1+vars.getMaxPreDelaySamples());
		bw = new Filter();
        int[][] sizes = vars.getSizes();
        // input diffusion
        int[] sz = sizes[4];
		id1a = new Diffuser(sz[0]);
		id1b = new Diffuser(sz[1]);
		id2a = new Diffuser(sz[2]);
		id2b = new Diffuser(sz[3]);
        // the 4 blocks form a tank for the reverb tail
        int[][] tapsLeft = vars.getLeftTaps();
        int[][] tapsRight = vars.getRightTaps();
        block1 = new Block(sizes[0], tapsLeft[0], tapsRight[0]);
        block2 = new Block(sizes[1], tapsLeft[1], tapsRight[1]);
        block3 = new Block(sizes[2], tapsLeft[2], tapsRight[2]);
        block4 = new Block(sizes[3], tapsLeft[3], tapsRight[3]);
        damp1 = new Filter();
        damp2 = new Filter();
	}
	
	protected void cacheVariables() {
        
		preDelaySamples = vars.getPreDelaySamples();
		bandwidth = vars.getBandwidth();
		inputDiffusion = vars.getInputDiffusion();
		damping = vars.getDamping();
		decay = vars.getDecay();
		decayDiffusion = vars.getDecayDiffusion();
        size = vars.getSize();
        if ( size != prevSize ) {
            block1.resize(size);
            block2.resize(size);
            block3.resize(size);
            block4.resize(size);
            prevSize = size;
        }
        decay -= 0.25f*(1f - size); // fix blowup at small sizes 
	}
	
    // could feed left into 1, right into 3 but input diffusion would need to be stereo!
	protected void reverb(float left, float right) {
		float sample = GAIN_8*idiffuse(left + right);
        float sample2 = halfInserts ? 0f : sample;
        zm1 = 
            block4.tick(sample2 + 
                damp2.filter(
                    block3.tick(sample + 
                        block2.tick(sample2 +
                            damp1.filter(
                                block1.tick(sample + zm1), 
                            damping))), 
                damping));
	}
	
    protected float left() { 
        return block1.left()+ block2.left()+block3.left()+block4.left();
    }
    
    protected float right() { 
        return block1.right()+block2.right()+block3.right()+block4.right();
    }
    
	private float idiffuse(float sample) {
		// pre delay
		ipd.delay(sample);
		// bandwidth, input diffusion 1 x 2, input diffusion 2 x 2
		return id2b.diffuse(
				id2a.diffuse(
					id1b.diffuse(
						id1a.diffuse(
							bw.filter(
								ipd.tap(preDelaySamples), 
                                1-bandwidth), 
							inputDiffusion), 
						inputDiffusion), 
					inputDiffusion), 
				inputDiffusion);
	}
	
	private class Block
	{
		private final Diffuser dif1, dif2;
		private final Delay del;
        private final int[] tapsLeft, tapsRight, tl, tr;
		
		public Block(int[] sz, int[] left, int[] right) {
            tapsLeft = left;
            tapsRight = right;
            tl = Arrays.copyOf(tapsLeft, 2);
            tr = Arrays.copyOf(tapsRight, 2);
			dif1 = new Diffuser(sz[0]);
			dif2 = new Diffuser(sz[1]);
            del = new Delay(sz[2]);
		}
		
		public float tick(float sample) {
            // diffuse, diffuse, delay, decay
			return decay * del.delay(
                    dif2.diffuse(
                        dif1.diffuse(sample, 
                            decayDiffusion), 
                        decayDiffusion));
		}
		
        public float left() {
            return del.tap(tl[0]) + del.tap(tl[1]);
        }
        
        public float right() {
            return del.tap(tr[0]) + del.tap(tr[1]);
        }
        
        public void resize(float factor) {
            dif1.resize(factor);
            dif2.resize(factor);
            del.resize(factor);
            calcTaps();
        }
        
        private void calcTaps() {
            for ( int i = 0; i < 2; i++ ) {
                tl[i] = (int)(tapsLeft[i] * size);
                tr[i] = (int)(tapsRight[i] * size);
            }
        }
	}
    
    public interface Variables
    {
        boolean isBypassed();
        int getPreDelaySamples();
        float getBandwidth();       // 0..1
        float getInputDiffusion(); // 0..1
        float getDecayDiffusion(); // 0..1
        float getDamping();         // 0..1
        float getDecay();           // 0..1
        float getSize();            // >0..1
        // following methods are called once at startup
        int getMaxPreDelaySamples();
        int[][] getSizes();         // [block][3|4] (dif1, dif2, del)|(id1, id2, id3, id4)
        int[][] getLeftTaps();      // [block][2]
        int[][] getRightTaps();     // [block][2]
    }

}
