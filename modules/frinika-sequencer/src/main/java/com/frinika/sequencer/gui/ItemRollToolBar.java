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

import com.frinika.gui.ToolbarSeperator;
import com.frinika.gui.util.ButtonFactory;
import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import javax.sound.midi.Sequence;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class ItemRollToolBar extends JToolBar implements ActionListener {

    private static final long serialVersionUID = 1L;

    Insets insets = new Insets(0, 0, 0, 0);

    Cursor writeCursor;

    JToggleButton zoomBut = null; // need to rememebr the zoom button to

    // deselect after zooming
    JToggleButton follow;

    JToggleButton quantize;
    JPanel zoom;

    List<ItemPanel> clients;

    AbstractProjectContainer project;

    private JButton quantizeSet;
    JPanel tools;
    ButtonGroup toolGroup;

    public ItemRollToolBar(List<ItemPanel> cli, AbstractProjectContainer project) {
        this.setMargin(new Insets(0, 0, 0, 0));
        this.project = project;
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 0, 0);
        this.clients = cli;
        for (ItemPanel client : clients) {
            client.setToolBar(this);
        }
        tools = new JPanel(layout);
        tools.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tools.setOpaque(false);

        toolGroup = new ButtonGroup();

        JToggleButton but = ButtonFactory.makeToggleButton(ItemRollToolBar.class.getResource("/icons/select.png"), "select",
                CurrentLocale.getMessage("sequencer.toolbar.select_tip"), this, toolGroup, tools);
        but.setMargin(insets);

        // Toolkit.getDefaultToolkit().createCustomCursor(
        // icon.getImage(), new Point(2,icon.getIconHeight()-2), "select");
        // selectTool =
        ButtonFactory.makeToggleButton(ItemRollToolBar.class.getResource("/icons/pencil.png"), "write", CurrentLocale.getMessage("sequencer.toolbar.write_tip"), this, toolGroup, tools)
                .setMargin(insets);

        ButtonFactory.makeToggleButton(ItemRollToolBar.class.getResource("/icons/eraser.png"), "erase", CurrentLocale.getMessage("sequencer.toolbar.erase_tip"), this, toolGroup, tools)
                .setMargin(insets);

        ButtonFactory.makeToggleButton(ItemRollToolBar.class.getResource("/icons/hand.png"), "dragview", CurrentLocale.getMessage("sequencer.toolbar.dragclick_tip"),
                this, toolGroup, tools).setMargin(insets);

        add(tools);
        add(new ToolbarSeperator());

        JPanel settings = new JPanel(layout);
        //settings.setBorder(BorderFactory.createEtchedBorder());
        settings.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        settings.setOpaque(false);

        follow = makeFollowSongButton(this, settings);

        follow.setSelected(clients.get(0).isFollowSong());
        follow.setMargin(insets);

        quantize = ButtonFactory.makeToggleButton(ItemRollToolBar.class.getResource("/icons/quantize.png"), "snaptoON", CurrentLocale.getMessage("sequencer.toolbar.snapto_toggle_tip"), this,
                null, settings);

        quantize.setMargin(insets);
        quantize.setSelected(clients.get(0).isSnapQuantized());

        List<? extends Snapable> snapable = clients;
        quantizeSet = makeSnapToButton((List<Snapable>) snapable, settings, project.getSequence());
        quantizeSet.setMargin(insets);

        // this assumes action listneres are notify in the order they are added
        add(settings);
        add(new ToolbarSeperator());

        zoom = new JPanel(layout);
        // zoom.setMargin(insets);
        //zoom.setBorder(BorderFactory.createEtchedBorder());
        zoom.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        zoom.setOpaque(false);

        zoomBut = ButtonFactory.makeToggleButton(ItemRollToolBar.class.getResource("/icons/viewmagfit.png"), "magrect",
                CurrentLocale.getMessage("sequencer.toolbar.zoomtorect_tip"), this, null, zoom);

        zoomBut.setMargin(insets);

        ButtonFactory.makePressButton(ItemRollToolBar.class.getResource("/icons/viewmag+.png"), "zoomin", CurrentLocale.getMessage("sequencer.toolbar.zoomin_tip"), this, zoom).setMargin(
                insets);

        ButtonFactory.makePressButton(ItemRollToolBar.class.getResource("/icons/viewmag-.png"), "zoomout", CurrentLocale.getMessage("sequencer.toolbar.zoomout_tip"), this, zoom)
                .setMargin(insets);

        //	ButtonFactory.makePressButton("extend", "extend", "Extend project", this, zoom)
        //	.setMargin(insets);
        add(zoom);
        add(new ToolbarSeperator());

        // JPanel trans = new TransportPanel(pianoRoll.sequencer);
        // trans.setBorder(BorderFactory.createEtchedBorder());
        // add(trans);
        but.setSelected(true);
        for (ItemPanel client : clients) {
            client.setTool("select");
        }
        // buttonPressed("select");
    }

    public void addButtonToTools(URL iconUrl, String cmd, String popup) {
        ButtonFactory.makeToggleButton(iconUrl, cmd, popup, this, toolGroup, tools)
                .setMargin(insets);

    }

    public static JToggleButton makeFollowSongButton(ActionListener actionListener, JPanel panel) {
        return ButtonFactory.makeToggleButton(ItemRollToolBar.class.getResource("/icons/follow.png"), CurrentLocale.getMessage("sequencer.play.follow"), CurrentLocale.getMessage("sequencer.play.follow_song"), actionListener,
                null, panel);
    }

    public static JButton makeSnapToButton(final List<Snapable> clients, JPanel panel, final Sequence sequence) {
        final JButton snapToButton = ButtonFactory.makePressButton(ItemRollToolBar.class.getResource("/icons/music_quarternote.png"), "snaptoSET",
                CurrentLocale.getMessage("sequencer.toolbar.snaptolength_tip"), null, panel);
        snapToButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new NoteLengthPopup(snapToButton, clients, sequence).show(snapToButton, 0, 0);
            }
        });
        NoteLengthPopup.updateButton(snapToButton, clients, sequence);
        return snapToButton;
    }

    public void rectZoomFinished() {
        for (ItemPanel client : clients) {
            client.setTool("origtool");
        }
        zoomBut.setSelected(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();
        // System.out.println(" PRollToolPanel " + cmd);
        switch (cmd) {
            case "zoomin":
                // TODO keep it still
                for (ItemPanel client : clients) {
                    client.zoomIn();
                }
                return;
            case "zoomout":
                for (ItemPanel client : clients) {
                    client.zoomOut();
                }
                return;
            case "follow":
                for (ItemPanel client : clients) {
                    client.followSong(follow.isSelected());
                }
                return;
            case "extend":
                project.setEndTick(project.getEndTick() + project.getSequence().getResolution() * 8);
                return;
            case "snaptoON":
                for (ItemPanel client : clients) {
                    client.setSnapQuantized(quantize.isSelected());
                }
                return;
            default:
                for (ItemPanel client : clients) {
                    client.setTool(cmd);
                }
                // setTool(tool);
                break;
        }
    }

    public JPanel getZoomPanel() {
        return zoom;
    }

    public JPanel getToolsPanel() {
        return tools;
    }
}
