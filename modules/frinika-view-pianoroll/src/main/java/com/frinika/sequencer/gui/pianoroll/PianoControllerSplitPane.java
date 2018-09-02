/*
 * Created on Mar 21, 2006
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
package com.frinika.sequencer.gui.pianoroll;

import com.frinika.audio.gui.ListProvider;
import com.frinika.gui.util.ButtonFactory;
import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.gui.ItemRollToolBar;
import com.frinika.sequencer.gui.ItemScrollPane;
import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.gui.PopupClient;
import com.frinika.sequencer.gui.PopupSelectorButton;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

public class PianoControllerSplitPane extends ItemScrollPane implements
        ComponentListener, SelectionListener<Lane> {

    private static final long serialVersionUID = 1L;

    JSplitPane splitPane;

    JPanel bot = new JPanel();

    // PianoRollEditor pianoRollEditor;
    ControllerView cntrlView;

//	ControllerHandle cntrls[];
    private JPopupMenu cntrlPopup;

    PopupSelectorButton cntrlBut;

    AbstractProjectContainer project;

    PadPanel pianoHeader;

    @SuppressWarnings("serial")
    public PianoControllerSplitPane(final AbstractProjectContainer project) {

        this.project = project;

        // Create the main piano and contrller views using this as the
        // scrollController
        pianoRoll = new PianoRoll(project, this);
        cntrlView = new ControllerView(project, this);

        // Create a toll bar and set the clients
        List<ItemPanel> clients = new ArrayList<>();
        clients.add(pianoRoll);
        clients.add(cntrlView);

        ItemRollToolBar toolBar = new ItemRollToolBar(clients, project);

        noteEditPanel = new MultiEventEditPanel(project);
        project.getDragList().addFeedbackItemListener(noteEditPanel);
        project.getEditHistoryContainer().addEditHistoryListener(noteEditPanel);
        project.getMultiEventSelection().addSelectionListener(noteEditPanel);
        toolBar.add(noteEditPanel);
        Insets insets = new Insets(0, 0, 0, 0);

        final PartSelectedAction wpl = new PartSelectedAction(project,
                pianoRoll);

        ButtonFactory.makePressButton(PianoControllerSplitPane.class.getResource("/icons/viewpageleft.png"), "warptopartleft",
                CurrentLocale.getMessage("sequencer.pianoroll.warptopartleft_tip"), wpl,
                toolBar.getZoomPanel()).setMargin(insets);

        final JToggleButton bb = ButtonFactory.makeToggleButton(PianoControllerSplitPane.class.getResource("/icons/music_drumnote.png"), "music_drumnote",
                CurrentLocale.getMessage("sequencer.pianoroll.drumwrite_tip"), wpl,
                toolBar.getToolsPanel());

        bb.setMargin(insets);

        bb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pianoRoll.setDrumWriteMode(bb.isSelected());
            }
        });

        SelectionListener listener = new SelectionListener() {

            Part focusOld = null;

            public void selectionCleared(SelectionContainer src) {
                // TODO Auto-generated method stub
            }

            public void addedToSelection(SelectionContainer src,
                    Collection items) {
                // TODO Auto-generated method stub
            }

            public void removedFromSelection(SelectionContainer src,
                    Collection items) {
                // TODO Auto-generated method stub
            }

            @Override
            public void selectionChanged(SelectionContainer src) {
                // System.out.println(" PRSP select changed" );
                Part newFocus = project.getPartSelection().getFocus();
                if (focusOld == newFocus) {
                    return;
                }

                if (newFocus != null) {
                    wpl.actionPerformed(null);
                }
                focusOld = newFocus;
            }
        };

        // TODO toggle this
        project.getPartSelection().addSelectionListener(listener);
        pianoRoll.setToolBar(toolBar);
        cntrlView.setToolBar(toolBar);

        // TODO pianoRoll is the master view ?
        setView(pianoRoll);

        JPanel top = new JPanel(new BorderLayout());
        top.setDoubleBuffered(false);
        top.add(pianoRoll, BorderLayout.CENTER);
        setToolBar(toolBar);

        pianoHeader = new PadPanel(pianoRoll, Layout.timePanelHeight,
                vertScroll.getValue());

        project.getPartSelection().addSelectionListener(pianoHeader);
        top.add(pianoHeader, BorderLayout.WEST);

        bot.setLayout(null);

        ListProvider resource = new ListProvider() {
            @Override
            public Object[] getList() {
                Lane lane = project.getLaneSelection().getFocus();
                if (lane instanceof MidiLane) {
                    return ((MidiLane) lane).getControllerList().getList().toArray();
                }
                return null;
            }
        };

        PopupClient client = new PopupClient() {
            @Override
            public void fireSelected(PopupSelectorButton but, Object o, int pos) {
                Lane lane = project.getLaneSelection().getFocus();
                if (lane instanceof MidiLane) {
                    cntrlView.setControllerType(((ControllerHandle) o));
                }
            }
        };

        cntrlBut = new PopupSelectorButton(resource, client);

        cntrlBut.setBounds(0, 0, pianoHeader.getWidth(), 20);

        bot.add(cntrlBut);
        cntrlBut.setLocation(0, 0);
        cntrlBut.setLayout(null);
        cntrlBut.label.setBounds(0, 0, pianoHeader.getWidth(), 20);
        cntrlBut.validate();
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDoubleBuffered(false);
        splitPane.add(top, JSplitPane.TOP);
        bot.add(cntrlView);

        splitPane.setResizeWeight(.8);
        splitPane.add(bot, JSplitPane.BOTTOM);
        pianoRoll.addComponentListener(this);
        bot.addComponentListener(this);
        add(splitPane);
        validate();

        horizScroll.setModel(pianoRoll.getXRangeModel());
        vertScroll.addAdjustmentListener(pianoHeader);
        horizScroll.addAdjustmentListener(cntrlView);

        vertScroll.setModel(pianoRoll.getYRangeModel());

        project.getLaneSelection().addSelectionListener(this);
        rebuild();
    }

    @Override
    protected void rebuild() {
        int maxY = 128 * Layout.getNoteItemHeight();
        pianoRoll.getYRangeModel().setMaximum(maxY);

        itemPanel.setDirty();
        itemPanel.repaint();
        pianoHeader.repaint();
    }

    public void rightButtonPressed(int x, int y) {
        cntrlPopup.show(this, x, y);
    }

    /**
     * detach all the listeners
     */
    public void dispose() {
        pianoRoll.removeComponentListener(this);
        bot.removeComponentListener(this);
        project.getLaneSelection().removeSelectionListener(this);
        project.getDragList().removeFeedbackItemListener(noteEditPanel);
        project.getEditHistoryContainer().removeEditHistoryListener(
                noteEditPanel);
        project.getMultiEventSelection().removeSelectionListener(noteEditPanel);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Rectangle rect = pianoRoll.getBounds();

        rect.height = bot.getHeight();
        rect.y = 0;
        cntrlView.setBounds(rect);
        bot.validate();
        bot.repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    public PianoRoll pianoRoll;

    MultiEventEditPanel noteEditPanel;

    public PianoRoll createPianoRoll(AbstractProjectContainer project) {
        return pianoRoll;
    }

    @Override
    public void selectionChanged(SelectionContainer<? extends Lane> src) {
        Lane lane = src.getFocus();
        if (lane instanceof MidiLane) {
            cntrlView.setControllerType((ControllerHandle) ((MidiLane) lane)
                    .getControllerList().getList().get(0));
        }
    }

    public PianoRoll getPianoRoll() {
        return pianoRoll;
    }

    public ControllerView getControllerView() {
        return cntrlView;
    }

    @Override
    protected void vertZoom(int inc) {
        Layout.noteHeightIndex += inc;
        Layout.noteHeightIndex = Math.min(Layout.noteHeightIndex, Layout.noteItemHeights.length - 1);
        Layout.noteHeightIndex = Math.max(Layout.noteHeightIndex, 0);
    }
}
