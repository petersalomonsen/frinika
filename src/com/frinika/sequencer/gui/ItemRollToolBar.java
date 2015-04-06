/*
 * Created on Jan 19, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.sound.midi.Sequence;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.frinika.gui.ToolbarSeperator;
import com.frinika.project.ProjectContainer;

import static com.frinika.gui.util.ButtonFactory.makePressButton;
import static com.frinika.gui.util.ButtonFactory.makeToggleButton;
import static com.frinika.localization.CurrentLocale.getMessage;

public class ItemRollToolBar extends JToolBar implements ActionListener {

	private static final long serialVersionUID = 1L;

	Insets insets = new Insets(0, 0, 0, 0);

	Cursor writeCursor;

	JToggleButton zoomBut = null; // need to rememebr the zoom button to

	// deselect after zooming
	JToggleButton follow;

	JToggleButton quantize;
	JPanel zoom;
	
	Vector<ItemPanel> clients;

	ProjectContainer project;

	private JButton quantizeSet;
	JPanel tools;
	ButtonGroup toolGroup;
	
	public ItemRollToolBar(Vector<ItemPanel> cli, ProjectContainer project) {
		this.setMargin(new Insets(0, 0, 0, 0));
		this.project = project;
		FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 0, 0);
		this.clients = cli;
		for (ItemPanel client : clients)
			client.setToolBar(this);
		tools = new JPanel(layout);
		tools.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		tools.setOpaque(false);

		toolGroup = new ButtonGroup();

		JToggleButton but = makeToggleButton("select", "select",
				getMessage("seqeuncer.toolbar.select_tip"), this, toolGroup, tools);
		but.setMargin(insets);

		// Toolkit.getDefaultToolkit().createCustomCursor(
		// icon.getImage(), new Point(2,icon.getIconHeight()-2), "select");
		// selectTool =

		makeToggleButton("pencil", "write",getMessage("seqeuncer.toolbar.write_tip"), this, toolGroup, tools)
		.setMargin(insets);

		makeToggleButton("eraser", "erase", getMessage("seqeuncer.toolbar.erase_tip"), this, toolGroup, tools)
				.setMargin(insets);

		makeToggleButton("hand", "dragview", getMessage("seqeuncer.toolbar.dragclick_tip"),
				this, toolGroup, tools).setMargin(insets);

	
		
		add(tools);
		add(new ToolbarSeperator());

		JPanel settings = new JPanel(layout);
		//settings.setBorder(BorderFactory.createEtchedBorder());
		settings.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		settings.setOpaque(false);
		
		follow = makeFollowSongButton(this,settings);
        
		follow.setSelected(clients.firstElement().isFollowSong());
		follow.setMargin(insets);

		quantize = makeToggleButton("quantize", "snaptoON", getMessage("sequencer.toolbar.snapto_toggle_tip"), this,
				null, settings);

		quantize.setMargin(insets);
		quantize.setSelected(clients.firstElement().isSnapQuantized());

        quantizeSet = makeSnapToButton(clients,settings,project.getSequence());
		quantizeSet.setMargin(insets);

		// this assumes action listneres are notify in the order they are added

		add(settings);
		add(new ToolbarSeperator());

		zoom = new JPanel(layout);
		// zoom.setMargin(insets);
		//zoom.setBorder(BorderFactory.createEtchedBorder());
		zoom.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		zoom.setOpaque(false);

		zoomBut=makeToggleButton("viewmagfit", "magrect",
				getMessage("sequencer.toolbar.zoomtorect_tip"), this, null, zoom);
		
		zoomBut.setMargin(insets);

		makePressButton("viewmag+", "zoomin", getMessage("sequencer.toolbar.zoomin_tip"), this, zoom).setMargin(
				insets);
		

		makePressButton("viewmag-", "zoomout", getMessage("sequencer.toolbar.zoomout_tip"), this, zoom)
				.setMargin(insets);
		

	//	makePressButton("extend", "extend", "Extend project", this, zoom)
	//	.setMargin(insets);

		add(zoom);
		add(new ToolbarSeperator());

	
		
		// JPanel trans = new TransportPanel(pianoRoll.sequencer);
		// trans.setBorder(BorderFactory.createEtchedBorder());
		// add(trans);
		but.setSelected(true);
		for (ItemPanel client : clients)
			client.setTool("select");
		// buttonPressed("select");
	}

	public void addButtonToTools(String icon,String cmd,String popup){
		makeToggleButton(icon, cmd, popup, this, toolGroup, tools)
		.setMargin(insets);

	
	}
	
	public static JToggleButton makeFollowSongButton(ActionListener actionListener,JPanel panel)
    {
        return makeToggleButton(getMessage("sequencer.play.follow"), getMessage("sequencer.play.follow"), getMessage("sequencer.play.follow_song"), actionListener,
                null, panel);
    }

    public static JButton makeSnapToButton(final Vector clients, JPanel panel, final Sequence sequence)
    {
        final JButton snapToButton = makePressButton("music_quarternote", "snaptoSET",
            getMessage("sequencer.toolbar.snaptolength_tip"), null, panel);
        snapToButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new NoteLengthPopup(snapToButton, clients, sequence).show(snapToButton, 0, 0);
                    }
                });
        NoteLengthPopup.updateButton(snapToButton, clients, sequence);
        return snapToButton;
    }
    
	public void rectZoomFinished() {
		for (ItemPanel client : clients)
			client.setTool("origtool");
		zoomBut.setSelected(false);
	}

	public void actionPerformed(ActionEvent e) {

		String cmd = e.getActionCommand();
		// System.out.println(" PRollToolPanel " + cmd);

		if (cmd.equals("zoomin")) { // TODO keep it still
			for (ItemPanel client : clients)
				client.zoomIn();
			return;
		} else if (cmd.equals("zoomout")) {
			for (ItemPanel client : clients)
				client.zoomOut();
			return;
		} else if (cmd.equals("follow")) {
			for (ItemPanel client : clients)
				client.followSong(follow.isSelected());
			return;
		} else if (cmd.equals("extend")) {
			project.setEndTick(project.getEndTick()+project.getSequence().getResolution()*8);
			return;	
		} else if (cmd.equals("snaptoON")) {
			for (ItemPanel client : clients)
				client.setSnapQuantized(quantize.isSelected());
			return;
		} else {
			for (ItemPanel client : clients) {
				client.setTool(cmd);
			}
			// setTool(tool);
		}
	}

	public JPanel getZoomPanel() {
		return zoom;
	}
	

	public JPanel getToolsPanel() {
		return tools;
	}
	
}
