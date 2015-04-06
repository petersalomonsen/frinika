/** 
 * Copyright (c) 2005 - Bob Lang (http://www.cems.uwe.ac.uk/~lrlang/)
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

package com.frinika.contrib.boblang;
import javax.sound.midi.*;
import javax.swing.*;

/**
  A concrete implementation of MidiInputBase for pure Java Midi Input

  @author Bob Lang
  @version 22 Mar 2003
*/
public class PcMidiInputDriver extends MidiInputBase implements Receiver {
  /**
    Constructor for this class.  The parameters for this method are the
    command list which will receive the midi commands, and the index number
    of the input midi device (as given by MidiSystem.getMidiDevice ())
  */
  public PcMidiInputDriver (CommandList inCommands, int midiInputDevice) {
    // Call the parent constructor
    super (inCommands);

    // Start of exception handling block
    try {
      // Get the midi input from the stated device
      MidiDevice.Info [] midiInfo = MidiSystem.getMidiDeviceInfo ();
      MidiDevice
        dev = MidiSystem.getMidiDevice (midiInfo [midiInputDevice]);

      // Open the midi input and connect it to this object
      dev.open ();
      Transmitter t = dev.getTransmitter ();
      t.setReceiver (this);
    }
    catch (Exception e) {
      String m = "PcMidiInputDriver reports exception\n";
      m += e.toString ();
      JOptionPane.showMessageDialog (null,
                                     m,
                                     null,
                                     JOptionPane.ERROR_MESSAGE);
      
      System.out.println ("PcMidiInputDriver reports exception");
      System.out.println ("Unable to open input midi device");
      System.out.println (e);
    } // catch
  } // PcMidiInputDriver ()

  // **** Methods required by the Receiver interface

  /**
    Required method that doesn't do anything
  */
  public void close () {
    System.out.println ("Receiver Close requested");
  } // close

  /**
    Paradoxically, this method is called when a midi message is
    received from the input port.
  */
  public void send (MidiMessage message, long timeStamp) {
    // Extract the appropriate fields from the message
    ShortMessage m = (ShortMessage) message;

    /*
      int command = m.getCommand ()>>>4;
      int pitch = m.getData1 ();
      int velocity = m.getData2 ();
    */

    // Put the command into the buffer (inherited protected method)
    acceptCommand (m.getCommand (), m.getData1 (), m.getData2 ());
  } // send
} // PcMidiInputDriver
