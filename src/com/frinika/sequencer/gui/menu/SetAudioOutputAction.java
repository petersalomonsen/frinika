/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
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
package com.frinika.sequencer.gui.menu;

import com.frinika.audio.toot.AudioInjector;
import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.IOAudioProcess;


import com.frinika.project.FrinikaAudioSystem;
import com.frinika.project.gui.ProjectFrame;

public class SetAudioOutputAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ProjectFrame project;
	
	public SetAudioOutputAction(ProjectFrame project) {
		super(getMessage("project.menu.settings.set_audio_output"));
	
		this.project=project;		
	}
	
	public void actionPerformed(ActionEvent arg0) {

		AudioServer audioServer = FrinikaAudioSystem.getAudioServer();
	
		List<String> list = audioServer.getAvailableOutputNames();

		if (!list.isEmpty()) {

			Object a[] = new Object[list.size()];
			a = list.toArray(a);

			Object selectedValue = JOptionPane.showInputDialog(null,
					getMessage("setup.select_audio_output"), "Output",
					JOptionPane.INFORMATION_MESSAGE, null, a, a[0]);

			try {
				IOAudioProcess out=audioServer.openAudioOutput((String) selectedValue, "output");
				if (out == null) return;
				AudioInjector injector=project.getProjectContainer().getOutputProcess();
				injector.setOutputProcess(out);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			System.err.println(" No output devices found ");
		}

				
	}	
}
