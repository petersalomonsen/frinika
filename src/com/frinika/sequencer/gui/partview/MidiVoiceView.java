/*
 * Created on Jun 22, 2006
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
package com.frinika.sequencer.gui.partview;

import com.frinika.global.FrinikaConfig;
import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import com.frinika.midi.DrumMapper;
import com.frinika.project.MidiDeviceDescriptor;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.project.gui.ProjectNewFileFilter;
import com.frinika.sequencer.MidiResource;
import com.frinika.sequencer.gui.JSpinnerDraggable;
import com.frinika.sequencer.gui.ListProvider;
import com.frinika.sequencer.gui.PopupClient;
import com.frinika.sequencer.gui.PopupSelectorButton;
import com.frinika.sequencer.gui.TimeFormat;
import com.frinika.sequencer.gui.TimeSelector;
import com.frinika.sequencer.gui.menu.midi.MidiQuantizeAction;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPlayOptions;
import com.frinika.sequencer.model.Quantization;
import com.frinika.sequencer.model.util.TimeUtils;
import com.frinika.sequencer.patchname.MyPatch;
import com.frinika.sequencer.patchname.Node;
import com.frinika.sequencer.patchname.PatchNameMap;

public class MidiVoiceView extends LaneView {

    final MidiResource midiResource;
    MidiDevice midiDev = null;
    int channel;
    ProjectFrame frame;
    boolean drumMapView = false;
    DrumMapper mapper = null;
    TimeUtils timeUtil;
    static HashMap<Lane, MidiQuantizeAction> quantizeDialogCache = new HashMap<Lane, MidiQuantizeAction>();

    public MidiVoiceView(MidiLane lane, ProjectFrame frame) {
        super(lane);
        this.frame = frame;
        timeUtil = frame.getProjectContainer().getTimeUtils(); // Jens
        frame.getProjectContainer().getSequencer().setPlayOptions(lane.getTrack(), lane.getPlayOptions());
        midiResource = lane.getProject().getMidiResource();
        midiDev = ((MidiLane) lane).getMidiDevice();
        if (midiDev instanceof SynthWrapper) {
            MidiDevice dev = ((SynthWrapper) midiDev).getRealDevice();
            if (dev instanceof DrumMapper) {
                drumMapView = true;
                mapper = (DrumMapper) dev;
            }
        }

        init();
    }
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private void toggleDrumMapperView() {
        drumMapView = !drumMapView;
        init();
    }

    protected void makeButtons() {

        JPanel devP = new JPanel();

        gc.gridwidth = GridBagConstraints.REMAINDER; //gc.gridwidth = 2; Jens
        gc.gridy = gc.gridx = 0;

        JComponent but = null;
        if (mapper == null || drumMapView) {
            but = createDeviceSelector();
            devP.add(but);
        } else {
            but = createDrumMapperDeviceSelector();
            devP.add(but);
        }

        gc.gridx = 0;
        //	gc.gridy++;

        if (mapper != null) {

            but = createDrumMapperChannelSelector();
            devP.add(but, gc);

            JButton targetToggle;
            if (drumMapView) {
                targetToggle = new JButton("View Target");
            } else {
                targetToggle = new JButton("View DrumMap");
            }
            targetToggle.setMargin(new Insets(0, 0, 0, 0));
            targetToggle.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    toggleDrumMapperView();
                }
            });
            devP.add(targetToggle);

        } else {
            but = createChannelSelector();
            devP.add(but, gc);

        }

        but = createPatchMapSelector();
        devP.add(but, gc);


        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; // Jens
        //	gc.gridwidth = 1; // Jens


        add(devP, gc);



        if (drumMapView) {
            MidiDevice dev = ((SynthWrapper) midiDev).getRealDevice();
            if (dev instanceof DrumMapper) {
                gc.gridx = 0;
                gc.gridy++;
                gc.gridwidth = GridBagConstraints.REMAINDER;
                JPanel panel = ((DrumMapper) dev).getGUIPanel(frame, (MidiLane) lane);
                gc.fill = GridBagConstraints.BOTH;
                gc.weighty = 1.0;
                gc.weightx = 1.0;
                add(panel, gc);
                return;
            }
        }

        /* Jens: */

        gc.insets.left = gc.insets.right = gc.insets.top = gc.insets.bottom = 5;

        final MidiPlayOptions opt = ((MidiLane) lane).getPlayOptions();
        if (opt.quantization == null) {
            opt.quantization = new Quantization(); // make sure it's there
        }

        // shift-time
        JLabel shiftedLabel = new JLabel("Shift");
        final JSpinner shifted = new JSpinnerDraggable(new SpinnerNumberModel((int) opt.shiftedTicks, -999, 999, 1));
        shifted.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                ((MidiLane) lane).getPlayOptions().shiftedTicks = (Integer) shifted.getValue();
            }
        });
        gc.gridx = 0;
        gc.gridwidth = 1; // Jens
        gc.gridy++;
        gc.anchor = GridBagConstraints.WEST;
        add(shiftedLabel, gc);
        gc.anchor = GridBagConstraints.EAST;
        gc.gridx++;
        gc.fill = GridBagConstraints.HORIZONTAL;
        add(shifted, gc);
        //gc.fill = GridBagConstraints.NONE;

        // loop-time
        JLabel loopedLabel = new JLabel("Looped");
        final TimeSelector loopedTimeSelector = new TimeSelector(opt.loopedTicks, true, frame.getProjectContainer(), TimeFormat.BEAT_TICK);
        /*ActionListener a = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        ((MidiLane)lane).getPlayOptions().loopedTicks = loopedTimeSelector.getTicks();
        try {
        frame.partViewEditor.partViewEditor.partView.repaintItems(); // little bit dirty, would be better via listener
        } catch (NullPointerException npe) {
        //nop (allow missing references during init)
        }
        }
        };
        loopedTimeSelector.addActionListener(a);
        a.actionPerformed(null); // update first time*/
        ChangeListener l = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                ((MidiLane) lane).getPlayOptions().loopedTicks = loopedTimeSelector.getTicks();
                try {
                    //frame.partViewEditor.partViewEditor.partView.repaintItems(); // TODO little bit dirty, would be better via listener
                    frame.repaintViews();
                } catch (NullPointerException npe) {
                    //nop (allow missing references during init)
                }
            }
        };
        loopedTimeSelector.addChangeListener(l);
        l.stateChanged(null); // update first time
        gc.gridx++;
        gc.gridx++;
        gc.anchor = GridBagConstraints.WEST;
        add(loopedLabel, gc);
        gc.anchor = GridBagConstraints.EAST;
        gc.gridx++;
        //gc.fill = GridBagConstraints.HORIZONTAL;
        add(loopedTimeSelector, gc);
        //gc.fill = GridBagConstraints.NONE;

        // velocity-offset
        JLabel velLabel = new JLabel("Vel. +/-");
        final JSpinner vel = new JSpinnerDraggable(new SpinnerNumberModel(opt.velocityOffset, -127, 127, 1));
        vel.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                ((MidiLane) lane).getPlayOptions().velocityOffset = (Integer) vel.getValue();
            }
        });
        gc.gridx = 0;
        gc.gridy++;
