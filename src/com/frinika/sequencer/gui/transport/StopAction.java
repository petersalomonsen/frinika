package com.frinika.sequencer.gui.transport;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequencer;

public class StopAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FrinikaSequencer sequencer;
	private ProjectContainer project;
	
	public StopAction(ProjectFrame project) {
		super(getMessage("sequencer.project.start_stop"));
		this.sequencer=project.getProjectContainer().getSequencer();
		this.project=project.getProjectContainer();
		
	//	putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(
	//			KeyEvent.VK_SPACE,0));
	}
	
	public void actionPerformed(ActionEvent arg0) {
		
		
		if (sequencer.isRunning()) {
			  boolean recording = sequencer.isRecording();
		
			sequencer.stop();
		
			if(recording)
        
            if(sequencer.getNumberOfTakes() == 1)
            {
                project.getEditHistoryContainer().mark(getMessage("recording"));
                sequencer.deployTake(new int[] {sequencer.getNumberOfTakes()-1});
                project.getEditHistoryContainer().notifyEditHistoryListeners();
            }
        }
	}
	
}
