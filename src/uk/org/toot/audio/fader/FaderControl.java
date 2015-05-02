// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.fader;

import uk.org.toot.control.LawControl;

/**
 * A FaderControl extends LawControl and differs from FloatControl in that
 * it is concerned with the unity gain point.
 */
public class FaderControl extends LawControl {
    // INSTANCE VARIABLES
    /** A label for the minimum value, such as "-inf" */
    private final String minLabel;

    /** A label for the maximum value, such as "+15" */
    private final String maxLabel;

    /** A label for the unity (0dB) value, such as "U" or "0" */
    private final String unityLabel;

    private static final String[] presetNames = { "Unity" };

    public FaderControl(int id, FaderLaw law) {
        this(id, law, 0f,
        "-inf", "U", String.valueOf(law.getMaxdB()));
    }

    /**
     * Constructs a new fader control object with the given parameters
     * @param initialValue the value that the control starts with when constructed
     * @param minLabel the label for the minimum value, such as "-infinity"
     * @param unityLabel the label for the unity value, such as "0dB"
     * @param maxLabel the label for the maximum value, such as "+15dB"
     */
    public FaderControl(int id, FaderLaw law, float initialValue,
        String minLabel, String unityLabel, String maxLabel) {
        super(id, "Level", law, 0.1f, initialValue);
        this.minLabel = ((minLabel == null) ? "" : minLabel);
        this.unityLabel = ((unityLabel == null) ? "" : unityLabel);
        this.maxLabel = ((maxLabel == null) ? "" : maxLabel);
    }

    /**
     * Constructs a new float control object with the given parameters.
     * The labels are set to zero-length strings.
     * @param initialValue the value that the control starts with when constructed
     */
    public FaderControl(int id, FaderLaw law, float initialValue) {
        this(id, law, initialValue, "", "", "");
    }

    // METHODS

	public String[] getPresetNames() { return presetNames; }

    public void applyPreset(String name) {
//        System.out.println("FaderControl.applyPreset: "+name);
        if ( name.equals("Unity") ) {
            setValue(0f);
        }
    }

    /**
     * Obtains the label for the minimum value, such as "Left" or "Off."
     * @return the minimum value label, or a zero-length string if no label	has been set
     */
    public String getMinLabel() {
        return minLabel;
    }

    /**
     * Obtains the label for the mid-point value, such as "Center" or "Default."
     * @return the mid-point value label, or a zero-length string if no label has been set
     */
    public String getUnityLabel() {
        return unityLabel;
    }

    /**
     * Obtains the label for the maximum value, such as "Right" or "Full."
     * @return the maximum value label, or a zero-length string if no label	has been set
     */
    public String getMaxLabel() {
        return maxLabel;
    }

    /**
     * Provides a string representation of the control
     * @return a string description
     */
    public String toString() {
        return getName() + " with current value: " + getValue() + " " + getLaw().getUnits() + " (max: " +((FaderLaw)getLaw()).getMaxdB() + ")";
    }

} // class FaderControl
