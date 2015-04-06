/*
 * Created on 17 Nov 2007
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

package com.frinika.chart;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Observable;
import java.util.StringTokenizer;
import java.util.Vector;

import uk.org.toot.music.tonality.Key;
import uk.org.toot.music.tonality.Keys;
import uk.org.toot.music.tonality.Pitch;
import uk.org.toot.music.tonality.Scale;
import uk.org.toot.music.tonality.Scales;

public class Chart extends Observable implements Serializable {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private Vector<Bar> bars;

	private int beatsPerBar=4;   // TODO look at time sig
	private String keyRoot;
	private String scale;
	
	public Chart() {
		bars = new Vector<Bar>();
	}


	public void appendBar() {
		bars.add(new Bar(beatsPerBar));	
	}

	public void appendBar(String string, int beats) {
		Bar bar = new Bar(beats);
		bars.add(bar);
		bar.set(string,keyRoot,scale);
		setChanged();
	}
	
	public void appendBar(String string,String keyRoot,String scale, int beats) {
		Bar bar = new Bar(beats);
		bars.add(bar);
		bar.set(string,keyRoot,scale);
		setChanged();
	}

	
	
	public List<Bar> getBars() {
		return bars;
	}

	public class Bar implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Vector<Chord> chords;

		int beats;

		private Bar(int beats) {
			chords = new Vector<Chord>();
			this.beats = beats;
		}

		private void addChord(String string,String keyRoot,String scaleName) {
			string=string.trim();
			if (!string.equals("/")) {
				chords.add(new Chord(string,keyRoot,scaleName));
			}else {
				chords.add(new Chord(chords.lastElement()));
			}
		}

		public int getBeats() {
			return beats;
		}

		public List<Chord> getChords() {
			return chords;
		}

		private void spaceChords() {
			int n = chords.size();

			if (n == 1)
				chords.get(0).setDuration(beats);
			else if (n == beats) {
				for (Chord chord : chords)
					chord.setDuration(1);

			} else {
				int inc=beats/chords.size();
				int bb=0;
				for (Chord chord : chords)
					chord.setDuration(bb+inc);
				
				
				try {
					throw new Exception(" chords do not fit the bar");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		
		public String toString() {

			StringBuffer str=new StringBuffer();

			for (Chord chord:chords) {
				str.append(chord.toString()+" ");
			}
			return str.toString();
		}

		private void set(String string,String keyRoot,String scale) {
			chords.clear();
			StringTokenizer toker = new StringTokenizer(string);
			while (toker.hasMoreTokens()) {
				addChord(toker.nextToken(),keyRoot,scale);
			}
			spaceChords();
			setChanged();
		}

	}

	public class Chord 	implements Serializable {
		String name;
		private static final long serialVersionUID = 1L;
		transient uk.org.toot.music.tonality.Chord chord;
		transient int root;
		transient Key key;
		
		int beats;
		String scaleName;
		String scaleRoot;
		int [] chordInt;
		
		private Chord(Chord cloneMe) {
			this.name = cloneMe.name;
			this.root=cloneMe.root;
			this.beats=cloneMe.beats;
			this.chord=cloneMe.chord;
			this.chordInt=cloneMe.chordInt;
			this.scaleName=cloneMe.scaleName;
			this.scaleRoot=cloneMe.scaleRoot;
			this.key=cloneMe.key;
		}
		
		private Chord(String name,String keyRoot,String scaleName) {
			this.name=name;
			this.scaleName=scaleName;
			
			Scale scale=Scales.getScale(scaleName);
			int kr=Pitch.classValue(keyRoot);
			key=new Key(kr,scale);
			buildFromName();
		}
		
		private void buildFromName() {

			root = Pitch.classValue(name);
			
			
			String string=name;
			
			if (string.length() > 1) {
				char c = string.charAt(1);
				if (c == 'b' || c == '#') {
					string = string.substring(2, string.length());
				} else {
					string = string.substring(1, string.length());
				}
			} else {
				string = string.substring(1, string.length());
			}
			chord = uk.org.toot.music.tonality.Chords.withSymbol(string);
			assert(chord != null);
//			chordInt=chord.getIntervals();
//			assert(chordInt != null);
		}

		 private void readObject(java.io.ObjectInputStream in)
		 	throws IOException, ClassNotFoundException {
			 in.defaultReadObject();
			 buildFromName();
		}
		 
		 
		private void setDuration(int beats) {
			this.beats = beats;
		}

		public int getDuration() {
			return beats;
		}

		public int getRoot() {
			return root;
		}
		
		public String toString() {
			return name;
		}
		
		public int getChordNoteAt(int i) {
			return root+chord.getIntervals()[i%chordInt.length];
		}

		public Key getKey() {
			return key;
		}

	}

	public void setbarAt(int index, String string) {
		bars.get(index).set(string,keyRoot,scale);	
		setChanged();
		notifyObservers();
	}
	
	public void setbarAt(int index, String string,String keyRoot,String scale) {
		bars.get(index).set(string,keyRoot,scale);	
	}

	
	public String toString() {
		StringBuffer buff=new StringBuffer();
		for(Bar bar:bars) {
			buff.append(bar.toString());
			buff.append("|");
		}
		return buff.toString();		
	}


	public void setDefaultKey(String keyRoot, String scale) {
		this.keyRoot=keyRoot;
		this.scale=scale;
		
	}


}
