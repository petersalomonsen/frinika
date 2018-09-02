// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp.filter;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

/**
 * @author st
 *
 * A 'decramped' peak equaliser which matches an analogue equaliser far more
 * than the traditional RBJ designs. It works by introducing an extra degree
 * of freedom to the analogue filter to avoid the bilinear transform warping
 * which maps s = infinity to z = -1, hence having incorrect gain of 1 at Nyquist.
 * From the paper 
 * Digital Parametric Equaliser Design With Prescribed Nyquist-Frequency Gain
 * By Sophocles J Orfanidis
 * 
 * Three helper methods are provided for deigning peak, resonator and notch
 * filters with the required gain constants.
 */
public class OrfanidisBiQuadDesigner
{
    private final static float THREE_DB = (float)(sqrt(2)/2);
    private final static double PI2 = PI * PI;
    
    public static double[] designPeak(float fs, float f0, float fb, float G) {
        // define Gb as the geometric mean of the linear gains
        // equivalent to the arithmetic mean of the dB gains
        return design(fs, f0, fb, 1f, G, (float)sqrt(G));
    }
    
    public static double[] designResonator(float fs, float f0, float fb) {
        // define Gb as 3dB below unity gain
        return design(fs, f0, fb, 0f, 1f, THREE_DB);
    }
    
    public static double[] designNotch(float fs, float f0, float fb) {
        // define Gb as 3dB below unity gain
        return design(fs, f0, fb, 1f, 0f, THREE_DB);
    }

    /**
     * Design a decramped equaliser
     * @param fs - sample rate in Hz
     * @param f0 - centre frequency in Hz
     * @param fb - bandwidth in Hz (defined at gain Gb)
     * @param G0 - gain at DC
     * @param G  - gain at centre frequency
     * @param Gb - gain at bandwidth
     * @return the coefficients b0, b1, b2, a1, a2
     */
    public static double[] design(float fs, float f0, float fb, 
                                  float G0, float G, float Gb) {
        // normalise frequencies
        double w0 = 2f * PI * f0 / fs; // centre
        double wb = 2f * PI * fb / fs; // bandwidth
        // convenient squares
        double wb2 = wb * wb;
        double G02 = G0 * G0;
        double G2  = G  * G;
        double Gb2 = Gb * Gb;
       
        double F = abs(G2 - Gb2);
        double G00 = abs(G2 - G02);
        double F00 = abs(Gb2 - G02);
        double L = w0 * w0 - PI2;
        double L2 = L * L;
        double R = PI2 * wb2 * F00 / F;
        double num = G02 * L2 + G2 * R;
        double den = L2 + R;
        double G1 = sqrt(num/den);                          // nyquist gain
        double G01 = abs(G2 - G0*G1);
        double G11 = abs(G2 - G1*G1);
        double F01 = abs(Gb2 - G0*G1);
        double F11 = abs(Gb2 - G1*G1);
        double r0 = tan(w0/2);                              // prewarped centre
        double W2 = sqrt(G11/G00) * r0*r0;
        double rb = (1 + sqrt(F00/F11) * W2) * tan(wb/2);   // prewarped bandwidth
        double C = F11 * rb*rb - 2 * W2 * (F01 - sqrt(F00 * F11));
        double D = 2 * W2 * (G01 - sqrt(G00 * G11));
        double A = sqrt((C+D) / F);
        double B = sqrt((G2 * C + Gb2 * D) / F);
        // coefficients resulting from applying the bilinear transform
        double a0 = 1 + W2 + A;
        double a1 = -2*(1 - W2);
        double a2 = 1 + W2 - A;
        double b0 = G1 + G0*W2 + B;
        double b1 = -2*(G1 - G0*W2);
        double b2 = G1 + G0*W2 - B;
        // normalise a0 to 1 to reduce to 5 coefficients
        double[] a = new double[5];
        a[0] = b0 / a0;
        a[1] = b1 / a0;
        a[2] = b2 / a0;
        a[3] = a1 / a0;
        a[4] = a2 / a0;
        return a;
    }
}
