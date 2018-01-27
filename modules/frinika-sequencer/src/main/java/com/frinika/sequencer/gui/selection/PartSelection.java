package com.frinika.sequencer.gui.selection;

import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.project.SequencerProjectContainer;

/**
 *
 * @author Paul
 */
public class PartSelection extends SelectionContainer<Part> {

    public PartSelection(SequencerProjectContainer project) {
        super(project);

    }

    @Override
    protected void setMetaFocus() {
        Part part = (Part) focus;
        Lane lane = part.getLane();
        project.getLaneSelection().setFocus(lane);
    }
}
