/*
 * Created on Sep 28, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth.importers.soundfont;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.*;

/**
 * @author peter
 *
 */
public class SoundFontImporterGUI extends JFrame {
	static File lastSelectedPath;
	static File previousFolder = null;
	
	SoundFontImporter sfi;
	private JComboBox cb = null;
	
	public SoundFontImporterGUI(SoundFontImporter sfi)
	{
		super();
		setLayout(new FlowLayout());
		this.sfi = sfi;
		initialize();

	}
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		final JButton but = new JButton("Open soundfont");
		but.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					try
					{
						JFileChooser chooser = new JFileChooser();
						chooser.setDialogTitle("Open soundfont");
						chooser.setFileFilter(new SoundFontFileFilter());
						if(SoundFontImporterGUI.lastSelectedPath !=null )
							chooser.setSelectedFile(SoundFontImporterGUI.lastSelectedPath);
						if(chooser.showOpenDialog(SoundFontImporterGUI.this)==
							JFileChooser.APPROVE_OPTION) {
							File soundFontFile = chooser.getSelectedFile();
							SoundFontImporterGUI.lastSelectedPath = soundFontFile;
							sfi.getSoundFont(soundFontFile);
							updateInstrumentList();
							pack();
							repaint();
						};
					} catch(Exception ex) { ex.printStackTrace(); }
				}		    
		    });
		add(but);
		updateInstrumentList();
        setSize(100,200);
        pack();
        setVisible(true);
	}
	
    public static void getMissingSoundFont(File file, SoundFontImporter sfi) throws Exception
    {
	if(previousFolder!=null) {
	    file = new File(previousFolder,file.getName());
	}
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("A soundfont is missing");
        chooser.setFileFilter(new SoundFontFileFilter());
        chooser.setSelectedFile(file);
        
        if(chooser.showOpenDialog(null)==
            JFileChooser.APPROVE_OPTION) {
            File soundFontFile = chooser.getSelectedFile();
            sfi.getSoundFont(soundFontFile);
	    previousFolder = soundFontFile.getParentFile();
        }
    }
        
	void updateInstrumentList()
	{
		if(sfi.inst != null)
		{
			if(cb!=null)
				remove(cb);
			cb = new JComboBox();
			cb.addItem("");
			for(String name : sfi.getInstrumentNames())
				cb.addItem(name);
			cb.addItemListener(new ItemListener() {
	
				public void itemStateChanged(ItemEvent e) {
					try
					{
						sfi.getInstrument(cb.getSelectedIndex()-1);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}});
			add(cb);
		}
	}
}
