package uk.org.toot.dsp.filter;

/*
 * (C) 1998 Dr Iain A Robin (i.robin@bell.ac.uk)
 * Derived from Parks-McClellan algorithm for FIR filter design (C version)
 * Copyright (C) 1995  Jake Janovetz (janovetz@coewl.cen.uiuc.edu)
 * Refactoring (C) 2010 Steve Taylor (toot.org.uk)
 */
public class FIRTools
{
    /**
     * Calculate the response at the specified frequency
     * @param fn f/fs, i.e. normalised to sample rate, 0 .. 0.5
     * @param a the filter coefficients
     * @return
     */
    public static float dB(float fn, float a[]) {
        float theta, s, c, sac, sas;
        theta = (float)(2*Math.PI*fn); // 0..PI
        sac = 0.0f;
        sas = 0.0f;
        for (int k = 0; k < a.length; k++) {
            c = (float) Math.cos(k * theta);
            s = (float) Math.sin(k * theta);
            sac += c * a[k];
            sas += s * a[k];
        }
        return 10f * (float) Math.log10(sac * sac + sas * sas);        
    }
    
    public static float[] filterGain (int freqPoints, float[] a, boolean norm) {
        // filter gain at uniform frequency intervals
        float[] g = new float[freqPoints + 1];
        float gMax = -100.0f;
        float t = 0.5f / freqPoints;
        for (int i = 0; i <= freqPoints; i++) {
            g[i] = dB(i*t, a);
            gMax = Math.max(gMax, g[i]);
        }
        if (norm) {
            // normalise to 0 dB maximum gain
            for (int i = 0; i <= freqPoints; i++)
                g[i] -= gMax;
/*            // normalise coefficients
            float normFactor = (float) Math.pow(10.0, -0.05 * gMax);
            for (int i = 0; i < a.length; i++)
                a[i] *= normFactor; */
        }
        return g;
    }



}
