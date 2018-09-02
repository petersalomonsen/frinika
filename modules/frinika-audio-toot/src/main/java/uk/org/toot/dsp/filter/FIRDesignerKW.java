package uk.org.toot.dsp.filter;

import uk.org.toot.dsp.filter.FilterShape;

/*
 * http://www.dsptutor.freeuk.com/KaiserFilterDesign/KaiserFilterDesign.html
 */
public class FIRDesignerKW
{
    public static int estimatedOrder(FIRSpecification s) {
        return estimatedOrder(s.ft, s.fN, s.dBatten);
    }
    
    public static int estimatedOrder(float trband, float fN, float atten) {
        // estimate filter order
        int order = 2 * (int) ((atten - 7.95) / (14.36*trband/fN) + 1.0f);
        return order;
    }

    public static float[] design(FIRSpecification s) {
        if ( s.order < 1 ) {
            int order = estimatedOrder(s);
            if ( s.mod > 0 ) {
                while ( (order % s.mod) != s.mod-1 ) {
                    order++;
                }
            }
            s.order = order;
        }
        return design(s.shape, s.f1, s.f2, s.ft, s.fN, s.dBatten, s.order);
    }
    
    public static float[] design(FilterShape filterShape, float f1, float f2, float ft, float fN, float atten, int order) {
        if ( order < 1 ) order = estimatedOrder(ft, fN, atten);
        // estimate Kaiser window parameter (alpha):
        float alpha = 0f;
        if (atten >= 50.0f) alpha = 0.1102f*(atten - 8.7f);
        else
        if (atten > 21.0f)
            alpha = 0.5842f*(float)Math.exp(0.4f*(float)Math.log(atten - 21.0f))
                        + 0.07886f*(atten - 21.0f);
        // window function values
        float I0alpha = I0(alpha);
        int m = order / 2;
        float[] win = new float[m+1];
        for (int n=1; n <= m; n++)
            win[n] = I0(alpha*(float)Math.sqrt(1.0f - sqr((float)n/m))) / I0alpha;

        float w0 = 0.0f;
        float w1 = 0.0f;
        switch (filterShape) {
            case LPF: w0 = 0.0f;
                     w1 = (float)Math.PI*(f2 + 0.5f*ft)/fN;
            break;
            case HPF: w0 = (float)Math.PI;
                     w1 = (float)Math.PI*(1.0f - (f1 - 0.5f*ft)/fN);
            break;
            case BPF: w0 = 0.5f * (float)Math.PI * (f1 + f2) / fN;
                     w1 = 0.5f * (float)Math.PI * (f2 - f1 + ft) / fN;
            break;
        }

        // filter coefficients (NB not normalised to unit maximum gain)
        float[] a = new float[order+1];
        a[0] = w1 / (float)Math.PI;
        for (int n=1; n <= m; n++)
            a[n] = (float)Math.sin(n*w1)*(float)Math.cos(n*w0)*win[n]/(n*(float)Math.PI);
        // shift impulse response to make filter causal:
        for (int n=m+1; n<=order; n++) a[n] = a[n - m];
        for (int n=0; n<=m-1; n++) a[n] = a[order - n];
        a[m] = w1 / (float)Math.PI;
        return a;
    }

    private static float I0 (float x) {
        // zero order Bessel function of the first kind
        float eps = 1.0e-6f;   // accuracy parameter
        float fact = 1.0f;
        float x2 = 0.5f * x;
        float p = x2;
        float t = p * p;
        float s = 1.0f + t;
        for (int k = 2; t > eps; k++) {
            p *= x2;
            fact *= k;
            t = sqr(p / fact);
            s += t;
        }
        return s;
    }

    private static float sqr(float x) {
        return x*x;
    }

}
