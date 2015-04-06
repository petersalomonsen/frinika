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

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.gui.ItemRollToolBar;
import com.frinika.sequencer.gui.ItemScrollPane;
import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.gui.PopupClient;
import com.frinika.sequencer.gui.PopupSelectorButton;
import com.frinika.sequencer.gui.ListProvider;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;

import com.frinika.sequencer.model.Part;

import static com.frinika.gui.util.ButtonFactory.makePressButton;
import static com.frinika.gui.util.ButtonFactory.makeToggleButton;
import static com.frinika.localization.CurrentLocale.getMessage;

public class PianoControllerSplitPane extends ItemScrollPane implements
		ComponentListener, SelectionListener<Lane> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JSplitPane splitPane;

	JPanel bot = new JPanel();

	// PianoRollEditor pianoRollEditor;

	ControllerView cntrlView;

//	ControllerHandle cntrls[];

	private JPopupMenu cntrlPopup;

	PopupSelectorButton cntrlBut;

	ProjectFrame frame;

	PadPanel pianoHeader;

	@SuppressWarnings("serial")
	public PianoControllerSplitPane(ProjectFrame frame) {

		this.frame = frame;

		// Create the main piano and contrller views using this as the
		// scrollController
		cntrlView = new ControllerView(frame, this);
		pianoRoll = new PianoRoll(frame, this);

		// Create a toll bar and set the clients
		Vector<ItemPanel> clients = new Vector<ItemPanel>();
		clients.add(pianoRoll);
		clients.add(cntrlView);
		
		final ProjectContainer project = frame.getProjectContainer();
		ItemRollToolBar toolBar = new ItemRollToolBar(clients, project);

		noteEditPanel = new MultiEventEditPanel(project);
		project.getDragList().addFeedbackItemListener(noteEditPanel);
		project.getEditHistoryContainer().addEditHistoryListener(noteEditPanel);
		project.getMultiEventSelection().addSelectionListener(noteEditPanel);
		toolBar.add(noteEditPanel);
		Insets insets = new Insets(0, 0, 0, 0);

		final PartSelectedAction wpl = new PartSelectedAction(project,
				pianoRoll);
		
		makePressButton("viewpageleft", "warptopartleft",
				getMessage("sequencer.pianoroll.warptopartleft_tip"), wpl,
				toolBar.getZoomPanel()).setMargin(insets);

		final JToggleButton bb=makeToggleButton("music_drumnote", "music_drumnote",
				getMessage("sequencer.pianoroll.drumwrite_tip"), wpl,
				toolBar.getToolsPanel());
		
		bb.setMargin(insets);
				
		bb.addActionListener(new ActionListener(){

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

			public void selectionChanged(SelectionContainer src) {
				// System.out.println(" PRSP select changed" );
				Part newFocus = project.getPartSelection().getFocus();
				if (focusOld == newFocus)
					return;

				if (newFocus != null)
					wpl.actionPerformed(null);
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

			public Object[] getList() {
				Lane lane = project.getLaneSelection().getFocus();
				if (lane instanceof MidiLane) {
					return ((MidiLane) lane).getControllerList().getList();
				}
				return null;
			}

		};

		PopupClient client = new PopupClient() {

			public void fireSelected(PopupSelectorButton but, Object o, int pos) {
				Lane lane = project.getLaneSelection().getFocus();
				if (lane instanceof MidiLane) {
					cntrlView.setControllerType(((ControllerHandle) o));
				}
			}
		};

		cntrlBut = new PopupSelectorButton(resource, client);

		cntrlBut.setBounds(0, 0,   pianoHeader.getWidth(), 20);

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

	protected void rebuild() {
		int maxY = 128 * Layout.getNoteItemHeight();
		pianoRoll.getYRangeModel().setMaximum(maxY);
		
		itemPanel.setDirty();
		itemPanel.repaint();
		pianoHeader.repaint();
	}

	/**
	 * 
	 */
	public void rightButtonPressed(int x, int y) {
		cntrlPopup.show(this, x, y);
	}

	/*
	 * void setControllerPopUp(JPopupMenu cPop) {
	 * 
	 * 
	 * cntrlPopupMenu = cPop; }
	 */

	/**
	 * detach all the listeners
	 */
	public void dispose() {
		pianoRoll.removeComponentListener(this);
		bot.removeComponentListener(this);
		frame.getProjectContainer().getLaneSelection().removeSelectionListener(this);
		frame.getProjectContainer().getDragList().removeFeedbackItemListener(noteEditPanel);
		frame.getProjectContainer().getEditHistoryContainer().removeEditHistoryListener(
				noteEditPanel);
		frame.getProjectContainer().getMultiEventSelection().removeSelectionListener(noteEditPanel);

	}

	public void componentResized(ComponentEvent e) {
		Rectangle rect = pianoRoll.getBounds();

		rect.height = bot.getHeight();
		rect.y = 0;
		cntrlView.setBounds(rect);
		bot.validate();
		bot.repaint();
	}

	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	public PianoRoll pianoRoll;

	MultiEventEditPanel noteEditPanel;

	public PianoRoll createPianoRoll(ProjectContainer project) {
		return pianoRoll;
	}

	public void selectionChanged(SelectionContainer<? extends Lane> src) {
		Lane lane = src.getFocus();
		if (lane instanceof MidiLane) {
			cntrlView.setControllerType((ControllerHandle) ((MidiLane) lane)
					.getControllerList().getList()[0]);
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
		Layout.noteHeightIndex = Math.min(Layout.noteHeightIndex,Layout.noteItemHeights.length-1);
		Layout.noteHeightIndex = Math.max(Layout.noteHeightIndex,0);				
	}

}
