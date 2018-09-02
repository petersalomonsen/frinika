// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

import uk.org.toot.dsp.filter.BiQuadDesigner;

public class BiQuadDesign extends AbstractFilterDesign 
{
    double[] a;

    public BiQuadDesign(FilterSpecification spec) {
        super(spec);
    }

    public void design(int sampleRate) {
        a = BiQuadDesigner.design(spec.getShape(), spec.getLeveldB(), spec.getFrequency(), (float)sampleRate, spec.getResonance());
    }
}
