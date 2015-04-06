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

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.SwingSongPositionListenerWrapper;
import com.frinika.sequencer.gui.ColorScheme;
import com.frinika.sequencer.gui.DragViewTool;
import com.frinika.sequencer.gui.EraseTool;
import com.frinika.sequencer.gui.Item;
import com.frinika.sequencer.gui.ItemScrollPane;

import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.gui.MyCursors;
import com.frinika.sequencer.gui.RectZoomTool;
import com.frinika.sequencer.gui.SelectTool;
import com.frinika.sequencer.gui.WriteTool;
import com.frinika.sequencer.gui.pianoroll.ControllerHandle;
import com.frinika.sequencer.model.EditHistoryAction;
import com.frinika.sequencer.model.EditHistoryContainer;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.util.EventsInPartsIterator;

public class ControllerView extends PianoRollPanelAdapter  implements AdjustmentListener {

	// Height of each NoteItem (multiple of ? to make piano nice ?)
	;

	int defaultLength; // create notes 1 beat long

	int pitchTop = 127; // top of screen in pitch (actually value in this case)
	int noteItemHeight;
	int yScreenBot;

	int velocity = 100;

	int channel = 1;

	private static final long serialVersionUID = 1L;

	private static final Color selCol = new Color(127, 127, 127, 200);

//	private static final Color noteCol = Color.RED;

//	private static final Color dragCol = Color.BLACK;

	//Vector<MultiEvent> dragList;

	EditHistoryContainer editHistory;

	Vector<MultiEvent> notesUnder;

	private ItemPanelMultiEventListener multiEventListener;

	private ItemPanelPartListener partListener;

	private int panelHeight;

	private int xLast;

	private ControllerHandle cntrl;

	private boolean velocityMode;

	MultiEvent dragEvent;

	Iterable<MultiEvent> eventsOnScreen;

	Iterable<MultiEvent> eventsInFocus;

	private int dValLast;

	/**
	 * Constructor.
	 * 
	 * @param project
	 *            project to view
	 * @param scroller
	 *            controls the view onto the virtualScreen.
	 */
	public ControllerView(final ProjectFrame frame, ItemScrollPane scroller) {
		super(frame.getProjectContainer(), scroller,false,false);
		final ProjectContainer project = frame.getProjectContainer();
		this.sequencer = project.getSequencer();

		multiEventListener = new ItemPanelMultiEventListener(this);
		partListener = new ItemPanelPartListener(this);

		eventsOnScreen = new Iterable<MultiEvent>() {
			public Iterator<MultiEvent> iterator() {
				return new EventsInPartsIterator(project.getPartSelection()
						.getSelected(), ControllerView.this);
			}
		};

		eventsInFocus = new Iterable<MultiEvent>() {
			public Iterator<MultiEvent> iterator() {
				Part focus = project.getPartSelection().getFocus();
				if (focus == null)
					return null;
				return new EventsInPartsIterator(project.getPartSelection()
						.getFocus(), ControllerView.this);
			}
		};

		project.getMultiEventSelection().addSelectionListener(
				multiEventListener);
		project.getPartSelection().addSelectionListener(partListener);

		// focusLane = null;

		sequencer.addSongPositionListener(new SwingSongPositionListenerWrapper(this));
		FrinikaSequence seq = (FrinikaSequence) sequencer.getSequence();
		editHistory = project.getEditHistoryContainer();
		editHistory.addEditHistoryListener(this);

		ticksPerBeat = seq.getResolution();
		defaultLength = ticksPerBeat;
	//	snaptoQuantization = ticksPerBeat;

		setLayout(null);
		setBackground(Color.WHITE);

		repaintItems();
		addComponentListener(this);
		makeTools();
	}

	public void dispose() {
		project.getMultiEventSelection().removeSelectionListener(
				multiEventListener);
		project.getPartSelection().removeSelectionListener(partListener);
		editHistory.removeEditHistoryListener(this);
	}

