/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frinika.tootX.midi;

import javax.sound.midi.MidiMessage;

/**
 * A MidiFilter is used to remove events from a midi stream.
 *
 * @author pjl
 */
public interface MidiFilter {

    /**
     * 
     * @param message
     * @param timeStamp
     * @return true if we have consumed the message
     */
    boolean consume(MidiMessage message, long timeStamp);

}