//		gc.anchor = GridBagConstraints.WEST;
        //	add(new JPanel(), gc);
        //	gc.gridx = 1;
        gc.anchor = GridBagConstraints.WEST;
        add(velLabel, gc);
        gc.gridx++;
        //gc.anchor = GridBagConstraints.EAST;
        add(vel, gc);

        // velocity-compression
        JLabel compressorLabel = new JLabel("Compress");
        String[] list = new String[20];
        int c = 100;
        float current = opt.velocityCompression;
        int selectedIndex = 10;
        float delta = 1f;
        for (int i = 0; i < list.length; i++) {
            list[i] = (c != 0) ? (c + "%") : "-";
            c -= 10;
            float compr = (((float) (100 - i * 10)) / 100f);
            float d = Math.abs(compr - current);
            if (d < delta) {
                delta = d;
                selectedIndex = i;
            }
        }
        final JComboBox compressor = new JComboBox(list);
        compressor.setSelectedIndex(selectedIndex);
        compressor.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int index = compressor.getSelectedIndex();
                float compr = (((float) (100 - index * 10)) / 100f);
                ((MidiLane) lane).getPlayOptions().velocityCompression = compr;
            }
        });
        gc.gridx++;
        gc.gridx++;
        gc.anchor = GridBagConstraints.WEST;
        add(compressorLabel, gc);
        gc.gridx++;
        gc.anchor = GridBagConstraints.EAST;
        //gc.fill = GridBagConstraints.HORIZONTAL;
        add(compressor, gc);

