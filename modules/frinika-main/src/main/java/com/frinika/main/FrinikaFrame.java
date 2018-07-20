/*
 * Created on Mar 6, 2006
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * Copyright (c) 2007 Karl Helgason (added flexdock support)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.main;

import com.frinika.base.FrinikaAudioSystem;
import com.frinika.base.MessageDialogUtils;
import com.frinika.global.ConfigDialog;
import com.frinika.global.ConfigDialogPanel;
import com.frinika.global.ConfigError;
import com.frinika.global.ConfigListener;
import com.frinika.global.FrinikaConfig;
import com.frinika.global.property.ConfigurationProperty;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.global.property.FrinikaGlobalProperty;
import com.frinika.gui.DefaultOptionsBinder;
import com.frinika.gui.util.PresentationPanel;
import com.frinika.gui.util.WindowUtils;
import com.frinika.localization.CurrentLocale;
import com.frinika.main.action.AboutAction;
import com.frinika.main.action.CreateProjectAction;
import com.frinika.main.action.SelectAllAction;
import com.frinika.mod.MODImporter;
import com.frinika.model.EditHistoryAction;
import com.frinika.model.EditHistoryListener;
import com.frinika.notation.NotationPanel;
import com.frinika.project.FrinikaProjectContainer;
import com.frinika.project.dialog.BounceToLane;
import com.frinika.project.dialog.ExportWavDialog;
import com.frinika.project.gui.MultiPartMenuPlugin;
import com.frinika.project.gui.ProjectNewFileFilter;
import com.frinika.project.gui.StatusBar;
import com.frinika.project.gui.action.ScriptingAction;
import com.frinika.radio.LocalOGGHttpRadio;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.gui.GlobalToolBar;
import com.frinika.sequencer.gui.ProjectFrame;
import com.frinika.sequencer.gui.menu.CreateAudioLaneAction;
import com.frinika.sequencer.gui.menu.CreateMidiDrumLaneAction;
import com.frinika.sequencer.gui.menu.CreateMidiLaneAction;
import com.frinika.sequencer.gui.menu.CreateTextLaneAction;
import com.frinika.sequencer.gui.menu.CutPasteMenu;
import com.frinika.sequencer.gui.menu.DisplayStructureAction;
import com.frinika.sequencer.gui.menu.GuiCursorUpdateMenu;
import com.frinika.sequencer.gui.menu.ImportAudioAction;
import com.frinika.sequencer.gui.menu.ImportMidiToLaneAction;
import com.frinika.sequencer.gui.menu.MidiIMonitorAction;
import com.frinika.sequencer.gui.menu.MidiInSetupAction;
import com.frinika.sequencer.gui.menu.RepeatAction;
import com.frinika.sequencer.gui.menu.SetAudioOutputAction;
import com.frinika.sequencer.gui.menu.SplitSelectedPartsAction;
import com.frinika.sequencer.gui.menu.TempoListEditAction;
import com.frinika.sequencer.gui.menu.TimeSignatureEditAction;
import com.frinika.sequencer.gui.menu.ToggleShowVoiceViewAction;
import com.frinika.sequencer.gui.menu.WarpToLeftAction;
import com.frinika.sequencer.gui.menu.WarpToRightAction;
import com.frinika.sequencer.gui.menu.midi.GroovePatternCreateFromMidiPartAction;
import com.frinika.sequencer.gui.menu.midi.GroovePatternManagerAction;
import com.frinika.sequencer.gui.menu.midi.MidiDurationAction;
import com.frinika.sequencer.gui.menu.midi.MidiInsertControllersAction;
import com.frinika.sequencer.gui.menu.midi.MidiQuantizeAction;
import com.frinika.sequencer.gui.menu.midi.MidiReverseAction;
import com.frinika.sequencer.gui.menu.midi.MidiShiftAction;
import com.frinika.sequencer.gui.menu.midi.MidiStepRecordAction;
import com.frinika.sequencer.gui.menu.midi.MidiTimeStretchAction;
import com.frinika.sequencer.gui.menu.midi.MidiTransposeAction;
import com.frinika.sequencer.gui.menu.midi.MidiVelocityAction;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.gui.partview.VoicePartViewSplitPane;
import com.frinika.sequencer.gui.pianoroll.PianoControllerSplitPane;
import com.frinika.sequencer.gui.tracker.TrackerPanel;
import com.frinika.sequencer.gui.transport.RecordAction;
import com.frinika.sequencer.gui.transport.RewindAction;
import com.frinika.sequencer.gui.transport.StartStopAction;
import com.frinika.sequencer.model.AudioLane;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.project.MessageHandler;
import com.frinika.sequencer.project.ProjectRepaintListener;
import com.frinika.sequencer.project.mididevices.gui.MidiDevicesPanel;
import com.frinika.sequencer.tools.BufferedPlayback;
import com.frinika.soundhelix.FrinikaSoundHelixPanel;
import com.frinika.tootX.gui.FrinikaMixerPanel;
import com.frinika.tootX.gui.MidiLearnPanel;
import com.frinika.tootX.midi.MidiInDeviceManager;
import com.frinika.tootX.midi.MidiLearnIF;
import com.frinika.tools.MidiFileFilter;
import com.frinika.tools.ProjectFileFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.state.DockingState;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;
import uk.org.toot.swingui.audioui.mixerui.CompactMixerPanel;

/**
 * Frinika is designed to have one basis frame per project.
 *
 * A project frame is the main window for a project.
 *
 * @author Peter Johan Salomonsen
 */
public class FrinikaFrame extends JFrame implements ProjectFrame {

    private static final long serialVersionUID = 1L;

    public static final String TRACKS_VIEW = "tracks";
    public static final String VOICE_VIEW = "voice";
    public static final String PIANOROLL_VIEW = "pianoroll";
    public static final String TRACKER_VIEW = "tracker";
    public static final String NOTATION_VIEW = "notation";
    public static final String MIDIOUT_VIEW = "midiout";
    public static final String MIXER_VIEW = "mixer";

    // PianoRollEditor pianoRollEditor;
    TrackerPanel trackerPanel;

    PianoControllerSplitPane pianoControllerPane;

    NotationPanel notationPanel;

    VoicePartViewSplitPane partViewEditor;

    GlobalToolBar globalToolBar;

    FrinikaProjectContainer project;

    private File midiFile;

    private StartStopAction startStop;

    private RewindAction rewind;

    private WarpToLeftAction warpToLeft;

    private WarpToRightAction warpToRight;

    private Viewport viewport;

    private RecordAction record;

    private Rectangle position;

    private MidiLearnPanel midiLearnFrame;

    private boolean withRef = false;

    // private List<Action> audioPartContextActions = new ArrayList<Action>();
    // private List<Action> midiPartContextActions = new ArrayList<Action>();
    public JPopupMenu newLaneMenu = new JPopupMenu();
    private StatusBar statusBar;

    private static JDialog showingDialog = null;
    private static JFrame showingDialogFrame = null;

    private Dimension frameSize;
    private int positionX;
    private int positionY;

    public FrinikaFrame() throws Exception {
        initFrame();
    }

