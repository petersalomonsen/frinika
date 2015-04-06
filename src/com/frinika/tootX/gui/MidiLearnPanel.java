/*
 * Created on 24 Aug 2007
 *
 * Copyright (c) 2004-2007 Peter Johan Salomonsen
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
package com.frinika.tootX.gui;

import com.frinika.tootX.midi.MidiLearnIF;
import com.frinika.tootX.midi.MidiEventRouter;
import com.frinika.tootX.midi.MidiDeviceRouter;
import com.frinika.tootX.midi.MidiInDeviceManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.ShortMessage;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.org.toot.swingui.controlui.ControlPanel;

public class MidiLearnPanel extends JPanel implements MidiLearnIF  {

   
     
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
  
    ShortMessage lastMessage;
    MidiDevice dev = null;
    MidiInDeviceSelectPanel deviceSelector;
    ControlPanel focus;
   // static MidiLearnFrame the;
    MidiDeviceRouter devRouter;
    
    public  MidiLearnPanel(final MidiDeviceRouter devRouter) {
  //      assert (the == null);
    
        this.devRouter=devRouter;
     //   the = this;
//        JPanel panel = new JPanel();
//        setContentPane(panel);

        JPanel panel=this;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        deviceSelector = new MidiInDeviceSelectPanel();
        panel.add(deviceSelector);

        deviceSelector.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                dev = deviceSelector.getSelected();

            //			monit(dev);
            }
        });

        JPanel buts = new JPanel();
        panel.add(buts);
        buts.setLayout(new BoxLayout(buts, BoxLayout.X_AXIS));
        JButton apply = new JButton("OK");
        buts.add(apply);

        apply.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                dev = deviceSelector.getSelected();
                if (dev == null) {
                    return;
                }
                MidiEventRouter router = devRouter.getRouter(dev);
                router.assignMapper();
            }
        });

        JButton learn = new JButton("Learn");
        buts.add(learn);
        learn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                dev = deviceSelector.getSelected();
                if (dev == null) {
                    return;
                }
                MidiEventRouter router = devRouter.getRouter(dev);
                router.setLearning(focus.getControl());
            }
        });
    //    pack();
        //this.setVisible(true);
  //      deviceSelector.setSelectedIndex(0);

    }

//    static MidiLearnFrame the() {
//        if (the == null) {
//            the = new MidiLearnFrame();
//        }
//        return the;
//    }

    public void setFocus(ControlPanel focus1) {
//       MidiInDeviceManager.open();
        this.focus = focus1;

//		Runtime.getRuntime().addShutdownHook(new Thread(new ExitHandler()));

    }

  

//	void monit(MidiDevice dev1) {
//		// if (in != null)
//		// if (in.isOpen()) in.close();
//
//		if (!dev1.isOpen()) {
//			try {
//				dev1.open();
//				in = dev1.getTransmitter();
//			} catch (MidiUnavailableException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			dev = dev1;
//		}
//		System.out.println(" COnnecting " + dev);
//		in.setReceiver(monit);
//	}
    class ExitHandler implements Runnable {

        public void run() {
            if (dev != null) {
                dev.close();
            }
        }
    }
}
