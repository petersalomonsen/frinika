package com.frinika.sequencer.gui.transport;

import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.gui.RecordingDialog;
import com.frinika.sequencer.project.SequencerProjectContainer;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class RecordAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private FrinikaSequencer sequencer;
    private SequencerProjectContainer project;

    public RecordAction(SequencerProjectContainer project) {
        super(CurrentLocale.getMessage("sequencer.project.record"));
        this.sequencer = project.getSequencer();
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        final JOptionPane recordingOptionPane = new JOptionPane(CurrentLocale.getMessage("sequencer.recording.takes"));

        //Frame to pop-up while recording to display each take
        final RecordingDialog recordingDialog = new RecordingDialog(recordingOptionPane, sequencer);

        recordingOptionPane.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String prop = evt.getPropertyName();

                if (recordingDialog.isVisible()
                        && (evt.getSource() == recordingOptionPane)
                        && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                    int[] deployableTakes = recordingDialog.getDeployableTakes();
                    if (deployableTakes.length > 0) {
                        project.getEditHistoryContainer().mark(CurrentLocale.getMessage("recording"));
                        sequencer.deployTake(deployableTakes);
                        project.getEditHistoryContainer().notifyEditHistoryListeners();
                    }
                    sequencer.stop();
                }
            }
        });

        sequencer.startRecording();
    }
}
