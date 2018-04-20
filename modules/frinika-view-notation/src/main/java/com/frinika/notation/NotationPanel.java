/*
 * Created on 16.4.2007
 *
 * Copyright (c) 2006-2007 Karl Helgason
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
package com.frinika.notation;

import com.frinika.audio.gui.ListProvider;
import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.gui.ItemRollToolBar;
import com.frinika.sequencer.gui.ItemScrollPane;
import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.gui.pianoroll.MultiEventEditPanel;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;

public class NotationPanel extends ItemScrollPane implements
        ComponentListener, SelectionListener<Lane> {

    private static final long serialVersionUID = 1L;

    AbstractProjectContainer project;
    NotationEditor notationEditor;
    NotationHeader notationHeader;

    MultiEventEditPanel noteEditPanel;

    public NotationPanel(final AbstractProjectContainer project) {

        this.project = project;
        // Create the main piano and contrller views using this as the
        // scrollController
        notationEditor = new NotationEditor(project, this);

        // Create a toll bar and set the clients
        List<ItemPanel> clients = new ArrayList<>();
        clients.add(notationEditor);

        ItemRollToolBar toolBar = new ItemRollToolBar(clients, project);

        noteEditPanel = new MultiEventEditPanel(project);
        project.getDragList().addFeedbackItemListener(noteEditPanel);    // TODO remove this lot if the panelm is disposed
        project.getEditHistoryContainer().addEditHistoryListener(noteEditPanel);
        project.getMultiEventSelection().addSelectionListener(noteEditPanel);
        toolBar.add(noteEditPanel);
        Insets insets = new Insets(0, 0, 0, 0);
        /*
		final WarpToPartLeftAction wpl = new WarpToPartLeftAction(project,
				notationEditor);
		
		makePressButton("viewpageleft", "warptopartleft",
				getMessage("sequencer.pianoroll.warptopartleft_tip"), wpl,
				toolBar.getZoomPanel()).setMargin(insets);*/

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
                /*
				if (newFocus != null)
					wpl.actionPerformed(null); */
                focusOld = newFocus;

                notationEditor.repaintItems();
            }

        };
        project.getPartSelection().addSelectionListener(listener);
        notationEditor.setToolBar(toolBar);

        // TODO pianoRoll is the master view ?
        setView(notationEditor);

        JPanel top = new JPanel(new BorderLayout());
        top.setDoubleBuffered(false);
        top.add(notationEditor, BorderLayout.CENTER);
        setToolBar(toolBar);

        notationHeader = new NotationHeader(notationEditor, Layout.timePanelHeight,
                vertScroll.getValue());

        notationEditor.header = notationHeader;

        top.add(notationHeader, BorderLayout.WEST);

        //bot.setLayout(null);
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

        add(top);
        validate();

        horizScroll.setModel(notationEditor.getXRangeModel());
        vertScroll.addAdjustmentListener(notationHeader);

        vertScroll.setModel(notationEditor.getYRangeModel());

        project.getLaneSelection().addSelectionListener(this);

        rebuild();
    }

    @Override
    protected void rebuild() {
        //int maxY = 128 * Layout.getNoteItemHeight();
        //pianoRoll.yRangeModel.setMaximum(maxY);

        itemPanel.setDirty();
        itemPanel.repaint();
        //pianoHeader.repaint();
    }

    @Override
    protected void vertZoom(int inc) {
        // TODO Auto-generated method stub
    }

    @Override
    public void componentResized(ComponentEvent e) {
        // TODO Auto-generated method stub
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

    @Override
    public void selectionChanged(SelectionContainer<? extends Lane> src) {
        // TODO Auto-generated method stub
        notationEditor.repaintItems();
    }
}
