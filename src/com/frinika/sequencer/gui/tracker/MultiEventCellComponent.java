 /*  
  * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
 
package com.frinika.sequencer.gui.tracker;

import java.awt.event.KeyEvent;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.frinika.sequencer.gui.virtualkeyboard.NoteKeyThread;
import com.frinika.sequencer.gui.virtualkeyboard.VirtualKeyboard;

class MultiEventCellComponent extends JTextField
{
    private static final long serialVersionUID = 1L;
    
    public static final int EVENT_VALUE_DELETE = -2101;
    public static final int EVENT_VALUE_PITCH_BEND = -128;
    public static final int EVENT_VALUE_SYSEX = -129; // Jens
    
    private Integer eventValue = -2808;
    
    private TrackerPanel trackerPanel;
    private NoteKeyThread[] noteKeyThreads = new NoteKeyThread[128];
        
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#processKeyBinding(javax.swing.KeyStroke, java.awt.event.KeyEvent, int, boolean)
	 */
    
    public MultiEventCellComponent(TrackerPanel trackerPanel) {
        this.trackerPanel = trackerPanel;

    }
    
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {	    
	try{            
            if(e.getKeyCode()==KeyEvent.VK_DELETE) {
		eventValue = EVENT_VALUE_DELETE;
	    } else if(e.isControlDown() && e.getKeyChar()>='0' && e.getKeyChar()<='9') {
		if(pressed) {
                    if(this.getText().length() <= 3)
			this.setText("CC0"+e.getKeyChar());
		    else
			this.setText("CC"+this.getText().charAt(3)+""+e.getKeyChar());
    
                    // Control changes return event values -1 to -127
                    eventValue = (-1) * Integer.parseInt(this.getText().substring(2));
                }
	    } else if(e.isControlDown() && e.getKeyCode()==KeyEvent.VK_P) {
		this.setText("PB");
                eventValue = EVENT_VALUE_PITCH_BEND;
	    } else if(e.isControlDown() && e.isAltDown() && e.getKeyCode()==KeyEvent.VK_S) { // Jens
		this.setText("SYX");
                eventValue = EVENT_VALUE_SYSEX;
	    } else if(e.getModifiers()==0) {
		final int note = VirtualKeyboard.keyToInt(e.getKeyChar());
				
                eventValue = note;
                
                if(trackerPanel.getAutomaticRowJump()>0 )
                {
		    JTable trackerTable = trackerPanel.getTable();
		    // ----------------------------------------- This is for automatic row jumping
		    trackerTable.getCellEditor().stopCellEditing();
		    trackerTable.changeSelection(trackerTable.getSelectedRow()+trackerPanel.getAutomaticRowJump(),trackerTable.getSelectedColumn(),false,false);

		    // If automatic jump we don't want to hold the key as long as it's pressed (Since we want to move further down as soon as its pressed)
		    // - we'll just play it for 0.5 secs

		    new Thread()
		    {
			public void run()
			{
			    VirtualKeyboard.noteOn(getReceiver(),note,trackerPanel.getPart().getMidiChannel(),trackerPanel.getTableModel().getEditVelocity());
			    try
			    {
				    Thread.sleep(300);
			    } catch(Exception e) {}
			    VirtualKeyboard.noteOff(getReceiver(),note,trackerPanel.getPart().getMidiChannel());
			}
		    }.start();
		    //--------------------------------------------------
                }
                else
                {
		    if(noteKeyThreads[note]==null) {
			if(getReceiver()!=null)
			    noteKeyThreads[note] = new NoteKeyThread(noteKeyThreads,getReceiver(),note,trackerPanel.getPart().getMidiChannel(),trackerPanel.getTableModel().getEditVelocity());
			this.setText(VirtualKeyboard.getNoteString(note));
		    } else {
			noteKeyThreads[note].addKeyEvent(e);
		    }
		}	
	    } else {
		return super.processKeyBinding(ks, e, condition, pressed);
	    }
	} catch(Exception ex) {
	    return super.processKeyBinding(ks, e, condition, pressed);
	}
		
	return true;
    }

    /**
     * This is how to get the right receiver to use for a virtual keyboard
     * @return
     */
    private Receiver getReceiver() {
       try {
    	    MidiDevice midiDevice = trackerPanel.getPart().getTrack().getMidiDevice();
    	   	if(midiDevice!=null)
    	   		return midiDevice.getReceiver();
    	   	else
    	   		return null;
	} catch (MidiUnavailableException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * @return Returns the eventValue.
     */
    public Integer getEventValue() {
        return eventValue;
    }

    public void clearEventValue() {
	this.eventValue = -2808;
    }
}	