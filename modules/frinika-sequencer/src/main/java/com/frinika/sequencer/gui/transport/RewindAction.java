package com.frinika.sequencer.gui.transport;

import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class RewindAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private FrinikaSequencer sequencer;

    public RewindAction(AbstractProjectContainer project) {
        super(CurrentLocale.getMessage("sequencer.project.rewind"));
        this.sequencer = project.getSequencer();
        //	putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(
        //			KeyEvent.VK_SPACE,0));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        sequencer.setTickPosition(0);
    }
}
