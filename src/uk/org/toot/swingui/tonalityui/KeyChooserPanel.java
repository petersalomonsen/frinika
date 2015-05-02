// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.tonalityui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.List;

import uk.org.toot.music.tonality.Key;
import uk.org.toot.music.tonality.Keys;
import uk.org.toot.music.tonality.Pitch;

public class KeyChooserPanel extends JPanel 
{
	private NoteField noteField;
	private KeyList keyList;
	
	public KeyChooserPanel() {
		build();
	}
	
	protected void build() {
		setLayout(new BorderLayout());
		noteField = new NoteField();
		add(noteField, BorderLayout.NORTH);
		keyList = new KeyList();
		JScrollPane scrollPane = new JScrollPane(keyList);
		add(scrollPane, BorderLayout.CENTER);
		
		// update keyList when noteField changed
		noteField.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String notes = noteField.getText();
					setNotesImpl(Pitch.classValues(notes));
				}
			}
		);
	}

	public void setNotes(int[] notes) {
		noteField.setText(Pitch.classNames(notes));
		setNotesImpl(notes);
	}
	
	protected void setNotesImpl(int[] notes) {
//		System.out.println("Analysing "+PitchClass.names(notes));
		List<Key> keys = Keys.withNotes(notes);
//		System.out.println(keys.size()+" Keys match");
		keyList.setKeys(keys);
	}
}
