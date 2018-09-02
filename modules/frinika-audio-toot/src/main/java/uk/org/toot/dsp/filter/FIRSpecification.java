// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp.filter;

public class FIRSpecification
{
    public FilterShape shape = FilterShape.LPF;
    public float f1 = 0f, f2, ft, fN;
    public float dBatten = 48;
    public float dBripple = 1;
    public int order = -1; // -1 causes estimation
    public int mod = 0; // >0 causes order % mod == 0
}
