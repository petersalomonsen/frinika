/*
 * Created on Feb 23, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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
package com.frinika.tootX;

import com.frinika.midi.MidiMessageListener;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 * A simple MidiMessageMonitor that looks for the peak value. It is reset when
 * yoiu call getPeak()
 *
 * @author pjl
 */
public class MidiPeakMonitor implements MidiMessageListener {

    float monitVal = 0.0f;

    public float getPeak() {
        float t = monitVal;
        monitVal = 0.0f;
        return t;
    }

    @Override
    public void midiMessage(MidiMessage message) {
        if (!(message instanceof ShortMessage)) {
            return;
        }

        if (message.getStatus() == ShortMessage.NOTE_ON) {
            float val = (float) ((((ShortMessage) message).getData1()) / 127.0);
            if (val > monitVal) {
                monitVal = val;
            }
        }
    }
}
