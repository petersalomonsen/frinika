/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frinika.codesynth.note;

/**
 *
 * @author peter
 */
public class ChromaticScaleNote extends Note {

    double radians;
    double radianIncrement;

    /*
     * Default factor to be multiplied with each radian increment
     */
    public final double getDefaultPitchbendFactor() {
        int bend = midiChannel.getPitchBend();
        return (float)Math.pow(2.0,( ((double)(bend-0x2000) / (double)0x1000)/12.0));
    }

    public final double getDefaultRadianIncrement() {
        return (2*Math.PI) * (440.0 * Math.pow(2.0,((noteNumber-69.0)/12.0))) / sampleRate;
    }

    public final double getDefaultRadians() {
        final double ret = radians;
        radians += radianIncrement;
        return ret;
    }

    @Override
    public void beforeFill() {
        radianIncrement = (getDefaultRadianIncrement() * getDefaultPitchbendFactor() );
    }

    @Override
    public void fillFrame(float[] floatBuffer, int bufferPos, int channels) {
        
    }


}
