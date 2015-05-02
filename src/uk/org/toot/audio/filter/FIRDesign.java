// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

import uk.org.toot.dsp.filter.FIRDesignerKW;

public class FIRDesign extends AbstractFilterDesign
{
    private int order = -1; // estimated by design()
    private float transitionBandwidth;
    private float attenuation = 60.0f;
    private float[] a;

    public FIRDesign(FilterSpecification spec) {
        super(spec);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
    	assert order > 0;
        this.order = order;
    }

    public float getTransitionBandwidth() {
        return transitionBandwidth;
    }

    public void setTransitionBandwidth(float transitionBandwidth) {
    	assert transitionBandwidth > 0;
        this.transitionBandwidth = transitionBandwidth;
    }

    public float getAttenuation() {
        return attenuation;
    }

    public void setAttenuation(float attenuation) {
    	assert attenuation > 0;
        this.attenuation = attenuation;
    }

    public void design(int sampleRate) {
        float fNyquist = sampleRate / 2f;
        a = FIRDesignerKW.design( 
        		spec.getShape(),
                0f,
        		(float)spec.getFrequency(), 
        		getTransitionBandwidth(),
                fNyquist, 
        		getAttenuation(),
        		getOrder()
        		);
    }
    
    public float[] getCoefficients() {
        return a;
    }
}


