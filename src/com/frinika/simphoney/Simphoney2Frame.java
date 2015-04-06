/*
 * Created on 23-Feb-2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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
package com.frinika.simphoney;

import com.frinika.tootX.gui.ControlFocus;
import java.awt.BorderLayout; 
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.flexdock.view.Viewport;



import com.frinika.project.ProjectContainer;
import com.frinika.project.RecordingManager;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.midi.MidiMessageListener;
import com.frinika.tootX.midi.MidiRouterSerialization;
import uk.org.toot.control.BooleanControl;
import uk.org.toot.swingui.controlui.BooleanControlPanel;

public class Simphoney2Frame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ProjectContainer project;
    private Viewport viewport;
//    FrinikaGAWrapper wrapper;
    JPanel content;
    
    
    ControlFocus controlFocus;
    

    Simphoney2Frame(ProjectFrame projectFrame) {
        this.project = projectFrame.getProjectContainer();
        controlFocus=new ControlFocus(projectFrame.getMidiLearnIF());

        // detach current recordingManager

        for (MidiMessageListener l : project.getSequencer().getMidiMessageListeners()) {
            if (l instanceof RecordingManager) {
                project.getSequencer().removeMidiMessageListener(l);
                break;
            }
        }

        SimphoneyRecordManager rec=new SimphoneyRecordManager(projectFrame);

        BooleanControl loop=rec.getLoopMarkerControl();
        project.getControlResolver().register(loop);
        JPanel loopBut=new BooleanControlPanel(loop);
        controlFocus.addComponent(loopBut);
        
  //      createWrapper();
        createMenus();


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setIconImage(new javax.swing.ImageIcon(ProjectFrame.class.getResource("/icons/frinika.png")).getImage());

        content = new JPanel(new BorderLayout());
        setContentPane(content);
        content.add(loopBut,BorderLayout.NORTH);
        
        content.add((JPanel)projectFrame.getMidiLearnIF(),BorderLayout.SOUTH);
//
//		viewport = new Viewport();
//
//		content.add(viewport);
        validate();
        setVisible(true);

        Rectangle rect = new Rectangle(projectFrame.getBounds());

        rect.width = rect.width / 3;
        rect.height = rect.height / 2;
        setBounds(rect);

        // plugin context menus



     //   content.add(new PlayerFocusPanel(wrapper));

//        wrapper.setListening(true);

        MidiRouterSerialization s=project.getMidiRouterSerialization();
        if (s != null) s.buildDeviceRouter(project.getControlResolver(),
                    project.getMidiDeviceRouter());
        else
            System.out.println(" MidiRouter serializer is null ");
    }

//    void createWrapper() {
//        wrapper = new FrinikaGAWrapper(project);
//    }

    @SuppressWarnings("serial")
    void createMenus() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menu = new JMenu("AI");


//        MenuPlugin plugin = new SimphoneyPartMenuPlugin(wrapper);
//        Part.addPluginRightButtonMenu(plugin);
//        LaneHeaderItem.addPluginRightButtonMenu(plugin);
//
//        menu.add(new JMenuItem(new AbstractAction("rep ---> frinika") {
//
//            public void actionPerformed(ActionEvent e) {
//                wrapper.useMelodicTemplate();
//            }
//        }));
//
//
//        menu.add(new JMenuItem(new AbstractAction("AI listener") {
//
//            public void actionPerformed(ActionEvent e) {
//            }
//        }));
//
//
//        menu.add(new JMenuItem(new AbstractAction("Random part fill") {
//
//            public void actionPerformed(ActionEvent e) {
//                project.getEditHistoryContainer().mark("X");
//                wrapper.fillPart();
//                project.getEditHistoryContainer().notifyEditHistoryListeners();
//            }
//        }));
        menuBar.add(menu);
    }
}
