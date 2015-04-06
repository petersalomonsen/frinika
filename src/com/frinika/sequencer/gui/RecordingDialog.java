/*
 * Created on May 7, 2006
 *
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.gui;

import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import com.frinika.sequencer.FrinikaSequencer;

/**
 * Dialog for monitoring recording takes. Using this dialog you can do several takes using a loop - and then you'll be able to choose
 * the takes you want to insert into the track.
 * 
 * @author Peter Johan Salomonsen
 */
public class RecordingDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    Vector<Integer> recordingTakeNumbers = new Vector<Integer>();
    Vector<JToggleButton> recordingTakeTogglers = new Vector<JToggleButton>();
    
    int numberOfTakes = 1;
   
    private FrinikaSequencer sequencer;
    
    public RecordingDialog(JOptionPane recordingOptionPane, FrinikaSequencer sequencer) {
        super();
        this.sequencer = sequencer;
        sequencer.setRecordingTakeDialog(this);
        setContentPane(recordingOptionPane);
        setLayout(new GridLayout(0,1));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setAlwaysOnTop(true);
        pack();
    }

        
    public void notifyNewTake(int takeNo) {
        recordingTakeNumbers.add(takeNo);
        JToggleButton recordingTakeToggler = new JToggleButton("Take "+(numberOfTakes++),true);
        
        if(recordingTakeTogglers.size()>0)
            recordingTakeTogglers.get(recordingTakeTogglers.size()-1).setSelected(false);
        
        recordingTakeTogglers.add(recordingTakeToggler);
        add(recordingTakeToggler);
        validate();
        pack();
    }
    
    /**
     * Return the takes that are selected using the toggle buttons
     * @return
     */
    public int[] getDeployableTakes() {
        Vector<Integer> deployableTakes = new Vector<Integer>();
        
        for(int n = 0;n<recordingTakeTogglers.size();n++)
        {
            if(recordingTakeTogglers.get(n).isSelected())
                deployableTakes.add(recordingTakeNumbers.get(n));
            
        }
        
        int[] deployableTakesArr = new int[deployableTakes.size()];
        
        for(int n=0;n<deployableTakesArr.length;n++)
        {
            deployableTakesArr[n] = deployableTakes.get(n);
        }
        
        return deployableTakesArr;
    }
}
