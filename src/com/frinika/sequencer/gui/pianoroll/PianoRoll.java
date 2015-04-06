/*
 * Created on 14-Feb-2006
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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import com.frinika.sequencer.model.EditHistoryAction;
import com.frinika.sequencer.model.EditHistoryContainer;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.timesignature.TimeSignatureList.QStepIterator;
import com.frinika.sequencer.model.util.EventsInPartsIterator;
import static com.frinika.localization.CurrentLocale.getMessage;

/**
 * Implementation of a pianoRoll view and editor.
 * 
 * 
 * @author pjl
 * 
 */
public class PianoRoll extends PianoRollPanelAdapter {

	private int noteItemHeight;

	// int defaultLength; // create notes 1 beat long

	int pitchTop = 128; // top of screen in pitch

	int yScreenBot;

	int velocity = 100;

	int channel = 1;

	private static final long serialVersionUID = 1L;

	private static final Color selCol = new Color(127, 127, 127, 200);

	private static final Color noteCol = Color.RED;

	private static final Color dragCol = Color.BLACK;

	Iterable<MultiEvent> notesOnScreen;

	Iterable<MultiEvent> notesInFocus;

	boolean pasteing = false;

	EditHistoryContainer editHistory;

	private ItemPanelMultiEventListener multiEventListener;

	private ItemPanelPartListener partListener;

	private NoteEvent newNote;

	AudioFeedBack audioFeedBack;

	Point writeAtPoint;
	
	private boolean drumWriteMode = false; // if true write zero length notes