    private void initFrame() {
        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent evt) {
                FrinikaMain.getInstance().closeFrame(FrinikaFrame.this);
            }

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }

            @Override
            public void windowActivated(WindowEvent arg0) {
                FrinikaMain frinikaMain = FrinikaMain.getInstance();
                // System.out.println(" activated ");
                frinikaMain.setFocusFrame(FrinikaFrame.this);
                frinikaMain.notifyProjectFocusListeners();
//				try {

//                                    	MidiInDeviceManager.setReceiver(project.getSequencer()
//							.getReceiver());
// 
                MidiInDeviceManager.setProject(project);
//
//                                } catch (MidiUnavailableException e) {
//					e.printStackTrace();
//				}

            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {

            }
        });

        // TODO save positioning into config
        if (false) {
            addComponentListener(new ComponentListener() {

                @Override
                public void componentHidden(ComponentEvent e) {
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    info();
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    info();
                }

                @Override
                public void componentShown(ComponentEvent e) {
                }

                void info() {
                    GraphicsConfiguration gc = FrinikaFrame.this
                            .getGraphicsConfiguration();

                    System.out.println(gc);
                    System.out.println(FrinikaFrame.this.getBounds());
                }
            });
        }

        /*
		 * Rectangle windowSize = GraphicsEnvironment
		 * .getLocalGraphicsEnvironment().getDefaultScreenDevice()
		 * .getDefaultConfiguration().getBounds();
         */
 /*
		 * Point p = GraphicsEnvironment.getLocalGraphicsEnvironment()
		 * .getCenterPoint(); Dimension frameSize = new
		 * Dimension(windowSize.getSize()); setSize(frameSize); setLocation(0,
		 * 0);
         */
        if (position == null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Rectangle windowSize;
            Insets windowInsets;

            GraphicsEnvironment ge = java.awt.GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
            GraphicsConfiguration gc = ge.getDefaultScreenDevice()
                    .getDefaultConfiguration();
            if (gc == null) {
                gc = getGraphicsConfiguration();
            }

            if (gc != null) {
                windowSize = gc.getBounds();
                windowInsets = toolkit.getScreenInsets(gc);
            } else {
                windowSize = new java.awt.Rectangle(toolkit.getScreenSize());
                windowInsets = new java.awt.Insets(0, 0, 0, 0);
            }

            // PJL using xrandr and a virtual screen 2560x1024 then windowSize.x
            // == windowSize.width=1280;
            windowSize.x = 0;

            int width = (windowSize.width - windowSize.x)
                    - (windowInsets.left + windowInsets.right + 10);
            int height = (windowSize.height - windowSize.y)
                    - (windowInsets.top + windowInsets.bottom + 10);
            positionX = windowInsets.left
                    + ((windowSize.width - windowSize.x)
                    - (windowInsets.left + windowInsets.right) - width) / 2;
            positionY = windowInsets.top
                    + ((windowSize.height - windowSize.y)
                    - (windowInsets.top + windowInsets.bottom) - height) / 2;

            frameSize = new Dimension(width, height);

            setSize(new Dimension(width - 1, height - 1));
        } else {
            frameSize = position.getSize();
            positionY = position.y;
            positionX = position.x;

        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setIconImage(FrinikaFrame.getIconResource("frinika.png").getImage());
    }

    public void setProject(@Nonnull final FrinikaProjectContainer project) throws Exception {
        this.project = project;
        initProject();
    }

    public void setPosition(Rectangle position) {
        this.position = position;
    }

    private void initProject() throws Exception {
        project.resetEndTick();
        // This might be useful for more placement control.
        // Dimension frameSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Receiver receiver = project.getSequencer().getReceiver();

        midiLearnFrame = new MidiLearnPanel(project.getMidiDeviceRouter());

        /*
		 * Vector<String> midiInList = FrinikaConfig.getMidiInDeviceList(); for
		 * (String name : midiInList) { MidiDevice midiIn = null; // TOOT_FIXME
		 * MidiHub.getMidiInDeviceByName(name); * PJS: I use a USB midi device
		 * that I unplug every now and then without this test I'm not able to
		 * reopen my projects if I don't have this midi device
		 * 
		 * PJL: Should we ask for replacement devices here ?
		 * 
		 * 
		 * if (midiIn != null) { try { midiIn.open();
		 * midiIn.getTransmitter().setReceiver(receiver); System.
		 * out.println(midiIn + " ---> " + receiver); } catch (Exception e) {
		 * e.printStackTrace(); } } else System. out.println("Couldn't reopen
		 * mididevice: " + name); }
         */
        MidiInDeviceManager.setProject(project);

        initFrame();

        project.registerProjectRepaintListener(new ProjectRepaintListener() {
            @Override
            public void repaintViews() {
                partViewEditor.repaint();
                repaintPartView();
                // TODO repaint others if needed
            }

            @Override
            public void repaintPartView() {
                partViewEditor.getPartview().repaint();
                partViewEditor.getPartview().repaintItems();
            }
        });

        String name = "Frinika - Copyright (c) Frinika developers - Licensed under GNU General Public License";
        File file = project.getProjectFile();
        if (file != null) {
            name = file.getName();
        }

        setTitle(name);

        project.getEditHistoryContainer().addEditHistoryListener(
                new EditHistoryListener() {
            /**
             * An asterix sign next to the project title indicating if edits
             * have been done
             */
            boolean showAsterix = false;

            @Override
            public void fireSequenceDataChanged(
                    EditHistoryAction[] edithistoryEntries) {
                if (project.getEditHistoryContainer().hasChanges() != showAsterix) {
                    showAsterix = project.getEditHistoryContainer()
                            .hasChanges();
                    if (showAsterix) {
                        setTitle(getTitle() + "*");
                    } else {
                        setTitle(getTitle().substring(0,
                                getTitle().length() - 1));
                    }
                }
            }
        });

        project.setMessageHandler(new MessageHandler() {
            @Override
            public void message(String message) {
                MessageDialogUtils.message(FrinikaFrame.this, message);
            }

            @Override
            public String prompt(String message, String initialValue) {
                return MessageDialogUtils.prompt(FrinikaFrame.this, message, initialValue);
            }

            @Override
            public String prompt(String message) {
                return MessageDialogUtils.prompt(FrinikaFrame.this, message);
            }

            @Override
            public String promptFile(String defaultFilename, String[][] suffices, boolean saveMode) {
                return MessageDialogUtils.promptFile(FrinikaFrame.this, defaultFilename, suffices, saveMode);
            }

            @Override
            public String promptFile(String defaultFilename, String[][] suffices) {
                return MessageDialogUtils.promptFile(FrinikaFrame.this, defaultFilename, suffices);
            }

            @Override
            public boolean confirm(String message) {
                return MessageDialogUtils.confirm(FrinikaFrame.this, message);
            }

            @Override
            public void error(String message) {
                MessageDialogUtils.error(FrinikaFrame.this, message);
            }

            @Override
            public void error(Throwable ex) {
                MessageDialogUtils.error(FrinikaFrame.this, ex);
            }

            @Override
            public void error(String message, Throwable t) {
                MessageDialogUtils.error(FrinikaFrame.this, message, t);
            }
        });

        // Karl, flexdock, use -Dfrinika.window.flexdock=no to disable flexdock
        if (System.getProperty("frinika.window.flexdock") == null) {
            DockingManager.setFloatingEnabled(true);
            viewport = new Viewport();
        }

        createMenu();

        // SIDEEFECT: injects metronome audio client to audio mixer
        globalToolBar = new GlobalToolBar(project);

        JPanel content = new JPanel(new BorderLayout());
        setContentPane(content);

        content.add(globalToolBar, BorderLayout.NORTH);

        if (viewport == null) {
            JTabbedPane midPanels = new JTabbedPane();
            content.add(midPanels, BorderLayout.CENTER);

            midPanels.addTab(CurrentLocale.getMessage("project.maintabs.tracks"),
                    createSplit(frameSize));
            midPanels.addTab(CurrentLocale.getMessage("project.maintabs.midiout_devices"),
                    new JScrollPane(midiDevicesPanel = new MidiDevicesPanel(
                            project, new MidiDevicesPanel.MidiDevicesHandler() {
                        @Override
                        public void addMidiDevices(JPopupMenu popup) {
                            FrinikaFrame.this.addMidiDevices(popup);
                        }
                    })));

            midPanels.addTab(CurrentLocale.getMessage("project.maintabs.audiomixer"),
                    new FrinikaMixerPanel(project.getMixerControls(), midiLearnFrame));
        } else {
            content.add(viewport, BorderLayout.CENTER);

            initViews();

            perspectivePreset1();
        }

        statusBar = new StatusBar();

        content.add(statusBar, BorderLayout.SOUTH);
        validate();
        setVisible(true);
        // setVisible before setSize assures that the window doesn't open blank
        // (happens always when using XGL (compiz/beryl))
        setSize(frameSize);
        setLocation(positionX, positionY);

        FrinikaMain.getInstance().addFrame(this);

        overRideKeys();

        if (FrinikaGlobalProperties.MAXIMIZE_WINDOW.getValue()) {
            this.setExtendedState(MAXIMIZED_BOTH);
        }
    }

    private static final Icon MAXIMIZE_ICON = getIconResource("maximize.gif");
    private static final Icon MINIMIZE_ICON = getIconResource("minimize.gif");

    @Override
    public MidiLearnIF getMidiLearnIF() {
        return midiLearnFrame;
    }

    @Override
    public void resetViews() {
        for (View view : views.values()) {
            if (view.isMinimized()) {
                DockingManager.setMinimized((Dockable) view, false);
            }
            DockingManager.undock((Dockable) view);
        }
        viewport.grabFocus();
    }

    @Override
    public void repaintViews() {
        partViewEditor.repaint();
        repaintPartView();
        // TODO repaint others if needed
    }

    @Override
    public void repaintPartView() {
        partViewEditor.getPartview().repaint();
        partViewEditor.getPartview().repaintItems();
    }

    @Override
    public boolean hasChanges() {
        return project.getEditHistoryContainer().hasChanges();
    }

    JMenu perspectivemenu;

    JMenu perspective_showmenu;

    JMenu perspectiveMenu() {
        JMenu menu = perspectivemenu = new JMenu(
                CurrentLocale.getMessage("project.menu.perspectives"));
        menu.setMnemonic(KeyEvent.VK_P);

        JMenuItem item = new JMenuItem(
                CurrentLocale.getMessage("project.menu.perspectives.Default_Perspective"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                perspectivePreset1();
            }
        });

        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(item);

        item = new JMenuItem(
                CurrentLocale.getMessage("project.menu.perspectives.Mastering_Perspective"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                perspectivePreset2();
            }
        });

        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(item);

        item = new JMenuItem(
                CurrentLocale.getMessage("project.menu.perspectives.Minimal_Perspective"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                perspectivePreset3();
            }
        });

        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(item);

        menu.addSeparator();

        perspective_showmenu = new JMenu("Show View");
        menu.add(perspective_showmenu);

        return menu;
    }

    @Override
    public JFrame getFrame() {
        return this;
    }

    @Override
    public JPopupMenu getNewLaneMenu() {
        return newLaneMenu;
    }

    public TrackerPanel getTrackerPanel() {
        return trackerPanel;
    }

    public PianoControllerSplitPane getPianoControllerPane() {
        return pianoControllerPane;
    }

    public NotationPanel getNotationPanel() {
        return notationPanel;
    }

    @Override
    public VoicePartViewSplitPane getPartViewEditor() {
        return partViewEditor;
    }

    private class LateRunnable implements Runnable {

        Runnable runnable;

        public LateRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                SwingUtilities.invokeAndWait(runnable);
                Thread.sleep(200);
                SwingUtilities.invokeAndWait(runnable);
                Thread.sleep(200);
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    // HACK for flexdock
    public void runReallyLater(Runnable runnable) {
        new Thread(new LateRunnable(runnable)).start();
    }

    public void perspectivePreset1() {
        resetViews();

        viewport.dock(getView(TRACKS_VIEW));
        getView(TRACKS_VIEW).dock(getView(PIANOROLL_VIEW),
                DockingConstants.SOUTH_REGION);
        getView(TRACKS_VIEW).dock(getView(VOICE_VIEW), DockingConstants.WEST_REGION);
        getView(PIANOROLL_VIEW).dock(getView(TRACKER_VIEW));
        getView(PIANOROLL_VIEW).dock(getView(NOTATION_VIEW));
        getView(TRACKS_VIEW).dock(getView(MIDIOUT_VIEW));
        getView(TRACKS_VIEW).dock(getView(MIXER_VIEW));

        ((JSplitPane) getView(VOICE_VIEW).getParent().getParent())
                .setDividerLocation(300);
        ((JTabbedPane) getView(TRACKS_VIEW).getParent()).setSelectedIndex(0);
        ((JTabbedPane) getView(PIANOROLL_VIEW).getParent()).setSelectedIndex(0);

        viewport.grabFocus();

        validate();
        repaint();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ((JTabbedPane) getView(TRACKS_VIEW).getParent())
                        .setSelectedIndex(0);
                ((JTabbedPane) getView(PIANOROLL_VIEW).getParent())
                        .setSelectedIndex(0);
                viewport.grabFocus();
            }
        };
        runReallyLater(runnable);
    }

    public void perspectivePreset2() {
        resetViews();

        viewport.dock(getView(TRACKS_VIEW));
        getView(TRACKS_VIEW).dock(getView(MIXER_VIEW), DockingConstants.EAST_REGION,
                0.25f);
        getView(TRACKS_VIEW).dock(getView(PIANOROLL_VIEW),
                DockingConstants.SOUTH_REGION);
        getView(TRACKS_VIEW).dock(getView(VOICE_VIEW), DockingConstants.WEST_REGION,
                0.3f);
        getView(PIANOROLL_VIEW).dock(getView(TRACKER_VIEW));
        getView(PIANOROLL_VIEW).dock(getView(NOTATION_VIEW));
        getView(TRACKS_VIEW).dock(getView(MIDIOUT_VIEW));

        ((JSplitPane) getView(VOICE_VIEW).getParent().getParent())
                .setDividerLocation(300);

        ((JTabbedPane) getView(TRACKS_VIEW).getParent()).setSelectedIndex(0);
        ((JTabbedPane) getView(PIANOROLL_VIEW).getParent()).setSelectedIndex(0);

        viewport.grabFocus();

        validate();
        repaint();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ((JTabbedPane) getView(TRACKS_VIEW).getParent())
                        .setSelectedIndex(0);
                ((JTabbedPane) getView(PIANOROLL_VIEW).getParent())
                        .setSelectedIndex(0);
                viewport.grabFocus();
            }
        };
        runReallyLater(runnable);
    }

    public void perspectivePreset3() {
        resetViews();

        viewport.dock(getView(TRACKS_VIEW));
        getView(TRACKS_VIEW).dock(getView(VOICE_VIEW), DockingConstants.WEST_REGION);
        getView(TRACKS_VIEW).dock(getView(PIANOROLL_VIEW));
        getView(TRACKS_VIEW).dock(getView(TRACKER_VIEW));
        getView(TRACKS_VIEW).dock(getView(NOTATION_VIEW));
        getView(TRACKS_VIEW).dock(getView(MIDIOUT_VIEW));
        getView(TRACKS_VIEW).dock(getView(MIXER_VIEW));

        ((JSplitPane) getView(VOICE_VIEW).getParent().getParent())
                .setDividerLocation(300);
        ((JTabbedPane) getView(TRACKS_VIEW).getParent()).setSelectedIndex(0);

        DockingManager.setMinimized(getView(VOICE_VIEW), true, DockingState.LEFT);

        viewport.grabFocus();

        validate();
        repaint();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ((JTabbedPane) getView(TRACKS_VIEW).getParent())
                        .setSelectedIndex(0);
                viewport.grabFocus();
            }
        };
        runReallyLater(runnable);
    }

    @Override
    public void initViews() {
        partViewEditor = new VoicePartViewSplitPane(this, true);

        JComponent partViewEditor = this.partViewEditor.getPartViewEditor();
        JComponent laneView = this.partViewEditor.getLaneView();

        notationPanel = new NotationPanel(project);
        pianoControllerPane = new PianoControllerSplitPane(project);
        trackerPanel = new TrackerPanel(project.getSequence(), project);
        trackerPanel.setOpaque(false);
        JPanel mixer = new JPanel();
        mixer.setLayout(new BorderLayout());
        try {
            /*
			 * Class clazz =
			 * Class.forName("uk.org.toot.swingui.audioui.meterui.KMeterPanel");
			 * Field field = clazz.getDeclaredField("factory");
			 * field.setAccessible(true); if(field.get(null) == null) {
			 * System.err.println("factory has been nulled in KMeterPanel - Work
			 * around is in effect!");
			 * System.err.println("------------------------------------------------------------------");
			 * field.set(null, new MeterPanelFactory()); }
             */
            mixer.add(new FrinikaMixerPanel(project.getMixerControls(), midiLearnFrame));
            // mixer.add(new CompactMixerPanel(project.getMixerControls()));
        } catch (Throwable t) {
            // Sometimes java.lang.NullPointerException occurs in mixer
            // at
            // uk.org.toot.swingui.controlui.CompoundControlPanel.<init>(CompoundControlPanel.java:44)
            t.printStackTrace();
        }
        project.getPartSelection().addSelectionListener(trackerPanel);

        laneView.setPreferredSize(new Dimension(0, 0));
        partViewEditor.setPreferredSize(new Dimension(0, 0));
        pianoControllerPane.setPreferredSize(new Dimension(0, 0));
        pianoControllerPane.setOpaque(false);
        trackerPanel.setPreferredSize(new Dimension(0, 0));
        mixer.setPreferredSize(new Dimension(0, 0));

        laneView.setMinimumSize(new Dimension(0, 0));
        partViewEditor.setMinimumSize(new Dimension(0, 0));
        pianoControllerPane.setMinimumSize(new Dimension(0, 0));
        trackerPanel.setMinimumSize(new Dimension(0, 0));
        mixer.setMinimumSize(new Dimension(0, 0));

        createView(TRACKS_VIEW, CurrentLocale.getMessage("project.maintabs.tracks"),
                partViewEditor, DOCKICON_TRACKS);
        createView(VOICE_VIEW, CurrentLocale.getMessage("project.maintabs.lane_properties"), laneView, DOCKICON_VOICE);
        createView(PIANOROLL_VIEW, CurrentLocale.getMessage("project.maintabs.piano_roll"), pianoControllerPane,
                DOCKICON_PIANOROLL);
        createView(TRACKER_VIEW, CurrentLocale.getMessage("project.maintabs.tracker"), trackerPanel, DOCKICON_TRACKER);
        createView(NOTATION_VIEW, CurrentLocale.getMessage("project.maintabs.notation"), notationPanel, DOCKICON_NOTATION);
        createView(MIDIOUT_VIEW, CurrentLocale.getMessage("project.maintabs.midimixer"),
                new JScrollPane(midiDevicesPanel = new MidiDevicesPanel(project, new MidiDevicesPanel.MidiDevicesHandler() {
                    @Override
                    public void addMidiDevices(JPopupMenu popup) {
                        FrinikaFrame.this.addMidiDevices(popup);
                    }
                })), DOCKICON_MIDIOUT);
        createView(MIXER_VIEW, CurrentLocale.getMessage("project.maintabs.audiomixer"), mixer,
                DOCKICON_MIXER);
    }

    @Nullable
    public static ImageIcon getIconResource(@Nonnull String name) {
        try {
            ImageIcon icon = new ImageIcon(FrinikaFrame.class.getResource("/com/frinika/resources/icons/" + name));
            return icon;
        } catch (Exception ex) {
            Logger.getLogger(FrinikaFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private static final Icon DOCKICON_TRACKS = getIconResource("track.gif");
    private static final Icon DOCKICON_TRACKER = getIconResource("tracker.gif");
    private static final Icon DOCKICON_MIDIOUT = getIconResource("midi_mixer.gif");
    private static final Icon DOCKICON_VOICE = getIconResource("properties.gif");
    private static final Icon DOCKICON_PIANOROLL = getIconResource("piano.png");
    private static final Icon DOCKICON_MIXER = getIconResource("mixer.gif");
    private static final Icon DOCKICON_NOTATION = getIconResource("midilane.png");
    private static final Icon DEFAULT_MIDI_ICON = getIconResource("midi.png");

    public static Icon getMidiDeviceIcon(MidiDevice device) {
        Icon icon = getMidiDeviceLargeIcon(device);
        if (icon.getIconHeight() > 16 || icon.getIconWidth() > 16) {
            BufferedImage img = new BufferedImage(icon.getIconWidth(), icon
                    .getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = img.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            Image im = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            icon = new ImageIcon(im);
        }
        return icon;
    }

    public static Icon getMidiDeviceLargeIcon(MidiDevice device) {
        if (device instanceof SynthWrapper) {
            device = ((SynthWrapper) device).getRealDevice();
        }
        Icon icon = DEFAULT_MIDI_ICON;
        try {
            Method icon_method = device.getClass().getMethod("getIcon");
            icon = (Icon) icon_method.invoke(device);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
        }
        return icon;
    }

    private Map<String, View> views = new HashMap<>();

    @Nonnull
    private View getView(@Nonnull String viewId) {
        return (View) views.get(viewId);
    }

    private View createView(@Nonnull String viewId, String text, JComponent content,
            Icon icon) {

        final View view = new View(viewId, text);
        if (icon != null) {
            view.setIcon(icon);
        }
        if (icon != null) {
            view.setTabIcon(icon);
        }

        views.put(viewId, view);

        JMenuItem menuitem = new JMenuItem(text);
        menuitem.setIcon(icon);
        menuitem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DockingManager.undock((Dockable) view);
                Set dockables = viewport.getDockables();
                if (dockables.isEmpty()) {
                    viewport.dock(view);
                } else {
                    ((View) dockables.iterator().next()).dock(view);
                }

                validate();
                repaint();
            }
        });

        perspective_showmenu.add(menuitem);

        Action max_action = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getValue(String key) {
                if (key.equals("Name")) {
                    return "maximize";
                }
                if (key.equals("ShortDescription")) {
                    return "Maximize";
                }
                if (key.equals("SmallIcon")) {
                    return MAXIMIZE_ICON;
                }
                return super.getValue(key);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                DockingManager.toggleMaximized((Dockable) view);
                FrinikaFrame.this.repaint();
                DockingManager.display((Dockable) view);
            }

        };

        // Action min_action = new AbstractAction() {
        // private static final long serialVersionUID = 1L;
        //
        // public Object getValue(String key) {
        // if (key.equals("Name"))
        // return "minimize";
        // if (key.equals("ShortDescription"))
        // return "Minimize";
        // if (key.equals("SmallIcon"))
        // return minimize_icon;
        // return super.getValue(key);
        // }
        //
        // public void actionPerformed(ActionEvent e) {
        // DockingManager.setMinimized((Dockable) view,true);
        // focusFrame.repaint();
        // DockingManager.display((Dockable) view);
        // }
        //
        // };
        view.addAction(View.CLOSE_ACTION);
        view.getTitlebar().addAction(max_action);
        // view.getTitlebar().addAction(min_action);
        view.addAction(View.PIN_ACTION);
        view.getTitlebar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DockingManager.toggleMaximized((Dockable) view);
                    FrinikaFrame.this.repaint();
                    DockingManager.display((Dockable) view);
                    return;
                }
                super.mouseClicked(e);
            }
        });
        view.setContentPane(content);
        return view;
    }

    /**
     * Grab some keys before the focus manager gets them !!!! TODO discuss this.
     * Maybe better to have a focus panes and grab the keyboard for each of
     * these ?
     */
    void overRideKeys() {
        startStop = new StartStopAction(project);
        rewind = new RewindAction(project);
        record = new RecordAction(project);
        warpToLeft = new WarpToLeftAction(this);
        warpToRight = new WarpToRightAction(this);
        final SelectAllAction selectAllAction = new SelectAllAction(this);
        KeyboardFocusManager kbm = KeyboardFocusManager
                .getCurrentKeyboardFocusManager();

        kbm.addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {

                if (!FrinikaFrame.this.isActive()) {
                    return false;
                }
                // Let text components have all key hits
                if (e.getSource() instanceof JTextComponent) {
                    return false;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        if ((e.getID() == KeyEvent.KEY_PRESSED)) {
                            startStop.actionPerformed(null);
                        }
                        return true;

                    case KeyEvent.VK_HOME:
                        if ((e.getID() == KeyEvent.KEY_PRESSED)) {
                            rewind.actionPerformed(null);
                        }
                        return true;

                    case KeyEvent.VK_MULTIPLY:
                        if ((e.getID() == KeyEvent.KEY_PRESSED)) {
                            record.actionPerformed(null);
                        }
                        return true;

                    case KeyEvent.VK_NUMPAD1:
                        if ((e.getID() == KeyEvent.KEY_PRESSED)) {
                            warpToLeft.actionPerformed(null);
                        }
                        return true;

                    case KeyEvent.VK_NUMPAD2:
                        if ((e.getID() == KeyEvent.KEY_PRESSED)) {
                            warpToRight.actionPerformed(null);
                        }
                        return true;

                    case KeyEvent.VK_A:

                        if ((e.getID() == KeyEvent.KEY_PRESSED)) {

                            if (e.isControlDown()) {
                                return selectAllAction.selectAll(e);
                            }
                        }

                    default:
                        return false;
                }
            }
        });
    }

    public static void openLocalProject() {

    }

    JMenu fileMenu() {

        JMenu fileMenu = new JMenu(CurrentLocale.getMessage("project.menu.file"));

        final JMenuItem newProjectMenuItem = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.new_project"));
        newProjectMenuItem.setIcon(getIconResource("new.gif"));
        newProjectMenuItem.addActionListener(new CreateProjectAction());
        fileMenu.add(newProjectMenuItem);

        final JMenuItem openProjectMenuItem = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.open_project"));
        openProjectMenuItem.setIcon(getIconResource("open.gif"));
        openProjectMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser
                            .setDialogTitle(CurrentLocale.getMessage("project.menu.file.open_project.dialogtitle"));
                    chooser.setFileFilter(new ProjectNewFileFilter());
                    if (project.getProjectFile() != null) {
                        chooser.setSelectedFile(project.getProjectFile());
                    }
                    if (chooser.showOpenDialog(FrinikaFrame.this) == JFileChooser.APPROVE_OPTION) {
                        File newProject = chooser.getSelectedFile();
                        FrinikaFrame frame = new FrinikaFrame();
                        frame.setPosition(position);
                        frame.setProject(FrinikaProjectContainer.loadProject(newProject, null));
                        // FrinikaConfig.setLastProjectFilename(newProject
                        // .getAbsolutePath());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        fileMenu.add(openProjectMenuItem);

        final JMenuItem closeMenuItem = new JMenuItem(CurrentLocale.getMessage("close"));
        closeMenuItem.setMnemonic(KeyEvent.VK_C);
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        closeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrinikaMain.getInstance().closeFrame(FrinikaFrame.this);
            }
        });

        fileMenu.add(closeMenuItem);

        fileMenu.add(new JSeparator());

        final JMenuItem saveProjectMenuItem = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.save_project"));
        saveProjectMenuItem.setIcon(getIconResource("save.gif"));
        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, Toolkit.getDefaultToolkit()
                        .getMenuShortcutKeyMask()));
        saveProjectMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (project.getProjectFile() != null) {
                        project.saveProject(project.getProjectFile());
                        FrinikaConfig.setLastProject(project.getProjectFile());
                    } else {
                        withRef = false;
                        openSaveProjectDialog();
                    }
                } catch (IOException ex) {
                    MessageDialogUtils.error(getFrame(), "Error while saving", ex); // show error message,
                    // user should know that
                    // saving went wrong
                    ex.printStackTrace();
                }
            }
        });

        fileMenu.add(saveProjectMenuItem);

        final JMenuItem saveProjectAsMenuItem = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.save_project_as"));
        saveProjectAsMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                withRef = false;
                openSaveProjectDialog();
            }
        });

        fileMenu.add(saveProjectAsMenuItem);
