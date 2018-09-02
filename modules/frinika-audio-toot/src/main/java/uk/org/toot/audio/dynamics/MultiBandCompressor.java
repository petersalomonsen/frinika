// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import java.util.List;
import uk.org.toot.control.*;
import uk.org.toot.dsp.filter.FilterShape;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.filter.*;

import static uk.org.toot.audio.core.FloatDenormals.*;
import static uk.org.toot.misc.Localisation.*;

/**
 * A dual or quad band compressor.
 */
public class MultiBandCompressor implements AudioProcess
{
    private MultiBandControls multiBandControls;

    /**
     * @link aggregationByValue
     * @supplierCardinality 2..* 
     */
    private Compressor[] compressors;

    /**
     * @link aggregationByValue
     * @supplierCardinality 1
     * @label mid
     */
    private Crossover midXO;
    /**
     * @link aggregationByValue
     * @supplierCardinality 0..1
     * @label lo
     */
    private Crossover hiXO;
    /**
     * @link aggregationByValue
     * @supplierCardinality 0..1
     * @label hi
     */
    private Crossover loXO;

    private AudioBuffer[] bandBuffers;

    private int nbands;
    private int nchans = -1;
    private int nsamples = -1;
    private int sampleRate = -1;
    private boolean wasBypassed;

    public MultiBandCompressor(MultiBandControls c) {
        multiBandControls = c;
        wasBypassed = !c.isBypassed(); // force update
        List<Control> controls = c.getControls();
        nbands = (controls.size()+1) / 2; // !!! 1 for bypass !!! !!!
        if ( nbands > 2 ) {
            nbands = 4;
            loXO = createCrossover((CrossoverControl)controls.get(1+1));
	        midXO = createCrossover((CrossoverControl)controls.get(3+1));
            hiXO = createCrossover((CrossoverControl)controls.get(5+1));
        } else {
	        midXO = createCrossover((CrossoverControl)controls.get(1+1));
        	nbands = 2; // !!!
        }
        compressors = new Compressor[nbands];
        for ( int i = 0; i < nbands; i++ ) {
            compressors[i] = new Compressor((Compressor.Controls)controls.get(1+i*2));
        }
    }

    public void open() {} // open the filters !!!

    public void close() {} // close the filters !!!

    public void clear() {
       	// clear crossovers on transition to bypassed
		midXO.clear();
        if ( nbands > 2 ) {
            loXO.clear();
            hiXO.clear();
        }
        // clear compressors (envelope, gain reduction indicator)
	    for ( int b = 0; b < nbands; b++) {
    	    compressors[b].clear();
    	}
    }

    public int processAudio(AudioBuffer buffer) {
        boolean bypassed = multiBandControls.isBypassed();
        if ( bypassed ) {
            if ( !wasBypassed ) {
                clear();
		        wasBypassed = true;
            }
            return AUDIO_OK;
        }
        conformBandBuffers(buffer);
        // split once or twice to band buffers
        // broadband -> low, high
        split(midXO, buffer, bandBuffers[0], bandBuffers[1]);
        if ( nbands > 2 ) { // take as 4
        	// high -> hi-mid, hi
    	    split(hiXO, bandBuffers[1], bandBuffers[2], bandBuffers[3]);
            // low -> lo, lo-mid
	        split(loXO, bandBuffers[0], bandBuffers[0], bandBuffers[1]);
        }
        // compress each band
        for ( int b = 0; b < nbands; b++) {
            compressors[b].processAudio(bandBuffers[b]);
        }
        // sum band buffers
        buffer.makeSilence(); // clear before sum
        int nc = buffer.getChannelCount();
        int ns = buffer.getSampleCount();
        float out;
        for ( int c = 0; c < nc; c++ ) {
            float[] samples = buffer.getChannel(c);
	        for ( int b = 0; b < nbands; b++) {
                float[] bandsamples = bandBuffers[b].getChannel(c);
                for ( int i = 0; i < ns; i++ ) {
                    out = bandsamples[i];
                    if ( isDenormalOrZero(out) ) continue; // anti-denorm ???
                    // !!! inverts phase to avoid notch at crossover
                    samples[i] += ((b & 1) == 1) ? -out : out;
                }
            }
        }
        wasBypassed = bypassed;
		return AUDIO_OK;
    }

    protected void conformBandBuffers(AudioBuffer buf) {
        int nc = buf.getChannelCount();
        int ns = buf.getSampleCount();
        int sr = (int)buf.getSampleRate();
        if ( bandBuffers == null ) {
	        bandBuffers = new AudioBuffer[nbands];
		    for ( int b = 0; b < nbands; b++ ) {
    	        bandBuffers[b] = new AudioBuffer("MultiBandCompressor band "+(1+b), nc, ns, sr);
        	}
            updateSampleRate(sr);
        } else {
	        if ( nchans >= nc && nsamples == ns && sampleRate == sr ) return;
    	    for ( int b = 0; b < nbands; b++ ) {
        	    AudioBuffer bbuf = bandBuffers[b];
            	if ( nchans < nc ) {
		    		for ( int i = 0; i < nc - nchans; i++ ) {
						bbuf.addChannel(true); // create new silent channels
	        		}
    	        }
        	    if ( nsamples != ns ) {
            	    bbuf.changeSampleCount(ns, false);
            	}
	            if ( sampleRate != sr ) {
    	            bbuf.setSampleRate(sr);
                    updateSampleRate(sr);
        	    }
        	}
        }
        nchans = nc;
        nsamples = ns;
        sampleRate = sr;
    }

    protected void split(Crossover xo, AudioBuffer source,
        AudioBuffer low, AudioBuffer high) {
        for ( int c = 0; c < source.getChannelCount(); c++ ) {
        	xo.filter(source.getChannel(c),
               	low.getChannel(c),
                high.getChannel(c),
                source.getSampleCount(),
                c);
        }
    }

    protected Crossover createCrossover(CrossoverControl c) {
        return new IIRCrossover(new CrossoverSection(c, FilterShape.LPF),
            					new CrossoverSection(c, FilterShape.HPF));
    }

    protected void updateSampleRate(int rate) {
        // need to update all the filters !!!!
        midXO.setSampleRate(rate);
        if ( nbands > 2 ) {
	        loXO.setSampleRate(rate);
	        hiXO.setSampleRate(rate);
        }
    }

    public static class MultiBandControls extends AudioControls
    {
        public MultiBandControls(String name) {
            super(DynamicsIds.MULTI_BAND_COMPRESSOR_ID, name);
        }

	    public boolean hasOrderedFrequencies() { return true; } // crossover sections in frequency order
    }

    public static class DualBandControls extends MultiBandControls
    {
        public DualBandControls() {
            super(getString("Dual.Band.Compressor"));
            add(new Compressor.Controls(getString("Low"), 0));
            add(new CrossoverControl(getString("Mid"), 1000f)); // mid crossover control
            add(new Compressor.Controls(getString("High"), 30));
        }
    }

    public static class QuadBandControls extends MultiBandControls
    {
        public QuadBandControls() {
            super(getString("Quad.Band.Compressor"));
            add(new Compressor.Controls(getString("Low"), 0));
            add(new CrossoverControl(getString("Low"), 250f)); // low crossover control
            add(new Compressor.Controls(getString("Lo.Mid"), 10));
            add(new CrossoverControl(getString("Mid"), 1000f)); // mid crossover control
            add(new Compressor.Controls(getString("Hi.Mid"), 20));
            add(new CrossoverControl(getString("High"), 4000f)); // high crossover control
            add(new Compressor.Controls(getString("High"), 30));
        }
    }
}
