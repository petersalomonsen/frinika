
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
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import uk.org.toot.control.Control;

/**
 *
 * A tempory object to aid the serialization of the MidiRouting
 * 
 * @author pjl
 */
public class MidiRouterSerialization implements Serializable {

    private static final long serialVersionUID = 1L;
    //  transient MidiDeviceRouter router;
//    transient ProjectContainer project;
    Vector<DevRep> devReps;    // persist a list of DevReps

    class DevRep implements Serializable {

        DevRep(String name) {
            devName = name;
            maprep = new Vector<MapEntryRep>();
        }
        String devName;
        Vector<MapEntryRep> maprep;
    }

    class MapEntryRep implements Serializable {

        Long midiHash;
        String cntrlKey;

        private MapEntryRep(Long value, String cntrlName) {
            midiHash = value;
            cntrlKey = cntrlName;
        }
    }

    /**
     * Creates the serializable representation used for saving
     * 
     * @param router
     */
    public void buildSerialization(ControlResolver cntrlResolver,MidiDeviceRouter router) {
        devReps = new Vector<DevRep>();
        System.out.println(" BUilding MidiRouter serialization ");
        for (Entry<MidiDevice.Info, MidiEventRouter> e : router.map.entrySet()) {
            String devName = e.getKey().toString();
        
            System.out.println(" DEVICE:" + devName );
            
            
            DevRep dr = new DevRep(devName);
            MidiEventRouter er = e.getValue();
            devReps.add(dr);
            for (Entry<Control, Long> ee : er.controlToHash.entrySet()) {
                String cntrlName =cntrlResolver.generateKey(ee.getKey());
                dr.maprep.add(new MapEntryRep(ee.getValue(), cntrlName));
                System.out.println(ee.getValue() + "|"+ cntrlName);
            }
        }
    }

    public MidiRouterSerialization() {
    }

    /**
     *  Used after reading serialized form  
     */
    public void buildDeviceRouter(ControlResolver cntrlResolver, MidiDeviceRouter router) {


        System.out.println(" Building Device Router ");
        
        for (DevRep devrep : devReps) {
            System.out.println("Device:"+ devrep.devName);
            
            try {
                MidiDevice dev = MidiDeviceResolver.resolveDevice(devrep.devName);

                if (dev == null) {
                    throw new Throwable(" Unknown device in MidiRouter table " + devrep.devName);
                }

                MidiEventRouter eventRouter = router.getRouter(dev);
                for (MapEntryRep er : devrep.maprep) {
                    Control contrl = cntrlResolver.resolve(er.cntrlKey);
                     System.out.println("map:" + er.midiHash + ":" + er.cntrlKey );
                      
                    if (contrl == null) {
                        throw new Throwable(" Unknown control in MidiRouter table " + er.cntrlKey);
                    }
                    eventRouter.assignMapping(er.midiHash, contrl);
                }

            } catch (Throwable ex) {
                Logger.getLogger(MidiRouterSerialization.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}

