// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import java.awt.Color;
import java.util.List;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.EnumControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.control.LogLaw;

import static uk.org.toot.misc.Localisation.*;

/**
 * Models the comb filtering effects of multiple drive units of a guitar/bass cab with
 * a close microphone. Room effects, directivity and microphone response are ignored. 
 * In practice the frequency response of this comb filtering closely matches convolution 
 * models.
 * 
 * All dimensions in inches :)
 * @author st
 *
 */
public class CabMicingControls extends AudioControls implements CabMicingProcess.Variables
{
    private final static float MILLIS_PER_INCH = 1000f / (340*39f);
    private final static float DRIVE_UNIT_SEPARATION = 3;
    
    private final static ControlLaw NEAR_DISTANCE_LAW = new LinearLaw(1, 12, "inches");
    private final static ControlLaw POWER_LAW = new LogLaw(0.5f, 2f, "");
    
    private final static int N_ID = 0;
    private final static int D_ID = 1;
    private final static int NEAR_DISTANCE_ID = 2;
    private final static int COMB_ID = 3;
    
    private NControl nControl;
    private DControl dControl;
    private FloatControl nearControl, combControl;
    
    private int n = 2;  // number of drive units
    private int d = 12; // diameter, inches
    private float separation = d + DRIVE_UNIT_SEPARATION, offset = 3, near = 6; // inches
    
    private float levelPower = 2f; // 2 for inverse square, 1 for inverse, 0.5 for inverse root
    private float levelMin = 0.1f; // simulates reverberation limit of inverse law
    
    private Tap n2Tap, n3Tap, n4Tap;
    private List<DelayTap> taps = new java.util.ArrayList<DelayTap>();
    
    public CabMicingControls() {
        super(DelayIds.CAB_MICING_ID, getString("Cab.Mic"));
        ControlColumn col = new ControlColumn();
        col.add(nControl = new NControl());
        col.add(dControl = new DControl());
        col.add(combControl = createCombControl());
        col.add(nearControl = createNearControl());
        add(col);
        taps.add(n2Tap = new Tap()); // n2
        taps.add(n3Tap = new Tap()); // n3
        taps.add(n4Tap = new Tap()); // n4
    }
    
    public List<DelayTap> getTaps() {
        return taps;
    }

    protected void derive(Control c) {
        switch ( c.getId() ) {
        case N_ID: 
            n = extract(nControl);
            break;
        case D_ID: 
            d = extract(dControl);
            separation = d + DRIVE_UNIT_SEPARATION;
            break;
        case NEAR_DISTANCE_ID:
            near = nearControl.getValue();
            break;
        case COMB_ID:
            levelPower = combControl.getValue();
            break;
        default:
            return;
        }
        calculateTaps();
    }
    
    protected int extract(EnumControl c) {
        String s = ((String)(c.getValue()));
        s = s.substring(0, s.length()-1); // lose trailing x or "
        return Integer.valueOf(s);
    }

    protected void calculateTaps() {
        // calculate some squares which are used multiple times
        float sepsqr = separation*separation;
        float nearsqr = near*near;
        
        // calculate distances from near mic in the drive unit plane
        float d1 = offset;
        float d2 = (float)Math.sqrt(d1*d1 + sepsqr);
        float d3 = d1 + separation;
        float d4 = (float)Math.sqrt(d3*d3 + sepsqr);
        
        // calculate path lengths point to point
        float l1 = (float)Math.sqrt(d1*d1 + nearsqr);
        float l2 = (float)Math.sqrt(d2*d2 + nearsqr);
        float l3 = (float)Math.sqrt(d3*d3 + nearsqr);
        float l4 = (float)Math.sqrt(d4*d4 + nearsqr);
        
        // calculate levels using inverse square type law relative to l1
        n2Tap.level  = n > 3 ? level(l2/l1) : 0f;
        n3Tap.level  = n > 1 ? level(l3/l1) : 0f;
        n4Tap.level  = n > 3 ? level(l4/l1) : 0f;
        
        // subtract l1 from the rest (saves a tap)
        l2 -= l1;
        l3 -= l1;
        l4 -= l1;
        
        // calculate path length times in milliseconds
        n2Tap.millis  = l2 * MILLIS_PER_INCH;
        n3Tap.millis  = l3 * MILLIS_PER_INCH;
        n4Tap.millis  = l4 * MILLIS_PER_INCH;
    }
    
    private float level(float in) {
        float out = 1f / (float)Math.pow(in, levelPower);
        return out < levelMin ? levelMin : out;
    }
    
    private FloatControl createNearControl() {
        FloatControl c = new FloatControl(NEAR_DISTANCE_ID, getString("Distance"), NEAR_DISTANCE_LAW, 0.1f, near);
        c.setInsertColor(Color.RED.darker());
        return c;
    }
    
    private FloatControl createCombControl() {
        FloatControl c = new FloatControl(COMB_ID, getString("Resonance"), POWER_LAW, 0.1f, levelPower);
        return c;
    }
    
    private class Tap implements DelayTap
    {
        float millis = 1f;
        float level = 0f;
        
        public float getDelayMilliseconds() {
            return millis;
        }

        public float getLevel() {
            return level;
        }
        
    }
    private static class NControl extends EnumControl
    {
        private final static List<String> values;
        
        static {
            values = new java.util.ArrayList<String>();
            values.add("2x");
            values.add("4x");
        }

        public NControl() {
            super(N_ID, "", "4x");
        }
        
        @Override
        public List getValues() {
            return values;
        }
        
    }

    private static class DControl extends EnumControl
    {
        private final static List<String> values;
        
        static {
            values = new java.util.ArrayList<String>();
            values.add("8\"");
            values.add("10\"");
            values.add("12\"");
            values.add("15\"");
            values.add("18\"");
        }

        public DControl() {
            super(D_ID, "", "12\"");
        }
        
        @Override
        public List getValues() {
            return values;
        }
        
    }
}
