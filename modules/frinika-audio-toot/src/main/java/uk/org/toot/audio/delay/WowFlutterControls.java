// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import static uk.org.toot.misc.Localisation.getString;

import java.awt.Color;
import java.util.List;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.Control;
import uk.org.toot.control.EnumControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

/**
 * Controls for Wow and Flutter emulation of tape.
 * A 3.75/7.5/15/30 ips speed switch is provided.
 * Wow and Flutter may be raised to levels approximately 10 time greater than
 * a typical tape.
 * @author st
 */
public class WowFlutterControls extends AudioControls implements WowFlutterProcess.Variables
{
    private int fuid = -1;
    
    private final static int SPEED = 0;
    private final static int LEVEL = 1;
    
    private TapeSpeedControl speedControl;
    private FloatControl levelControl;
    
    private float level;
    private float[] amplitudes, frequencies, nextAmps, nextFreqs;
    
    public WowFlutterControls() {
        super(DelayIds.WOW_FLUTTER_ID, getString("Wow"));
        ControlColumn col = new ControlColumn();
        col.add(speedControl = new TapeSpeedControl());
        derive(speedControl);
        col.add(levelControl = createLevelControl());
        derive(levelControl);
        add(col);
    }

    @Override
    protected void derive(Control c) {
        switch ( c.getId() ) {
        case SPEED:
            float ips = Float.parseFloat(speedControl.getValueString());
            speedChanged(ips);
            break;
        case LEVEL:
            level = levelControl.getValue();
            break;
        default:
            break;
        }
    }
    
    protected FloatControl createLevelControl() {
        FloatControl control = new FloatControl(LEVEL, "Level", LinearLaw.UNITY, 0.1f, 0.1f);
        control.setInsertColor(Color.BLACK);
        return control;
    }
    
    protected void speedChanged(float ips) {
        float mmps = 25.4f*ips; // convert inches per second to mm per second
        float[] mm = getDiameters();
        int n = mm.length;
        nextFreqs = new float[n];
        nextAmps = new float[n];
        for ( int i = 0; i < n; i++ ) {
            // calculate frequencies from speed, diameters
            nextFreqs[i] = mmps / (float)(Math.PI * mm[i]);
            // normalise amplitudes by inverse frequency and n
            nextAmps[i] = 0.8f / (nextFreqs[i] * n); // !!! constant
        }
        frequencies = nextFreqs;
        fuid += 1;
    }
    
    public float getLevel() {
        return level;
    }

    public float[] getAmplitudes() {
        return amplitudes;
    }

    public int getFrequencyUid() {
        // this is s safe time to change amplitudes
        amplitudes = nextAmps;
        return fuid;
    }
    
    public float[] getFrequencies() {
        return frequencies;
    }

    public float getMaxDelayMilliseconds() {
        return 10f;
    }
    
    // details for a Studer A807 mastering tape recorder
    private static float[] A807mmdiameters = {
        9.06f,  // capstan (typically has harmonics)
        26f,    // pinch roller
        16f,    // tension sensor roller
        18f,    // guider roller (right)
        9.75f,  // guide roller (left)
        37.8f,  // counter roller
        1.292f  // equivalent diameter for various common faults
    };
    
    private static float[] getDiameters() {
        return A807mmdiameters;
    }
    
    private static List<String> speeds = new java.util.ArrayList<String>();
    
    static {
        speeds.add("3.75");
        speeds.add("7.5");
        speeds.add("15");
        speeds.add("30");
    }
    
    protected class TapeSpeedControl extends EnumControl
    {
        public TapeSpeedControl() {
            super(SPEED, "ips", "15");
        }

        @Override
        public List getValues() { return speeds; }  
                
        @Override
        public boolean hasLabel() { return true; } // show "ips" label

    }
}
