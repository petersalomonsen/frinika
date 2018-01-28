// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.tonalityui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;

import uk.org.toot.music.tonality.*;

public class ChordChooserPanel extends JPanel 
{
	private ScaleCombo scaleCombo;
	private ModeChordsPanel modeChordsView;
	
	public ChordChooserPanel() {
		build();
	}
	
	protected void build() {
		setLayout(new BorderLayout());
		scaleCombo = new ScaleCombo();
		add(scaleCombo, BorderLayout.NORTH);
		modeChordsView = new ModeChordsPanel();
		add(modeChordsView, BorderLayout.CENTER);
		
		// update scale when scaleCombo changed
		scaleCombo.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					Scale scale = Scales.getScale((String)scaleCombo.getSelectedItem());
					ChordChooserPanel.this.setScaleImpl(scale);
				}
			}
		);
		Scale scale = Scales.getScale((String)scaleCombo.getSelectedItem());
		setScaleImpl(scale);
	}
	
	public void setScale(Scale scale) {
		scaleCombo.setSelectedItem(scale);
		setScaleImpl(scale);			
	}

	protected void setScaleImpl(Scale scale) {
		modeChordsView.setScale(scale);			
	}
}
