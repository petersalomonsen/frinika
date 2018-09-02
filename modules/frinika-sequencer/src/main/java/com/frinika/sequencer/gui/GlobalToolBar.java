/*
 * Created on Mar 4, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.gui;

import com.frinika.gui.ToolbarSeperator;
import com.frinika.gui.util.ButtonFactory;
import com.frinika.gui.util.WindowUtils;
import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.Metronome;
import com.frinika.sequencer.TempoChangeListener;
import com.frinika.sequencer.gui.transport.TransportPanel;
import com.frinika.sequencer.model.tempo.TempoList;
import com.frinika.sequencer.model.tempo.TempoList.MyTempoEvent;
import com.frinika.sequencer.project.AbstractProjectContainer;
import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.JavaSoundVoiceServer;
import com.frinika.voiceserver.VoiceServer;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GlobalToolBar extends JToolBar {

    private static final long serialVersionUID = 1L;

    LoopPanel loopPanel;

    // ProjectFrame projectFrame;
    public GlobalToolBar(final AbstractProjectContainer project) {
        Insets insets = new Insets(0, 0, 0, 0);
        setMargin(insets);

        JPanel panel = new TransportPanel(project);
        panel.setOpaque(false);
        // panel.setMargin(insets);
        // panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"transport"));
        // panel.setBorder(BorderFactory.createEtchedBorder());
        add(panel);

        // item.setIcon();
        add(new ToolbarSeperator());

        loopPanel = new LoopPanel(project);
        loopPanel.setOpaque(false);
        // panel.setMargin(insets);
        // loopPanel.setBorder(BorderFactory.createEtchedBorder());
        add(loopPanel);
        add(new ToolbarSeperator());
        AudioContext context = AudioContext.getDefaultAudioContext();
        if (context != null) {
            VoiceServer vs = context.getVoiceServer();
            if (vs != null && vs instanceof JavaSoundVoiceServer) {
                ((JavaSoundVoiceServer) vs).cpuMeter.setBorder(BorderFactory
                        .createEtchedBorder());
                add(((JavaSoundVoiceServer) vs).cpuMeter);
            }
        }

        panel = new JPanel();
        panel.setOpaque(false);
        add(panel);

        /*
		 * final JTextField tempoTextField = new JTextField("" + (int)
		 * projectFrame.getProjectContainer().getSequencer().getTempoInBPM());
		 * tempoTextField.setColumns(5); tempoTextField.addFocusListener(new
		 * FocusAdapter() { public void focusLost(FocusEvent e) { float tempo =
		 * projectFrame.getProjectContainer().getTempo(); try { tempo =
		 * Float.parseFloat(tempoTextField.getText());
		 * projectFrame.getProjectContainer().setTempoInBPM(tempo); } catch
		 * (Exception ex) { tempoTextField.setText(tempo + ""); } } });
         */
        // TODO should be a float
        int bpm = (int) project.getSequencer()
                .getTempoInBPM();
        final JSpinner tempoSpinner = new JSpinnerDraggable(
                new SpinnerNumberModel((double) bpm, 0d, 999d, 1d));
        tempoSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                long tickNow = project.getSequencer().getTickPosition();

                float tempo = (((Double) tempoSpinner.getValue()).floatValue());
                TempoList list = project.getTempoList();
                MyTempoEvent ev = list.getTempoEventAt(tickNow);
                tickNow = ev.getTick();
                list.remove(ev.getTick(), ev.getTick() + 1);
                list.add(tickNow, tempo);
                list.notifyListeners();
                project.getSequencer().setTempoInBPM(tempo);
            }
        });

        project.getSequencer()
                .addTempoChangeListener(new TempoChangeListener() {

                    @Override
                    public void notifyTempoChange(float bpm) {
                        // System.out.println(" TEMPO CHANGE "+ bpm);
                        tempoSpinner.setValue((double) bpm);
                    }

                });

        panel.add(new JLabel("BPM"));
        // panel.add(tempoTextField);
        panel.add(tempoSpinner);
        add(panel);

        panel = new JPanel();
        panel.setOpaque(false);
        final JSlider metronomeSlider = new JSlider(0, 127, 0);
        metronomeSlider.setOpaque(false);
        try {
            // ------------------------- Choose midi based or sample based
            // metronome ----------------
            // Midi based metronome
            final Metronome metronome = new Metronome(project);
            project.getSequencer().addSongPositionListener(metronome);

            // new SwingSongPositionListenerWrapper(metronome));
            // Sample based metronome
            // final SampleBasedMetronome metronome=new
            // SampleBasedMetronome(project);
            // ---------------------------------------------------------------------------------------
            project.injectIntoOutput(metronome);

            metronomeSlider.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    metronome.setVelocity(metronomeSlider.getValue());
                }
            });
            panel.add(new JLabel(new javax.swing.ImageIcon(GlobalToolBar.class.getResource("/icons/" + (WindowUtils.isDarkMode() ? "metronome20-dark.png" : "metronome20.png")))));
            panel.add(metronomeSlider);
            add(panel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ActionListener act = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FrinikaSequencer sequencer = project.getSequencer();
                sequencer.panic();
            }

        };
        JButton reset = ButtonFactory.makePressButton(GlobalToolBar.class.getResource("/icons/exclamation32.png"), "reset",
                CurrentLocale.getMessage("sequencer.toolbar.reset_tip"), act, this);
        reset.setMargin(new Insets(0, 0, 0, 0));
    }

    public LoopPanel getLoopPanel() {
        return loopPanel;
    }
}
