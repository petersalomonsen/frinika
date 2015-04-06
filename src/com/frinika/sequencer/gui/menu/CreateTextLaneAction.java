/*
 * Created on Feb 1, 2007
 *
 * Copyright (c) 2006-2007 Jens Gulden
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

import static com.frinika.localization.CurrentLocale.getMessage;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.TextLane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Menu-action for creating a new text-lane.
 *
 * @see com.frinika.sequencer.model.TextLane
 * @author Jens Gulden
 */
public class CreateTextLaneAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ProjectFrame project;
	
	public CreateTextLaneAction(ProjectFrame project) {
		super(getMessage("sequencer.project.add_text_lane"));
		this.project=project;		
	}
	public void actionPerformed(ActionEvent arg0) {
		project.getProjectContainer().getEditHistoryContainer().mark(getMessage("sequencer.project.add_text_lane"));
		TextLane lane = project.getProjectContainer().createTextLane();
		project.getProjectContainer().getEditHistoryContainer().notifyEditHistoryListeners();
		//project.getProjectContainer().getLaneSelection().setSelected(lane);			
		//project.getProjectContainer().getLaneSelection().notifyListeners();
	}
}