//		gc.gridx = 0;
//		gc.gridy++;
//		add(new JPanel(), gc);
//		gc.gridx++;
//		add(new JPanel(), gc);
//		gc.gridx++;
//		add(new JPanel(), gc);
        JLabel transposeLabel = new JLabel("Transp. +/-");
        final JSpinner transpose = new JSpinnerDraggable(new SpinnerNumberModel(opt.transpose, -127, 127, 1));
        transpose.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                MidiPlayOptions opt = ((MidiLane) lane).getPlayOptions();
                int value = (Integer) transpose.getModel().getValue();
                opt.transpose = value;
            }
        });

        //gc.fill = GridBagConstraints.NONE;

        gc.gridx = 0;
        gc.gridy++;
        gc.anchor = GridBagConstraints.WEST;
        add(transposeLabel, gc);
        gc.gridx++;
        //gc.anchor = GridBagConstraints.EAST;
        //gc.gridwidth = GridBagConstraints.REMAINDER;
        add(transpose, gc);
        gc.gridx++;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        add(new JPanel(), gc); // filler

        //JLabel quantizeLabel = new JLabel("Quantize");
        final TimeSelector quantizeIntervalTimeSelector = new TimeSelector(frame.getProjectContainer(), TimeFormat.NOTE_LENGTH, false);
        quantizeIntervalTimeSelector.setTicks(opt.quantization.interval);
        quantizeIntervalTimeSelector.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                MidiPlayOptions opt = ((MidiLane) lane).getPlayOptions();
                int t = (int) quantizeIntervalTimeSelector.getTicks();
                opt.quantization.interval = t;
            }
        });

        //final JCheckBox quantizeCheckBox = new JCheckBox("Quantize", opt.quantize);
        final JToggleButton quantizeCheckBox = new JToggleButton("Quantize", opt.quantizationActive);
        quantizeCheckBox.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                MidiPlayOptions opt = ((MidiLane) lane).getPlayOptions();
                opt.quantizationActive = quantizeCheckBox.isSelected();
                //quantizeIntervalTimeSelector.setEnabled(opt.quantize);
                //quantizeIntensitySpinner.setEnabled(opt.quantize);

            }
        });

        final JSpinner quantizeIntensitySpinner = new JSpinnerDraggable(new SpinnerNumberModel((int) (opt.quantization.intensity * 100), 0, 100, 1));
        quantizeIntensitySpinner.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                MidiPlayOptions opt = ((MidiLane) lane).getPlayOptions();
                int value = (Integer) quantizeIntensitySpinner.getModel().getValue();
                opt.quantization.intensity = (float) value / 100;
                if (value == 0) {
                    quantizeCheckBox.setSelected(false);
                }
            }
        });

        final JLabel quantizeIntensitySpinnerPercentLabel = new JLabel("%");
        //final JPanel quantizeIntensityPanel = new JPanel(new BorderLayout(3 , 0));
        //quantizeIntensityPanel.add(quantizeIntensitySpinner, BorderLayout.CENTER);
        //quantizeIntensityPanel.add(quantizeIntensitySpinnerPercentLabel, BorderLayout.EAST);
        final JButton quantizePatternButton = new JButton("<" + quantizeOptionsInfoString(opt.quantization) + ">");
        //quantizePatternButton.setRolloverEnabled(true);
        quantizePatternButton.setBorderPainted(false);
        quantizePatternButton.setContentAreaFilled(false);
        quantizePatternButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MidiQuantizeAction action = quantizeDialogCache.get(lane);
                if (action == null) {
                    action = new MidiQuantizeAction(frame); // dummy action for showing a dialog
                    quantizeDialogCache.put(lane, action);
                }
                action.q = opt.quantization; // directly modify 'our' QuantizeOptions
                action.getDialog().show();
                quantizeIntervalTimeSelector.setTicks(opt.quantization.interval);
                if (opt.quantization.intensity < 0) {
                    opt.quantization.intensity = 0; // de-quantization (negaive intensity) disabled for on-the-fly-mode
                }
                quantizeIntensitySpinner.setValue((int) (opt.quantization.intensity * 100));
                quantizePatternButton.setText("<" + quantizeOptionsInfoString(opt.quantization) + ">");
            }
        });

        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.gridy = GridBagConstraints.RELATIVE;
        //gc.anchor = GridBagConstraints.WEST;
        add(quantizeCheckBox, gc);
        gc.gridx++;
        //gc.anchor = GridBagConstraints.WEST;
        //add(quantizeIntensityPanel, gc);
        add(quantizeIntensitySpinner, gc);
        gc.gridx++;
        //gc.weightx=1.0f;
        add(quantizeIntensitySpinnerPercentLabel, gc);
        //gc.weightx=0f;
        gc.gridx++;
        gc.anchor = GridBagConstraints.WEST;
        add(quantizeIntervalTimeSelector, gc);
        gc.gridx++;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        add(quantizePatternButton, gc);
