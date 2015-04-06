package com.frinika.project.gui;

import java.awt.event.KeyEvent;


public class SelectAllAction  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ProjectFrame project;
	
	public SelectAllAction(ProjectFrame project) {
		this.project=project;		
	}
	
	public boolean selectAll(KeyEvent e) {
		if (project.trackerPanel.getTable().hasFocus())
		{
			project.trackerPanel.getTable().selectAll();
		}
		else
		if (project.partViewEditor.getPartview().getMousePosition() != null) {
			project.partViewEditor.getPartview().selectAll();
			
		} else if (project.pianoControllerPane.getPianoRoll().getMousePosition() != null) { 
			
			project.pianoControllerPane.getPianoRoll().selectAll();
		} else if (project.pianoControllerPane.getControllerView().getMousePosition() != null) { 
			project.pianoControllerPane.getControllerView().selectAll();
		} else {
			return true;
		}
		return false;
	}	
}
