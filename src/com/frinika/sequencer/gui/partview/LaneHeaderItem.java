package com.frinika.sequencer.gui.partview;

import com.frinika.audio.gui.MeterPanel;
import java.awt.Color;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.AudioLane;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MenuPlugable;
import com.frinika.sequencer.model.MenuPlugin;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.ProjectLane;
import com.frinika.sequencer.model.RecordableLane;
import com.frinika.sequencer.model.SoloManager;
import com.frinika.sequencer.model.SynthLane;
import com.frinika.sequencer.model.TextLane;


import java.util.Vector;
import static com.frinika.localization.CurrentLocale.getMessage;

/**
 * left of each lane has a laneheaderitem.
 * 
 * Placement of these is done by LaneHeaderPanel
 * 
 * Do we need to subclass this ?
 * 
 * @author pjl
 * 
 */
public class LaneHeaderItem extends JPanel implements Observer, MenuPlugable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    JButton close = new JButton();
    JTextField voice;
    JButton mutate = new JButton();
    MyButton mute;
    MyButton solo;
    MyButton prerender;
    MyButton looped; // Jens
    MyButton record;
    MeterPanel meterPanel;
    Color selectedColor;
    Color defaultColor;
    boolean selected = false;
    Lane lane;
    LaneView voiceView;
    private boolean notify = true;

    // private int dy=-1;

    // ProjectContainer project;
    ProjectFrame frame; // Jens
    Color voice_selected_background;
    Color voice_unselected_background;
    JLabel midiLaneLabel;
    SoloManager soloManager;
    LaneHeaderPanel parent;

    public LaneHeaderItem(final ProjectFrame frame,
            final LaneHeaderPanel parent, final Lane lane, int index) {
        this.lane = lane;
        this.parent = parent;
        soloManager = frame.getProjectContainer().getSoloManager();
        if (lane instanceof SynthLane) {
            float[] rgb = getBackground().getRGBColorComponents(new float[3]);
            rgb[0] *= 0.95;
            rgb[1] *= 0.95;
            rgb[2] *= 0.95;
            setBackground(new Color(rgb[0], rgb[1], rgb[2]));
        }

        this.defaultColor = getBackground();
        this.selectedColor = Color.PINK;

        // this.project=project;
        this.frame = frame; // Jens
        if (lane instanceof MidiLane) {
            voiceView = new MidiVoiceView((MidiLane) lane, frame);
        } else if (lane instanceof AudioLane) {
            voiceView = new AudioLaneView((AudioLane) lane);
            AudioLane al = ((AudioLane) lane);
            al.getMixerControls().getSoloControl().addObserver(this);
            al.getMixerControls().getMuteControl().addObserver(this);

        } else if (lane instanceof TextLane) { // Jens
            voiceView = new TextLaneView((TextLane) lane, frame);
        } else if (lane instanceof ProjectLane) {
            voiceView = new ProjectView((ProjectLane) frame.getProjectContainer().getProjectLane());
        } else if (lane instanceof SynthLane) {
            voiceView = new SynthLaneView(lane);
            SynthLane al = ((SynthLane) lane);
            if (al.getMixerControls() != null) // If it isn't a synthesizer
            // then it has no mixer controls
            {
                al.getMixerControls().getSoloControl().addObserver(this);
                al.getMixerControls().getMuteControl().addObserver(this);
            }
        } else {
            voiceView = new LaneView(lane);
        }

        if (lane instanceof RecordableLane) {
            meterPanel = new MeterPanel();
        }

        Insets insets = new Insets(0, 0, 0, 0);

        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 0;
        c.ipadx = 4;

        Icon icon = lane.getIcon();
        JLabel label = new JLabel(icon);
        add(label, c);

        if (lane instanceof MidiLane) {

            midiLaneLabel = label;
            midiLaneLabel.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    if (((MidiLane) lane).isDrumLane()) {
                        ((MidiLane) lane).setType(MidiLane.MELODIC);
                    } else {
                        ((MidiLane) lane).setType(MidiLane.DRUM);
                    }
                    repaint();
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }
            });
        }

        c.ipady = 0;
        c.ipadx = 4;
        c.weightx = 1;

        voice = new JTextField(lane.getName(), 8);

        voice_selected_background = voice.getBackground();
        voice.setBackground(getBackground());
        voice_unselected_background = voice.getBackground();
        voice.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));
        this.voice.setMargin(insets);

        this.voice.setToolTipText("Click to select this lane");
        setToolTipText("Click to select this lane");

        this.voice.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                lane.setName(voice.getText());
                if (notify) {
                    lane.getProject().getLaneSelection().setSelected(lane);
                    lane.getProject().getLaneSelection().notifyListeners();
                }
                voice.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));
            }
        });


        voice.setToolTipText(getMessage("laneheader.tooltip.voice_name"));

        this.voice.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (notify) {
                    lane.getProject().getLaneSelection().setSelected(lane);
                    lane.getProject().getLaneSelection().notifyListeners();
                }
            }

            public void focusLost(FocusEvent e) {
                lane.setName(voice.getText());
                voice.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));
            }
        });

        voice.addMouseListener(new MouseInputAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                //		System.out.println(" Mouse Press in lane header ");

                if (e.getClickCount() == 2) {
                    // System.out.println(" DOUBLE CLICK");
                    voice.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON3) {
                    showContextMenu(e.getX(), e.getY());
                    e.consume();
                    return;
                }

                frame.getProjectContainer().getLaneSelection().clearSelection();
                frame.getProjectContainer().getLaneSelection().addSelected(lane);
                frame.getProjectContainer().getLaneSelection().notifyListeners();
                grabFocus();
            }
        });

        setFocusable(true);
        Color bordercolor = UIManager.getColor("Panel.background").darker();
        if (index == 0) {
            this.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0,
                    bordercolor));
        } else {
            this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                    bordercolor));
        }
        // this.setBorder(BorderFactory.createLineBorder(Color.black));
        c.insets = new Insets(0, 0, 0, 1);
        c.fill = GridBagConstraints.HORIZONTAL;
        add(voice, c);
        c.fill = GridBagConstraints.NONE;

        if (lane instanceof MidiLane) {
            this.prerender = new MyButton("P", Color.BLUE);
            this.prerender.setMargin(insets);
            this.prerender.setToolTipText("Pre-render this voice");
            prerender.setFocusable(false);

            this.prerender.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    ((MidiLane) lane).getPlayOptions().preRendered = (!((MidiLane) lane).getPlayOptions().preRendered);
                    if (notify) {
                        lane.getProject().getLaneSelection().setSelected(lane);
                        lane.getProject().getLaneSelection().notifyListeners();
                    }
                    frame.repaintViews();
                    setState();
                }
            });
        }

        if (lane instanceof MidiLane) {
            this.looped = new MyButton("L", Color.YELLOW);
            this.looped.setMargin(insets);
            this.looped.setToolTipText("Loop this voice");
            looped.setFocusable(false);

            this.looped.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ((MidiLane) lane).getPlayOptions().looped = (!((MidiLane) lane).getPlayOptions().looped);
                    if (notify) {
                        lane.getProject().getLaneSelection().setSelected(lane);
                        lane.getProject().getLaneSelection().notifyListeners();
                    }
                    frame.repaintViews();
                    setState();
                }
            });
        }

        if (lane instanceof RecordableLane) {
            this.mute = new MyButton("M", Color.ORANGE);
            this.mute.setMargin(insets);
            this.mute.setToolTipText("Mute this voice");
            mute.setFocusable(false);
            this.mute.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    RecordableLane rl = (RecordableLane) lane;
//					rl.setMute(!rl.isMute());
                    soloManager.toggleMute(rl);
                    if (notify) {
                        lane.getProject().getLaneSelection().setSelected(lane);
                        lane.getProject().getLaneSelection().notifyListeners();
                    }
                    parent.repaint();
                //	setState();
                }
            });

            this.solo = new MyButton("S", Color.GREEN);
            this.solo.setMargin(insets);
            this.solo.setToolTipText("Solo this voice");
            solo.setFocusable(false);
            this.solo.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    RecordableLane rl = (RecordableLane) lane;
