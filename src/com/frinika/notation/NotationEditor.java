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

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import com.frinika.notation.NotationGraphics.Note;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.SwingSongPositionListenerWrapper;
import com.frinika.sequencer.gui.DragViewTool;
import com.frinika.sequencer.gui.EraseTool;
import com.frinika.sequencer.gui.Item;
import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.gui.ItemScrollPane;
import com.frinika.sequencer.gui.MyCursors;
import com.frinika.sequencer.gui.RectZoomTool;
import com.frinika.sequencer.gui.SelectTool;
import com.frinika.sequencer.gui.WriteTool;
import com.frinika.sequencer.gui.pianoroll.AudioFeedBack;
import com.frinika.sequencer.gui.pianoroll.DragEventListener;
import com.frinika.sequencer.gui.pianoroll.ItemPanelMultiEventListener;
import com.frinika.sequencer.gui.pianoroll.ItemPanelPartListener;
import com.frinika.sequencer.model.EditHistoryAction;
import com.frinika.sequencer.model.EditHistoryContainer;
import com.frinika.sequencer.model.EditHistoryListener;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.notation.ClefChange;
import com.frinika.sequencer.model.util.EventFilter;
import com.frinika.sequencer.model.util.EventsInPartsIterator;

public class NotationEditor extends ItemPanel implements EditHistoryListener,
		EventFilter {

	public static void main(String[] args) {

		double tr;
		int[] ret;
		/*
		 * tr = 4; ret = parseDurToNotation(tr); System.out.println(tr + " => " +
		 * ret[0]+ " , " + ret[1]);
		 * 
		 * tr = 2; ret = parseDurToNotation(tr); System.out.println(tr + " => " +
		 * ret[0]+ " , " + ret[1]);
		 * 
		 * tr = 1; ret = parseDurToNotation(tr); System.out.println(tr + " => " +
		 * ret[0]+ " , " + ret[1]);
		 * 
		 * tr = 0.5; ret = parseDurToNotation(tr); System.out.println(tr + " => " +
		 * ret[0]+ " , " + ret[1]);
		 */
		tr = 1;
		ret = parseDurToNotationLength(tr);
		System.out.println(tr + " => " + ret[0] + " , " + ret[1] + " , "
				+ ret[2]);

		tr = 1 * 2.0 / 3.0;
		ret = parseDurToNotationLength(tr);
		System.out.println(tr + " => " + ret[0] + " , " + ret[1] + " , "
				+ ret[2]);

	}

	private static double calcDottedDiffLen(int i) {
		if (i < 1)
			return 0;
		if (i == 1) {
			return 1.0 - Math.log(1.5) / Math.log(4);
		}
		return calcDottedDiffLen(i - 1)
				- Math.log(1 + 3 / ((Math.pow(2, i)) * 2 - 4)) / Math.log(4);
	}

	// 0,046554702 -0,29248125 0 dotted
	// 0,142877241 0,046554702 3 dotted
	// 0,303841289 0,142877241 2 dotted
	// 0,70751875 0,303841289 1 dotted

	private static double[] NOTE_DIFFLEN = new double[20];
	// NOTE_DIFFLEN[1] = 0.707518749639422
	// NOTE_DIFFLEN[2] = 0.30384128861061993
	// NOTE_DIFFLEN[3] = 0.14287724116693876
	// NOTE_DIFFLEN[4] = 0.06945654700230317

	static {
		for (int i = 0; i < 20; i++) {
			NOTE_DIFFLEN[i] = calcDottedDiffLen(i);
		}
	}

	private static double NOTE_TRIPLET = 2 - Math.log(4 * (2.0 / 3.0))
			/ Math.log(2);

	// NOTE_TRIPLET = 0.5849625007211563

	public static int[] parseDurToNotationLength(double tickdur) {
		// returns int array
		// 
		// 0: dur length = 0(whole note) 1(half note) 2(quarter note) ....
		// 1: dotted = 0(not dotted) 1(dotted once) 2(dotted twice) ....
		// 2: tuplet = 0(not tupled) 2(triplet)
		// 3: tuplet = 0(not tupled) 3(triplet)

		int[] ret = new int[4];

		double loglen = 2.0 - Math.log(tickdur) / Math.log(2);

		int dur = (int) (loglen + (1.0 - NOTE_DIFFLEN[1]));
		int dotted = 0;
		double durmod = loglen - dur;

		// Is it triplet ?
		if (dur >= 1)
			if (durmod >= NOTE_TRIPLET - 0.1 && durmod <= NOTE_TRIPLET + 0.1) {
				ret[0] = dur;
				ret[1] = dotted;
				ret[2] = 2;
				ret[3] = 3;
				return ret;
			}

		// Is it dotted ?
		if (durmod >= NOTE_DIFFLEN[2])
			dotted = 1;
		else if (durmod >= NOTE_DIFFLEN[3])
			dotted = 2;
		else if (durmod >= NOTE_DIFFLEN[4])
			dotted = 3;

		if (dotted != 0)
			dur++;

		ret[0] = dur;
		ret[1] = dotted;
		return ret;
	}

	Iterable<MultiEvent> notesOnScreen;

	Iterable<MultiEvent> notesInFocus;

	EditHistoryContainer editHistory;

	ItemPanelMultiEventListener multiEventListener;

	ItemPanelPartListener partListener;

	AudioFeedBack audioFeedBack;

	private NoteEvent newNote = null;

	int velocity = 100;

	int channel = 1;

	protected NotationEditor(final ProjectFrame frame, ItemScrollPane scroller) {
		super(frame.getProjectContainer(), scroller, true, true,.5,false);   // PJL added ticksToScreen and sampledBased 

		notesOnScreen = new Iterable<MultiEvent>() {
			public Iterator<MultiEvent> iterator() {
				return new EventsInPartsIterator(project.getPartSelection()
						.getSelected(), NotationEditor.this);
			}
		};
		notesInFocus = new Iterable<MultiEvent>() {
			public Iterator<MultiEvent> iterator() {
				Part focus = project.getPartSelection().getFocus();
				if (focus == null)
					return null;
				return new EventsInPartsIterator(project.getPartSelection()
						.getFocus(), NotationEditor.this);
			}
		};

		project.getDragList().addDragEventListener(new DragEventListener() {
			public void update() {
				repaintItems();
			}
		});

		this.sequencer = project.getSequencer();
		sequencer.addSongPositionListener(new SwingSongPositionListenerWrapper(
				this));

		audioFeedBack = new AudioFeedBack(project);

		multiEventListener = new ItemPanelMultiEventListener(this);
		partListener = new ItemPanelPartListener(this);

		project.getMultiEventSelection().addSelectionListener(
				multiEventListener);
		project.getPartSelection().addSelectionListener(partListener);

		FrinikaSequence seq = (FrinikaSequence) sequencer.getSequence();
		ticksPerBeat = seq.getResolution();

		editHistory = project.getEditHistoryContainer();
		editHistory.addEditHistoryListener(this);

		/*
		 * setLayout(new BorderLayout()); ScoreTest1 test = new ScoreTest1();
		 * add(test);
		 */

		ng.setSize(30);
		romanfont = new Font("Times new roman", Font.BOLD, 16).deriveFont(ng
				.getGridSize() * 2f);
		romanfont2 = new Font("Times new roman", Font.PLAIN, 16).deriveFont(ng
				.getGridSize() * 1.8f);

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

	void makeTools() {
		Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
		selectTool = new SelectTool(c);
		rectZoomTool = new RectZoomTool(c);

		// drumWriteTool = new WriteTool(MyCursors.getCursor("pencil"));
		writeTool = new WriteTool(MyCursors.getCursor("pencil"));
		eraseTool = new EraseTool(MyCursors.getCursor("eraser"));
		dragViewTool = new DragViewTool(MyCursors.getCursor("move"));

	}

	public NotationHeader header;

	NotationGraphics ng = new NotationGraphics();

	Font romanfont;

	Font romanfont2;

	public boolean isBarTick(long tick) {
		int bar = project.getSequence().getResolution() * 4;
		return (tick % bar) == 0;
	}

	public long nextBarTick(long tick) {
		int bar = project.getSequence().getResolution() * 4;
		return (tick - tick % bar) + bar;
	}

	public long previousBarTick(long tick) {
		int bar = project.getSequence().getResolution() * 4;
		return tick - tick % bar;
	}

	double bar_zoomout_level = 0.1;

	private long org_screenToTick(int x, boolean quantizeMe) {
		return super.screenToTickAbs(x, quantizeMe); // PJL TODO check this
														// is really abs
	}

	private int org_tickToScreen(long tick) {
		return (int)super.userToScreen(tick);
	}

	public MidiLane getLaneAtY(int y) {

		HashSet<Lane> selectedlanes = new HashSet<Lane>();
		Collection<Part> parts = project.getPartSelection().getSelected();
		for (Part part : parts) {
			if (!selectedlanes.contains(part.getLane()))
				selectedlanes.add(part.getLane());
		}
		ng.absoluteY(0);
		List<Lane> lanes = project.getLanes();// getPartSelection().getSelected();
		for (Lane lane : lanes) {
			if (selectedlanes.contains(lane))
				if (lane instanceof MidiLane) {
					MidiLane midilane = (MidiLane) lane;
					ng.relativeLine(20);
					if (y < ng.getCurrentY()) {
						return midilane;
					}
					ng.relativeLine(-6);
				}
		}

		return null;
	}

	public ClefChange getClef(MidiLane lane) {
		MidiPart head = lane.getHeadPart();
		ClefChange clef_event = null;
		Iterator<MultiEvent> iter = head.getMultiEvents().iterator();
		while (iter.hasNext()) {
			MultiEvent event = iter.next();
			if (event.getStartTick() > 0)
				break;
			if (event instanceof ClefChange) {
				return (ClefChange) event;
			}
		}
		return new ClefChange(head, 0);
	}

	public long screenToTick(int x, boolean quantizeMe) {
		long tick = super.screenToTickAbs(x, false);
		long b1 = previousBarTick(tick);
		long b2 = nextBarTick(tick);
		long blen = b2 - b1;
		double d = (tick - b1) / (1.0 - bar_zoomout_level)
				- (blen * bar_zoomout_level + 0.5);
		if (d < 0)
			d = 0;
		else if (d > blen)
			d = blen;
		tick = b1 + (long) d;

		if (quantizeMe) {
			double tt = tick;
			double quant = this.getSnapQuantization();
			if (quant < 0) {
				try {
					throw new Exception(
							" SNAP TO BAR NOT IMPLEMENTED IN NOTATION ");
				} catch (Exception e) {
					
					e.printStackTrace();
					return tick;
				}

			}
			tt = (long) (Math.round(tt / this.getSnapQuantization()))
					* this.getSnapQuantization();
			tick = (long) tt;
		}

		return tick;
	}

	public double tickToScreen(long tick) {
		long b1 = previousBarTick(tick);
		long b2 = nextBarTick(tick);
		long blen = b2 - b1;
		tick = b1
				+ (long) ((blen * bar_zoomout_level * 0.5) + ((tick - b1) * (1.0 - bar_zoomout_level)));
		double x = super.userToScreen(tick);
		return x;
	}

	class NoteEventTick implements Comparable {
		boolean isdragged = false;

		int status;

		long tick;

		int id;

		NoteEvent event;

		Note lastnote;

		public int compareTo(Object o) {
			long t = tick - ((NoteEventTick) o).tick;
			if (t < 0)
				return -1;
			if (t > 0)
				return 1;
			return id - ((NoteEventTick) o).id;
		}
	}

	Map<MidiLane, Integer> laneY = new HashMap<MidiLane, Integer>();

	Map<MidiLane, ClefChange> laneClef = new HashMap<MidiLane, ClefChange>();

	public void paintHeader(Graphics g, int scroll) {

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2.setColor(Color.DARK_GRAY);
		g2.fillRect(0, 0, 400, 20);
		g2.setColor(Color.BLACK);

		ng.setGraphics(g2);
		ng.absoluteY(20 - scroll);
		// ng.absoluteY(0);

		HashSet<Lane> selectedlanes = new HashSet<Lane>();
		Collection<Part> parts = project.getPartSelection().getSelected();
		for (Part part : parts) {
			if (!selectedlanes.contains(part.getLane()))
				selectedlanes.add(part.getLane());
		}

		laneY.clear();
		laneClef.clear();

		List<Lane> lanes = project.getLanes();// getPartSelection().getSelected();
		for (Lane lane : lanes) {
			if (selectedlanes.contains(lane))
				if (lane instanceof MidiLane) {
					MidiLane midilane = (MidiLane) lane;

					ng.absolute(0);
					ng.relativeLine(14);
					laneY.put(midilane, (int) ng.getCurrentY());
					laneClef.put(midilane, getClef(midilane));
					ng.drawStaff(header.getSize().width);

					// ng.drawBarLine();
					ng.relative(1);

					ClefChange clefevent = getClef(midilane);
					ng.drawClef(clefevent.clef_type, clefevent.clef_pos); // Draw
																			// G-Clef

					g2.setFont(romanfont);
					g2.drawString(midilane.getName(), ng.getCurrentX(), ng
							.getCurrentY()
							- ng.getGridSize() * 6.5f);

					ng.relative(4);

					// ng.relative(ng.drawSharpKeySignature(8,5,9,6,3,7,4)+1);
					// ng.relative(ng.drawSharpKeySignature(8,5,9)+1);

					// ng.drawTimeSignature(4, 4);
					ng.drawTimeSignature(0);
					ng.relative(3);

					g2.setFont(romanfont2);
					// g2.drawString("0", ng.getCurrentX(),
					// ng.getCurrentY()-ng.getGridSize()*4.5f);
					ng.relative(3);
				}
		}
	}

	public int[] getNotationNotePos(ClefChange clef_event, int note) {
		int oct = (note / 12) - 5;
		int not = note % 12;
		int n = 0;
		int a = 0;
		if (not == 0) {
			n = 0;
			a = 0;
		}
		if (not == 1) {
			n = 0;
			a = 100;
		}
		if (not == 2) {
			n = 1;
			a = 0;
		}
		if (not == 3) {
			n = 1;
			a = 100;
		}
		if (not == 4) {
			n = 2;
			a = 0;
		}
		if (not == 5) {
			n = 3;
			a = 0;
		}
		if (not == 6) {
			n = 3;
			a = 100;
		}
		if (not == 7) {
			n = 4;
			a = 0;
		}
		if (not == 8) {
			n = 4;
			a = 100;
		}
		if (not == 9) {
			n = 5;
			a = 0;
		}
		if (not == 10) {
			n = 5;
			a = 100;
		}
		if (not == 11) {
			n = 6;
			a = 0;
		}

		n = n + oct * 7;

		if (clef_event.clef_type == NotationGraphics.CLEF_G) {
			n -= -(0) - (clef_event.clef_pos - 2);
		} else if (clef_event.clef_type == NotationGraphics.CLEF_C) {
			n -= -(4) - (clef_event.clef_pos - 2);
		} else if (clef_event.clef_type == NotationGraphics.CLEF_F) {
			n -= -(8) - (clef_event.clef_pos - 2);
		}

		int[] ret = new int[2];
		ret[0] = n;
		ret[1] = a;
		return ret;
	}

	public int getNoteFromPos(ClefChange clef_event, int n) {

		if (clef_event.clef_type == NotationGraphics.CLEF_G) {
			n += -(0) - (clef_event.clef_pos - 2);
		} else if (clef_event.clef_type == NotationGraphics.CLEF_C) {
			n += -(4) - (clef_event.clef_pos - 2);
		} else if (clef_event.clef_type == NotationGraphics.CLEF_F) {
			n += -(8) - (clef_event.clef_pos - 2);
		}

		int oct = n / 7;
		n = n - oct * 7;
		if (n < 0) {
			n = n + 7;
			oct--;
		}

		int not = 0;

		if (n == 0) {
			not = 0;
		}
		if (n == 1) {
			not = 2;
		}
		if (n == 2) {
			not = 4;
		}
		if (n == 3) {
			not = 5;
		}
		if (n == 4) {
			not = 7;
		}
		if (n == 5) {
			not = 9;
		}
		if (n == 6) {
			not = 11;
		}

		oct += 5;

		return not + oct * 12;

	}

	@Override
	protected void paintImageImpl(Rectangle clipRect, Graphics2D g) {

		int clip_min_x = clipRect.x - 500;
		if (clip_min_x < 0)
			clip_min_x = 0;
		int clip_max_x = clipRect.x + clipRect.width + 500;

		// TODO Auto-generated method stub
		/*
		 * }
		 * 
		 * 
		 * public void paint(Graphics g) {
		 * 
		 * super.paint(g);
		 */

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
		// RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		// g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
		// RenderingHints.VALUE_STROKE_PURE);
		g2.setColor(Color.WHITE);
		g2.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);// 0,
																				// 40,
																				// 2048,
																				// 600);
		g2.setColor(Color.BLACK);
		ng.setGraphics(g2);

		HashSet<Lane> selectedlanes = new HashSet<Lane>();
		Collection<Part> parts = project.getPartSelection().getSelected();
		for (Part part : parts) {
			if (!selectedlanes.contains(part.getLane()))
				selectedlanes.add(part.getLane());
		}

		ng.absoluteLine(0);

		boolean newNoteAdded = true;
		if (newNote != null)
			newNoteAdded = false;

		int res = project.getSequence().getResolution();

		Collection<Item> dragList = project.getDragList();

		List<Lane> lanes = project.getLanes();// getPartSelection().getSelected();
		for (Lane lane : lanes) {
			if (selectedlanes.contains(lane))
				if (lane instanceof MidiLane) {

					ng.absolute(0);
					ng.relativeLine(14);
					ng.drawStaff(clipRect.width + clipRect.x);

					long firsttick = org_screenToTick(clipRect.x, false);
					int max_x = clipRect.width + clipRect.x;
					long tt = firsttick;
					while (true) {
						tt = nextBarTick(tt);
						int x = org_tickToScreen(tt);
						if (x > max_x)
							break;
						ng.absoluteX(x);
						ng.drawBarLine();
					}

					MidiLane midilane = (MidiLane) lane;

					ClefChange clef_event = getClef(midilane);

					// TreeSet<MultiEvent> multiEvents = new
					// TreeSet<MultiEvent>();

					int id_count = 0;
					TreeSet<NoteEventTick> multiEventsTicks = new TreeSet<NoteEventTick>();

					for (Part part : midilane.getParts())
						if (part instanceof MidiPart)
							if (project.getPartSelection().getSelected()
									.contains(part)) {
								MidiPart midipart = (MidiPart) part;
								// if(midipart.getStartTick() >= 0)
								// if(midipart.getEndTick() < 1000)
								{

									boolean startread = false;

									for (MultiEvent event : midipart
											.getMultiEvents())
										if (event instanceof NoteEvent) {
											NoteEvent noteevent = (NoteEvent) event;

											if (!dragList.isEmpty())
												if (noteevent.isSelected())
													noteevent = null;

											if (noteevent != null) {

												if (!newNoteAdded) {
													if (noteevent
															.getStartTick() > newNote
															.getStartTick()) {
														newNoteAdded = true;
														NoteEventTick n = new NoteEventTick();
														n.id = id_count++;
														n.event = newNote;
														n.tick = newNote
																.getStartTick();
														n.status = 1;
														multiEventsTicks.add(n);

														n = new NoteEventTick();
														n.id = id_count++;
														n.event = newNote;
														n.tick = newNote
																.getEndTick();
														n.status = 0;
														multiEventsTicks.add(n);
													}
												}

												int x = (int)tickToScreen(noteevent
														.getStartTick());

												if (!startread) {
													if (x >= clip_min_x)
														startread = true;
												}
												if (startread) {
													NoteEventTick n = new NoteEventTick();
													n.id = id_count++;
													n.event = noteevent;
													n.tick = noteevent
															.getStartTick();
													n.status = 1;
													multiEventsTicks.add(n);

													if (noteevent.isDrumHit()) {
														n.status = 2;
													} else {
														n = new NoteEventTick();
														n.id = id_count++;
														n.event = noteevent;
														n.tick = noteevent
																.getEndTick();
														n.status = 0;
														multiEventsTicks.add(n);
													}

													if (x > clip_max_x)
														break;

												}

											}

											// multiEvents.addAll(midipart.getMultiEvents());
										}

								}
							}
				//	System.out.println("DRAG PAINT");

					for (Item item : dragList) {
						NoteEvent noteevent = (NoteEvent) item;
						if (noteevent.getPart().getLane() == lane) {
							int x = (int) tickToScreen(noteevent.getStartTick());
							NoteEventTick n = new NoteEventTick();
							n.isdragged = true;
							n.id = id_count++;
							n.event = noteevent;
							n.tick = noteevent.getStartTick();
							n.status = 1;
							multiEventsTicks.add(n);

							if (noteevent.isDrumHit()) {
								n.status = 2;
							} else {
								n = new NoteEventTick();
								n.isdragged = true;
								n.id = id_count++;
								n.event = noteevent;
								n.tick = noteevent.getEndTick();
								n.status = 0;
								multiEventsTicks.add(n);
							}

						}
					}

					// Avarage tick together 0..32 ticks
					/*
					 * ArrayList<NoteEventTick> currentgroup = new ArrayList<NoteEventTick>();
					 * double tickavg = 0;
					 * 
					 * for(NoteEventTick event : multiEventsTicks) {
					 * NoteEventTick noteevent = (NoteEventTick)event; long
					 * curtick = event.tick; if(currentgroup.isEmpty()) {
					 * currentgroup.add(event); tickavg = curtick; } else {
					 * if(Math.abs(tickavg - curtick) < res/4) {
					 * currentgroup.add(event); int c = currentgroup.size();
					 * tickavg = (tickavg*(c-1) + curtick)/c; } else {
					 * for(NoteEventTick event2 : currentgroup) event2.tick =
					 * (long)tickavg; currentgroup.clear(); } } }
					 * for(NoteEventTick event2 : currentgroup) event2.tick =
					 * (long)tickavg; currentgroup.clear();
					 */

					ng.relative(1);

					long max_tick = 0;

					TreeMap<Long, List<NoteEventTick>> tickslist = new TreeMap<Long, List<NoteEventTick>>();
					for (NoteEventTick event : multiEventsTicks) {
						List<NoteEventTick> list = tickslist.get(event.tick);
						if (list == null) {
							list = new ArrayList<NoteEventTick>();
							tickslist.put(event.tick, list);
						}
						if (event.tick > max_tick)
							max_tick = event.tick;
						list.add(event);
					}

					// Add phantom events for bar/measure seperation
					long m = 0;
					m = screenToTick(clip_min_x, false);
					if (m < 0)
						m = 0;
					m = m - (m % (res * 4));
					while (m < max_tick) {
						NoteEventTick event = new NoteEventTick();
						event.tick = m;
						event.status = 2; // phantom status // not a real
											// event
						List<NoteEventTick> list = tickslist.get(event.tick);
						if (list == null) {
							list = new ArrayList<NoteEventTick>();
							tickslist.put(event.tick, list);
						}
						list.add(event);
						m += res * 4; // res=quart note, res*4=whole note = in
										// 4/4 a whole is a bar/measure
					}

					ng.startNoteGroup();

					TreeMap<Integer, NoteEventTick> act = new TreeMap<Integer, NoteEventTick>();

					Iterator<List<NoteEventTick>> iter = tickslist.values()
							.iterator();
					List<NoteEventTick> nextitem = iter.hasNext() ? iter.next()
							: null;
					while (nextitem != null) {
						List<NoteEventTick> curitem = nextitem;
						nextitem = iter.hasNext() ? iter.next() : null;

						if (isBarTick(curitem.get(0).tick)) {
							ng.endNoteGroup();
							ng.startNoteGroup();
						}

						ng.absoluteX((int)tickToScreen(curitem.get(0).tick));

						long tickdur = nextitem == null ? res
								: nextitem.get(0).tick - curitem.get(0).tick;

						int[] ret = parseDurToNotationLength(((double) tickdur)
								/ ((double) res));
						int dur = ret[0];
						int dotted = ret[1];
						int tuplet_a = ret[2];
						int tuplet_b = ret[3];

						boolean noteprinted = false;
						for (NoteEventTick event : curitem) {
							NoteEventTick noteevent = (NoteEventTick) event;
							if (noteevent.status == 0)
								act.remove(noteevent.event.getNote());
							if (noteevent.status == 1)
								act.put(noteevent.event.getNote(), noteevent);
						}

						for (NoteEventTick event : act.values()) {
							NoteEventTick noteevent = (NoteEventTick) event;
							if (noteevent.status == 1) {
								noteprinted = true;
								int note = noteevent.event.getNote();

								/*
								 * g.setColor(Color.PINK); Rectangle rec =
								 * getItemBounds(noteevent.event);
								 * g.drawRect(rec.x, rec.y, rec.width,
								 * rec.height); g.setColor(Color.BLACK);
								 */

								int[] n_ret = getNotationNotePos(clef_event,
										note);
								int n = n_ret[0];
								int a = n_ret[1];

								Note notegraphic = ng.drawNote(n - 2, dur); // G-clef
																			// is
																			// at 2
								notegraphic.dotted = dotted;

								if (tuplet_a == 2 && tuplet_b == 3) // Triplet
									notegraphic.color = Color.BLUE;

								if (event.isdragged)
									notegraphic.color = Color.RED;
								if (noteevent.event.isSelected())
									notegraphic.color = Color.RED;
								if (newNote != null)
									if (noteevent.event == newNote)
										notegraphic.color = Color.GRAY;
								notegraphic.accidental = a;

								if (noteevent.lastnote != null)
									ng.drawNoteTie(noteevent.lastnote,
											notegraphic);
								noteevent.lastnote = notegraphic;
							}

						}

						if (!noteprinted) {
							if (dur < 8)
								if (nextitem != null)
									ng.drawRest(dur, dotted);
						}

					}

					ng.endNoteGroup();

					/*
					 * ng.startNoteGroup(); ng.drawNote(-2,2); ng.relative(6);
					 * ng.drawNote(-2,2); ng.relative(6); ng.drawNote(0,2);
					 * ng.relative(6); ng.drawNote(2,2); ng.relative(6);
					 * ng.endNoteGroup();
					 * 
					 * ng.drawBarLine(); g2.setFont(romanfont2);
					 * //g2.drawString("1", ng.getCurrentX(),
					 * ng.getCurrentY()-ng.getGridSize()*4.5f); ng.relative(3);
					 * 
					 * ng.startNoteGroup(); ng.drawNote(-2,2); ng.relative(6);
					 * ng.drawNote(-2,2); ng.relative(6); ng.drawNote(0,2);
					 * ng.relative(6); ng.drawNote(2,3); ng.relative(3);
					 * ng.drawNote(2,3); ng.relative(6); ng.endNoteGroup();
					 * 
					 * ng.drawBarLine(); g2.setFont(romanfont2);
					 * //g2.drawString("2", ng.getCurrentX(),
					 * ng.getCurrentY()-ng.getGridSize()*4.5f); ng.relative(3);
					 * 
					 *  // TEST
					 * 
					 * ng.startNoteGroup(); ng.drawNote(2,3); ng.drawNote(4,3);
					 * ng.drawNote(6,3); ng.relative(3); ng.endNoteGroup();
					 * 
					 * ng.startNoteGroup(); ng.drawNote(2,2); ng.drawNote(4,2);
					 * ng.drawNote(6,2); ng.relative(3); ng.endNoteGroup();
					 * 
					 * ng.startNoteGroup(); ng.drawNote(2,1); ng.drawNote(4,1);
					 * ng.drawNote(6,1); ng.relative(3); ng.endNoteGroup();
					 * 
					 * ng.startNoteGroup(); ng.drawNote(2,0); ng.drawNote(4,0);
					 * ng.drawNote(6,0); ng.relative(3); ng.endNoteGroup();
					 * 
					 * 
					 * ng.startNoteGroup(); ng.drawNote(2,3,0,0,0,1);
					 * ng.drawNote(4,3,0,0,0,1); ng.drawNote(6,3,0,0,0,1);
					 * ng.relative(3); ng.endNoteGroup();
					 * 
					 * ng.startNoteGroup(); ng.drawNote(8,3); ng.relative(3);
					 * ng.drawNote(5,4); ng.relative(3); ng.drawNote(4,4);
					 * ng.relative(3); ng.endNoteGroup();
					 * 
					 * 
					 * ng.startNoteGroup(); ng.drawNote(0,2).mark =
					 * NotationGraphics.ARTICULATION_MARK_OPEN_NOTE;
					 * ng.relative(3); ng.drawNote(0,3); ng.relative(3);
					 * ng.drawNote(2,4).mark =
					 * NotationGraphics.ORNAMENT_MARK_TRILL; ng.relative(3);
					 * ng.drawNote(6,5).mark =
					 * NotationGraphics.ORNAMENT_MARK_TURN; ng.relative(3);
					 * ng.drawNote(4,1); ng.relative(3); ng.endNoteGroup();
					 * 
					 * ng.relative(3);
					 * 
					 * ng.startNoteGroup(); ng.drawNote(8,4); ng.drawNote(5,4);
					 * ng.drawNote(4,4,0,100); ng.endNoteGroup();
					 * 
					 * ng.relative(6);
					 * 
					 * ng.startNoteGroup(); ng.drawNote(-1,4);
					 * ng.drawNote(0,4,0,100); ng.drawNote(4,4);
					 * ng.endNoteGroup();
					 */

					// TEST END

				}

		}

	}

	private static final long serialVersionUID = 1L;

	@Override
	public double getSnapQuantization() {
		return this.project.getPianoRollSnapQuantization();
	}

	@Override
	public void setSnapQuantization(double quant) {
		this.project.setPianoRollSnapQuantization(quant);
		repaintItems();
	}

	@Override
	public boolean isSnapQuantized() {
		return this.project.isPianoRollSnapQuantized();
	}

	@Override
	public void setSnapQuantized(boolean b) {
		this.project.setPianoRollSnapQuantized(b);
		/*
		 * 
		 * quantize = b; if (b)
		 * MultiEventClipboard.getDefaultMultiEventClipboard()
		 * .setQuantization(snaptoQuantization); else
		 * MultiEventClipboard.getDefaultMultiEventClipboard()
		 * .setQuantization(1);
		 */
	}

	@Override
	public void setFocus(Item item) {
		this.project.getMultiEventSelection().setFocus((MultiEvent) item);
		this.project.getPartSelection().notifyListeners();
		repaintItems();
	}

	@Override
	public void clientNotifySelectionChange() {
		this.project.getPartSelection().notifyListeners();
	}

	@Override
	public void setTimeAtX(int x) {
		long tick = screenToTick(x, this.project.isPianoRollSnapQuantized());
		this.sequencer.setTickPosition(tick);
	}

	public void startDrag() {

		// project.clearDragList();
		project.getDragList().startDrag(dragItem);
	}

	/**
	 * drags the dragList to Point p.
	 * 
	 */

	int noteItemHeight = 3;

	public void dragTo(Point p) {

		int dxDragged = p.x - xAnchor;
		int dyDragged = p.y - yAnchor;

		long dtick = org_screenToTick(dxDragged, project
				.isPianoRollSnapQuantized());

		/*
		 * if (project.isPianoRollSnapQuantized()) { dtick =
		 * snaptoQuantize(dxDragged); } else { dtick = (long) (dxDragged /
		 * ticksToScreen); }
		 */
		int dpitch = 0;

		if (dragMode == OVER_ITEM_MIDDLE)
			dpitch = -dyDragged / noteItemHeight;

		if (dtick == 0 && dpitch == 0)
			return;

		if (dragArmed)
			startDrag();

		dragArmed = false;

		Collection<Item> dragList = project.getDragList();

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
				try {
					throw new Exception("WHY OH WHY ARE WE HERE NOW");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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

	@Override
	public void endDrag() {
		if (dragArmed) {
			dragArmed = false;
			return;
		}
		project.getDragList().endDrag(controlIsDown);
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
					/*
					 * if (((NoteEvent)e).isDrumHit()) return OVER_ITEM_MIDDLE;
					 * if (rect.width < endTol * 5) tol = rect.width / 3; if
					 * ((p.x - rect.x) <= tol) return OVER_ITEM_LEFT; if
					 * ((rect.x + rect.width - p.x) <= tol) return
					 * OVER_ITEM_RIGHT;
					 */
					return OVER_ITEM_MIDDLE;
				}

			}
		}
		return OVER_NOTHING;
	}

	@Override
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

	@Override
	protected void paintImageImplLabel(Graphics2D graphics) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clientClearSelection() {
		project.getMultiEventSelection().clearSelection();
	}

	final Rectangle rectTmp = new Rectangle();

	private Rectangle getItemBounds(Item it) {

		NoteEvent e = (NoteEvent) it;
		long tick = e.getStartTick();

		// Careful that origins must always conincide
		int x1 = (int) tickToScreen(tick);

		int pitch = e.getNote();

		int cy = laneY.get(e.getMidiPart().getLane());
		ClefChange clef = laneClef.get(e.getMidiPart().getLane());
		if (clef == null)
			return null;
		int[] n_ret = getNotationNotePos(clef, e.getNote());
		int n = n_ret[0] + 4;

		float grid = ng.getGridSize();
		float y = cy - (n * grid * 0.5f);
		int noteItemWidth = (int) (grid * 1.2f);
		int noteItemHeight = (int) grid;

		int y1 = (int) y;// pitchToScreen(pitch);

		int dd = noteItemHeight / 2 - 1;
		rectTmp.setBounds((int) x1, (int) y1, noteItemWidth, noteItemHeight);

		return rectTmp;

		// return new Rectangle((int) x1, (int) y1 + 1, w, noteItemHeight - 2);
	}

	@Override
	public void selectInRect(Rectangle rect, boolean shift) {
		Collection<MultiEvent> addTmp = new Vector<MultiEvent>();
		Collection<MultiEvent> delTmp = new Vector<MultiEvent>();

		Iterable<? extends MultiEvent> list;

		Part focusPart = project.getPartSelection().getFocus();
		if (!(focusPart instanceof MidiPart))
			return;
		/*
		 * if (focusPart != null) list = ((MidiPart)
		 * focusPart).getMultiEvents(); else
		 */
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

	}

	public synchronized void writeDraggedAt(Point p) {
		if (newNote == null)
			return;
		long tick = screenToTick(p.x, true);

		long tick1 = newNote.getStartTick();
		long tick2 = tick1 + newNote.getDuration();
		int pitch = screenToPitch((MidiLane) newNote.getMidiPart().getLane(),
				p.y);

		boolean doit = false;

		if (tick > tick2) {
			newNote.setDuration(tick - tick1);
			doit = true;
		} else if (tick < tick2 && tick > tick1) {
			newNote.setDuration(tick - tick1);
			doit = true;
		}
		if (pitch != newNote.getNote()) {
			doit = true;
			newNote.setNote(pitch);
			feedBack(newNote);
		}

		project.getDragList().notifyFeedbackItemListeners(newNote);

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

		long tick = screenToTick(p.x, true);
		int pitch = screenToPitch((MidiLane) focusPart.getLane(), p.y);
		assert (pitch > 0);
		assert (pitch < 128);

		project.getEditHistoryContainer().mark(
				getMessage("sequencer.pianoroll.add_note"));
		/*
		 * if (drumWriteMode) { newNote = new NoteEvent((MidiPart) focusPart,
		 * tick, pitch, velocity, channel, 0); } else {
		 */
		newNote = new NoteEvent((MidiPart) focusPart, tick, pitch, velocity,
				channel, (long) project.getPianoRollSnapQuantization());

		// }

		feedBack(newNote);
		project.getDragList().notifyFeedbackItemListeners(newNote);
		repaintItems();

	}

	public int screenToPitch(MidiLane lane, int y) {
		Integer cy_o = laneY.get(lane);
		if (cy_o == null)
			return 64;
		int cy = cy_o;
		ClefChange clef = laneClef.get(lane);

		float grid = ng.getGridSize();

		int n = (int) ((cy - (y - grid * 0.6f)) / (grid * 0.5f));
		n -= 4;

		int note = getNoteFromPos(clef, n);
		if (note < 0)
			note = 0;
		if (note > 127)
			note = 127;
		return note;

		// int note = getNoteFromPos(clef, n)

		/*
		 * int cy = laneY.get(e.getMidiPart().getLane()); ClefChange clef =
		 * laneClef.get(e.getMidiPart().getLane()); if(clef == null) return
		 * null; int[] n_ret = getNotationNotePos(clef, e.getNote()); int n =
		 * n_ret[0]+4;
		 */
		// TODO FINISH WRITING THIS
		// return 64;
	}

	@Override
	public void rightButtonPressedOnItem(int x, int y) {
		// TODO Auto-generated method stub

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
	public void erase(Item it) {
		NoteEvent note = (NoteEvent) it;
		editHistory.mark(getMessage("sequencer.pianoroll.erase_note"));
		note.getPart().remove(note);
		editHistory.notifyEditHistoryListeners();
	}

	public void fireSequenceDataChanged(EditHistoryAction[] edithistoryEntries) {
		repaintItems();

	}

	public boolean isValidEvent(MultiEvent event) {
		return event instanceof NoteEvent;
	}

	public void repaintItems() {
		super.repaintItems();
		header.repaint();
	}

	public void dispose() {
		project.getMultiEventSelection().removeSelectionListener(
				multiEventListener);
		project.getPartSelection().removeSelectionListener(partListener);
		editHistory.removeEditHistoryListener(this);
	}

}
