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
/**
  <p>An abstract class which will have concrete extensions to accept midi
  input and format the commands into an active buffer.

  <p>This class is abstract so that the various extensions can use different
  input techniques, such as Pure Java for windows and apple.com for Max OS X.

  @author Bob Lang
  @version 23 Mar 2003
*/
public abstract class MidiInputBase {
  // Links to other objects
  private CommandList
    commands;                   // Circular buffer of note on/off commands

  /**
    Constructor for the class.
  */
  protected MidiInputBase (CommandList inCommands) {
    commands = inCommands;
  } // MidiInputBase ()

  /**
    Accept a midi command.  This method is called by an instatiating object
    when a midi command is input.  The method filters out any unwanted
    commands and stores the rest in the command buffer.  It also converts all
    note off messages to true note off commands.
  */
  protected void acceptCommand (int status, int data1, int data2) {
    // Map from the message into appropriate variables
    int command = status >>>4;
    int pitch = data1;
    int velocity = data2;

    // Note on message (or special case of note off?)
    if (command == 9) {
      // True note on command
      if (velocity != 0) {
        //System.out.print ("+");
        commands.putCommand (CommandList.NOTE_ON, pitch);
        //System.out.println ("+");
      } // if
      else {
        // Simulate a note off command
        //System.out.print ("-");
        commands.putCommand (CommandList.NOTE_OFF, pitch);
        //System.out.println ("-");
      } // else
    } // if

    // Test for true note off command
    else if (command == 8) {
      // Note off command
      //System.out.print ("_");
      commands.putCommand (CommandList.NOTE_OFF, pitch);
      //System.out.println ("_");
    } // else if
  } // acceptCommand
} // MidiInputBase
