/*
 * Created on 27 Aug 2007
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

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import uk.org.toot.control.BooleanControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LawControl;


/**
 * 
 * Used to control a toot Control using the data of a midi message.
 * 
 * 
 * @author pjl
 */
public class ControlMapper implements Receiver {

    Control cntrl;
    ShortMessage proto;

    interface Valueizer {

        float getValue(ShortMessage mess);
    }
    Valueizer valueizer;

   /**
    * 
    * @param cntrl  the control to be tweaked 
    * @param proto  an example of the midi message to do the job
    */
    public ControlMapper(Control cntrl, ShortMessage proto) {
        this.cntrl = cntrl;
        this.proto = proto;
        switch (proto.getCommand()) {
            case ShortMessage.CONTROL_CHANGE:
                valueizer = new Valueizer() {

                    public float getValue(ShortMessage mess) {
                        return (float) (mess.getData2() / 127.0);
                    }
                };
                break;

            case ShortMessage.NOTE_ON:
                valueizer = new Valueizer() {

                    public float getValue(ShortMessage mess) {
                        return (float) (mess.getData2() / 127.0);
                    }
                };
                break;

            case ShortMessage.PITCH_BEND:
                valueizer = new Valueizer() {

                    public float getValue(ShortMessage mess) {
                        short low = (byte) mess.getData1();
                        short high = (byte) mess.getData2();

                        short val = (short) ((high << 7) | low);
                        System.out.println(" val = " + val);
                        // shm.setMessage(ShortMessage.PITCH_BEND,channel,value & 0x3f,(value >> 7));
                        return (float) (val / 8192.0);
                    }
                };

        }


    }

    public void close() {
    }

    public void send(MidiMessage mess, long arg1) {
        ShortMessage smsg = (ShortMessage) mess;
        System.out.println("ch cmd data1 data2: " + smsg.getChannel() + " " + smsg.getCommand() + " " + smsg.getData1() + " " + smsg.getData2());
        double t = valueizer.getValue((ShortMessage) mess);

        System.out.println(" Send message to " + cntrl + " " + t);

        if (cntrl instanceof ControlLaw) {
            ControlLaw law = ((LawControl) cntrl).getLaw();

            float val = (float) (law.getMaximum() * t + law.getMinimum() * (1 - t));
            ((LawControl) cntrl).setValue(val);
        } else if (cntrl instanceof BooleanControl) {
              System.out.println(" BOOLEAN" );
              ((BooleanControl) cntrl).setValue(t>0);       
        }
    }
}
