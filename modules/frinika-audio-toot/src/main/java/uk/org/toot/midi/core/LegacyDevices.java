// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Synthesizer;

/**
 * Support for legacy Java Sound Midi devices.
 * @author Steve Taylor
 */
public class LegacyDevices
{
    public static void addDevice(javax.sound.midi.MidiDevice device, MidiSystem system)
        throws MidiUnavailableException {
        system.addMidiDevice(new DeviceAdaptor(device));
    }

    /** Platform Ports are the resource limited ports provided by the platform. You can only have one of each of these Ports. */
    public static void installPlatformPorts(MidiSystem system) {
        javax.sound.midi.MidiDevice device = null;
        javax.sound.midi.MidiDevice.Info[] infos = 
        	javax.sound.midi.MidiSystem.getMidiDeviceInfo();
        int maxtx = 0;
        int maxrx = 0;
        for (int i = 0; i < infos.length; i++) {
            try {
                device = javax.sound.midi.MidiSystem.getMidiDevice(infos[i]);
                maxtx = device.getMaxTransmitters();
                maxrx = device.getMaxReceivers();
                if (maxtx != 0 && maxrx != 0) continue; // it's a Component
                addDevice(device, system);
            } catch (MidiUnavailableException e) {
                System.out.println(infos[i].getName()+" Unavailable!");
            }
        }
    }

    /**
     * Platform Components are the port groups provided by the platform. You may be able to have more than one of these.
     * Or you may not. Hmmm...
     */
    public static void installPlatformComponents(MidiSystem system) {
    	javax.sound.midi.MidiDevice device = null;
    	javax.sound.midi.MidiDevice.Info[] infos = 
    		javax.sound.midi.MidiSystem.getMidiDeviceInfo();
        int maxtx = 0;
        int maxrx = 0;
        for (int i = 0; i < infos.length; i++) {
            try {
                device = javax.sound.midi.MidiSystem.getMidiDevice(infos[i]);
                maxtx = device.getMaxTransmitters();
                maxrx = device.getMaxReceivers();
                if (maxtx == 0 || maxrx == 0) continue; // it's NOT a Component
                addDevice(device, system);
            } catch (MidiUnavailableException e) {
                System.out.println(infos[i].getName()+" Unavailable!");
            }
        }
    }

    public static class DeviceAdaptor extends AbstractMidiDevice
    {
        protected javax.sound.midi.MidiDevice device;
        private DeviceMidiInput input;
        private DeviceMidiOutput output;
        protected String name;

        public DeviceAdaptor(javax.sound.midi.MidiDevice device) throws MidiUnavailableException {
        	super("");
            this.device = device;
            name = simpleName(device.getDeviceInfo().getName());
            if (device.getMaxReceivers() != 0) {
            	input = new DeviceMidiInput(device);
                addMidiInput(input);
            }
            if (device.getMaxTransmitters() != 0) {
            	output = new DeviceMidiOutput(device);
                addMidiOutput(output);
            }
        }

        protected static String simpleName(String name) {
            String[] parts = name.split("\\s");
            if (parts == null) return name;
            if (name.startsWith("Microsoft ")) {
                return name.substring("Microsoft ".length(), name.length());
            }
            return name;
        }

        public void open() throws MidiUnavailableException {
            if ( !isOpen() ) {
            	System.out.print("Opening "+name+" ... ");
            	device.open();
            	System.out.println("opened");
            }
        }

        public void closeMidi() {
        	if ( input != null ) {
        		input.closeInput();
        	}
        	if ( output != null ) {
        		output.closeOutput();
        	}
        	if ( isOpen() ) {
            	System.out.print("Closing "+name+" ... ");
        		device.close();
    			System.out.println("closed");
        	}
        }

        public boolean isOpen() {
            return device.isOpen();
        }

        public String getProvider() { return device.getDeviceInfo().getVendor(); }

        public String getDescription() { return device.getDeviceInfo().getDescription(); }

        public class DeviceMidiInput implements MidiInput
        {
            private Receiver receiver;
            private javax.sound.midi.MidiDevice device;

            public DeviceMidiInput(javax.sound.midi.MidiDevice device) throws MidiUnavailableException {
                this.device = device;
                receiver = device.getReceiver();
            }

            public void transport(MidiMessage message, long timestamp) {
            	if ( !isOpen() ) {
            		try { 
            			open();
            		} catch ( MidiUnavailableException mua ) {
            			System.err.println(name+" failed open on demand");
            		}
            	}
                receiver.send(message, timestamp);
            }

            public void closeInput() {
            	receiver.close();
            }
            
            public int getLatency() {
                if (device instanceof Synthesizer) {
                    return (int)((Synthesizer)device).getLatency();
                }
                return 0;
            }
            
            public String getName() { return name; }
            
            public String toString() { return getName(); }
        }


        public class DeviceMidiOutput extends DefaultMidiOutput
        {
            private Transmitter tx;

            public DeviceMidiOutput(javax.sound.midi.MidiDevice device) throws MidiUnavailableException {
                super(simpleName(device.getDeviceInfo().getName()));
                // ensure the device transmits to a receiver
                tx = device.getTransmitter();
                tx.setReceiver(
                    new Receiver() {
                        public void send(MidiMessage message, long timestamp) {
							transport(message, timestamp);
                        }
                        public void close() { };
                    });
            }

            public void closeOutput() {
                tx.close();
            }
            
            public void addConnectionTo(MidiInput input) {
            	super.addConnectionTo(input);
            	if ( getConnectionCount() > 0 && !isOpen() ) {
            		try {
            			open();
            		} catch ( Exception e) {
            			e.printStackTrace();
            		}
            	}
            }

            public void removeConnectionTo(MidiInput input) {
            	super.removeConnectionTo(input);
            }
        }
    }        
}
