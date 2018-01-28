// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

public class ToneStackDesigner
{
    // circuit components
    private double R1, R2, R3, R4, C1, C2, C3;
    // equation helpers
    private double R3sq, R1p4, C1p2;
    private float gain;
    
    public void setComponents(Components c) {
        R1 = c.getR1();
        R2 = c.getR2();
        R3 = c.getR3();
        R4 = c.getR4();
        C1 = c.getC1();
        C2 = c.getC2();
        C3 = c.getC3();
        R3sq = R3 * R3;
        R1p4 = R1 + R4;
        C1p2 = C1 + C2;
        gain = c.getGain();
    }
    
    /*
     * Intermediate calculations must be double.
     * Using floats causes a wierd problem: after a while the filter sounds like 
     * the power has been turned off and just fades away without denormals or NaNs.
     * Only changing Bass or Middle (but not Treble) solves the problem once it has
     * happened, which suggested the coefficients were responsible.
     */
    public ToneStackSection.Coefficients design(float l, float m, float t, float fs) {
    	double b1 = t*C1*R1 + m*C3*R3 + l*R2*C1p2 + R3*C1p2;
    	
    	double b2 = t*C1*R1*R4*(C2 + C3) 
    			- m*m*C3*R3sq*C1p2
    			+ m*C3*R3*(C1*R1 + C1*R3 + C2*R3)
    			+ l*C1*R2*(C2*R1 + C2*R4 + C3*R4)
    			+ l*m*C3*R2*R3*C1p2
    			+ C1*R3*(C2*R1 + C2*R4 + C3*R4);
    	
    	double b3 = l*m*R2*R3*R1p4
    			- m*m*R3sq*R1p4
    			+ m*R3sq*R1p4
    			+ t*R1*R3*R4 
    			- t*m*R1*R3*R4
    			+ t*l*R1*R2*R4;    	
    	b3 *= C1*C2*C3;
    	
    	double a0 = 1;
    	
    	double a1 = (C1*R1 + C1*R3 + C2*R3 + C2*R4 + C3*R4)
    			+ m*C3*R3 + l*R2*C1p2;
    	
    	double a2 = m*C3*R3*(C1*R1 - C2*R4 + R3*C1p2) 
    			+ l*m*R2*R3*C3*C1p2
    			- m*m*C3*R3sq*C1p2 
				+ l*R2*(C1*C2*R4 + C1*C2*R1 + C1*C3*R4 + C2*C3*R4)
    			+ (C1*C2*R1*R4 + C1*C3*R1*R4 + C1*C2*R3*R4
    			+ C1*C2*R1*R3 + C1*C3*R3*R4 + C2*C3*R3*R4);
    	
    	double a3 = l*m*R2*R3*R1p4
    			- m*m*R3sq*R1p4
    			+ m*R3*(R3*R4 + R1*R3 - R1*R4) 
    			+ l*R1*R2*R4
    			+ R1*R3*R4;
    	a3 *= C1*C2*C3;
    	
    	float c = 2*fs;
    	float c2 = c*c;
    	float c3 = c*c2;
    	
        double B0 = -b1*c - b2*c2 - b3*c3;
        double B1 = -b1*c + b2*c2 + 3*b3*c3;
        double B2 = b1*c + b2*c2 - 3*b3*c3;
        double B3 = b1*c - b2*c2 + b3*c3;
        double A0 = -a0 - a1*c - a2*c2 - a3*c3;
        double A1 = -3*a0 - a1*c + a2*c2 + 3*a3*c3;
        double A2 = -3*a0 + a1*c + a2*c2 - 3*a3*c3;
        double A3 = -a0 + a1*c - a2*c2 + a3*c3;
    	
    	// normalise coefficients
    	ToneStackSection.Coefficients coeffs = new ToneStackSection.Coefficients();
    	coeffs.b0 = (float) (B0/A0);
    	coeffs.b1 = (float) (B1/A0);
    	coeffs.b2 = (float) (B2/A0);
    	coeffs.b3 = (float) (B3/A0);
    	coeffs.a1 = (float) (A1/A0);
    	coeffs.a2 = (float) (A2/A0);
    	coeffs.a3 = (float) (A3/A0);
        coeffs.gain = gain;
    	return coeffs;
    }
    
    public static abstract class Components
    {
        public abstract float getR1();
        public abstract float getR2();
        public abstract float getR3();
        public abstract float getR4();
        public abstract float getC1();
        public abstract float getC2();
        public abstract float getC3();
        public float getGain() { return 2f; }
    }

    public static class Fender59BassmanComponents extends Components
    {
        @Override
        public float getC1() { return 0.25E-9f; }

        @Override
        public float getC2() { return 20E-9f; }

        @Override
        public float getC3() { return 20E-9f; }

        @Override
        public float getR1() { return 250E3f; }

        @Override
        public float getR2() { return 1E6f; }

        @Override
        public float getR3() { return 25E3f; }

        @Override
        public float getR4() { return 56E3f; }
        
        public String toString() { return "'59 Bassman"; }        
    }
    
    public static class FenderComponents extends Components
    {
        @Override
        public float getC1() { return 0.25E-9f; }

        @Override
        public float getC2() { return 100E-9f; }

        @Override
        public float getC3() { return 47E-9f; }

        @Override
        public float getR1() { return 250E3f; }

        @Override
        public float getR2() { return 250E3f; }

        @Override
        public float getR3() { return 10E3f; }

        @Override
        public float getR4() { return 100E3f; }
                
        public String toString() { return "Fender"; }        

        public float getGain() { return super.getGain()*4f; }
    }
    
    public static class MarshallComponents extends Components
    {
        @Override
        public float getC1() { return 0.47E-9f; }

        @Override
        public float getC2() { return 22E-9f; }

        @Override
        public float getC3() { return 22E-9f; }

        @Override
        public float getR1() { return 220E3f; }

        @Override
        public float getR2() { return 1E6f; }

        @Override
        public float getR3() { return 25E3f; }

        @Override
        public float getR4() { return 33E3f; }
        
        public String toString() { return "Marshall"; }        
    }
    
}