//------------

        final JMenuItem saveProjectMenuItemWithRef = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.save_project_with_ref"));
        saveProjectMenuItemWithRef.setIcon(getIconResource("save.gif"));
//		saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(
        //KeyEvent.VK_S, Toolkit.getDefaultToolkit()
        //			.getMenuShortcutKeyMask()));
        saveProjectMenuItemWithRef.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (project.getProjectFile() != null) {
                        project.saveProject(project.getProjectFile());
                        FrinikaConfig.setLastProject(project.getProjectFile());
                    } else {
                        withRef = true;
                        openSaveProjectDialog();
                    }
                } catch (IOException ex) {
                    MessageDialogUtils.error(getFrame(), "Error while saving", ex); // show error message,
                    // user should know that
                    // saving went wrong
                    ex.printStackTrace();
                }
            }
        });

        fileMenu.add(saveProjectMenuItemWithRef);

        final JMenuItem saveProjectAsMenuItemWithRef = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.save_project_as_with_ref"));
        saveProjectAsMenuItemWithRef.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                withRef = true;
                openSaveProjectDialog();
            }
        });

        fileMenu.add(saveProjectAsMenuItemWithRef);

//------------
        fileMenu.addSeparator();

        final JMenuItem importMidiMenuItem = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.import_midi"));
        importMidiMenuItem.setIcon(getIconResource("import.gif"));
        importMidiMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser
                            .setDialogTitle(CurrentLocale.getMessage("project.menu.file.import_midi"));
                    chooser.setFileFilter(new MidiFileFilter());
                    if (midiFile != null) {
                        chooser.setSelectedFile(midiFile);
                    }

                    if (chooser.showOpenDialog(FrinikaFrame.this) == JFileChooser.APPROVE_OPTION) {
                        File newMidiFile = chooser.getSelectedFile();

                        MidiDevice mididdevice = selectMidiDevice();

                        FrinikaFrame frame = new FrinikaFrame();
                        frame.setProject(new FrinikaProjectContainer(MidiSystem.getSequence(newMidiFile), mididdevice));

                        midiFile = newMidiFile;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        fileMenu.add(importMidiMenuItem);

        JMenuItem item = new JMenuItem("Import Module...");
        item.setIcon(getIconResource("import.gif"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MODImporter.load(FrinikaFrame.this);
            }
        });
        fileMenu.add(item);

        final JMenuItem exportMidiMenuItem = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.export_midi"));
        exportMidiMenuItem.setIcon(getIconResource("export.gif"));
        exportMidiMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser
                            .setDialogTitle(CurrentLocale.getMessage("project.menu.file.export_midi"));
                    chooser.setFileFilter(new MidiFileFilter());
                    chooser.setSelectedFile(midiFile);
                    if (chooser.showSaveDialog(FrinikaFrame.this) == JFileChooser.APPROVE_OPTION) {
                        if (chooser.getFileFilter() instanceof MidiFileFilter) {
                            if (!chooser.getFileFilter().accept(
                                    chooser.getSelectedFile())) {
                                chooser.setSelectedFile(new File(chooser
                                        .getSelectedFile().getPath()
                                        + ".mid"));
                            }
                        }
                        File newMidiFile = chooser.getSelectedFile();
                        MidiSystem.write(project.getSequence().export(), 1,
                                newMidiFile);
                        midiFile = newMidiFile;
                    }
                } catch (HeadlessException | IOException | InvalidMidiDataException ex) {
                    ex.printStackTrace();
                }
            }
        });

        fileMenu.addSeparator();

        fileMenu.add(exportMidiMenuItem);

        final JMenuItem exportWavMenuItem = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.export_wav"));
        exportWavMenuItem.setIcon(getIconResource("export.gif"));
        exportWavMenuItem.addActionListener(new ActionListener() {

            class AudioFileFilter extends FileFilter {

                javax.sound.sampled.AudioFileFormat.Type type;

                AudioFileFilter(javax.sound.sampled.AudioFileFormat.Type type) {
                    this.type = type;
                }

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().toLowerCase().endsWith(
                            "." + type.getExtension().toLowerCase());
                }

                @Override
                public String getDescription() {
                    return type.toString() + " (*." + type.getExtension() + ")";
                }

                public javax.sound.sampled.AudioFileFormat.Type getType() {
                    return type;
                }
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Export Audio");

                    byte[] data = new byte[16];
                    AudioFormat format = new AudioFormat(
                            (float) FrinikaGlobalProperties.getSampleRate(), 16, 2, true, true);
                    AudioInputStream ais = new AudioInputStream(
                            new ByteArrayInputStream(data), format, 4);
                    javax.sound.sampled.AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(ais);
                    AudioFileFilter[] filters = new AudioFileFilter[types.length];
                    for (int i = 0; i < types.length; i++) {
                        filters[i] = new AudioFileFilter(types[i]);
                    }

                    for (AudioFileFilter filter : filters) {
                        chooser.setFileFilter(filter);
                    }
                    for (AudioFileFilter filter : filters) {
                        if (filter.getType().getExtension().toUpperCase().equals("WAV")) {
                            chooser.setFileFilter(filter);
                        }
                    }

                    /*
					 * chooser .setFileFilter(new MyFileFilter(".wav", "Wav
					 * files"));
                     */
                    if (chooser.showSaveDialog(FrinikaFrame.this) == JFileChooser.APPROVE_OPTION) {
                        if (chooser.getFileFilter() instanceof AudioFileFilter) {
                            if (!chooser.getFileFilter().accept(
                                    chooser.getSelectedFile())) {
                                chooser.setSelectedFile(new File(chooser
                                        .getSelectedFile().getPath()
                                        + "."
                                        + ((AudioFileFilter) chooser
                                                .getFileFilter()).getType()
                                                .getExtension().toLowerCase()));
                            }
                        }
                        FrinikaSequencer sequencer = project.getSequencer();
                        long startTick = sequencer.getLoopStartPoint();
                        long endTick = sequencer.getLoopEndPoint();
                        javax.sound.sampled.AudioFileFormat.Type type = ((AudioFileFilter) chooser.getFileFilter())
                                .getType();
                        new ExportWavDialog(FrinikaFrame.this, project, type,
                                chooser.getSelectedFile(), startTick, endTick);

                        project.getSequencer().stop();
                    }
                } catch (HeadlessException ex) {
                    ex.printStackTrace();
                }
            }
        });

        fileMenu.add(exportWavMenuItem);

        /*
		 * final JMenuItem exportWavPerTrackMenuItem = new JMenuItem( "Export
		 * wav per track"); exportWavPerTrackMenuItem.addActionListener(new
		 * ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent e) { try { JFileChooser
		 * chooser = new JFileChooser();
		 * chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); chooser
		 * .setDialogTitle("Export wav per track - select directory"); if
		 * (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) { //
		 * Mute all synths for (int n = 0; n < synth.getNumberOfSynths(); n++) {
		 * Synth currentSynth = synth.getSynth(n); if (currentSynth != null) {
		 * currentSynth.setMute(true); } } // Export one wavfile per synth for
		 * (int n = 0; n < synth.getNumberOfSynths(); n++) { Synth currentSynth =
		 * synth.getSynth(n); if (currentSynth != null) {
		 * currentSynth.setMute(false); String fileName = n + "_" +
		 * currentSynth.getInstrumentName() + ".wav"; File file = new
		 * File(chooser.getSelectedFile(), fileName); new ExportWavDialog(
		 * frame, sequencer, voiceServer, file, sectionPanel.getSectionStart()
		 * mySeq.getResolution(), (sectionPanel.getSectionStart() + sectionPanel
		 * .getSectionLength()) mySeq.getResolution()); sequencer.stop();
		 * currentSynth.setMute(true); } } // UnMute all synths for (int n = 0;
		 * n < synth.getNumberOfSynths(); n++) { Synth currentSynth =
		 * synth.getSynth(n); if (currentSynth != null) {
		 * currentSynth.setMute(false); } } } ; } catch (Exception ex) {
		 * ex.printStackTrace(); } } });
		 * 
		 * fileMenu.add(exportWavPerTrackMenuItem);
		 * 
         */
        if (!isMac()) {

            fileMenu.addSeparator();

            final JMenuItem quitMenuItem = new JMenuItem(CurrentLocale.getMessage("quit"));
            quitMenuItem.setMnemonic(KeyEvent.VK_Q);
            quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            quitMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    FrinikaMain.getInstance().quit(FrinikaFrame.this);
                }
            });

            fileMenu.add(quitMenuItem);
        }

        return fileMenu;
    }

    JMenu debugMenu() {

        JMenu debugMenu = new JMenu("debug"); // new

        JMenuItem item = new JMenuItem(new DisplayStructureAction(this));
        debugMenu.add(item);

        item = new JMenuItem("Buffered Playback");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedPlayback bp = new BufferedPlayback(FrinikaFrame.this,
                        getProjectContainer());
                bp.start();
            }
        });
        debugMenu.add(item);

        item = new JMenuItem(new MidiIMonitorAction());
        debugMenu.add(item);

        // item = new JMenuItem(new SynthRenderAction(this));
        // debugMenu.add(item);
        // This is how you get rid of the 1.6 stuff !!!
        // deprecated ?
        if (false) {
            ClassLoader loader = ClassLoader.getSystemClassLoader();

            try {

                Class clazz = loader.loadClass("uk.co.simphoney.music.Plug");
                Class params[] = {FrinikaFrame.class};
                Constructor plug = clazz.getConstructor(params);
                Object args[] = {this};
                plug.newInstance(args);
                // debugMenu.add(x); // new SimphoneyMenu(project));

            } catch (ClassNotFoundException e1) {
                System.out
                        .println(" info: Unable to load Simphoney Menus. Your probably using java < 1.6 ?");
                // TODO Auto-generated catch block
                // e1.printStackTrace();
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        item = new JMenuItem("ConfigListener");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ConfigListener l = new ConfigListener() {

                    @Override
                    public void configurationChanged(ChangeEvent event) {
                        ConfigurationProperty<?> meta = (ConfigurationProperty<?>) event
                                .getSource();

                        System.out.println("configuration changed: "
                                + meta.getName() + "="
                                + meta.getValue());
                    }

                };

                FrinikaConfig.addConfigListener(l); // once added, will continue
                // displaying all changes in
                // config

            }
        });
        debugMenu.add(item);

        return debugMenu;
    }

    JMenu renderMenu() {
        JMenu menu = new JMenu(CurrentLocale.getMessage("project.menu.render"));
        menu.setMnemonic(KeyEvent.VK_R);

        JMenuItem item = new JMenuItem("Render Selected Timeline");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (project.getSequencer().isRunning()) {
                    project.getSequencer().stop();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                if (project.getSequencer().getLoopStartPoint() == project
                        .getSequencer().getLoopEndPoint()) {
                    JOptionPane.showMessageDialog(FrinikaFrame.this,
                            "Please select timeline using loop markers!",
                            "Render Selected Timeline",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                project.getRenderer().render(FrinikaFrame.this,
                        project.getSequencer().getLoopStartPoint(),
                        project.getSequencer().getLoopEndPoint());
            }
        });
        menu.add(item);

        item = new JMenuItem("Rerender Selected Timeline");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (project.getSequencer().isRunning()) {
                    project.getSequencer().stop();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                if (project.getSequencer().getLoopStartPoint() == project
                        .getSequencer().getLoopEndPoint()) {
                    JOptionPane.showMessageDialog(FrinikaFrame.this,
                            "Please select timeline using loop markers!",
                            "Rerender Selected Timeline",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                project.getRenderer().rerender(FrinikaFrame.this,
                        project.getSequencer().getLoopStartPoint(),
                        project.getSequencer().getLoopEndPoint());
            }
        });
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Clear Render Cache");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (project.getSequencer().isRunning()) {
                    project.getSequencer().stop();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                project.getRenderer().purgeRenderCache();
            }
        });
        menu.add(item);

        menu.addSeparator();
        final JCheckBoxMenuItem item_supress_realtime = new JCheckBoxMenuItem(
                "Suppress RealTime Devices while playing");
        item_supress_realtime.setSelected(true);
        item_supress_realtime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                project.getRenderer().setSupressRealTime(
                        item_supress_realtime.isSelected());
            }
        });
        menu.add(item_supress_realtime);

        return menu;
    }

    private ActionListener addMidiDevices_listener = null;

    private MidiDevice addMidiDevices_selected = null;

    private Icon addMidiDevices_selected_icon = null;

    @Override
    public void addMidiDevices(JComponent menu, List<MidiDevice.Info> infos,
            List<Icon> icons) {
        Iterator<Icon> icon_iterator = icons.iterator();
        for (MidiDevice.Info info : infos) {
            JMenuItem item = new JMenuItem(info.toString());
            item.setIcon(icon_iterator.next());
            final Icon icon = item.getIcon();
            final MidiDevice.Info f_info = info;
            item.addActionListener(new ActionListener() {
                MidiDevice.Info m_info = f_info;

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub

                    MidiDevice midiDevice = null;
                    try {
                        midiDevice = MidiSystem.getMidiDevice(m_info);

                        if (addMidiDevices_listener != null) {
                            addMidiDevices_selected = midiDevice;
                            addMidiDevices_selected_icon = icon;
                            addMidiDevices_listener.actionPerformed(null);
                            return;
                        }

                        midiDevice = new SynthWrapper(project, midiDevice);

                    } catch (MidiUnavailableException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }

                    try {
                        project.getEditHistoryContainer().mark("X");
                        project.addMidiOutDevice(midiDevice);
                        project.getEditHistoryContainer()
                                .notifyEditHistoryListeners();
                    } catch (MidiUnavailableException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }
                    midiDevicesPanel.updateDeviceTabs();
                }
            });
            menu.add(item);
        }
    }

    @Override
    public void addMidiDevices(JComponent menu) {
        List<MidiDevice.Info> infos = new ArrayList<>();
        List<Icon> icons = new ArrayList<>();

        List<MidiDevice.Info> infos1 = new ArrayList<>();
        List<Icon> icons1 = new ArrayList<>();

        List<MidiDevice.Info> infos2 = new ArrayList<>();
        List<Icon> icons2 = new ArrayList<>();

        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                if (dev.getMaxReceivers() != 0) {
                    Icon icon = getMidiDeviceIcon(dev);
                    if (dev instanceof Synthesizer) {
                        infos1.add(info);
                        icons1.add(icon);
                    } else if (icon == DEFAULT_MIDI_ICON) {
                        infos2.add(info);
                        icons2.add(icon);
                    } else {
                        infos.add(info);
                        icons.add(icon);
                    }
                }

            } catch (MidiUnavailableException e) {
            }
        }

        addMidiDevices(menu, infos1, icons1);
        menu.add(new JSeparator());
        addMidiDevices(menu, infos, icons);
        menu.add(new JSeparator());
        addMidiDevices(menu, infos2, icons2);
    }

    @Override
    public MidiDevice selectMidiDevice() {
        try {

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("MIDI Out Device:"));
            final JButton but = new JButton("No Device Selected");
            but.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    JPopupMenu popup = new JPopupMenu();
                    addMidiDevices(popup);
                    popup.show(but, 0, 0);
                }

            });

            addMidiDevices_selected = null;
            addMidiDevices_listener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    but.setText(addMidiDevices_selected.getDeviceInfo()
                            .getName());
                    but.setIcon(addMidiDevices_selected_icon);
                }
            };

            panel.add(but);

            int opt = JOptionPane.showConfirmDialog(this, panel,
                    "Select MIDI Output", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            addMidiDevices_listener = null;
            if (opt == JOptionPane.OK_OPTION) {
                return addMidiDevices_selected;
            }
        } finally {
            addMidiDevices_listener = null;
        }

        return null;
    }

    JMenu newDeviceMenu() {
        final JMenu newDeviceMenu = new JMenu(CurrentLocale.getMessage("newdevice.menu"));
        newDeviceMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                // System. out.println("mem_sel");
                newDeviceMenu.removeAll();
                addMidiDevices(newDeviceMenu);
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        return newDeviceMenu;
    }

    JMenu editMenu() {

        JMenu editMenu = new CutPasteMenu(project); // new
        // MultiEventEditMenu(project);

        editMenu.add(new JSeparator());
        JMenuItem item = new JMenuItem(new CreateMidiLaneAction(this));
        item.setIcon(getIconResource("new_track_midi.gif"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        editMenu.add(item);

        item = new JMenuItem(new CreateMidiDrumLaneAction(this));
        item.setIcon(getIconResource("new_track_midi_drum.gif"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        editMenu.add(item);

        item = new JMenuItem(new CreateAudioLaneAction(this));
        item.setIcon(getIconResource("new_track_audio.gif"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        editMenu.add(item);

        // item = new JMenuItem(new CreateXAudioLaneAction(this));
        // item.setIcon(getIconResource("new_track_audio.gif"));
        // item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit
        // .getDefaultToolkit().getMenuShortcutKeyMask()
        // | KeyEvent.SHIFT_MASK));
        // editMenu.add(item);
        item = new JMenuItem(new CreateTextLaneAction(this)); // Jens
        item.setIcon(getIconResource("new_track_text.gif"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        editMenu.add(item);
        editMenu.addSeparator();
        editMenu.add(newDeviceMenu());

        item = new JMenuItem(new CreateMidiLaneAction(this));
        item.setIcon(getIconResource("new_track_midi.gif"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        newLaneMenu.add(item);

        item = new JMenuItem(new CreateMidiDrumLaneAction(this));
        item.setIcon(getIconResource("new_track_midi_drum.gif"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        newLaneMenu.add(item);

        item = new JMenuItem(new CreateAudioLaneAction(this));
        item.setIcon(getIconResource("new_track_audio.gif"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        newLaneMenu.add(item);
        item = new JMenuItem(new CreateTextLaneAction(this)); // Jens
        item.setIcon(getIconResource("new_track_text.gif"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        newLaneMenu.add(item);
        newLaneMenu.addSeparator();
        newLaneMenu.add(newDeviceMenu());

        editMenu.addSeparator();

        final JMenuItem bounceToLane = new JMenuItem(
                CurrentLocale.getMessage("project.menu.file.bounce_to_lane"));

        bounceToLane.setIcon(getIconResource("new_track_audio.gif"));

        bounceToLane.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Lane lane = project.getLaneSelection().getFocus();
                project.getEditHistoryContainer().mark(
                        CurrentLocale.getMessage("sequencer.audiolane.record"));
                if (lane == null || !(lane instanceof AudioLane)) {
                    lane = project.createAudioLane();
                }

                File file = ((AudioLane) lane).newFilename();
                FrinikaSequencer sequencer = project.getSequencer();
                long startTick = sequencer.getLoopStartPoint();
                long endTick = sequencer.getLoopEndPoint();

                new BounceToLane(FrinikaFrame.this, project, file, startTick,
                        endTick, (AudioLane) lane);

                project.getSequencer().stop();
            }

        });

        editMenu.add(bounceToLane);

        item = new JMenuItem(new ImportAudioAction(this));
        item.setIcon(getIconResource("import.gif"));
        editMenu.add(item);

        item = new JMenuItem(new ImportMidiToLaneAction(this));
        item.setIcon(getIconResource("import.gif"));
        editMenu.add(item);

        // editMenu.add(item);
        // item = new JMenuItem(new ImportAudioAction(this));
        // item = new JMenuItem(new DeleteLaneAction(this));
        // editMenu.add(item);
        // item = new JMenuItem(new SplitLaneAction(this));
        // editMenu.add(item);
        editMenu.add(new JSeparator());
        item = new JMenuItem(new RepeatAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_R);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
        editMenu.add(item);

        /*
		 * item = new JMenuItem(new RepeatAction(this));
		 * //item.setText(item.getText()+"..."); // hack
		 * item.setMnemonic(KeyEvent.VK_R);
		 * item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
		 * midiPartMenu.add(item);
         */
        editMenu.add(new JMenuItem(new SplitSelectedPartsAction(this)));
        // midiPartMenu.add(new JMenuItem(new SplitSelectedPartsAction(this)));

        // editMenu.add(new JSeparator());
        // editMenu.add(new JMenuItem(new ImportAudioAction(this)));
        // editMenu.add(new JMenuItem(new AudioPartToMidiAction(this)));
        // audioPartMenu.add(new JMenuItem(new AudioPartToMidiAction(this)));
        // audioPartMenu.add(new JMenuItem(new AudioAnalysisAction(this)));
        editMenu.add(new JSeparator());

        editMenu.add(new TimeSignatureEditAction(this));
        editMenu.add(new TempoListEditAction(this));

        return editMenu;

    }

    // ------------------------------------ Settings menu
    // ------------------------------------
    JMenu settingsMenu() {

        JMenu settingsMenu = new JMenu(CurrentLocale.getMessage("project.menu.settings"));
        settingsMenu.setMnemonic(KeyEvent.VK_S);

        /*
		 * try { settingsMenu.add(new MidiSetupMenuItem(this)); } catch
		 * (MidiUnavailableException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
         */
        JMenuItem item;

        item = new JMenuItem(new MidiInSetupAction());
        item.setIcon(getIconResource("midi.png"));
        settingsMenu.add(item);

        /*
		 * item=new JMenuItem(new AudioInSetupAction()); settingsMenu.add(item);
         */
        settingsMenu.addSeparator();

        item = new JMenuItem(new SetAudioOutputAction(this));
        settingsMenu.add(item);
        item.setIcon(getIconResource("output.gif"));

        // boolean dm = FrinikaConfig.getDirectMonitoring();
        item = new CheckBoxMenuItemConfig(
                CurrentLocale.getMessage("project.menu.settings.direct_monitoring"),
                FrinikaGlobalProperties.DIRECT_MONITORING);
        /*
		 * item.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * FrinikaConfig.setDirectMonitoring(((JCheckBoxMenuItem) e
		 * .getSource()).isSelected()); } });
         */
        settingsMenu.add(item);

        // String multiS=FrinikaConfig.getProperty("multiplexed_audio");
        // boolean ma=false;
        // if (multiS != null) {
        // ma=Boolean.parseBoolean(multiS);
        // }
        item = new CheckBoxMenuItemConfig(
                CurrentLocale.getMessage("project.menu.settings.multiplexed_audio"),
                FrinikaGlobalProperties.MULTIPLEXED_AUDIO);
        /*
		 * item.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * FrinikaConfig.setMultiplexedAudio(((JCheckBoxMenuItem)e.getSource()).isSelected());
		 * //FrinikaConfig.setProperty("multiplexed_audio",String.valueOf(((JCheckBoxMenuItem)
		 * e // .getSource()).isSelected())); } });
         */
        settingsMenu.add(item);

        item = new JMenuItem(CurrentLocale.getMessage("project.menu.settings.audio_latency"));
        item.setIcon(getIconResource("output.gif"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrinikaAudioSystem.latencyMeasureSet();
            }
        });
        // audioDevicesMenuItem.setMnemonic(KeyEvent.VK_A);
        settingsMenu.add(item);

        settingsMenu.add(item);
        item = new JMenuItem(
                CurrentLocale.getMessage("project.menu.settings.configure_audio_server"));
        item.setIcon(getIconResource("output.gif"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrinikaAudioSystem.configure();
            }
        });
        // audioDevicesMenuItem.setMnemonic(KeyEvent.VK_A);

        settingsMenu.add(item);

        // boolean ac = FrinikaConfig.getJackAutoconnect();
        settingsMenu.addSeparator();

        item = new CheckBoxMenuItemConfig(
                CurrentLocale.getMessage("project.menu.settings.jack_autoconnect"),
                FrinikaGlobalProperties.JACK_AUTO_CONNECT);
        /*
		 * item.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * FrinikaConfig.setJackAutoconnect(((JCheckBoxMenuItem) e
		 * .getSource()).isSelected()); } });
         */
        settingsMenu.add(item);

        settingsMenu.addSeparator();

        settingsMenu.add(new GuiCursorUpdateMenu(project));

        settingsMenu.addSeparator();

        item = new JMenuItem(CurrentLocale.getMessage("project.menu.settings.show"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDialog(FrinikaFrame.this);
            }
        });
        settingsMenu.add(item);

        return settingsMenu;
    }

    JMenu toolsMenu() {
        JMenu toolsMenu = new JMenu(CurrentLocale.getMessage("project.menu.tools"));
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        // project.getMidiSelection().addMenuItem(midiMenu);

        JMenuItem item = new JMenuItem(new MidiQuantizeAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_Q);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0));
        toolsMenu.add(item);
        project.getMidiSelection().addMenuItem(item);

        item = new JMenuItem(new MidiTransposeAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_T);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
        toolsMenu.add(item);
        project.getMidiSelection().addMenuItem(item);

        item = new JMenuItem(new MidiVelocityAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_V);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
        toolsMenu.add(item);
        project.getMidiSelection().addMenuItem(item);

        item = new JMenuItem(new MidiShiftAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_S);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
        toolsMenu.add(item);
        project.getMidiSelection().addMenuItem(item);

        item = new JMenuItem(new MidiDurationAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_D);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
        toolsMenu.add(item);
        project.getMidiSelection().addMenuItem(item);

        item = new JMenuItem(new MidiTimeStretchAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_M);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
        toolsMenu.add(item);
        project.getMidiSelection().addMenuItem(item);

        item = new JMenuItem(new MidiReverseAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_E);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));
        toolsMenu.add(item);
        project.getMidiSelection().addMenuItem(item);

        item = new JMenuItem(new MidiInsertControllersAction(project));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_I);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0));
        toolsMenu.add(item);
        project.getMidiSelection().addMenuItem(item);

        toolsMenu.addSeparator();

        item = new JMenuItem(new MidiStepRecordAction(project));
        item.setText(item.getText() + "..."); // hack
        item.setMnemonic(KeyEvent.VK_INSERT);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
        toolsMenu.add(item);
        // project.getMidiSelection().addMenuItem(item);

        toolsMenu.addSeparator();

        JMenu submenu = new JMenu(CurrentLocale.getMessage("sequencer.midi.groovepattern"));
        item = new JMenuItem(new GroovePatternManagerAction(this));
        item.setText(item.getText() + "..."); // hack
        item.setMnemonic(KeyEvent.VK_M);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        submenu.add(item);
        item = new JMenuItem(new GroovePatternCreateFromMidiPartAction(this));
        // item.setText(item.getText()+"..."); // hack
        item.setMnemonic(KeyEvent.VK_C);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()
                | KeyEvent.SHIFT_MASK));
        submenu.add(item);
        toolsMenu.add(submenu);

        toolsMenu.addSeparator();

        submenu = new JMenu(
                CurrentLocale.getMessage(ScriptingAction.actionId));
        ScriptingAction sa = new ScriptingAction(
                project);
        item = new JMenuItem(sa);
        item.setText(item.getText() + "..."); // hack
        item.setMnemonic(KeyEvent.VK_G);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        submenu.add(item);
        sa.initDialog(submenu);
        toolsMenu.add(submenu);

        /*
		 * toolsMenu.addSeparator();
		 * 
		 * item = new JMenuItem( CurrentLocale.getMessage("project.menu.settings.show"));
		 * item.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { SettingsDialog.showSettings(); } });
		 * 
		 * toolsMenu.add(item); // alternative: item = new JMenuItem(
		 * CurrentLocale.getMessage("project.menu.settings.show")); item.addActionListener(new
		 * ActionListener() { public void actionPerformed(ActionEvent e) {
		 * Config.showDialog(ProjectFrame.this); } });
		 * 
		 * toolsMenu.add(item);
         */
        toolsMenu.addSeparator();

        submenu = new JMenu("Generator");
        item = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrinikaSoundHelixPanel panel = new FrinikaSoundHelixPanel(project);
                JDialog dialog = WindowUtils.createDialog(panel, FrinikaFrame.this, Dialog.ModalityType.APPLICATION_MODAL);
                WindowUtils.addHeaderPanel(dialog, "SoundHelix Generator", "Generate song using SoundHelix generator", panel.getHeaderIcon());
                panel.setOkCancelListener(new WindowUtils.OkCancelListener() {
                    @Override
                    public void okEvent() {
                        WindowUtils.closeWindow(dialog);
                    }

                    @Override
                    public void cancelEvent() {
                        WindowUtils.closeWindow(dialog);
                    }
                });
                dialog.setLocationByPlatform(true);
                dialog.setVisible(true);
            }
        });
        item.setText("SoundHelix...");
        submenu.add(item);
        toolsMenu.add(submenu);

        return toolsMenu;
    }

    JMenu helpMenu() {

        // ------------------------------------ Help menu
        JMenu helpMenu = new JMenu(CurrentLocale.getMessage("project.menu.help"));
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutMenuItem = new JMenuItem(
                CurrentLocale.getMessage("project.menu.help.about"));
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutAction.about(FrinikaFrame.this);
            }
        });
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        helpMenu.add(aboutMenuItem);
        return helpMenu;
    }

    JMenu viewMenu() {
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        JMenuItem item = new JMenuItem(new ToggleShowVoiceViewAction(this));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask())); // Jens
        menu.add(item);
        return menu;
    }

    JMenu connectedMenu() {
        JMenu menu = new JMenu(CurrentLocale.getMessage("project.menu.connected.connected"));
        menu.setMnemonic(KeyEvent.VK_C);
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(CurrentLocale.getMessage("project.menu.connected.radio"));
        item.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (item.getState()) {
                    LocalOGGHttpRadio.startRadio(project);
                } else {
                    LocalOGGHttpRadio.stopRadio();
                }
            }
        });
        menu.add(item);
        return menu;
    }

    /**
     * Create menus
     *
     * @return
     */
    JMenuBar createMenu() {

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        menuBar.add(fileMenu());
        // menuBar.add(editMenu());
        menuBar.add(editMenu());
        // menuBar.add(transportMenu());
        if (viewport == null) {
            menuBar.add(viewMenu());
        } else {
            menuBar.add(perspectiveMenu());
        }
        menuBar.add(settingsMenu());
        menuBar.add(toolsMenu()); // Jens
        menuBar.add(renderMenu());
        menuBar.add(connectedMenu());
        if (!isMac()) {
            menuBar.add(helpMenu());
        }
        menuBar.add(debugMenu());

        // recording menu plugin.
        Part.addPluginRightButtonMenu(new MultiPartMenuPlugin(getProjectContainer()));

        return menuBar;
    }

    /**
     * The splitpane is created here
     *
     * @param frameSize
     * @return
     */
    JSplitPane createSplit(Dimension frameSize) {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setOneTouchExpandable(true);
        // Initialize editors

        pianoControllerPane = new PianoControllerSplitPane(project);
        TrackerPanel trackerPanel = new TrackerPanel(project.getSequence(),
                project);

        // frameSize.height = frameSize.height*2;
        frameSize.height = frameSize.height / 3;

        partViewEditor = new VoicePartViewSplitPane(this, false);

        partViewEditor.setPreferredSize(frameSize);

        project.getPartSelection().addSelectionListener(trackerPanel);

        JTabbedPane editorTabs = new JTabbedPane();
        editorTabs.setPreferredSize(frameSize);
        editorTabs.addTab("Piano roll", pianoControllerPane);
        editorTabs.addTab("Tracker", trackerPanel);
        JPanel mixer = new CompactMixerPanel(project.getMixerControls());
        mixer.setMinimumSize(new Dimension(0, 0));

        editorTabs.addTab(CurrentLocale.getMessage("project.maintabs.audiomixer"), mixer);

        split.setTopComponent(partViewEditor);
        split.setBottomComponent(editorTabs);
        return split;
    }

    @Override
    public void dispose() {
        project.close();
        FrinikaMain.getInstance().removeFrame(this);
        super.dispose();
    }

    @Override
    public FrinikaProjectContainer getProjectContainer() {
        return project;
    }

    /**
     *
     * @param string
     * @deprecated
     */
    @Override
    public void infoMessage(String string) {
        MessageDialogUtils.message(this, string);
    }

    MidiDevicesPanel midiDevicesPanel;

    @Override
    public MidiDevicesPanel getMidiDevicesPanel() {
        return midiDevicesPanel;
    }

    @Override
    public VoicePartViewSplitPane getVoicePartViewSplitPane() {
        // TODO Auto-generated method stub
        return partViewEditor;
    }

    public static void midiInDeviceChange() {
        System.out.println("MIDIIN CHANGER");
        MidiInDeviceManager.reset(FrinikaGlobalProperties.MIDIIN_DEVICES_LIST.getStringList());
    }

    @Override
    public void setStatusBarMessage(String message) {
        statusBar.setMessage(message);
    }

    /**
     * Opens the save-project dialog
     *
     */
    void openSaveProjectDialog() {
        try {

            ProjectFileFilter no_compression = new ProjectFileFilter();
            ProjectFileFilter zip_compressed = new ProjectFileFilter() {
                @Override
                public String getDescription() {
                    return "Frinika project (ZIP Compressed)";
                }
            };
            ProjectFileFilter lzma_compressed = new ProjectFileFilter() {
                @Override
                public String getDescription() {
                    return "Frinika project (LZMA Compressed)";
                }
            };

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Frinika project");
            chooser.setFileFilter(no_compression);
            chooser.setFileFilter(zip_compressed);
            chooser.setFileFilter(lzma_compressed);

            chooser.setFileFilter(no_compression);
            chooser.setSelectedFile(project.getProjectFile());
            if (chooser.showSaveDialog(FrinikaFrame.this) == JFileChooser.APPROVE_OPTION) {
                File newProject = chooser.getSelectedFile();
                if (chooser.getFileFilter() instanceof ProjectFileFilter) {
                    if (!chooser.getFileFilter().accept(newProject)) {
                        newProject = new File(newProject.getPath() + ".frinika");
                    }
                }

//				int saveOption = JOptionPane
//						.showOptionDialog(
//								ProjectFrame.this,
//								CurrentLocale.getMessage("project.save.include_referenced_data"),
//								CurrentLocale.getMessage("project.save.include_referenced_data.title"),
//								JOptionPane.YES_NO_OPTION,
//								JOptionPane.QUESTION_MESSAGE, null, null, null);
//				if (saveOption == 1)
//					project.setSaveReferencedData(false);
//				else if (saveOption == 0)
//					project.setSaveReferencedData(true);
                project.setSaveReferencedData(withRef);

                if (chooser.getFileFilter() == lzma_compressed) {
                    project.compression_level = 2;
                } else if (chooser.getFileFilter() == zip_compressed) {
                    project.compression_level = 1;
                } else {
                    project.compression_level = 0;
                }

                project.saveProject(newProject);
                FrinikaConfig.setLastProject(newProject);
                setTitle(newProject.getName());
            }
        } catch (HeadlessException | IOException ex) {
            MessageDialogUtils.error(this, "Error while saving", ex); // show error message, user
            // should know that saving went
            // wrong
            ex.printStackTrace();
        }
    }

    @Override
    public void showRightButtonPartPopup(Component invoker, int x, int y) {

        // System.out.println(" RIGHT BUTTON PRESS ");
        Part part = project.getPartSelection().getFocus();
        if (part == null) {
            return;
        }
        if (part.showRightButtonMenu(invoker, x, y)) {
            return;
        }

        part.showContextMenu(this, invoker, x, y);

        /*
		 * // TODO put these into the parts ! (done, see
		 * Part.initContextMenu(..) (Jens)) if (part instanceof AudioPart ) {
		 * audioPartMenu.show(invoker,x,y); } else if(part instanceof MidiPart ) {
		 * midiPartMenu.show(invoker,x,y); }
         */
    }

    /**
     * CheckBoxMenuItem which is directly bound to a boolean config value.
     *
     * @author Jens Gulden
     */
    static class CheckBoxMenuItemConfig extends JCheckBoxMenuItem implements
            ActionListener, ConfigListener {

        private ConfigurationProperty<Boolean> configOption;

        public CheckBoxMenuItemConfig(String text,
                ConfigurationProperty<Boolean> configOption) {
            super(text);
            this.configOption = configOption;
            if ((configOption.getType() == boolean.class)
                    || (configOption.getType() == Boolean.class)) {
                refresh();
            } else {
                throw new ConfigError(
                        "cannot bind checkbox-menu-item to option '"
                        + configOption.getName()
                        + "' which is of type "
                        + configOption.getType().getName());
            }
            this.addActionListener(this);
            FrinikaConfig.addConfigListener(this);
        }

        // @Override
        @Override
        public void actionPerformed(ActionEvent e) {
            update();
        }

        // @Override
        @Override
        public void configurationChanged(ChangeEvent e) {
            if (e.getSource() == configOption) {
                refresh();
            }
        }

        private void refresh() {
            boolean b = Boolean.valueOf(configOption.getValue().toString());
            this.setSelected(b);
        }

        private void update() {
            boolean b = this.isSelected();
            configOption.setValue(b);
        }
    }

    private static boolean ismac = false;

    public static boolean isMac() {
        return ismac;
    }

    public static void showDialog(ProjectFrame frame) {
        if (showingDialog != null) { // already showing (or initialized and hidden)?
            if (showingDialogFrame == frame) { // for same frame?
                showingDialog.show();
                showingDialog.toFront(); // then just put to front
                return;
            } else { // showing for different frame: close old one first
                showingDialog.dispose();
            }
        }
        showingDialogFrame = frame.getFrame();
        showingDialog = createDialog(frame);
        showingDialog.show();
    }

    protected static JDialog createDialog(ProjectFrame frame) {
        ConfigDialogPanel configDialogPanel = new ConfigDialogPanel(frame.getFrame());
        Map<FrinikaGlobalProperty, Object> map = ConfigDialogPanel.bindMap(configDialogPanel);
        //Map<Field, Object> map = FrinikaConfig.convertMap(m);
        /*Object[][] m2 = dynamicBindMap(configDialogPanel);
		Map<String, Object> map2 = new HashMap<String, Object>();
		for (int i = 0; i < m2.length; i++) {
			map2.put((String)m2[i][0], m2[i][1]);
		}*/
        //DefaultOptionsBinder optionsBinder = new DefaultOptionsBinder(map, map2, properties);
        DefaultOptionsBinder optionsBinder = new DefaultOptionsBinder(map, FrinikaConfig.getProperties());
        ConfigDialog dialog = new ConfigDialog(frame, optionsBinder);
        PresentationPanel presentationPanel = new PresentationPanel(configDialogPanel.getTabbedPane());
        dialog.getContentPane().add(presentationPanel, BorderLayout.CENTER);
        return dialog;
    }
}