//		gc.gridwidth = GridBagConstraints.REMAINDER;

        gc.gridx = 0;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridy = GridBagConstraints.RELATIVE;
        gc.weighty = 1f;

        /* /Jens */

        boolean noPanel = midiDev == null || !(midiDev instanceof SynthWrapper);

        if (!noPanel) {
            but = createVoiceTree();
        } else {
            but = null;
        }

        if (but == null) {
            gc.weighty = 1.0;
            add(new Box.Filler(new Dimension(0, 0),
                    new Dimension(10000, 10000), new Dimension(10000, 10000)),
                    gc);
        } else {

            gc.fill = GridBagConstraints.BOTH;
            gc.weighty = 1.0;
            add(but, gc);
        }
    }

    JComponent createPatchMapSelector() {

        JButton but = new JButton(getMessage("midilane.properties.select_patchmap"));

        but.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser;

                if (FrinikaConfig.PATCHNAME_DIRECTORY == null) {
                    chooser = new JFileChooser();
                } else {
                    chooser = new JFileChooser(FrinikaConfig.PATCHNAME_DIRECTORY);
                }
                chooser.setDialogTitle(getMessage("midilane.properties.select_patchmap"));

                if (chooser.showOpenDialog(MidiVoiceView.this) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    String patchMapName = file.getAbsolutePath();
                    ((MidiLane) lane).setPatchMapName(patchMapName);
                    FrinikaConfig.PATCHNAME_DIRECTORY=chooser.getCurrentDirectory();
                    init();
                }

            }
        });

        return but;

    }

    JComponent createVoiceTree() {

        MidiDevice midiDev1 = midiDev;

        if (mapper != null) {
            midiDev1 = mapper.getDefaultMidiDevice();
        }

        MidiLane ml = ((MidiLane) lane);
        channel = ml.getMidiChannel();

        String patchMapName = ml.getPatchMapName();

        System.out.println(midiDev1);

        PatchNameMap vList;

        if (patchMapName == null) {
            vList = midiResource.getVoiceList(midiDev1, channel);
        } else {
            vList = midiResource.getVoiceList(new File(patchMapName));
        }
        if (vList == null) {
            return null;
        }
        // String name = ((MidiLane) lane).getVoiceName();

        final VoiceTree vTree = new VoiceTree(vList);
        JComponent ret = new JScrollPane(vTree);
        vTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) vTree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }

                if (node.isLeaf()) {
                    Object nodeInfo = node.getUserObject();

                    System.out.println(nodeInfo);

                    if (!(((Node) nodeInfo).getData() instanceof MyPatch)) {
                        System.err.println(" Sorry this is not a patch node ");
                        return;
                    }
                    MyPatch patch = (MyPatch) ((Node) nodeInfo).getData();
                    System.out.println(patch);
                    Receiver recv = ((MidiLane) lane).getReceiver();
                    if (recv == null) {
                        return;
                    }
                    int chan = ((MidiLane) lane).getMidiChannel();
                    if (chan < 0) {
                        return;
                    }
                    int bank = ((int) (0xff & patch.msb) << 8) + patch.lsb;

                    ShortMessage shm = new ShortMessage();
                    try {
                        shm.setMessage(ShortMessage.CONTROL_CHANGE, chan, 0,
                                patch.msb);
                        recv.send(shm, -1);
                        shm.setMessage(ShortMessage.CONTROL_CHANGE, chan, 0x20,
                                patch.lsb);
                        recv.send(shm, -1);
                        shm.setMessage(ShortMessage.PROGRAM_CHANGE, chan,
                                patch.prog, 0);
                        recv.send(shm, -1);
                    } catch (InvalidMidiDataException e1) {
                        e1.printStackTrace();
                    }

                    ((MidiLane) lane).setProgram(patch);
                    ((MidiLane) lane).setKeyNames(((Node) nodeInfo).getKeyNames());

                }
            }
        });

        MyPatch patch = ((MidiLane) lane).getProgram();
        if (patch != null) {
            vTree.select(patch);
        }

        return ret;

    }

    PopupSelectorButton createChannelSelector() {

        // Channel selector
        // ----------------------------------------------------------------------------

        String chanStr;
        channel = ((MidiLane) lane).getMidiChannel();
        Object channelHandle = null;
        if (channel > -1) {
            channelHandle = midiResource.getOutChannelList(midiDev)[channel];
        }

        if (channelHandle == null) {
            chanStr = "null";
        } else {
            chanStr = channelHandle.toString();
        }

        ListProvider resource = new ListProvider() {

            public Object[] getList() {
                return midiResource.getOutChannelList(((MidiLane) lane).getMidiDevice());
            }
        };

        PopupClient client = new PopupClient() {

            public void fireSelected(PopupSelectorButton but, Object o, int cnt) {
                ((MidiLane) lane).setMidiChannel(cnt);
                init();
                validate();
            }
        };

        PopupSelectorButton popsel = new PopupSelectorButton(resource, client, chanStr);
        popsel.setIcon(ProjectFrame.getIconResource("jack_connector.png"));
        return popsel;

    }

    PopupSelectorButton createDrumMapperChannelSelector() {

        // Channel selector
        // ----------------------------------------------------------------------------

        System.out.println(" DMCS create ");

        MidiDevice target = mapper.getDefaultMidiDevice();

        String chanStr;
        int channel = ((MidiLane) lane).getMidiChannel();
        Object channelHandle = null;
        if (channel > -1) {
            channelHandle = midiResource.getOutChannelList(target)[channel];
        }


        System.out.println(" DMCS create " + channel + "   " + channelHandle);
        if (channelHandle == null) {
            chanStr = "null";
        } else {
            chanStr = channelHandle.toString();
        }

        ListProvider resource = new ListProvider() {

            public Object[] getList() {
                return midiResource.getOutChannelList(mapper.getDefaultMidiDevice());
            }
        };

        PopupClient client = new PopupClient() {

            public void fireSelected(PopupSelectorButton but, Object o, int cnt) {
                ((MidiLane) lane).setMidiChannel(cnt);
                //mapper.setChannel(cnt);
                validate();
                repaint();
            }
        };

        return new PopupSelectorButton(resource, client, chanStr);

    }

    PopupSelectorButton createDeviceSelector() {
        midiDev = ((MidiLane) lane).getMidiDevice();

        // Device selector
        // ------------------------------------------------------------------------------------

        ListProvider resource = new ListProvider() {

            public Object[] getList() {
                return MidiVoiceView.this.lane.getProject().getMidiDeviceDescriptors().toArray();// midiResource.getMidiOutList();
            }
        };

        PopupClient client = new PopupClient() {

            public void fireSelected(PopupSelectorButton but, Object o, int cnt) {

                MidiDevice d = ((MidiDeviceDescriptor) o).getMidiDevice();
                ((MidiLane) lane).setMidiDevice(d);

                drumMapView = false;
                mapper = null;
                if (d instanceof SynthWrapper) {
                    MidiDevice dev = ((SynthWrapper) d).getRealDevice();
                    if ((dev instanceof DrumMapper)) {
                        drumMapView = true;
                        mapper = (DrumMapper) dev;
                    }
                }
                if (o != midiDev) {
                    init();
                }
            }
        };

        String name = "null";

        Icon icon = null;
        if (midiDev != null) {
            MidiDeviceDescriptor des = lane.getProject().getMidiDeviceDescriptor(midiDev);
            //				((MidiLane) lane).getMidiDevice());
            if (des == null) {
                try {
                    throw new Exception(" MidiLane has a mididevice without a descriptor !!! check this ? ");
                } catch (Exception e) {
                    e.printStackTrace();
                } // TODO PJL
            } else {
                name = des.getProjectName();
                icon = des.getIcon();
            }
        } else {
            name = "null";
        }

        PopupSelectorButton popsel = new PopupSelectorButton(resource, client, name);
        if (icon != null) {
            popsel.setIcon(icon);
        } else {
            popsel.setIcon(ProjectFrame.getIconResource("midi.png"));
        }
        return popsel;

    }

    PopupSelectorButton createDrumMapperDeviceSelector() {


        //midiDev = ((MidiLane) lane).getMidiDevice();

        // Device selector
        // ------------------------------------------------------------------------------------

        ListProvider resource = new ListProvider() {

            public Object[] getList() {
                return MidiVoiceView.this.lane.getProject().getMidiDeviceDescriptors().toArray();// midiResource.getMidiOutList();
            }
        };

        PopupClient client = new PopupClient() {

            public void fireSelected(PopupSelectorButton but, Object o, int cnt) {

                MidiDevice d = ((MidiDeviceDescriptor) o).getMidiDevice();

                drumMapView = false;

                if (d instanceof SynthWrapper) {
                    MidiDevice dev = ((SynthWrapper) d).getRealDevice();
                    if ((dev instanceof DrumMapper)) {
                        System.err.println("Sorry but I don't think this is a good idea !!!!");
                        init();
                        return;
                    }
                }

                mapper.setDefaultMidiDevice(d);

                init();
            }
        };

        String name;

        MidiDevice d = mapper.getDefaultMidiDevice();
        if (d != null) {
            name = d.toString();
        } else {
            name = "null";
        }

        return new PopupSelectorButton(resource, client, name);

    }

    private static String quantizeOptionsInfoString(Quantization options) {
        StringBuffer sb = new StringBuffer();
        if (options.groovePattern != null) {
            sb.append(options.groovePattern.getName());
        }
        if (options.swing != 0f) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("swing ");
            sb.append((int) (options.swing * 100));
            sb.append('%');
        }
        if (sb.length() == 0) {
            return "default";
        } else {
            return sb.toString();
        }
    }
}
