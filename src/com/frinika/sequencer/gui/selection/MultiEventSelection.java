package com.frinika.sequencer.gui.selection;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.Part;

public class MultiEventSelection extends SelectionContainer<MultiEvent>{

	
	
	public MultiEventSelection(ProjectContainer project){
		super(project);
	}
	protected void setMetaFocus() {
		MultiEvent ev=(MultiEvent)focus;
		Part part=ev.getPart();
		project.getPartSelection().setFocus(part);		
	}


}