//					rl.setMute(!rl.isMute());
                    soloManager.toggleSolo(rl);
//					((RecordableLane) lane).setSolo(!((RecordableLane) lane)
//							.isSolo());
                    if (notify) {
                        lane.getProject().getLaneSelection().setSelected(lane);
                        lane.getProject().getLaneSelection().notifyListeners();
                    }
                    parent.repaint();
                //setState();
                }
            });

            if (lane instanceof SynthLane) {
                this.record = new MyButton("P", Color.BLUE);
            } else {
                this.record = new MyButton("R", Color.RED);
            }

            this.record.setMargin(insets);
            this.record.setFocusable(false);
            // this.record.setToolTipText(getMessage("sequencer.lane.record_arm_tip"));
            // record.setBackground(Color.RED);

            this.record.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    RecordableLane rl = (RecordableLane) lane;
                    rl.setRecording(!rl.isRecording());
                    if (notify) {
                        lane.getProject().getLaneSelection().setSelected(lane);
                        lane.getProject().getLaneSelection().notifyListeners();
                    }
                    setState();
                }
            });
            c.weightx = 0;

            if (lane instanceof MidiLane) {
                add(looped, c);
            } // Jens
            if (lane instanceof MidiLane) {
                add(prerender, c);
            }

            boolean show_msr = true;
            if (lane instanceof SynthLane) {
                if (!((SynthLane) lane).isSynthesizer()) {
                    show_msr = false;
                }
            }

            if (show_msr) {
                add(record, c);
                add(mute, c);
                add(solo, c);
            }
        }




        if (meterPanel != null) {
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.VERTICAL;
            // c.weightx=.5;
            c.insets = new Insets(2, 2, 2, 2);
            c.ipadx = 0;
            c.ipady = 0;
            c.weighty = 1.0;
            add(meterPanel, c);
        }
    }
    static Vector<MenuPlugin> menuPlugins = new Vector<MenuPlugin>();

    /**
     * Allow custom menus to be added.
     * 
     * This will appear at the top of the right button popup menu.
     * 
     * @param menuPlugin
     */
    public static void addPluginRightButtonMenu(MenuPlugin menuPlugin) {
        LaneHeaderItem.menuPlugins.add(menuPlugin);
    }

    /**
     * Shows the right-click context menu of the current component.
     * 
     * @param frame
     * @param invoker
     * @param x
     * @param y
     */
    public void showContextMenu(int x, int y) {
        // build popup-menu from menuPrefix and own items
        if (menuPlugins == null) {
            return;
        }

        JPopupMenu popup = new JPopupMenu();
        for (MenuPlugin plugin : menuPlugins) {
            plugin.initContextMenu(popup, this);
        }
        //initContextMenu(frame, popup);
        popup.show(this, x, y);
    }

    @Override
    public void paintComponent(Graphics g) {
        setState();
        super.paintComponent(g);
    }

    void setState() {
        // avoid selecting
        notify = false;
        if (!this.voice.hasFocus()) {
            this.voice.setText(lane.getName());
            this.voice.setCaretPosition(0);
        }
        if (this.lane.isSelected()) {
            if (!this.selected) {
                // setBackground(this.selectedColor);
                setBackground(this.voice_selected_background);
                voice.setBackground(voice_selected_background);
                this.selected = true;
            }
        } else {
            if (this.selected) {
                setBackground(this.defaultColor);
                voice.setBackground(voice_unselected_background);
                if (voice.hasFocus()) {
                    grabFocus();
                }
                this.selected = false;
            }
        }

        if (lane instanceof RecordableLane) {
            // weird inverse logic to get color right TODO create my own button
            RecordableLane rl = (RecordableLane) lane;

            record.draw(rl.isRecording());

            mute.draw(rl.isMute(), soloManager.isMute(rl));
            solo.draw(soloManager.isSolo(rl));


            if (lane instanceof MidiLane) {
                looped.draw(((MidiLane) lane).getPlayOptions().looped);
            } // Jens
            if (lane instanceof MidiLane) {
                prerender.draw(((MidiLane) lane).getPlayOptions().preRendered);
            } // Jens
        }

        if (lane instanceof MidiLane) {
            midiLaneLabel.setIcon(lane.getIcon());
            midiLaneLabel.validate();
//			if (!((MidiLane) lane).isDrumLane()) {
//			//	System.out.println(" set icon   note");
//				midiLaneLabel.setIcon(iconNoteLane);
//				midiLaneLabel.validate();
//			} else {
//			//	System.out.println(" set icon  drum");
//				midiLaneLabel.setIcon(iconDrumLane);
//				midiLaneLabel.validate();
//			}
        }

        notify = true;
    }
    static double dBmax = 20.0 * Math.log10(1.0);
    static double dBmin = dBmax - 40.0;
    static double dBrange = dBmax - dBmin;

    public void updateMeter() {
        if (meterPanel == null) {
            return;
        }
        Graphics g = meterPanel.getGraphics();
        if (g == null) {
            return;
        }
        RecordableLane r = (RecordableLane) lane;
        double val = r.getMonitorValue();
        double dBval = 20.0 * Math.log10(val + 1e-40);
        double fact = (dBval - dBmin) / dBrange;
        // System.out.println(fact);
        if (val >= 1.0) {
            meterPanel.updateMeter(fact, Color.RED);
        } else {
            meterPanel.updateMeter(fact, Color.GREEN);
        }
    }

  
    class MyButton extends JButton {

        private static final long serialVersionUID = 1L;
        private Color onCol;
        private Color userOnCol;

        MyButton(String t, Color onCol) {
            super(t);
            this.onCol = onCol.brighter();
            this.userOnCol = onCol;

        /*
         * setUI(new MetalButtonUI()); setBorderPainted(true);
         * setContentAreaFilled(false); setOpaque(false); setBorder(
         * BorderFactory.createCompoundBorder(
         * BorderFactory.createLineBorder(Color.GRAY),
         * BorderFactory.createEmptyBorder(2,2,2,2)));
         */
        }

        void draw(boolean on) {
            // setOpaque(on);

            if (on) {
                setBackground(userOnCol);
            } else {
                setBackground(defaultColor);
            }
        }

        void draw(boolean on, boolean userOn) {
            // setOpaque(on);

            //		assert( ! (on && !userOn));

            if (userOn) {
                setBackground(userOnCol);
            } else if (on) {
                setBackground(onCol);
            } else {
                setBackground(defaultColor);
            }
        }
    }

    public void update(Observable o, Object arg) {
        repaint();
    }

    public Lane getLane() {

        return lane;
    }
}
