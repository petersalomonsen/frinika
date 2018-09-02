/*
 * Created on Mar 16, 2006
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

import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.sound.midi.Sequence;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author P.J. Leonard
 * @author Peter Salomonsen
 */
public class NoteLengthPopup extends JPopupMenu implements ActionListener {

    private static final long serialVersionUID = 1L;

    String[] notes = {"bar", "1", "1/2", "1/3", "1/4", "1/6", "1/8", "1/16"};

    static Icon[] icons = {
        AbstractProjectContainer.getIconResource("note_1.png"), AbstractProjectContainer.getIconResource("note_4.png"), AbstractProjectContainer.getIconResource("note_8.png"), AbstractProjectContainer.getIconResource("note_12.png"), AbstractProjectContainer.getIconResource("note_16.png"), AbstractProjectContainer.getIconResource("note_24.png"), AbstractProjectContainer.getIconResource("note_32.png"), AbstractProjectContainer.getIconResource("note_64.png"), // 1/16 note (64th note)  [1/64]
    };

    int numA[] = {-1, 1, 1, 1, 1, 1, 1, 1};

    int denA[] = {1, 1, 2, 3, 4, 6, 8, 16};

    public int den = 1;

    public int num = 1;

    List<Snapable> clients;
    Sequence sequence;
    JButton button;

    public static void updateButton(JButton button, double snap) {
        int index = -1;
        if (Math.abs(snap - 4.0) < 0.01 || snap < 0.0) {
            index = 0;
        }
        if (Math.abs(snap - 1.0) < 0.01) {
            index = 1;
        }
        if (Math.abs(snap - 1.0 / 2.0) < 0.01) {
            index = 2;
        }
        if (Math.abs(snap - 1.0 / 3.0) < 0.01) {
            index = 3;
        }
        if (Math.abs(snap - 1.0 / 4.0) < 0.01) {
            index = 4;
        }
        if (Math.abs(snap - 1.0 / 6.0) < 0.01) {
            index = 5;
        }
        if (Math.abs(snap - 1.0 / 8.0) < 0.01) {
            index = 6;
        }

        if (index != -1) {
            button.setIcon(icons[index]);
        }
    }

    public static void updateButton(JButton button, List<Snapable> clients, Sequence sequence) {
        for (Snapable client : clients) {
            updateButton(button, client.getSnapQuantization() / sequence.getResolution());
        }
    }

    NoteLengthPopup(JButton button, List<Snapable> clients,
            Sequence sequence) {

        this.button = button;
        this.sequence = sequence;
        this.clients = clients;

        updateButton(button, clients, sequence);

        int i = 0;
        for (String str : notes) {

            JMenuItem item = new JMenuItem(str, icons[i++]);
            item.addActionListener(this);
            add(item);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < notes.length; i++) {
            if (e.getActionCommand().equals(notes[i])) {
                double quant = sequence.getResolution();
                quant = (quant * numA[i]) / denA[i];
                updateButton(button, quant / sequence.getResolution());
                for (Snapable client : clients) {
                    client.setSnapQuantization(quant);
                }
                return;
            }
        }
    }
}
