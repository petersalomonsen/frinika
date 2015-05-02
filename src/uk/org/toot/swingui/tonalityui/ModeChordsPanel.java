// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.tonalityui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

import uk.org.toot.music.tonality.*;

public class ModeChordsPanel extends JPanel 
{
	private Scale scale;
	private int degrees = 0;
	
	public ModeChordsPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
	
	public void setScale(Scale scale) {
		int d = scale.length();
		if ( d > degrees) {
			for ( int i = degrees; i < d; i++) {
//				System.out.println("Adding degree "+(1+i));
				add(new DegreeChordsView(i));
			}
		} else if ( d < degrees ) {
			for ( int i = degrees-1; i >= d; i--) {
//				System.out.println("Removing degree "+(1+i));
				this.remove(i);
			}
		}
		degrees = d;
		this.scale = scale;
		
		for ( int i = 0; i < getComponentCount(); i++) {
			Component comp = getComponent(i);
			if ( comp instanceof DegreeChordsView ) {
				((DegreeChordsView)comp).updateChords();
			}
		}
	}
	
	class DegreeChordsView extends JPanel
	{
		private ChordList chordList;
		private int degree;
		private JLabel label;
		
		public DegreeChordsView(int degree) {
			setLayout(new BorderLayout());
			label = new JLabel(String.valueOf(degree+1));
			add(label, BorderLayout.NORTH);
			chordList = new ChordList();
			add(chordList, BorderLayout.CENTER);
			this.degree = degree;
		}
		
		public void updateChords() {
			int[] chordMode = scale.getChordMode(degree);
			label.setToolTipText(Interval.spell(chordMode));
			List<Chord> chords = Chords.fromChordMode(chordMode);
			chordList.setChords(chords);			
		}
	}
}
