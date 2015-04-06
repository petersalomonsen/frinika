/*
 * Created on Jun 14, 2007
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

package com.frinika.sequencer.gui.pianoroll;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.Part;

public class PadPanel extends JPanel implements MouseListener,
		AdjustmentListener, SelectionListener<Part> {
	private static final long serialVersionUID = 1L;

	final int ON = 0, OFF = 1;

	final int nNote = 127;

	protected Key keys[] = new Key[nNote];

	// Config config = null;

	Key prevKey;

	int lastKeyPress = 0;

	PianoRoll pianoRoll;

	int timePanelHeight;

	final int keyDepth = 50;

	int yScroll;

	int yBot;

	PadPanelIF padIF;

	PadPanelIF pianoPad;

	PadPanelIF drumPad;

	MidiLane midiLane;

	// public PadPanel(Config config) {
	// super(false);
	// this.yScroll = 0;
	// this.config = config;
	// this.pianoRoll = null;
	// this.timePanelHeight = 0;
	// addMouseListener(this);
	// }

	public PadPanel(PianoRoll pr, int top, int scroll) {
		this.pianoRoll = pr;
		this.timePanelHeight = top;
		this.yScroll = scroll;
		addMouseListener(this);
		pianoPad = new VirtualPiano();
		drumPad = new DrumPad();
		yBot = 129 * Layout.getNoteItemHeight();
		setSize(new Dimension(keyDepth, yBot));
		setPreferredSize(new Dimension(keyDepth, yBot));
		setMaximumSize(new Dimension(keyDepth, 20000));
	}

	
	
	public Key createkey(int x, int y, int width, int height, int num) {
		return new Key(x, y, width, height, num);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (padIF != null) {
			padIF.paintComponent(g);
		}
	}

	public class Key extends Rectangle {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Receiver recv = null;

		int chan = 0;

		int noteState = OFF;

		int kNum;

		public Key(int x, int y, int width, int height, int num) {
			super(x, y, width, height);
			kNum = num;
		}

		public boolean isNoteOn() {
			return noteState == ON;
		}

		public void on() {
			setNoteState(ON);
			if (pianoRoll == null)
				return;
			
//			Part focusPart = pianoRoll.getProjectContainer().getPartSelection()
//					.getFocus();
//			if (focusPart == null || !(focusPart instanceof MidiPart))
//				return;
//			midiLane = ((MidiLane) (focusPart.getLane()));

			recv = midiLane.getReceiver();
			chan = midiLane.getMidiChannel();

			if (recv == null)
				return;

			ShortMessage shm = new ShortMessage();
			int kk = midiLane.mapNote(kNum);
			try {
				shm.setMessage(ShortMessage.NOTE_ON, chan, kk, 100);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
			recv.send(shm, -1);
		}

		public void off() {
			setNoteState(OFF);

			if (recv == null)
				return;
			ShortMessage shm = new ShortMessage();

			int kk = midiLane.mapNote(kNum);

			try {
				shm.setMessage(ShortMessage.NOTE_ON, chan, kk, 0);
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			recv.send(shm, -1);
			recv = null;
		}

		public void setNoteState(int state) {
			noteState = state;
			if (state == ON)
				lastKeyPress = kNum;
		}
	}// End class Key

	public void adjustmentValueChanged(AdjustmentEvent e) {

		yScroll = e.getValue();
		repaint();
	}

	public int getLastKeytPress() {

		return lastKeyPress;
	}

	public Key getKey(int i) {
		return keys[i];
	}

	protected Key getKey(Point p) {
		return padIF.getKey(p);
	}

	public void mousePressed(MouseEvent e) {
            if (padIF == null) return;
		prevKey = getKey(e.getPoint());
		if (prevKey != null) {
			prevKey.on();
			repaint();
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			if (midiLane.isDrumLane()) {
				popupMapper(e.getX(), e.getY(), prevKey);
			}
		}
	}

	void popupMapper(int x, int y, final Key key) {

            
//		JPopupMenu menu = new JPopupMenu();
//
//		Font plain = getGraphics().getFont();
//		Font small = new Font(plain.getFamily(), Font.PLAIN, 9);
//		menu.setFont(small);

//		int count = 0;

		String[] keyNames = midiLane.getKeyNames();

                if (keyNames == null ) return;
                
		class XXX {
			String name;

			int key;

			public XXX(int i, String string) {
				key = i;
				name = string;
			}

			public String toString() {
				return name;
			}
		}
		;

		Vector<XXX> vec = new Vector<XXX>();
		XXX xx = null;
		XXX zz = null;
		for (int i = 0; i < keyNames.length; i++) {
			if (keyNames[i] != null)
				vec.add(0, zz = new XXX(i, keyNames[i]));
			if (i == key.kNum)
				xx = zz;
		}

		final JList list = new JList(vec);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedValue(xx, true);

		list.addListSelectionListener(new ListSelectionListener() {
			int zzz = -1;

			public void valueChanged(ListSelectionEvent e) {
				int jj = ((XXX) list.getSelectedValue()).key;
				midiLane.setDrumMapping(key.kNum, jj);
				if (zzz != jj) {
					key.on();
				} else {
					key.off();
				}
				zzz = jj;
			}
		});
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(200, 800));

		JFrame frame = new JFrame();
		frame.setContentPane(listScroller);
		frame.pack();
		frame.setVisible(true);

	}

	public void mouseReleased(MouseEvent e) {
		if (prevKey != null) {
			prevKey.off();
			repaint();
		}
	}

	public void mouseExited(MouseEvent e) {
		if (prevKey != null) {
			prevKey.off();
			repaint();
			prevKey = null;
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	interface PadPanelIF {
		void resizeKeys();

		void paintComponent(Graphics g);

		Key getKey(Point point);
	}

	class DrumPad implements PadPanelIF {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		int noteItemHeight;

		public void resizeKeys() {

			noteItemHeight = Layout.getNoteItemHeight();

			yBot = (nNote + 1) * noteItemHeight;
			int y = 0;
			for (int j = 0; j < nNote; j++, y += noteItemHeight) {
				keys[j] = createkey(0, yBot - y, keyDepth, noteItemHeight, j);
			}
			validate();
		}

		public void paintComponent(Graphics g) {

			if (noteItemHeight != Layout.getNoteItemHeight())
				resizeKeys();

			String keyNames[] = midiLane.getKeyNames();

			Font plain = g.getFont();
			Font small = new Font(plain.getFamily(), Font.PLAIN, 9);

			Graphics2D g2 = (Graphics2D) g;

			g2.setFont(small);
			g2.translate(0, timePanelHeight - yScroll);

			g2.setColor(MY_DRUM_COLOR);
			g2.fillRect(0, 0, keyDepth, yBot);

			for (int i = 0; i < keys.length; i++) {
				// if (keyNames != null)
				// System.out.println(keyNames[i]);
				Key key = keys[i];
				if (key.isNoteOn()) {
					g2.setColor(MY_KEYDOWN_COLOR);
					g2.fill(key);
				}
				int kk = midiLane.mapNote(i);
				g2.setColor(Color.black);
				g2.draw(key);
				if (keyNames != null && kk < keyNames.length
						&& keyNames[kk] != null) {
					if (kk != i)
						g2.setColor(Color.red);

					g2.drawString(keyNames[kk], key.x + 2, key.y
							+ noteItemHeight - 2);
				}

			}

			g2.translate(0, -(timePanelHeight - yScroll));

		}

		public Key getKey(Point point) {
			point.translate(0, -timePanelHeight + yScroll);
			for (Key key : keys) {
				if (key == null)
					continue;
				if (key.contains(point)) {
					return key;
				}
			}
			return null;
		}

	}
	static Color MY_PIANO_COLOR = new Color(0xffeeee);
	static Color MY_DRUM_COLOR  = new Color(0xeeeeff);
	static Color MY_KEYDOWN_COLOR = new Color(0xaaaaaa);
	
	class VirtualPiano implements PadPanelIF {
//
//		final Color jfcBlue = new Color(204, 204, 255);
//
//		final Color pink = new Color(255, 175, 175);

		Vector<Key> blackKeys = new Vector<Key>();

		Vector<Key> whiteKeys = new Vector<Key>();

		final int nWhiteNote = (nNote / 12) * 7 + 5;

		final int nOctave = nNote / 12 + 1;

		int whiteKeyWidth, blackKeyDepth = (int) (keyDepth * .6);

		int blackWhiteGap;

		int noteItemHeight;

		public void resizeKeys() {

			blackKeys.clear();
			whiteKeys.clear();

			noteItemHeight = Layout.getNoteItemHeight();

			// keyDepth=30;
			this.whiteKeyWidth = (noteItemHeight * 12) / 7;
			this.blackWhiteGap = whiteKeyWidth / 3;

			int whiteIDs[] = { 0, 2, 4, 5, 7, 9, 11 };

			// yBot = (((nWhiteNote +2) *
			// whiteKeyWidth)/noteItemHeight)*noteItemHeight-whiteKeyWidth;
			yBot = 129 * noteItemHeight - whiteKeyWidth;

			// / You will be the size I want . . . .
			// setSize(new Dimension(keyDepth, yBot));
			// // setMinimumSize(new Dimension(keyDepth, yBot));
			// setPreferredSize(new Dimension(keyDepth, yBot));
			// setMaximumSize(new Dimension(keyDepth, 20000));
			for (int i = 0, y = 0; i < nOctave; i++) {
				for (int j = 0; j < 7; j++, y += whiteKeyWidth) {
					int keyNum = i * 12 + whiteIDs[j];
					if (keyNum >= nNote)
						break;
					whiteKeys.add(new Key(0, yBot - y, keyDepth, whiteKeyWidth,
							keyNum));
				}
			}

			int halfGap = blackWhiteGap / 2;
			int yBot1 = yBot + 7 * whiteKeyWidth / 8;
			int blackKeyWidth = 2 * whiteKeyWidth / 3;
			for (int i = 0, y = 0; i < nOctave; i++, y += whiteKeyWidth) {
				int keyNum = i * 12;

				if (keyNum >= nNote - 1)
					break;
				blackKeys.add(new Key(0, yBot1 - (y += whiteKeyWidth) - halfGap
						/ 2, blackKeyDepth, blackKeyWidth, keyNum + 1));
				if (keyNum >= nNote - 3)
					break;
				blackKeys
						.add(new Key(0, yBot1 - (y += whiteKeyWidth) - 3
								* halfGap / 2, blackKeyDepth, blackKeyWidth,
								keyNum + 3));
				if (keyNum >= nNote - 6)
					break;
				y += whiteKeyWidth;
				blackKeys.add(new Key(0, yBot1 - (y += whiteKeyWidth) - halfGap
						/ 2 + 1, blackKeyDepth, blackKeyWidth, keyNum + 6));
				if (keyNum >= nNote - 8)
					break;
				blackKeys.add(new Key(0,
						yBot1 - (y += whiteKeyWidth) - halfGap, blackKeyDepth,
						blackKeyWidth, keyNum + 8));
				if (keyNum >= nNote - 10)
					break;
				blackKeys.add(new Key(0, yBot1 - (y += whiteKeyWidth) - 3
						* halfGap / 2 - 1, blackKeyDepth, blackKeyWidth,
						keyNum + 10));

			}
			for (Key key : blackKeys) {
				keys[key.kNum] = key;
			}
			for (Key key : whiteKeys) {
				keys[key.kNum] = key;
			}
			// validate();

		}
	
		
		public void paintComponent(Graphics g) {

			if (noteItemHeight != Layout.getNoteItemHeight())
				// || (config != null && noteItemHeight != config.noteHeight))
				resizeKeys();

			Graphics2D g2 = (Graphics2D) g;

			g2.translate(0, timePanelHeight - yScroll);

			g2.setColor(MY_PIANO_COLOR);
			g2.fillRect(0, 0, keyDepth, yBot);

			for (int i = 0; i < whiteKeys.size(); i++) {
				Key key = (Key) whiteKeys.get(i);
				if (key.isNoteOn()) {
					// || (config != null && key.kNum == lastKeyPress)) {
					g2.setColor(MY_KEYDOWN_COLOR);
					g2.fill(key);

				}
				g2.setColor(Color.black);

				g2.draw(key);

				if (key.kNum % 12 == 0) {
					// g2.setColor(Color.red);
					//
					// g2.fill(key);

					g2.setColor(Color.BLACK);
					g2.drawString(String.valueOf(key.kNum / 12), keyDepth - 15,
							key.y + key.height - 1);

					// g2.setPaintMode();
				}
			}
			for (int i = 0; i < blackKeys.size(); i++) {
				Key key = (Key) blackKeys.get(i);
				if (key.isNoteOn()) {
					// || (config != null && key.kNum == lastKeyPress)) {
					g2.setColor(MY_KEYDOWN_COLOR);
					g2.fill(key);
					g2.setColor(Color.black);
					g2.draw(key);
				} else {
					g2.setColor(Color.black);
					g2.fill(key);
				}

			}

			g2.translate(0, -(timePanelHeight - yScroll));
			// Thread.yield();

		}

		public Key getKey(Point point) {
			point.translate(0, -timePanelHeight + yScroll);
			for (Key key : blackKeys) {
				if (key == null)
					continue;
				if (key.contains(point)) {
					return key;
				}
			}
			for (Key key : whiteKeys) {
				if (key == null)
					continue;
				if (key.contains(point)) {
					return key;
				}
			}

			return null;
		}

	}

	
	
	public void selectionChanged(SelectionContainer<? extends Part> src) {
		
		Part focus = pianoRoll.getProjectContainer().getPartSelection().getFocus();
		
		if (focus == null)
			return;
		if (!(focus instanceof MidiPart))
			return;

	//	System.out.println("PadPanel  focus change ");

		midiLane = (MidiLane) focus.getLane();

		if (midiLane.isDrumLane()) {
			if (padIF != drumPad) {
				padIF=drumPad;
				padIF.resizeKeys();
			}
		} else {
			if (padIF != pianoPad) {
				padIF=pianoPad;
				padIF.resizeKeys();
			}
		}

		validate();
		repaint();

	}

}