	public void dragTo(Point p) {

		if (dragArmed)
			dValLast = 0;

		int dyDragged = p.y - yAnchor;

		int dValToT = 0;

		dValToT = -(dyDragged * (cntrl.maxVal - cntrl.minVal)) / panelHeight;

		int dVal = dValToT - dValLast;

		if (dVal == 0)
			return;

		if (dragArmed)
			dragStart();

		dragArmed = false;
		dValLast = dValToT;

		Collection<Item> dragList=project.getDragList();
		
 		if (dVal != 0) {
			int valLim;
			if (dVal > 0)
				valLim = cntrl.minVal;
			else
				valLim = cntrl.maxVal;

			for (Item ev1 : dragList) {
				MultiEvent ev=(MultiEvent)ev1;
					int pitch = ev.getValueUI() + dVal;
				if (dVal > 0)
					valLim = Math.max(pitch, valLim);
				else
					valLim = Math.min(pitch, valLim);
			}

			if (dVal > 0 && valLim > cntrl.maxVal)
				dVal = dVal - (valLim - cntrl.maxVal);
			else if (dVal < 0 && valLim < cntrl.minVal)
				dVal = dVal - (valLim - cntrl.minVal);
		}

		if (dVal == 0)
			return;

//		boolean first = true;
//		long dt = 0;

	//	MultiEvent dev = null;

	//	boolean velMode = cntrl.name.equals("velocity");

		for (Item ev1 : dragList) {
			MultiEvent ev=(MultiEvent)ev1;
			
			switch (dragMode) {
			case OVER_ITEM_TOP:
			
				int val = ev.getValueUI() + dVal;
				ev.setValue(val);
				break;
			default:
				try {
					throw new Exception(" unknown dragmode " + dragMode);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
			}
		}

		project.getDragList().notifyDragEventListeners();
		project.getDragList().notifyFeedbackItemListeners();
		repaintItems();
	}

	public void clientClearSelection() {
		project.getMultiEventSelection().clearSelection();
	}

	/**
	 * Call this to start dragging with the reference point. See dragTo A copy
	 * is made of the valid selected events into dragList.
	 * 
	 */

	public void dragStart() {
		
		project.getDragList().startDrag(dragItem);
//		dragList = new Vector<MultiEvent>();
//		for (MultiEvent it : eventsInFocus) { 
//			// {
//			// if (it instanceof NoteEvent) {
//			assert (isValidEvent(it));
//			if (!it.isSelected())
//				continue;
//			try {
//				MultiEvent dragMe = (MultiEvent) (it.clone());
//				dragList.add(dragMe);
//				if (it == dragItem) {
//					dragEvent = dragMe;
//					project.getDragList().notifyDragEventListeners(dragMe);
//				}
//			} catch (CloneNotSupportedException e) {
//				e.printStackTrace();
//			}
//		}
	}

	/**
	 * Select/deselect all the items in a rectange.
	 * 
	 * @param yes
	 *            true to select. false to deselect
	 * @param rect
	 *            rectangle in score screen space.
	 */
	public synchronized void selectInRect(Rectangle rect,boolean shift) {

	
		Collection<MultiEvent> addTmp = new Vector<MultiEvent>();
		Collection<MultiEvent> delTmp = new Vector<MultiEvent>();
		Iterable<MultiEvent> list;

		Part focusPart = project.getPartSelection().getFocus();
		if (focusPart != null)
			list = eventsInFocus;
		else
			list = eventsOnScreen;

		for (MultiEvent note : list) {
			if (!isValidEvent(note))
				continue;

			if (rect.intersects(getItemBounds(note))) {
				if (shift) {
					if (note.isSelected())
						delTmp.add(note);
					else
						addTmp.add(note);
				} else {
					addTmp.add(note);
				}
			
			}
		}
		
		project.getMultiEventSelection().removeSelected(delTmp);
		project.getMultiEventSelection().addSelected(addTmp);
		project.getMultiEventSelection().notifyListeners();
	}

    @Override
	public void writeDraggedAt(Point p) {
		Part focusPart = project.getPartSelection().getFocus();

		if (focusPart == null || !(focusPart instanceof MidiPart)) {
			System.out.println(" Please slectect a part ");
			return;
		}

		int x1 = Math.min(xLast, p.x);
		int x2 = Math.max(xLast, p.x);

		long tick1 = 0;
		long tick2 = 0;

		if (velocityMode) {
			notesUnder = eventsUnderScreen(x1, x2);
			if (notesUnder.size() == 0)
				return;
		} else {
			tick1 = screenToTickAbs(x1, true);
			tick2 = screenToTickAbs(x2, true) + (long) project.getPianoRollSnapQuantization();
			notesUnder = eventsUnderTime(tick1, tick2); // TODO fixme
		}

		int val = screenToValue(p.y);
		xLast = p.x;

		val = Math.max(val, cntrl.minVal);
		val = Math.min(val, cntrl.maxVal);

		if (velocityMode) {
			setValue(notesUnder, val);
			project.getDragList().notifyFeedbackItemListeners(notesUnder.firstElement());
		} else {
			if (notesUnder.size() != 0) {
				for (MultiEvent ev : notesUnder) {
					ev.getPart().remove(ev);
				}
			}
			MultiEvent ev = null;
			for (long tick = tick1; tick < tick2; tick += project.getPianoRollSnapQuantization()) {

				ev = cntrl.createEvent((MidiPart) focusPart, tick, val);
				((MidiPart) focusPart).add(ev);
			}
			if (ev != null)
				project.getDragList().notifyFeedbackItemListeners(ev);
		}
		repaintItems();
	}

	protected synchronized void writePressedAt(Point p) {
		Part focusPart = project.getPartSelection().getFocus();

		velocityMode = cntrl.isVelocity();

		if (focusPart == null || !(focusPart instanceof MidiPart)) {
			System.out.println(" Please slectect a part ");
			return;
		}

		if (velocityMode) {
			notesUnder = eventsUnderScreen(p.x, p.x);

		} else {
			long tick = screenToTickAbs(p.x, true);
			long tick2 = tick + (long) project.getPianoRollSnapQuantization();
			notesUnder = eventsUnderTime(tick, tick2); // TODO fixme
		}

		xLast = p.x;

		int val = screenToValue(p.y);

		val = Math.max(val, cntrl.minVal);
		val = Math.min(val, cntrl.maxVal);

		if (velocityMode) {

			project.getEditHistoryContainer().mark(
					getMessage("sequencer.controllerview.adjust_velocity"));

			if (notesUnder.size() != 0) {
				setValue(notesUnder, val);
				project.getDragList().notifyFeedbackItemListeners(notesUnder.firstElement());
			}
		} else {
			project.getEditHistoryContainer().mark(
					getMessage("sequencer.controllerview.write_control"));

			if (notesUnder.size() != 0) {
				for (MultiEvent ev : notesUnder) {
					ev.getPart().remove(ev);
				}
			}

			long tick = screenToTickAbs(p.x, true);
			MultiEvent ev = cntrl.createEvent((MidiPart) focusPart, tick, val);

			// new ControllerEvent((MidiPart) focusPart, tick,
			// cntrl.contrl, val);
			((MidiPart) focusPart).add(ev);
			project.getDragList().notifyFeedbackItemListeners(ev);
		}
		repaintItems();
	}

	void setValue(Vector<MultiEvent> notes, int val) {

		for (MultiEvent note : notes) {
			if (note.getValueUI() == val)
				continue;
			// ---------- Example by PJS
			MidiPart p = note.getPart();
			p.remove(note);
			note.setValue(val);
			p.add(note);
			// -----------------------------
			// note.commitChanges();
		}
		

		repaintItems();

	}

	private int screenToValue(int y) {
		return cntrl.minVal
				+ ((panelHeight - y) * (cntrl.maxVal - cntrl.minVal))
				/ panelHeight;
		// TODO Auto-generated method stub

	}

	private int valueToScreen(int val) {
		return panelHeight - ((val - cntrl.minVal) * panelHeight)
				/ (cntrl.maxVal - cntrl.minVal);
	}

	private Vector<MultiEvent> eventsUnderScreen(int x1, int x2) {
		Vector<MultiEvent> noteEvents = new Vector<MultiEvent>();
		for (MultiEvent note : ((MidiPart) project.getPartSelection()
				.getFocus()).getMultiEvents()) {
			if (!isValidEvent(note))
				continue;
			Rectangle rect = getItemBounds(note);
			int x11 = rect.x;
			int x22 = rect.x + rect.width;

			if ((x1 <= x22 && x1 >= x11) || (x2 <= x22 && x2 >= x11)
					|| (x1 <= x11 && x2 >= x22)) {
				noteEvents.add(note);
			}
		}
		return noteEvents;
	}

	private Vector<MultiEvent> eventsUnderTime(long tick1, long tick2) {
		Vector<MultiEvent> noteEvents = new Vector<MultiEvent>();
		for (MultiEvent note : ((MidiPart) project.getPartSelection()
				.getFocus()).getMultiEvents()) {
			if (!isValidEvent(note))
				continue;
			long tick = note.getStartTick();

			if (tick1 <= tick && tick < tick2) {
				noteEvents.add(note);
			}
		}
		return noteEvents;
	}

	/**
	 * convert screen y to a pitch
	 * 
	 * @param y
	 * @return pitch for the given screen y
	 */
	private int screenToPitch(int y) {
		return pitchTop - (y / noteItemHeight);
	}

	/**
	 * Find component the contains point x,y and set. first component found is
	 * set (is this what we want ?) TODO multitrack thinking.
	 * 
	 * @param x
	 * @param y
	 * @return the NoteEvent at this point.
	 */
	public Item itemAt(Point p) {
		MultiEvent at = null;
		for (MultiEvent note : eventsOnScreen) {
			if (!isValidEvent(note))
				continue;
			if (getItemBounds(note).contains(p)) {
				if (note.isSelected())
					return note;
				else if (at == null)
					at = note;
			}
		}
		return at;
	}

	public void fireSequenceDataChanged(EditHistoryAction[] edithistoryActions) {

	
		repaintItems();
	}

	public synchronized void paintImageImpl(final Rectangle visibleRect,
			Graphics2D g) {

		if (g == null)
			return;
		if (cntrl == null)
			return;
	
		noteItemHeight = Layout.getNoteItemHeight();
		yScreenBot = pitchTop * noteItemHeight;

		velocityMode = cntrl.isVelocity();
		if (velocityMode)
			paintImageImplVel(visibleRect, g);
		else
			paintImageImplCntrl(visibleRect, g);

	}

	public synchronized void paintImageImplLabel(Graphics2D g) {

		if (g == null)
			return;
		if (cntrl == null)
			return;
	

		String label = cntrl.getName();
		TextLayout tl = new TextLayout(label, g.getFont(), g
				.getFontRenderContext());
		Rectangle2D bounds = tl.getBounds();
		int margin = 3;

		// bot right of BB
		Point2D.Float locBB = new Point2D.Float(0f, (float) tl.getBounds()
				.getHeight()
				+ 2 * margin);
		Point2D locTEXT = new Point2D.Float(margin, locBB.y - margin);

		bounds.setRect(0, 0, bounds.getWidth() + margin * 2, bounds.getHeight()
				+ margin * 2);

	
		g.setColor(Color.WHITE);
		g.fill(bounds);

		g.setColor(Color.BLACK);
		g.draw(bounds);
		tl.draw(g, (float) locTEXT.getX(), (float) locTEXT.getY());
	}

	private synchronized void paintImageImplCntrl(final Rectangle visibleRect,
			Graphics2D g) {

	
		int w = visibleRect.width;
		int x = visibleRect.x;
		int y = visibleRect.y;
		panelHeight = visibleRect.height;
		int yBot = yScreenBot; 
		
		g.setColor(Color.WHITE);
		g.fill(visibleRect);

		// Vertical lines

		double tick1 = screenToTickAbs(x, true);

		int x1 = (int) userToScreen((long) tick1);
		if (x1 < x) {
			tick1 += project.getPianoRollSnapQuantization();
			x1 = (int) userToScreen((long) tick1);
		}

		assert (x1 >= x);

		while (x1 < x + w) {
			if (Math.abs((tick1 + 1.0) % ticksPerBeat) < 2.0)
				g.setColor(Color.BLACK);
			else
				g.setColor(Color.lightGray);
			g.drawLine(x1, y, x1, y + panelHeight);
			tick1 += project.getPianoRollSnapQuantization();
			x1 = (int) userToScreen((long) tick1);
		}

		// Horizontal lines
		g.setColor(Color.lightGray);

		// maxVal = 127;
		int inc = 10;
		for (int i = 0; i < 127; i += inc) {
			if (i % 10 != 0)
				continue;
			int yy = panelHeight - (i * panelHeight)
					/ (cntrl.maxVal - cntrl.minVal);
			g.drawLine(x, yy, x + w, yy);
		}

		// Now draw the notes....................
		
		Part fc=project.getPartSelection().getFocus();
		if (fc != null && !( fc instanceof MidiPart)) return;
		MidiPart focusPart = (MidiPart) fc;

		MultiEvent evPrev = null;
		MultiEvent evNext = null;

		int valPrev = 0;
		int valNext = 0;

		int xNext = 0;
		int xPrev = 0;

		if (focusPart == null)
			return;

		Iterator<MultiEvent> iter = eventsInFocus.iterator();
		if (!iter.hasNext())
			return;

		Iterator<Item> diter = null;

		Collection<Item> dragList=project.getDragList();
		if (!dragList.isEmpty())
			diter = dragList.iterator();

		boolean doit = true;
		boolean highLightNext=false;
		Rectangle rect = new Rectangle();
		do {

			valPrev = valNext;
			evPrev = evNext;
			xPrev = xNext;
			boolean hilight = highLightNext;
			if (iter.hasNext()) {
				MultiEvent ev = iter.next();
				if (!isValidEvent(ev))
					continue;

				if (ev.isSelected()) {
					highLightNext = true;
					if (diter != null) {
						ev = (MultiEvent)diter.next();
					}
				} else {
					highLightNext = false;				
				}

				evNext = ev;
				xNext = (int)userToScreen(evNext.getStartTick());

				valNext = evNext.getValueUI();
				if (xNext < visibleRect.x)
					continue;

			} else {
				xNext = visibleRect.x + visibleRect.width;
				doit = false;
			}

			if (valPrev == cntrl.minVal)
				continue;

		

			rect.y = valueToScreen(valPrev);
			rect.height = panelHeight - rect.y;

			if (xNext > (visibleRect.x + visibleRect.width)) {
				xNext = visibleRect.x + visibleRect.width;
			}

			rect.x = xPrev;
			rect.width = xNext - xPrev;


			if (evPrev != null) {

				if (hilight) {
				//	if (dragList == null) {
						g.setColor(ColorScheme.selectedColor);
						g.fill(rect);
				//	}
					evPrev = evNext;
				} else {

					MidiLane myLane = (MidiLane) focusPart.getLane();

					if (myLane == null) {
						g.setColor(Color.BLACK);
					} else {
						g.setColor(focusPart.getColor());
					}
					g.fill(rect);
				}
			}

			evPrev = evNext;
		} while (doit);
	}

	public synchronized void paintImageImplVel(final Rectangle visibleRect,
			Graphics2D g) {

		int w = visibleRect.width;
		int x = visibleRect.x;
		int y = visibleRect.y;
		panelHeight = visibleRect.height;
		int yBot = yScreenBot; // - itemViewRect.y + Layout.timePanelHeight;
		// Draw vertical lines

		g.setColor(Color.WHITE);
		g.fill(visibleRect);

		// Vertical lines

		double tick1 = screenToTickAbs(x, true);
		double snaptoQuantization=project.getPianoRollSnapQuantization();
		
		
		int x1 = (int)userToScreen((long) tick1);
		
		if (snaptoQuantization <= 0) {
			snaptoQuantization = project.getTicksPerBeat();		
		}
	
		if (x1 < x) {
			tick1 += snaptoQuantization;
			x1 =(int) userToScreen((long) tick1);
		}

		assert (x1 >= x);

		while (x1 < x + w) {
			if (Math.abs((tick1 + 1.0) % ticksPerBeat) < 2.0)
				g.setColor(Color.BLACK);
			else
				g.setColor(Color.lightGray);
			g.drawLine(x1, y, x1, y + panelHeight);
			tick1 += snaptoQuantization;
			x1 = (int)userToScreen((long) tick1);
		}

		// Horizontal lines
		g.setColor(Color.lightGray);

		// maxVal = 127;
		int inc = 10;
		for (int i = 0; i < 127; i += inc) {
			if (i % 10 != 0)
				continue;
			int yy = valueToScreen(i);
			g.drawLine(x, yy, x + w, yy);
		}

		// Now draw the notes....................
		Part focusPart = project.getPartSelection().getFocus();
	
		Collection<Item> dragList=project.getDragList();
		
		if (!(focusPart instanceof MidiPart))  return;
		for (MultiEvent note : eventsOnScreen) {
			if (!isValidEvent(note))
				continue;
			if (focusPart != null && note.getPart() == focusPart)
				continue;
			Rectangle noteRect = getItemBounds(note);
			if (visibleRect != null && !visibleRect.intersects(noteRect))
				continue;

			if (note.isSelected()) {
				if (!dragList.isEmpty()) {
					g.setColor(ColorScheme.selectedColor);
					g.fill(noteRect);
				}

			} else {

				MidiLane myLane = (MidiLane) note.getPart().getLane();

				if (myLane == null) {
					g.setColor(Color.BLACK);
				} else {
					g.setColor(note.getPart().getColor());

				}
				g.fill(noteRect);
			}
		}

		if (focusPart != null) {
			for (MultiEvent note : ((MidiPart) focusPart).getMultiEvents()) {
				if (!isValidEvent(note))
					continue;
				Rectangle noteRect = getItemBounds(note);
				if (visibleRect != null && !visibleRect.intersects(noteRect))
					continue;

				if (note.isSelected()) {
					if (dragList.isEmpty()) {
						g.setColor(selCol);
						g.fill(noteRect);
					}
				} else {
					MidiLane myLane = (MidiLane) note.getPart().getLane();
					if (myLane == null) {
						g.setColor(Color.BLACK);
					} else {
						g.setColor(note.getPart().getColor());

					}
					g.fill(noteRect);
				}
				g.setColor(Color.BLACK);
				g.draw(noteRect);
			}
		}

	
		for (Item it: dragList) {
			MultiEvent note=(MultiEvent)it;
			Rectangle noteRect = getItemBounds(note);
			if (visibleRect != null && !visibleRect.intersects(noteRect))
				continue;
			g.setColor(selCol);
			g.fill(noteRect);
			g.setColor(Color.RED);
			g.draw(noteRect);
		}

	}

	/**
	 * Find the screen rectangle for the event
	 * 
	 * WOuld it be better to pass a reference save creating off the stack (doubt
	 * it?)
	 * 
	 * @param e
	 *            NoteEvent
	 * @return rectangle on screen
	 */

	final Rectangle tmpRect=new Rectangle();
	
	private Rectangle getItemBounds(Item it) {
		// final  Rectangle rect=new Rectangle();
		MultiEvent e = (MultiEvent) it;
		long tick = e.getStartTick();
		// long dur = e.getDuration();

		int h1 = 0;

		h1 = ((e.getValueUI() - cntrl.minVal) * panelHeight)
				/ (cntrl.maxVal - cntrl.minVal);

		// Careful that origins must always conincide
		int x1 = (int)userToScreen(tick);
		int w = 5;
		//rect.x=x1;
		tmpRect.setBounds((int) x1, panelHeight - h1, w, h1);
		return tmpRect;
		//return new Rectangle();
	}

	@Override
	public void endDrag() {


		if (dragArmed) {
			dragArmed = false;
			return;
		}

		project.getDragList().endDragController();

	}

	public void erase(Item it) {
		MultiEvent note = (MultiEvent) it;
		velocityMode = cntrl.isVelocity();
		if (velocityMode)
			editHistory.mark("erase note");
		else
			editHistory.mark("erase event");
		note.getPart().remove(note);
		editHistory.notifyEditHistoryListeners();
	}

/*	@Override
	public void setQuantizeOn(boolean b) {
		quantize = b;
		if (b)
			MultiEventClipboard.getDefaultMultiEventClipboard()
					.setQuantization(snaptoQuantization);
		else
			MultiEventClipboard.getDefaultMultiEventClipboard()
					.setQuantization(1);

	}*/

	@Override
	public void rightButtonPressedOnItem(int x,int y) {
		System.out.println(" Right button pressed (so what?) ");
	}

	@Override
	public void clientAddToSelection(Item item) {
		project.getMultiEventSelection().addSelected((MultiEvent) item);
		project.getMultiEventSelection().notifyListeners();
	}

	@Override
	public void clientRemoveFromSelection(Item item) {
		project.getMultiEventSelection().removeSelected((MultiEvent) item);
		project.getMultiEventSelection().notifyListeners();
	}

	/**
	 * Only used for it's iterator
	 * 
	 * @author pjl
	 * 
	 */
	class NoteOnScreenCollection implements Collection<MultiEvent> {

		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean contains(Object o) {
			// TODO Auto-generated method stub
			return false;
		}

		public Iterator<MultiEvent> iterator() {
			return new NoteOnScreenIterator();
		}

		public Object[] toArray() {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> T[] toArray(T[] a) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean add(MultiEvent o) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean remove(Object o) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean containsAll(Collection<?> c) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean addAll(Collection<? extends MultiEvent> c) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean removeAll(Collection<?> c) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean retainAll(Collection<?> c) {
			// TODO Auto-generated method stub
			return false;
		}

		public void clear() {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * TODO Rough implmentation needs to be optimized
	 * 
	 * @author pjl
	 * 
	 */
	class NoteOnScreenIterator implements Iterator<MultiEvent> {

		Iterator<Part> partIter = null;

		Iterator<MultiEvent> noteIter = null;

		NoteOnScreenIterator() {
			partIter = project.getPartSelection().getSelected().iterator();
			advanceToNextMidiPart();
		}

		private boolean advanceToNextMidiPart() {

			Part part = null;

			while (partIter.hasNext()) {
				part = partIter.next();
				if (part instanceof MidiPart) {
					noteIter = ((MidiPart) part).getMultiEvents().iterator();
					return true;
				}
			}
			noteIter = null;
			return false;
		}

		public boolean hasNext() {
			if (noteIter == null)
				return false;
			if (noteIter.hasNext())
				return true;

			// TODO Auto-generated method stub
			return advanceToNextMidiPart();
		}

		public MultiEvent next() {
			if (noteIter.hasNext()) {
				MultiEvent ev = noteIter.next();
				return ev;
			}

			// He he sneak a bit of recursion in here (PJL)
			if (advanceToNextMidiPart())
				return next();
			return null;
		}

		public void remove() {
			assert (false);
			// TODO Auto-generated method stub
		}

	}

	@Override
	public int getHoverStateAt(Point p) {

		final int endTol = 20;
		// final int extraX = 20 ;
		// if (true) return OVER_ITEM_MIDDLE;

		int tol = endTol;
		Iterable<MultiEvent> list;
		Part focusPart = project.getPartSelection().getFocus();
		if (! (focusPart instanceof MidiPart) ) return OVER_NOTHING;
		if (focusPart == null)
			list = eventsOnScreen;
		else
			list = eventsInFocus;

		for (MultiEvent e : list) {
			assert (isValidEvent(e));

			Rectangle rect = getItemBounds(e);

			if (rect.contains(p)) {
				return OVER_ITEM_TOP;
			}
		}
		return OVER_NOTHING;
	}

	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		setX(arg0.getValue());
	}

	void makeTools() {
		Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
		selectTool = new SelectTool(c);
		rectZoomTool = new RectZoomTool(c);

		writeTool = new WriteTool(MyCursors.getCursor("pencil"));
		eraseTool = new EraseTool(MyCursors.getCursor("eraser"));
		dragViewTool = new DragViewTool(MyCursors.getCursor("move"));

	}

	public void setControllerType(ControllerHandle handle) {
		this.cntrl = handle;
		setDirty();
		repaintItems();
	}

	final public boolean isValidEvent(MultiEvent ev) {

		// are we in velocity mode then only use NoteEvents.
		if (cntrl == null)
			return false;

		return cntrl.isValid(ev);

	}

	@Override
	protected void writeReleasedAt(Point p) {
		Part focusPart = project.getPartSelection().getFocus();
		if (focusPart == null || !(focusPart instanceof MidiPart)) return;
		project.getEditHistoryContainer().notifyEditHistoryListeners();
	}
	
	public void selectAll() {
		Part focus=project.getPartSelection().getFocus();
		if (focus == null ) return;

		Vector<MultiEvent> events=new Vector<MultiEvent> ();
		for (MultiEvent e:eventsInFocus) {
			events.add(e);
		}
		
		project.getMultiEventSelection().setSelected(events);
		project.getMultiEventSelection().notifyListeners();

	}
}
