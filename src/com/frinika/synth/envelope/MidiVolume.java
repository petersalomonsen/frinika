/*
 * Created on Mar 6, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth.envelope;

import com.frinika.audio.Decibel;

/**
 * 
 * @author Peter Johan Salomonsen
 *
 */
public class MidiVolume {

    static final float[] amplitudeRatios = new float[] {
        0,
        6.196825E-5f,
        2.4791213E-4f,
        5.5830705E-4f,
        9.927761E-4f,
        0.0015499542f,
        0.002232504f,
        0.0030392734f,
        0.0039696326f,
        0.0050203404f,
        0.006197518f,
        0.0075024636f,
        0.008926705f,
        0.010475616f,
        0.012152587f,
        0.0139527125f,
        0.015872644f,
        0.01791185f,
        0.020073919f,
        0.02236785f,
        0.024780883f,
        0.027328558f,
        0.02999975f,
        0.03278072f,
        0.035695985f,
        0.03873648f,
        0.041891024f,
        0.045198258f,
        0.04859845f,
        0.05213425f,
        0.055798665f,
        0.059583288f,
        0.06347827f,
        0.067472324f,
        0.07163517f,
        0.075967334f,
        0.08028371f,
        0.084845334f,
        0.08945992f,
        0.09432548f,
        0.09922692f,
        0.10414298f,
        0.109302595f,
        0.11458584f,
        0.11998623f,
        0.12549657f,
        0.13110894f,
        0.1369723f,
        0.14276874f,
        0.14881049f,
        0.15492944f,
        0.16129999f,
        0.16754626f,
        0.1740344f,
        0.1807738f,
        0.18755811f,
        0.19437312f,
        0.20143576f,
        0.20851481f,
        0.21584265f,
        0.2231709f,
        0.23074798f,
        0.23830779f,
        0.24611527f,
        0.25388607f,
        0.2619022f,
        0.2698606f,
        0.2783811f,
        0.2865102f,
        0.29521632f,
        0.303837f,
        0.3123496f,
        0.32147056f,
        0.33047718f,
        0.33934522f,
        0.34885263f,
        0.35821378f,
        0.36740285f,
        0.37726176f,
        0.3869395f,
        0.39686546f,
        0.40657768f,
        0.41700745f,
        0.4272126f,
        0.43716386f,
        0.4478623f,
        0.4582946f,
        0.46896988f,
        0.47989386f,
        0.49107227f,
        0.50193286f,
        0.5130336f,
        0.52437997f,
        0.5359772f,
        0.5478309f,
        0.55930245f,
        0.5710142f,
        0.5829712f,
        0.5951786f,
        0.60764164f,
        0.6196518f,
        0.63262725f,
        0.645131f,
        0.6578818f,
        0.6701127f,
        0.68335736f,
        0.6968637f,
        0.7098194f,
        0.7230159f,
        0.73645777f,
        0.7501495f,
        0.76409584f,
        0.77740586f,
        0.7918589f,
        0.80565256f,
        0.81968653f,
        0.83396494f,
        0.848492f,
        0.86327225f,
        0.87729925f,
        0.8925812f,
        0.9070845f,
        0.92288536f,
        0.937881f,
        0.95312035f,
        0.96860725f,
        0.98434585f,
        1f
    };
    
    public static float midiVolumeTodB(float midiVolume)
    {
        return((float)(20.0 * Math.log10(Math.pow((127.0/midiVolume),2.0))));
    }

    public static float midiVolumeToAmplitudeRatio(int midiVolume)
    {
        return(amplitudeRatios[midiVolume]);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(amplitudeRatios.length);
        for(int n=1;n<128;n++)
            System.out.println(Decibel.getAmplitudeRatio(-MidiVolume.midiVolumeTodB(n))+"f,");
    }

}
