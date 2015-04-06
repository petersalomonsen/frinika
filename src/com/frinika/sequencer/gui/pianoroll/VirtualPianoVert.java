/*
 * Created on Jan 18, 2006
 *
 * Based on Sun code see notice below.
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
 *
 */

/*
 * @(#)MidiSynth.java	1.15	99/12/03
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package com.frinika.sequencer.gui.pianoroll;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.sound.midi.ShortMessage;
import javax.swing.JPanel;

import javax.sound.midi.Receiver;

import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.Part;

public class VirtualPianoVert extends JPanel implements MouseListener,
		AdjustmentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final int ON = 0, OFF = 1;

	final Color jfcBlue = new Color(204, 204, 255);

	final Color pink = new Color(255, 175, 175);

	Vector<Key> blackKeys = new Vector<Key>();

	Key keys[] = new Key[128];

	Vector<Key> whiteKeys = new Vector<Key>();

	Key prevKey;

	int lastKeyPress = 0;

	final int nNote = 128;

	final int nWhiteNote = (nNote / 12) * 7 + 5;

	final int nOctave = nNote / 12 + 1;

	int whiteKeyWidth, blackKeyDepth = 20;

	int keyDepth = 30;

	int blackWhiteGap;

	PianoRoll pianoRoll;

	int timePanelHeight;

	private int yScroll;

	int yBot;

	int noteItemHeight;

	public static class Config {

		public Config(int keyDepth, int noteHeight, Receiver recv, MidiLane lane) {
			this.noteHeight = noteHeight;
			this.keyDepth = keyDepth;
			this.recv = recv;
			this.lane = lane;
		}

		final int noteHeight;

		final int keyDepth;

		public Receiver recv;

		public MidiLane lane;

		public void setReceiver(Receiver recv2) {
			recv = recv2;
		}
	}

	Config config = null;

	public VirtualPianoVert(Config config) {
		super(false);
		this.config = config;
		this.pianoRoll = null;
		this.timePanelHeight = 0;
		this.yScroll = 0;

		resizeKeys();

		addMouseListener(this);
		setBackground(Color.ORANGE);
	}

	public VirtualPianoVert(PianoRoll pr, int yTop, int yScroll) {
		super(false);
		this.pianoRoll = pr;
		this.timePanelHeight = yTop;
		this.yScroll = yScroll;

		resizeKeys();

		addMouseListener(this);
		setBackground(Color.ORANGE);
	}

	// public Dimension getPreferredSize() {
	// return new Dimension(50,200);
	// }
	//	
	// public Dimension getMinimumSize() {
	// return new Dimension(50,200);
	// }

	private void resizeKeys() {

		blackKeys.clear();
		whiteKeys.clear();
		//keys.clear();

		if (config == null) {
			noteItemHeight = Layout.getNoteItemHeight();
		} else {
			noteItemHeight = config.noteHeight;
			keyDepth = config.keyDepth;
		}
		// setLayout(new BorderLayout());
		this.whiteKeyWidth = (noteItemHeight * 12) / 7;
		this.blackWhiteGap = whiteKeyWidth / 3;

		int whiteIDs[] = { 0, 2, 4, 5, 7, 9, 11 };

		// yBot = (((nWhiteNote +2) *
		// whiteKeyWidth)/noteItemHeight)*noteItemHeight-whiteKeyWidth;
		yBot = 129 * noteItemHeight - whiteKeyWidth;
		
		/// You will be the size I want  . . .  .
		setSize(new Dimension(keyDepth, yBot));
	//	setMinimumSize(new Dimension(keyDepth, yBot));
		setPreferredSize(new Dimension(keyDepth, yBot));
		setMaximumSize(new Dimension(keyDepth, 20000));
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
			blackKeys.add(new Key(0,
					yBot1 - (y += whiteKeyWidth) - halfGap / 2, blackKeyDepth,
					blackKeyWidth, keyNum + 1));
			if (keyNum >= nNote - 3)
				break;
			blackKeys.add(new Key(0, yBot1 - (y += whiteKeyWidth) - 3 * halfGap
					/ 2, blackKeyDepth, blackKeyWidth, keyNum + 3));
			if (keyNum >= nNote - 6)
				break;
			y += whiteKeyWidth;
			blackKeys.add(new Key(0, yBot1 - (y += whiteKeyWidth) - halfGap / 2
					+ 1, blackKeyDepth, blackKeyWidth, keyNum + 6));
			if (keyNum >= nNote - 8)
				break;
			blackKeys.add(new Key(0, yBot1 - (y += whiteKeyWidth) - halfGap,
					blackKeyDepth, blackKeyWidth, keyNum + 8));
			if (keyNum >= nNote - 10)
				break;
			blackKeys.add(new Key(0, yBot1 - (y += whiteKeyWidth) - 3 * halfGap
					/ 2 - 1, blackKeyDepth, blackKeyWidth, keyNum + 10));

		}
		for (Key key:blackKeys) {
			keys[key.kNum]=key;
		}
		for (Key key:whiteKeys) {
			keys[key.kNum]=key;
		}

	}

	public void mousePressed(MouseEvent e) {
		prevKey = getKey(e.getPoint());
		if (prevKey != null) {
			prevKey.on();
			repaint();
		}
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

	public Key getKey(Point point) {
		point.translate(0, -timePanelHeight + yScroll);
		for (Key key:blackKeys) {
			if (key == null) continue;
			if (key.contains(point)) {
				return key;
			}
		}
		for (Key key:whiteKeys) {
			if (key == null) continue;
			if (key.contains(point)) {
				return key;
			}
		}

		return null;
	}

	public void paintComponent(Graphics g) {
		//Thread.yield();
		super.paintComponent(g);
		//Thread.yield();

		if ((config == null && noteItemHeight != Layout.getNoteItemHeight())
				|| (config != null && noteItemHeight != config.noteHeight))
			resizeKeys();

		Graphics2D g2 = (Graphics2D) g;

		g2.translate(0, timePanelHeight - yScroll);

		g2.setColor(Color.PINK);
		g2.fillRect(0, 0, keyDepth, yBot);

		for (int i = 0; i < whiteKeys.size(); i++) {
			Key key = (Key) whiteKeys.get(i);
			if (key.isNoteOn() || (config != null && key.kNum == lastKeyPress) ) {
				g2.setColor(jfcBlue);
				g2.fill(key);

			}
			g2.setColor(Color.black);

			g2.draw(key);

			if (key.kNum % 12 == 0) {
//				g2.setColor(Color.red);
//
//				g2.fill(key);

				g2.setColor(Color.BLACK);
				g2.drawString(String.valueOf(key.kNum / 12), keyDepth-15, key.y + key.height-1);
					
				// g2.setPaintMode();
			}
		}
		for (int i = 0; i < blackKeys.size(); i++) {
			Key key = (Key) blackKeys.get(i);
			if (key.isNoteOn()|| (config != null && key.kNum == lastKeyPress) ) {
				g2.setColor(jfcBlue);
				g2.fill(key);
				g2.setColor(Color.black);
				g2.draw(key);
			} else {
				g2.setColor(Color.black);
				g2.fill(key);
			}

		}


		g2.translate(0, -(timePanelHeight - yScroll));
		//Thread.yield();

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
			if (config == null) {
				if (pianoRoll == null)
					return;
				Part focusPart = pianoRoll.getProjectContainer()
						.getPartSelection().getFocus();
				if (focusPart == null || !(focusPart instanceof MidiPart ))
					return;
				MidiLane lane = ((MidiLane) (focusPart.getLane()));
				recv = lane.getReceiver();
				chan = lane.getMidiChannel();
			} else {
				recv = config.recv;
				chan = config.lane.getMidiChannel();
			}

			if (recv == null)
				return;

			ShortMessage shm = new ShortMessage();
			try {
				shm.setMessage(ShortMessage.NOTE_ON, chan, kNum, 100);
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// try {
			recv.send(shm, -1);
			// } catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

		public void off() {
			setNoteState(OFF);
			// if (pianoRoll == null)
			// return;

			if (recv == null)
				return;
			ShortMessage shm = new ShortMessage();

			try {
				shm.setMessage(ShortMessage.NOTE_ON, chan, kNum, 0);
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// try {
			recv.send(shm, -1);
			// } catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// }
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
}
