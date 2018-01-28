// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

/**
 * A logarithmic control law.
 * Note that audio faders are not generally logarithmic!
 * @see uk.org.toot.audio.fader.FaderLaw
 */
public class LogLaw extends AbstractLaw
{
    private double logMin, logMax;
    private double logSpan;

    public LogLaw(float min, float max, String units) {
        super(min, max, units);
        assert min != 0f;
        assert max != 0f;
        logMin = Math.log10(min); // error on zero min
        logMax = Math.log10(max); // error zero max
        logSpan = logMax - logMin;
    }

    //  min <= userVal <= max
    public int intValue(float userVal) {
        if ( userVal == 0 ) userVal = 1; // !!! protect log
        return (int)((resolution-1) * (Math.log10(userVal)-logMin) / logSpan);
    }

    // 0 <= intVal < resolution
    public float userValue(int intVal) {
        double p = logMin + (logSpan * intVal) / (resolution-1) ;
        return (float)Math.pow(10, p);
    }

    public static void main(String[] args) {
        // test an audio frequency log law
        ControlLaw law = new LogLaw(20f, 20000f, "Hz");
        // check 20, 200, 2000, 20000 at 1/3 intervals
        System.out.println("   20 Hz => "+law.intValue(20)+" => "+law.userValue(law.intValue(20)));
        System.out.println("  200 Hz => "+law.intValue(200)+" => "+law.userValue(law.intValue(200)));
        System.out.println(" 2000 Hz => "+law.intValue(2000)+" => "+law.userValue(law.intValue(2000)));
        System.out.println("20000 Hz => "+law.intValue(20000)+" => "+law.userValue(law.intValue(20000)));
	}
}