	/**
	 * Constructor.
	 * 
	 * @param project
	 *            project to view
	 * @param scroller
	 *            controls the view onto the virtualScreen.
	 */
	public PianoRoll(final ProjectFrame frame, ItemScrollPane scroller) {
		super(frame.getProjectContainer(), scroller, true, true);

		final ProjectContainer project = frame.getProjectContainer();
		notesOnScreen = new Iterable<MultiEvent>() {
			public Iterator<MultiEvent> iterator() {
				return new EventsInPartsIterator(project.getPartSelection()
						.getSelected(), PianoRoll.this);
			}
		};

		notesInFocus = new Iterable<MultiEvent>() {
			public Iterator<MultiEvent> iterator() {
				Part focus = project.getPartSelection().getFocus();
				if (focus == null)
					return null;
				return new EventsInPartsIterator(project.getPartSelection()
						.getFocus(), PianoRoll.this);
			}
		};

		audioFeedBack = new AudioFeedBack(project);
		this.sequencer = project.getSequencer();

		multiEventListener = new ItemPanelMultiEventListener(this);
		partListener = new ItemPanelPartListener(this);

		project.getMultiEventSelection().addSelectionListener(
				multiEventListener);
		project.getPartSelection().addSelectionListener(partListener);

		// focusLane = null;

		sequencer.addSongPositionListener(new SwingSongPositionListenerWrapper(
				this));
		FrinikaSequence seq = (FrinikaSequence) sequencer.getSequence();
		editHistory = project.getEditHistoryContainer();
		editHistory.addEditHistoryListener(this);

		project.getDragList().addDragEventListener(new DragEventListener() {
			public void update() {
				repaintItems();
			}
		});

		ticksPerBeat = seq.getResolution();
		// defaultLength = ticksPerBeat;
		if (project.getPianoRollSnapQuantization() == 0) {
			project.setPianoRollSnapQuantization(ticksPerBeat);
		}
		// snaptoQuantization = ticksPerBeat;

		setLayout(null);
		setBackground(Color.WHITE);

		repaintItems();
		addComponentListener(this);
		makeTools();
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		setFocusable(true);
	}

	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			grabFocus();
		}
		super.processMouseEvent(e);
	}

	public void dispose() {
		project.getMultiEventSelection().removeSelectionListener(
				multiEventListener);
		project.getPartSelection().removeSelectionListener(partListener);
		editHistory.removeEditHistoryListener(this);
	}

	/**
	 * drags the dragList to Point p.
	 * 
	 */
	public void dragTo(Point p) {

		int dxDragged = p.x - xAnchor;
		int dyDragged = p.y - yAnchor;

		long dtick;
		NoteEvent dddNote = null;

		dtick = screenToTickRel(xAnchor, dxDragged, project
				.isPianoRollSnapQuantized());

		/*
		 * if (project.isPianoRollSnapQuantized()) { dtick =
		 * snaptoQuantize(dxDragged); } else { dtick = (long) (dxDragged /
		 * ticksToScreen); }
		 */
		int dpitch = 0;

		if (dragMode == OVER_ITEM_MIDDLE)
			dpitch = -dyDragged / noteItemHeight;

		if (dtick == 0 && dpitch == 0 && !altIsDown)
			return;

		if (dragArmed)
			startDrag();

		dragArmed = false;

		List<Item> dragList = project.getDragList();

		if (dragList.isEmpty())
			return;

		if (altIsDown && project.isPianoRollSnapQuantized()) {

			dddNote = (NoteEvent) (dragList.get(0));
			if (dragMode != OVER_ITEM_RIGHT) {
				long refTick = ((NoteEvent) dddNote).getStartTick();
				long screenPos = (int) userToScreen(refTick);
				long newTick = screenToTickAbs((int) (screenPos + dxDragged),
						true);
				dtick = newTick - refTick;
			} else {
				long refTick = ((NoteEvent) dddNote).getEndTick();
				long screenPos = (int) userToScreen(refTick);
				long newTick = screenToTickAbs((int) (screenPos + dxDragged),
						true);
				dtick = newTick - refTick;
			}

			// System. out.println(dddNote + " " + dtick);
			if (dtick == 0)
				return;
		}

		if (dpitch != 0) {
			int pitlim;
			if (dpitch > 0)
				pitlim = 0;
			else
				pitlim = 127;

			for (Item it : dragList) {
				NoteEvent ev = (NoteEvent) it;
				int pitch = ((NoteEvent) ev).getNote() + dpitch;
				if (dpitch > 0)
					pitlim = Math.max(pitch, pitlim);
				else
					pitlim = Math.min(pitch, pitlim);
			}

			if (dpitch > 0 && pitlim > 127)
				dpitch = dpitch - (pitlim - 127);
			else if (dpitch < 0 && pitlim < 0)
				dpitch = dpitch - pitlim;
		}

		long dt = 0;

		for (Item it : dragList) {
			NoteEvent ev = (NoteEvent) it;

			switch (dragMode) {
			case OVER_ITEM_MIDDLE:
				long tick = ev.getStartTick() + dtick;
				int pitch = ((NoteEvent) ev).getNote() + dpitch;
				dt = Math.min(dt, tick);
				ev.setStartTick(tick);
				ev.setNote(pitch);
				break;
			case OVER_ITEM_RIGHT:
				long dur = ev.getDuration() + dtick;
				dur = Math.max(1, dur);
				ev.setDuration(dur);
				break;
			case OVER_ITEM_LEFT:
				dur = ev.getDuration() - dtick;
				long st = ev.getStartTick() + dtick;
				dur = Math.max(1, dur);
				ev.setDuration(dur);
				ev.setStartTick(st);
				break;
			default:
				System.err.println(" unknown dragmode " + dragMode);
			}

			if (dpitch != 0)
				feedBack(ev);
		}

		if (dt != 0) {
			for (Item it : dragList) {
				NoteEvent ev = (NoteEvent) it;
				long tick = ev.getStartTick() - dt;
				ev.setStartTick(tick);
			}
		}

		project.getDragList().notifyDragEventListeners();
		project.getDragList().notifyFeedbackItemListeners();

		xAnchor = xAnchor + (int) (dtick * userToScreen);
		yAnchor = yAnchor - dpitch * noteItemHeight;

		// repaintItems();
	}

	public void clientClearSelection() {
		project.getMultiEventSelection().clearSelection();
	}

	/**
	 * Call this to start dragging with the reference point. See dragTo
	 * 
	 * @param e
	 */

	public void startDrag() {

		project.getDragList().startDrag(dragItem);

	}

	/**
	 * Select/deselect all the items in a rectange.
	 * 
	 * @param yes
	 *            true to select. false to deselect
	 * @param rect
	 *            rectangle in score screen space.
	 */
	public synchronized void selectInRect(Rectangle rect, boolean shift) {
		Collection<MultiEvent> addTmp = new Vector<MultiEvent>();
		Collection<MultiEvent> delTmp = new Vector<MultiEvent>();

		Iterable<? extends MultiEvent> list;

		Part focusPart = project.getPartSelection().getFocus();
		if (!(focusPart instanceof MidiPart))
			return;

		if (focusPart != null)
			list = ((MidiPart) focusPart).getMultiEvents();
		else
			list = notesOnScreen;

		for (MultiEvent note : list) {
			if (note instanceof NoteEvent) {
				if (rect.intersects(getItemBounds((NoteEvent) note))) {
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
		}
		project.getMultiEventSelection().removeSelected(delTmp);

		project.getMultiEventSelection().addSelected(addTmp);
		project.getMultiEventSelection().notifyListeners();

	}

	@Override
	protected void writeReleasedAt(Point p) {
		if (newNote == null)
			return;
		newNote.getPart().add(newNote);
		project.getMultiEventSelection().setSelected(newNote);
		project.getEditHistoryContainer().notifyEditHistoryListeners();
		project.getMultiEventSelection().notifyListeners();

		newNote = null;
		writeAtPoint=null;
	}

	public synchronized void writeDraggedAt(Point p) {
		if (newNote == null)
			return;

		long tick = screenToTickAbs(p.x, true);

		long tick1 = newNote.getStartTick();
		long tick2 = tick1 + newNote.getDuration();
		int pitch = screenToPitch(p.y);

		boolean doit = false;

		if (drumWriteMode) {
			if (tick != tick1 ) {
				if (writeAtPoint != null) p.y=writeAtPoint.y; // keep pitch of first beat
				writeReleasedAt(p);
				writePressedAt(p);
				doit=true;
			}
			
		} else {

			if (tick > tick2) {
				newNote.setDuration(tick - tick1);
				doit = true;
			} else if (tick < tick2 && tick > tick1) {
				newNote.setDuration(tick - tick1);
				doit = true;
			}
			if (pitch != newNote.getNote() && pitch >= 0 && pitch < 128) {
				doit = true;
				newNote.setNote(pitch);
				feedBack(newNote);
			}

			project.getDragList().notifyFeedbackItemListeners(newNote);
		}
		
		if (doit)
			repaintItems();
	}

	
	/**
	 * Used by the write tool to insert a note.
	 */
	protected synchronized void writePressedAt(Point p) {
		Part focusPart = project.getPartSelection().getFocus();
		if (focusPart == null || !(focusPart instanceof MidiPart)) {
			System.out.println(" Please slectect a part ");
			return;
		}

		if (writeAtPoint == null) writeAtPoint=p;
		long tick = screenToTickAbs(p.x, true,drumWriteMode);
		int pitch = screenToPitch(p.y);
		assert (pitch > 0);
		assert (pitch < 128);

		project.getEditHistoryContainer().mark(
				getMessage("sequencer.pianoroll.add_note"));

		if (drumWriteMode) {
			newNote = new NoteEvent((MidiPart) focusPart, tick, pitch,
					velocity, channel, 0);
		} else {
			long quant = (long) project.getPianoRollSnapQuantization();

			if (quant <= 0)
				quant = project.getTicksPerBeat();
			newNote = new NoteEvent((MidiPart) focusPart, tick, pitch,
					velocity, channel, quant);

		}

		feedBack(newNote);
		project.getDragList().notifyFeedbackItemListeners(newNote);
		repaintItems();

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
		Item at = null;
		for (MultiEvent note : notesOnScreen) {
			if (getItemBounds(note).contains(p)) {
				if (note.isSelected())
					return note;
				else if (at == null)
					at = note;
			}
		}
		return at;
	}

	/**
	 * Translate pitch to screen (vertical)
	 * 
	 * @param pitch
	 * @return screen y for given pitch
	 */
	int pitchToScreen(int pitch) {
		return (pitchTop - pitch) * noteItemHeight;
	}

	public void fireSequenceDataChanged(EditHistoryAction[] edithistoryActions) {

		repaintItems();
	}

	private Color alphaColor(Color c, float alpha) {
		float[] rgb = c.getRGBComponents(new float[4]);
		return new Color(rgb[0], rgb[1], rgb[2], alpha);
	}

	private Color alphaColor(Color c, int velocity) {
		return alphaColor(c, velocity * (1.0f / 127.0f));
	}

	private Color alphaColor(Color c, MultiEvent note) {
		return alphaColor(c, ((NoteEvent) note).getVelocity());
	}

	public synchronized void paintImageImpl(final Rectangle visibleRect,
			Graphics2D g) {

		if (g == null)
			return;
		noteItemHeight = Layout.getNoteItemHeight();
		yScreenBot = pitchTop * noteItemHeight;

		Part focusPart = project.getPartSelection().getFocus();

		// System.out.println("paintImageImpl");
		// if (dragList != null && dragList.isEmpty())
		// dragList = null;
		int w = visibleRect.width;
		int x = visibleRect.x;
		int y = visibleRect.y;
		int h = visibleRect.height;
		int yBot = yScreenBot; // - itemViewRect.y + Layout.timePanelHeight;

		// Horizontal lines
		g.setColor(Color.lightGray);
		int p1 = screenToPitch(y);

		int y1 = pitchToScreen(p1);
		if (y1 <= y - noteItemHeight) {
			p1 -= 1;
			y1 = pitchToScreen(p1);
		}

		assert (y1 > y - noteItemHeight);

		Rectangle frect = new Rectangle(visibleRect);

		if (focusPart != null) {
			g.fill(frect);
			g.setColor(ColorScheme.pianaoRollInvalid);
			int fl = (int) (focusPart.getStartTick() * userToScreen);
			int fr = (int) (focusPart.getEndTick() * userToScreen);
			frect.x = fl;
			frect.width = fr - fl;
			frect = frect.intersection(visibleRect);
			// if (!frect.isEmpty()) {
			// // g.setColor(new Color(0xF0FEFE));
			// g.setColor(ColorScheme.validBackground);
			// g.fill(frect);
			// }
		}

		Color blackNoteStripColor = new Color(200, 200, 240);
		while (y1 < y + h) {
			int i = noteItemHeight;
			boolean drawLine = y1 >= y;
			if (!drawLine) {
				i -= y - y1;
				y1 = y;
			}
			switch (p1 % 12) {
			case 0:
			case 2:
			case 4:
			case 5:
			case 7:
			case 9:
			case 11:
				g.setColor(Color.WHITE);
				break;

			default:
				g.setColor(blackNoteStripColor);
				break;

			}
			g.fill(new Rectangle(frect.x, y1, frect.width, noteItemHeight));
			if (drawLine) {
				Color cc;
				if (p1 % 12 == 11) {
					cc = Color.red;
					g.setXORMode(cc);
					g.drawLine(x, y1, x + w, y1);
				} else if (p1 % 12 == 4) {
					cc = Color.gray;
					g.setXORMode(cc);
					g.drawLine(x, y1, x + w, y1);
					// cc = Color.lightGray;
				}
			}
			g.setPaintMode();
			p1 -= 1;
			y1 = pitchToScreen(p1);
		}

		// Draw vertical lines

		// Vertical lines
		int ticksPerBeat = project.getSequence().getResolution();

		double beat1 = screenToTickAbs(x, true) / ticksPerBeat;
		double beat2 = screenToTickAbs(x + w, true) / ticksPerBeat;
		double step = project.getPianoRollSnapQuantization() / ticksPerBeat;

		QStepIterator iter = project.getTimeSignatureList()
				.createQStepIterator(beat1, beat2, step);

		boolean drawSub = (int) userToScreen((long) (step * ticksPerBeat)) > 5;
		boolean drawBeat = (int) userToScreen((long) (ticksPerBeat)) > 5;

		while (iter.hasNext()) {
			iter.next();
			double beat = iter.getBeat();
			boolean isBar = iter.isBar();

			if (isBar) {
				g.setColor(ColorScheme.partViewLinesBar);
			} else {
				if (!drawBeat)
					continue;
				if (Math.abs((beat + 1e-7) % 1) < 2e-7) {
					g.setColor(ColorScheme.partViewLinesBeat);
				} else {
					if (!drawSub)
						continue;
					g.setColor(ColorScheme.partViewLinesSubBeat);
				}
			}

			long tick = (long) (beat * ticksPerBeat);
			int x1 = (int) userToScreen(tick);
			g.drawLine(x1, y, x1, y + h);

		}

		/***********************************************************************
		 * int x1 = (int)tickToScreen((long) tick1); if (x1 < x) { tick1 +=
		 * snaptoQuantization; x1 = (int)tickToScreen((long) tick1); }
		 * 
		 * assert (x1 >= x); // int beatsPerBar = project.beatsPerBar;
		 * 
		 * 
		 * while (x1 < x + w) { // if (Math.abs((tick1 + 1.0) % (ticksPerBeat *
		 * beatsPerBar)) < 2.0) g.setColor(Color.BLUE); // else if
		 * (Math.abs((tick1 + 1.0) % ticksPerBeat) < 2.0)
		 * g.setColor(Color.DARK_GRAY); // else g.setColor(Color.lightGray);
		 * g.drawLine(x1, y, x1, y + h); tick1 += snaptoQuantization; x1 =
		 * (int)tickToScreen((long) tick1); } }
		 **********************************************************************/

		if (!(focusPart instanceof MidiPart))
			return; // focusPart=null;
		// return;

		// Now draw the notes....................
		Collection<Item> dragList = project.getDragList();

		for (MultiEvent note : notesOnScreen) {

			if (focusPart != null && note.getPart() == focusPart)
				continue;
			// if (!validEvent(note))
			// continue;

			Rectangle2D noteRect = getItemBounds(note);
			if (visibleRect != null && !visibleRect.intersects(noteRect))
				continue;

			Shape shape = getItemShape(note);
			if (note.isZombie()) {
				// g.setColor(Color.LIGHT_GRAY);
				// g.fill(noteRect);
			} else if (note.isSelected()) {
				if (dragList.isEmpty()) {
					g.setColor(ColorScheme.selectedColor);
					g.fill(shape);
				}

			} else {

				MidiLane myLane = (MidiLane) note.getPart().getLane();

				if (myLane == null) {
					g.setColor(Color.BLACK);
				} else {
					g.setColor(note.getPart().getColor());

				}
				g.fill(shape);
			}
		}

		if (focusPart != null) {
			for (MultiEvent note : notesInFocus) {
				assert (note instanceof NoteEvent);
				Rectangle noteRect = getItemBounds(note);
				if (visibleRect != null && !visibleRect.intersects(noteRect))
					continue;

				Shape shape = getItemShape(note);
				if (note.isSelected()) {
					if (dragList.isEmpty()) {
						g.setColor(selCol);
						g.fill(shape);
					}
				} else if (note.isZombie()) {
					// g.setColor(Color.LIGHT_GRAY);
					// g.fill(noteRect);

				} else {
					MidiLane myLane = (MidiLane) note.getPart().getLane();
					if (myLane == null) {
						g.setColor(Color.BLACK);
						g.fill(shape);
					} else {
						Color col = note.getPart().getColor();
						g.setColor(alphaColor(col, note));
						g.fill(shape);
						g.setColor(col);
						if (!((NoteEvent) note).isDrumHit()) { // what's all
							// this about
							// anyway. I
							// should know I
							// wrote it PJL
							Rectangle noteRect2 = new Rectangle();
							noteRect2.width = noteRect.width - 2;
							noteRect2.height = noteRect.height - 2;
							noteRect2.x = noteRect.x + 1;
							noteRect2.y = noteRect.y + 1;
							g.draw(noteRect2);
						}
					}

				}
				g.setColor(Color.BLACK);
				g.draw(shape);
			}
		}

		if (!dragList.isEmpty()) {

			for (Item it : dragList) {
				NoteEvent note = (NoteEvent) it;
				Rectangle noteRect = getItemBounds(note);
				if (visibleRect != null && !visibleRect.intersects(noteRect))
					continue;

				Shape shape = getItemShape(note);
				g.setColor(selCol);
				g.fill(noteRect);
				g.setColor(Color.RED);
				g.draw(shape);
			}
		}

		if (newNote != null) {
			Rectangle noteRect = getItemBounds(newNote);
			/*
			 * if (visibleRect != null && !visibleRect.intersects(noteRect))
			 * continue;
			 */
			Shape shape = getItemShape(newNote);

			g.setColor(selCol);
			g.fill(noteRect);
			g.setColor(Color.BLACK);
			g.draw(shape);

		}
	}

	/**
	 * Find the screen rectangle for the event
	 * 
	 * use it quickly because we modify a single object and return a reference
	 * it could be changed by the next call.
	 * 
	 * @param e
	 *            NoteEvent
	 * @return rectangle on screen
	 */

	final Rectangle rectTmp = new Rectangle();

	/**
	 * returns the Rectangle contian the GUI representation of the item
	 * 
	 * @param it
	 * @return
	 */
	private Rectangle getItemBounds(Item it) {

		NoteEvent e = (NoteEvent) it;
		long tick = e.getStartTick();

		// Careful that origins must always conincide
		int x1 = (int) userToScreen(tick);

		int pitch = e.getNote();
		int y1 = pitchToScreen(pitch);

		if (!e.isDrumHit()) {
			long dur = e.getDuration();
			int w = (int) userToScreen(dur);
			if (w <= 6)
				w = 6;

			rectTmp.setBounds((int) x1, (int) y1 + 1, w, noteItemHeight - 2);
		} else {
			int dd = noteItemHeight / 2 - 1;
			rectTmp.setBounds((int) x1 - dd, (int) y1 + 1, 2 * dd,
					noteItemHeight - 2);

		}

		return rectTmp;

		// return new Rectangle((int) x1, (int) y1 + 1, w, noteItemHeight - 2);
	}

	/* a little sneakiness to avoid new */

	final int xp[] = new int[4];

	final int yp[] = new int[4];

	final Polygon drumShape = new Polygon(xp, yp, 4);

	/**
	 * returns the Rectangle contian the GUI representation of the item
	 * 
	 * @param it
	 * @return
	 */
	private Shape getItemShape(Item it) {

		NoteEvent e = (NoteEvent) it;

		long tick = e.getStartTick();

		// Careful that origins must always conincide
		int x1 = (int) userToScreen(tick);

		int pitch = e.getNote();
		int y1 = pitchToScreen(pitch);

		if (!e.isDrumHit()) {
			long dur = e.getDuration();

			// Careful that origins must always conincide
			int w = (int) userToScreen(dur);
			if (w == 0)
				w = 1;
			rectTmp.setBounds((int) x1, (int) y1 + 1, w, noteItemHeight - 2);

			return rectTmp;
		} else {
			int dd = noteItemHeight / 2;
			int yc = y1 + dd;
			dd -= 1;
			xp[0] = xp[2] = x1;
			xp[1] = x1 - dd;
			xp[3] = x1 + dd;
			yp[0] = yc - dd;
			yp[2] = yc + dd;
			yp[1] = yp[3] = yc;
			return new Polygon(xp, yp, 4); // drumShape;
		}

		// return new Rectangle((int) x1, (int) y1 + 1, w, noteItemHeight - 2);
	}

	@Override
	public void endDrag() {
		if (dragArmed) {
			dragArmed = false;
			return;
		}
		project.getDragList().endDrag(controlIsDown);
	}

	public void erase(Item it) {
		NoteEvent note = (NoteEvent) it;
		editHistory.mark(getMessage("sequencer.pianoroll.erase_note"));
		note.getPart().remove(note);
		editHistory.notifyEditHistoryListeners();
	}

	@Override
	public void rightButtonPressedOnItem(int x, int y) {
		System.out.println(" Right button pressed (so what?) ");
	}

	/**
	 * play the note
	 */
	public void feedBack(Item item) {
		audioFeedBack.select((NoteEvent) item);
	}

	@Override
	public void clientAddToSelection(Item item) {

		project.getPartSelection().setFocus(((MultiEvent) item).getPart());
		project.getMultiEventSelection().addSelected((NoteEvent) item);
		project.getMultiEventSelection().notifyListeners();

	}

	@Override
	public void clientRemoveFromSelection(Item item) {
		project.getMultiEventSelection().removeSelected((NoteEvent) item);
		project.getMultiEventSelection().notifyListeners();

	}

	@Override
	public int getHoverStateAt(Point p) {
		Part focusPart = project.getPartSelection().getFocus();
		if (!(focusPart instanceof MidiPart))
			return OVER_NOTHING;
		final int endTol = 20;
		// final int extraX = 20 ;
		// if (true) return OVER_ITEM_MIDDLE;

		int tol = endTol;
		Iterable<? extends MultiEvent> list;
		if (focusPart == null)
			list = notesOnScreen;
		else
			list = ((MidiPart) focusPart).getMultiEvents();
		for (MultiEvent e : list) {
			if (e instanceof NoteEvent) {

				Rectangle rect = getItemBounds(e);

				if (rect.contains(p)) {
					if (((NoteEvent) e).isDrumHit())
						return OVER_ITEM_MIDDLE;
					if (rect.width < endTol * 5)
						tol = rect.width / 3;
					if ((p.x - rect.x) <= tol)
						return OVER_ITEM_LEFT;
					if ((rect.x + rect.width - p.x) <= tol)
						return OVER_ITEM_RIGHT;

					return OVER_ITEM_MIDDLE;
				}

			}
		}
		return OVER_NOTHING;
	}

	void makeTools() {
		Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
		selectTool = new SelectTool(c);
		rectZoomTool = new RectZoomTool(c);

		// drumWriteTool = new WriteTool(MyCursors.getCursor("pencil"));
		writeTool = new WriteTool(MyCursors.getCursor("pencil"));
		eraseTool = new EraseTool(MyCursors.getCursor("eraser"));
		dragViewTool = new DragViewTool(MyCursors.getCursor("move"));

	}

	@Override
	protected void paintImageImplLabel(Graphics2D graphics) {
		// TODO Auto-generated method stub

	}

	public final boolean isValidEvent(MultiEvent event) {
		return event instanceof NoteEvent;
	}

	public void ignorePartWarp(boolean b) {
		PartSelectedAction.ignoreWarp = b;

	}

	public synchronized void componentResized(ComponentEvent arg0) {
		super.componentResized(arg0);

		int extent = (int) getVisibleRect().getHeight();
		getYRangeModel().setExtent(extent);
	}

	public void selectAll() {
		Part focus = project.getPartSelection().getFocus();
		if (focus == null)
			return;

		Vector<MultiEvent> notes = new Vector<MultiEvent>();
		for (MultiEvent e : notesInFocus) {
			notes.add(e);
		}

		project.getMultiEventSelection().setSelected(notes);
		project.getMultiEventSelection().notifyListeners();

	}

	interface NoteShape extends Shape {

	};

	@SuppressWarnings("serial")
	class MNoteShape extends Rectangle implements NoteShape {

	}

	@SuppressWarnings("serial")
	class DNoteShape extends Polygon implements NoteShape {

	}

	void setDrumWriteMode(boolean b) {
		drumWriteMode = b;
	}

}
