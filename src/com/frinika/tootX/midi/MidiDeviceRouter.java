
/*
 *
 * Copyright (c) 2006 P.J.Leonard
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

import java.io.Serializable;
import java.util.Hashtable;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;

/**
 * 
 * Allows routing of midievents from a particular device.
 * 
 * @author pjl
 */
public class MidiDeviceRouter  {
    
    
    Hashtable<MidiDevice.Info, MidiEventRouter> map;

  
    public MidiDeviceRouter() {
        map = new Hashtable<MidiDevice.Info, MidiEventRouter>();
    }

    /**
     * 
     * Returns or creates a new MidiEventRouter
     * 
     * @param dev
     * @return   MidiEventRouter associated with the dev
     */
   public  MidiEventRouter getRouter(MidiDevice dev) {
        if (dev == null) {
            return null;
        }
        MidiEventRouter router = map.get(dev.getDeviceInfo());
        if (router == null) {
            router = new MidiEventRouter();
            System.out.println(" MidiDeviceRouter adding device: " + dev.getDeviceInfo());
            map.put(dev.getDeviceInfo(), router);
//            try {
//                if (!dev.isOpen()) {
//                    dev.open();
//                }
//                dev.getTransmitter().setReceiver(router);
//            } catch (MidiUnavailableException e) {
//                e.printStackTrace();
//            }

        }
        return router;
    }

   /**
    * 
    * 
    * @param devInfo
    * @param mess 
    * @param stamp
    * @return
    */
    public boolean consume(Info devInfo, MidiMessage mess, long stamp) {
        MidiEventRouter cntrl=map.get(devInfo);
        if (cntrl != null) {
            if (cntrl.consume(mess, stamp)) return true;
        }
        return false;
    }
    
    
    
}
