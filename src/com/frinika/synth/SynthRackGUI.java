/*
 * Created on Sep 30, 2004
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
package com.frinika.synth;

import java.awt.*;
import javax.swing.*;

import com.frinika.tracker.filedialogs.OpenGearDialog;


/**
 * The Graphical User Interface for the generic synth container
 * @author Peter Johan Salomonsen
 *
 */
public class SynthRackGUI extends JPanel {
    private static final long serialVersionUID = 1L;

    SynthRack ms;
    SynthMixerSlot[] synthStrips;
    OpenGearDialog openGearDialog = null;
    JFrame frame;
    
    public SynthRackGUI(JFrame frame, SynthRack ms)
    {
        this.frame = frame;
        this.ms = ms;
        ms.gui = this;
        synthStrips = new SynthMixerSlot[ms.synths.length];
        initialize();
        frame.setTitle("Frinika SynthRack - Legacy Frinika soft synths by Peter Salomonsen");
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        
        for(int n=0;n<synthStrips.length;n++)
        {
                synthStrips[n]=new SynthMixerSlot(this,gc,ms,n);
        }        
    }

    public void initLoadSynthSetupProgress(Thread loadingThread)
    {
        openGearDialog = new OpenGearDialog(frame);
        loadingThread.start();
        openGearDialog.setProgress(0,"");
        openGearDialog.setVisible(true);
    }
    
    public void notifyLoadSynthSetupProgress(int progress, String synthName) {
        openGearDialog.setProgress(progress,synthName);
        if(progress==100)
            openGearDialog = null;
    }
    
    public static void main(String[] args) throws Exception
    {
        SynthRack.main(args);
    }
}
