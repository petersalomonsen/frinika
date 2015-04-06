package com.frinika.sequencer.gui.transport;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequencer;

public class StartAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FrinikaSequencer sequencer;
	private ProjectContainer project;
	
	public StartAction(ProjectFrame project) {
		super(getMessage("sequencer.project.start_stop"));
		this.sequencer=project.getProjectContainer().getSequencer();
		this.project=project.getProjectContainer();

	}
	
	public void actionPerformed(ActionEvent arg0) {
			
		if (!sequencer.isRunning()) sequencer.start();	
	}
	
}
