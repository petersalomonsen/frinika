// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

import java.util.Observer;
import java.util.Observable;

abstract public class AbstractFilter implements Filter
{
    /**
     * @link aggregation
     * @supplierCardinality 1*/
    private FilterDesign design = null;
    private Observer specObserver = null;

    private boolean doUpdate = true; // for first time
    private final static int MAX_CHANNELS = 6;
    private Filter.State[] states = new State[MAX_CHANNELS];
    protected float amplitudeAdj = 0f;
    protected int sampleRate = -1; // force initial update
    private float levelOffset;

    public AbstractFilter(FilterSpecification spec, boolean relative) {
    	// graphic eq and parametric eq are relative, formant eq isn't
    	levelOffset = relative ? 1f : 0f;
        // create an appropriate FilterDesign for the implementation
        design = createDesign(spec);
        // create an Observer to observe asynchronous FilterSpecification changes
        specObserver = 	new Observer() {
   			public void update(Observable obs, Object obj) {
			    if ( sampleRate < 1 ) return;
       			// the design is updated on the thread that notifies changes
	            design.design(sampleRate);
                // indicate the design has changed
       			doUpdate = true;
   			}
        };
    }

    public void open() {
        getDesign().getFilterSpecification().addObserver(specObserver);
    }

    public void close() {
        getDesign().getFilterSpecification().deleteObserver(specObserver);
    }

    public FilterDesign getDesign() { return design; }

    abstract public void filter(float[] buffer, float[] mixBuffer, int length, int chan, boolean mix);

    public void clear() {
        for ( int i = 0; i < MAX_CHANNELS; i++ ) {
            if ( states[i] != null ) {
                states[i].clear();
            }
        }
    }

    protected Filter.State getState(int chan) {
        if ( chan >= MAX_CHANNELS || chan < 0 ) return null;
        // act on asynchronous redesign notification
        if ( doUpdate ) {
            updateFilterCoefficients();
            doUpdate = false;
        }

        // level changes don't require a redesign so update them here
        // surely subtracting levelOffset doesn't work right !!! !!! TODO
       	amplitudeAdj = design.getFilterSpecification().getLevelFactor()-levelOffset;
        if ( states[chan] == null ) {
            states[chan] = createState();
        }
        return states[chan];
    }

    public void setSampleRate(int rate) {
        sampleRate = rate;
        design.design(rate);
        doUpdate = true;
    }

    abstract protected FilterDesign createDesign(FilterSpecification spec);

    abstract protected Filter.State createState();

    abstract protected void updateFilterCoefficients();
}
