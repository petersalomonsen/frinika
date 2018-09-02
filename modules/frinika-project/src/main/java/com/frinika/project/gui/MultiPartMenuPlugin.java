/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frinika.project.gui;

import com.frinika.project.FrinikaProjectContainer;
import com.frinika.project.MultiPart;
import com.frinika.sequencer.model.MenuPlugable;
import com.frinika.sequencer.model.MenuPlugin;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.Part;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

public class MultiPartMenuPlugin implements MenuPlugin {

    private FrinikaProjectContainer project;

    public MultiPartMenuPlugin(FrinikaProjectContainer project) {
        this.project = project;
    }

    @SuppressWarnings("serial")
    @Override
    public void initContextMenu(JPopupMenu popup, final MenuPlugable obj) {
        if ((obj instanceof MidiPart)) {
            final MidiPart mp = (MidiPart) obj;

            final MultiPart multi = mp.getMultiPart();
            if (multi != null) {

                JMenu subMenu = new JMenu("Takes");

                int take = 0;

                MidiLane lane = (MidiLane) mp.getLane();

                boolean isDrum = lane.isDrumLane();

                ButtonGroup grp = null;
                if (!isDrum) {
                    grp = new ButtonGroup();
                }

                for (final Part part : multi.getParts()) {
                    take++;

                    if (isDrum) {
                        final JCheckBoxMenuItem item = new JCheckBoxMenuItem();

                        item.setSelected(part.isAttached()
                        );

                        item.setAction(new AbstractAction(" take " + take) {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                project.getEditHistoryContainer().mark("X");
                                if (item.isSelected()) {
                                    part.addToModel();
                                } else {
                                    part.removeFromModel();
                                }
                                project.getEditHistoryContainer().notifyEditHistoryListeners();
                            }
                        });
                        subMenu.add(item);
                    } else {
                        final JRadioButtonMenuItem item = new JRadioButtonMenuItem(" take " + take, part.isAttached());

                        grp.add(item);

                        System.out.println(part + " " + take);
                        item.setAction(new AbstractAction(" take " + take) {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                project.getEditHistoryContainer().mark("X");
                                System.out.println(part + " " + item.isSelected());

                                for (Part partX : multi.getParts()) {
                                    System.out.println(partX + " --> " + partX.isAttached());
                                    if (partX != part) {
                                        if (partX.isAttached()) {
                                            partX.removeFromModel();
                                        }
                                    } else {
                                        if (!partX.isAttached()) {
                                            partX.addToModel();
                                        }
                                    }
                                }
                                project.getEditHistoryContainer().notifyEditHistoryListeners();
                            }
                        });
                        subMenu.add(item);
                    }
                }

                popup.add(subMenu);
            }
        }
    }
}
