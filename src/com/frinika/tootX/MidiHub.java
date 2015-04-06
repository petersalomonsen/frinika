package com.frinika.tootX;


import java.util.Vector;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;

import com.frinika.midi.MidiDebugDevice;
//import com.sun.media.sound.RealTimeSequencer;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author pjl
 * @version 1.0
 * 
 * @deprecated
 * 
 */
public class MidiHub {

	
    static MidiDeviceHandle[] recvHandle;
    
    static MidiDeviceHandle[] transHandle;


  
    static {

        MidiDevice[] recvList;
        MidiDevice[] transList;
        Info info[] = MidiSystem.getMidiDeviceInfo();

        Vector<MidiDevice> recV = new Vector<MidiDevice>();
        Vector<MidiDevice> transV = new Vector<MidiDevice>();

        for (int i = 0; i < info.length; i++) {
         //   System.out.println("---------------------------------------------");
            MidiDevice dev = null;
         
            
            
              try {
                dev = MidiSystem.getMidiDevice(info[i]);
           
              } catch (MidiUnavailableException ex1) {
                ex1.printStackTrace();
            }

              // TODO: Why do you want to exclude these two? This also excludes RasmusDSP
              // At one point they did not work with frinika. Do they now? The javvasound  synth ? (PJL)
              
              //if (dev instanceof javax.sound.midi.Synthesizer) continue;
              //if (dev instanceof javax.sound.midi.Sequencer) continue;
         
              
            if (dev.getMaxReceivers() != 0) {
                recV.add(dev);               
            }
   
            if (dev.getMaxTransmitters() != 0) {
                transV.add(dev);
            }
        }


        MidiDebugDevice filt= new MidiDebugDevice();
        recV.add(filt);
        recvList = new MidiDevice[recV.size()];
        recV.toArray(recvList);


        recvHandle = makeHandles(recvList);
        transV.add(filt);
        transList = new MidiDevice[transV.size()];
        transV.toArray(transList);

        transHandle = makeHandles(transList);

        System.out.println(" Trans devices ------------------------------ ");

        for (int i = 0; i < transList.length; i++) {
            System.out.println(transList[i].getDeviceInfo());

        }
        
        System.out.println(" Recv  devices ------------------------------- ");

        for (int i = 0; i < recvList.length; i++) {
            System.out.println(recvList[i].getDeviceInfo());
        }
    }

    static public MidiDeviceHandle[] getMidiOutHandles() {
    	return recvHandle;
    }
 
    static public MidiDeviceHandle[] getMidiInHandles() {
    	return transHandle;
    }
    
    static public MidiDeviceHandle getMidiInHandleOf(MidiDevice dev) {
        for (MidiDeviceHandle h : transHandle) {
            //  System.out.println(dev + "    " + h.getMidiDevice());
            if (dev == h.getMidiDevice()) {
                return h;
            }
        }

        System.err.println(" Device not in list " + dev);
        return null;

    }

    static public MidiDeviceHandle getMidiOutHandleOf(MidiDevice dev) {
        for (MidiDeviceHandle h : recvHandle) {
            if (dev == h.getMidiDevice()) {
                return h;
            }
        }
        System.err.println(" Device not in  list " + dev);
        return null;

    }

    public static MidiDevice getMidiOutDeviceByName(String name) {
    	for (MidiDeviceHandle handle:recvHandle)
    	{
    		if (handle.toString().equals(name)) return handle.getMidiDevice();
    	}
    	return null;
    }
    
    public static MidiDevice getMidiInDeviceByName(String name) {
    	for (MidiDeviceHandle handle:transHandle)
    	{
    		if (handle.toString().equals(name)) return handle.getMidiDevice();
    	}
    	return null;
    }
    
    static MidiDeviceHandle[] makeHandles(MidiDevice[] l) {
        MidiDeviceHandle[] ret = new MidiDeviceHandle[l.length + 1];
        ret[0] = new MidiDeviceHandle(null);
        for (int i = 0; i < l.length; i++) {
            ret[i + 1] = new MidiDeviceHandle(l[i]);
        }
        return ret;
    }

  
}
