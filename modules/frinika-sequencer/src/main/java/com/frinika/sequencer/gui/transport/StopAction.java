package com.frinika.sequencer.gui.transport;

import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class StopAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private FrinikaSequencer sequencer;
    private AbstractProjectContainer project;

    public StopAction(AbstractProjectContainer project) {
        super(CurrentLocale.getMessage("sequencer.project.start_stop"));
        this.sequencer = project.getSequencer();
        this.project = project;

        //	putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(
        //			KeyEvent.VK_SPACE,0));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        if (sequencer.isRunning()) {
            boolean recording = sequencer.isRecording();

            sequencer.stop();

            if (recording) {
                if (sequencer.getNumberOfTakes() == 1) {
                    project.getEditHistoryContainer().mark(CurrentLocale.getMessage("recording"));
                    sequencer.deployTake(new int[]{sequencer.getNumberOfTakes() - 1});
                    project.getEditHistoryContainer().notifyEditHistoryListeners();
                }
            }
        }
    }
}
