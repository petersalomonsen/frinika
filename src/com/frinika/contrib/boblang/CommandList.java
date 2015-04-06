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
  Implements a thread safe circular buffer of commands with appropriate
  notify and time out facilities.
  @author Bob Lang
  @version 15 Nov 2002
*/
public class CommandList {
  // Exported constants
  public static final int
    NOTE_ON  = 1,               // Command code for note on
    NOTE_OFF = 2;               // Command code for note off

  // Local constants
  private static final int
    MAX_COMMAND_LIST = 32;      // Max number of stored commands

  // Pointers and data counter
  private int
    putPointer,                 // Pointer to last command in list
    getPointer,                 // Pointer to first command in list
    counter;                    // Number of commands waiting

  // Circular buffers
  private int []
    commandType = new int [MAX_COMMAND_LIST],
    commandNote = new int [MAX_COMMAND_LIST];

  // Dummy array used in getCommand ()
  private int []
    retVal = new int [2];

  /**
    Constructor to create an empty buffer
  */
  public CommandList () {
    putPointer = 0;
    getPointer = 0;
    counter = 0;
  } // CommandList ()

  /**
    Put a command in the buffer and then notify any waiting
    tasks.
  */
  public synchronized void putCommand (int type, int note) {
    // Incr put pointer and check for overflow
    putPointer++;
    if (putPointer >= MAX_COMMAND_LIST) {
      putPointer = 0;
    } // if

    // Store the command in the buffer
    commandType [putPointer] = type;
    commandNote [putPointer] = note;

    // Flag that there's more data waiting and notify waiters
    counter++;
    //notifyAll ();
  } // putCommand ()

  /**
    Return true if a command is waiting in the command buffer
  */
  public synchronized boolean isCommandWaiting (long waitMsecs) {
    //try {
    //  wait (waitMsecs);
    // }
    //catch (Exception e) {
    //  System.out.println (e);
    //}
    
    // Return true if data in buffer
    return (counter > 0);
  } // isCommandWaiting ()

  /**
    Get the next command type and note from the buffer.  It is assumed
    that waitForCommand has been called prior to this method so that
    it is known that there is data waiting to be taken
  */
  public synchronized int [] getCommand () {
    // Establish that there is data waiting
    if (counter > 0) {
      // Increment take pointer, dealing with overflow
      getPointer++;
      if (getPointer >= MAX_COMMAND_LIST) {
        getPointer = 0;
      } // if

      // Extract the data
      retVal [0] = commandType [getPointer];
      retVal [1] = commandNote [getPointer];

      // Backup the counter by one
      counter--;
    } // if
    else {
      // return null result
      retVal [0] = 0;
      retVal [1] = 0;
    } // else

    // Return the resulting data
    return retVal;
  } // getCommand ()
} // CommandList
