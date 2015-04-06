/*
 * Created on 24 Aug 2007
 *
 * Copyright (c) 2004-2007 P.J.Leonard
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
package com.frinika.tootX.midi;

import java.util.HashMap;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;


import com.frinika.tootX.midi.MidiFilter;
import uk.org.toot.control.Control;

/**
 * 
 * Implements a MidiFilter. A Controls are associated
 * with events using a hastable.
 * 
 * Usage:
 * 
 *   MidiEventRouter router = midiDeviceRouter.getROuter(dev.getDeviceInfo());
 *   router.setLearning(control);
 *   // use midi controller 
 * 
 * 
 * See for example MidiLearnFrame.
 * 
 * @author pjl
 */
public class MidiEventRouter implements MidiFilter {

    boolean learning;
    HashMap<Long, ControlMapper> map;
    HashMap<Control, Long> controlToHash;
    transient ShortMessage lastMessage = null;
    private Control focus;
  //  private MidiDevice dev;

    /**
     *
     */
    MidiEventRouter() {
//        this.dev = dev;
        map = new HashMap<Long, ControlMapper>();
        controlToHash = new HashMap<Control, Long>();
        learning = false;
    }

    public void close() {
    }

    /**
     * set mode to learning 
     * listen to midi event 
     * You then  call assignMapper()
     * 
     * @param focus  (control to be manipulated)
     */
    public void setLearning(Control focus) {
        this.focus = focus;
        learning = true;
//        System.out.println(" Learning to control using " + dev.getDeviceInfo().getName());
    }

    /**
     * implements MidiFilter
     * 
     * @param mess   midimessage
     * @param stamp  time stamp
     * @return  true if the event was found in the map and used
     */
    public boolean consume(MidiMessage mess, long stamp) {

        if (mess.getStatus() >= ShortMessage.MIDI_TIME_CODE) {
            return true;
        }
        if (!(mess instanceof ShortMessage)) {
            return true;
        }
        ShortMessage smsg = (ShortMessage) mess;

        if (learning) {
            System.out.println("LEARNING: ch cmd data1 data2: " + smsg.getChannel() + " " + smsg.getCommand() + " " + smsg.getData1() + " " + smsg.getData2());
            if (smsg.getCommand() == ShortMessage.NOTE_OFF) {
                return true; // let's ignore note offs for now
            }
            lastMessage = smsg;
            return true;
        } else {

            long key = MidiHashUtil.hashValue((ShortMessage) mess);
            ControlMapper mapper = map.get(key);
            if (mapper == null) {
                return false;
            }
            mapper.send(smsg, stamp);
            return true;

        }
       
    }

    /**
     * called when last message was the type you want to do the control.
     * 
     */
    public void assignMapper() {
        if (lastMessage == null || focus == null) {
            return;
        }
        long newHash = MidiHashUtil.hashValue(lastMessage);
        // ControlMapper mapper = map.get(hash);

        // remove old control mapping
        Long lastHash = controlToHash.get(focus);
        if (lastHash != null) {
            map.remove(lastHash);        // remove any use of this midi message 
        }
        map.remove(newHash);

 //       System.out.println("Assign entry for MidiEventRouter:" + dev.getDeviceInfo());


        map.put(newHash, new ControlMapper(focus, lastMessage));
        controlToHash.put(focus, newHash);

       learning=false;
             

    }

    public void assignMapping(Long midiHash, Control contrl) {
        map.put(midiHash, new ControlMapper(contrl, MidiHashUtil.reconstructShortMessage(midiHash, null)));
    }
}
