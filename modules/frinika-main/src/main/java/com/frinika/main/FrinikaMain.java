/*
 * Copyright (c) Frinika
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

import com.frinika.audio.asio.AsioAudioServerServiceProvider;
import com.frinika.audio.jnajack.JackTootAudioServerServiceProvider;
import com.frinika.audio.dummy.DummyAudioServerServiceProvider;
import com.frinika.audio.osx.OSXAudioServerServiceProvider;
import com.frinika.base.FrinikaAudioSystem;
import com.frinika.base.FrinikaControl;
import com.frinika.global.FrinikaConfig;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.global.property.RecentProjectRecord;
import com.frinika.gui.util.WindowUtils;
import com.frinika.localization.CurrentLocale;
import com.frinika.main.action.CreateProjectAction;
import com.frinika.main.action.OpenProjectAction;
import com.frinika.main.model.ExampleProjectFile;
import com.frinika.main.model.ProjectFileRecord;
import com.frinika.main.panel.AudioSetupPanel;
import com.frinika.main.panel.WelcomePanel;
import com.frinika.project.FrinikaProjectContainer;
import com.frinika.project.gui.ProjectFocusListener;
import com.frinika.toot.FrinikaAudioServerServiceProvider;
import com.frinika.tootX.midi.MidiInDeviceManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.midi.Sequence;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import uk.org.toot.audio.server.AudioServerServices;
import uk.org.toot.audio.server.TootAudioServerServiceProvider;
import uk.org.toot.audio.server.spi.AudioServerServiceProvider;

/**
 * Main class for Frinika handling multiple frames and welcome window.
 *
 * @author hajdam
 */
public class FrinikaMain {

    private static final String DOWNLOAD_PATH_PREFIX = "http://sourceforge.net/projects/frinika/files/frinika-example-projects/Frinka-example-projects/";
    private static final String DOWNLOAD_PATH_POSTFIX = "/download";

    private final FrinikaExitHandler exitHook = new FrinikaExitHandler();
    private final List<ProjectFocusListener> projectFocusListeners = new ArrayList<>();

    private JFrame welcomeFrame = null;
    private WelcomePanel welcomePanel = null;

    private final List<FrinikaFrame> openProjectFrames = new ArrayList<>();
    private FrinikaFrame focusFrame = null;

    private static FrinikaMain instance = null;

    private FrinikaMain() {
    }

    @Nonnull
    public static FrinikaMain getInstance() {
        if (instance == null) {
            instance = new FrinikaMain();
        }

        return instance;
    }

    public void startFrinika(@Nullable String argProjectFile) {
        Runtime.getRuntime().addShutdownHook(exitHook);

        addProjectFocusListener((FrinikaProjectContainer project) -> {
            FrinikaAudioSystem.installClient(project.getAudioClient());
        });

        // TODO: This is generally bad approach to depend on deprecated / internal classes
        if (audioServicesScanCapable()) {
            // Java version up to 1.8
            AudioServerServices.scan();
        } else {
            // Workaround for SPI error in Java 9 and newer
            List<AudioServerServiceProvider> providers = new ArrayList<>();
            providers.add(new FrinikaAudioServerServiceProvider());
            providers.add(new TootAudioServerServiceProvider());
            providers.add(new AsioAudioServerServiceProvider());
            providers.add(new JackTootAudioServerServiceProvider());
            providers.add(new OSXAudioServerServiceProvider());
            providers.add(new DummyAudioServerServiceProvider());
            AudioServerServices.forceProviders(providers);
        }

        FrinikaAudioSystem.getAudioServer().start();

        FrinikaControl.getInstance().registerProjectHandler(new FrinikaControl.ProjectHandler() {
            @Override
            public void openProject(Sequence sequence) {
                try {
                    FrinikaProjectContainer newProject = new FrinikaProjectContainer(sequence);
                    FrinikaFrame newFrame = new FrinikaFrame();
                    newFrame.setProject(newProject);
                } catch (Exception ex) {
                    Logger.getLogger(FrinikaMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        if (argProjectFile != null) {
            try {
                OpenProjectAction.openProjectFile(new File(argProjectFile));
            } catch (Exception ex) {
                Logger.getLogger(FrinikaMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            getWelcomeFrameInstance().setVisible(true);
        }
    }

    /**
     * Returns frame using provided project or null if not found.
     *
     * @param project
     * @return Frinika frame or null
     */
    @Nullable
    public FrinikaFrame findFrame(@Nonnull FrinikaProjectContainer project) {
        for (FrinikaFrame frame : openProjectFrames) {
            if (frame.project == project) {
                return frame;
            }
        }

        return null;
    }

    protected void setFocusFrame(@Nonnull FrinikaFrame frame) {
        if (frame != focusFrame) {
            focusFrame = frame;
            System.out.println(" Setting focus project " + frame.getProjectContainer().getFile());
        }
    }

    @Nonnull
    public FrinikaFrame getFocusFrame() {
        Objects.requireNonNull(focusFrame, "Focused frame cannot be null");

        return focusFrame;
    }

    @Nonnull
    private static File getSampleFile(@Nonnull String projectFileName) {
        File exampleFilesPath = FrinikaConfig.getExampleFilesPath();
        exampleFilesPath.mkdirs();
        return new File(exampleFilesPath, projectFileName);
    }

    @Nonnull
    private JFrame getWelcomeFrameInstance() {
        if (welcomeFrame == null) {
            welcomeFrame = new JFrame();
            welcomeFrame.setTitle("Welcome to Frinika");
            welcomeFrame.setIconImage(new javax.swing.ImageIcon(FrinikaMain.class.getResource("/icons/frinika.png")).getImage());
            welcomeFrame.setResizable(false);

            welcomePanel = new WelcomePanel();
            setupWelcomePanel();
            WindowUtils.initWindowByComponent(welcomeFrame, welcomePanel);
            WindowUtils.setWindowCenterPosition(welcomeFrame);
            welcomeFrame.addWindowListener(welcomePanel.getWindowListener());
        }

        return welcomeFrame;
    }

    private void setupWelcomePanel() {
        welcomePanel.setActionListener(new WelcomePanel.ActionListener() {
            @Override
            public void newProject() {
                welcomeFrame.setEnabled(false);
                new CreateProjectAction().actionPerformed(null);
            }

            @Override
            public void openProject() {
                welcomeFrame.setEnabled(false);
                String lastFile = FrinikaGlobalProperties.LAST_PROJECT_FILENAME.getValue();
                OpenProjectAction openProjectAction = new OpenProjectAction(welcomeFrame);
                if (lastFile != null) {
                    openProjectAction.setSelectedFile(new File(lastFile));
                }
                openProjectAction.actionPerformed(null);
            }

            @Override
            public void configureAudio() {
                AudioSetupPanel audioSetupPanel = new AudioSetupPanel();
                Dimension panelSize = audioSetupPanel.getMinimumSize();
                JDialog audioSetupDialog = WindowUtils.createDialog(audioSetupPanel, welcomeFrame, Dialog.ModalityType.APPLICATION_MODAL);
                audioSetupDialog.setTitle("Audio Setup");
                audioSetupDialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        audioSetupPanel.close();
                    }
                });
                WindowUtils.addHeaderPanel(audioSetupDialog, "Primary Audio Device Setup", "Select primary audio device to be used by Frinika", new javax.swing.ImageIcon(FrinikaMain.class.getResource("/icons/frinika.png")));
                audioSetupDialog.setMinimumSize(panelSize);
                WindowUtils.centerWindowOnWindow(audioSetupDialog, welcomeFrame);
                audioSetupDialog.setVisible(true);
            }

            @Override
            public void closeDialog() {
                mainExit();
            }

            @Override
            public void openRecentProject(@Nonnull ProjectFileRecord projectFileRecord) {
                FrinikaFrame projectFrame = null;
                try {
                    welcomeFrame.setEnabled(false);
                    String projectName = projectFileRecord.getProjectName();
                    String filePath = projectFileRecord.getFilePath();
                    FrinikaConfig.setLastProject(filePath, projectName);
                    File file = new File(filePath);
                    projectFrame = new FrinikaFrame();
                    ProgressOperation.openProjectFile(projectFrame, file);
                } catch (Exception ex) {
                    Logger.getLogger(FrinikaMain.class.getName()).log(Level.SEVERE, null, ex);
                    if (projectFrame != null) {
                        FrinikaMain.getInstance().closeFrame(projectFrame);
                    }
                    welcomeFrame.setEnabled(true);
                }
            }

            @Override
            public void openExampleProject(@Nonnull ProjectFileRecord projectFileRecord) {
                FrinikaFrame projectFrame = null;
                try {
                    welcomeFrame.setEnabled(false);
                    projectFrame = new FrinikaFrame();

                    final File projectFile = getSampleFile(projectFileRecord.getFilePath());
                    if (!projectFile.exists()) {
                        String downloadUrl = DOWNLOAD_PATH_PREFIX + projectFileRecord.getFilePath() + DOWNLOAD_PATH_POSTFIX;
                        ProgressOperation.downloadSampleFile(projectFrame, projectFile, downloadUrl);
                    }

                    if (projectFile.exists()) {
                        ProgressOperation.openProjectFile(projectFrame, projectFile);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FrinikaMain.class.getName()).log(Level.SEVERE, null, ex);
                    if (projectFrame != null) {
                        FrinikaMain.getInstance().closeFrame(projectFrame);
                    }
                    welcomeFrame.setEnabled(true);
                }
            }

            @Override
            public void saveDefaultTheme(@Nullable String theme) {
                FrinikaGlobalProperties.THEME.setValue(theme);
            }
        });

        reloadRecentProjects();

        List<ProjectFileRecord> sampleProjects = new ArrayList<>();
        for (ExampleProjectFile exampleFile : ExampleProjectFile.values()) {
            sampleProjects.add(new ProjectFileRecord(exampleFile.getName(), exampleFile.getFileName()));
        }
        welcomePanel.setExampleProjects(sampleProjects);

        String theme = FrinikaGlobalProperties.THEME.getValue();
        welcomePanel.setInitialTheme(theme);
    }

    private void reloadRecentProjects() {
        List<ProjectFileRecord> recentProjects = new ArrayList<>();
        String lastProjectFile = FrinikaGlobalProperties.LAST_PROJECT_FILENAME.getValue();
        if (lastProjectFile != null) {
            String lastProjectName = FrinikaGlobalProperties.LAST_PROJECT_NAME.getValue();
            if (lastProjectName == null) {
                File lastFile = new File(lastProjectFile);
                lastProjectName = lastFile.getName();
            }
            ProjectFileRecord lastProject = new ProjectFileRecord(lastProjectName, lastProjectFile);
            recentProjects.add(lastProject);

            List<RecentProjectRecord> recentFiles = FrinikaGlobalProperties.RECENT_PROJECTS.getValue();
            if (recentFiles != null) {
                recentFiles.stream().map((recentFile) -> new ProjectFileRecord(recentFile.getProjectName(), recentFile.getProjectPath())).forEachOrdered((projectFileRecord) -> {
                    recentProjects.add(projectFileRecord);
                });
            }
        }
        welcomePanel.setRecentProjects(recentProjects);
    }

    public void addProjectFocusListener(@Nonnull ProjectFocusListener listener) {
        projectFocusListeners.add(listener);
    }

    public void removeProjectFocusListener(@Nonnull ProjectFocusListener listener) {
        projectFocusListeners.remove(listener);
    }

    public void notifyProjectFocusListeners() {
        projectFocusListeners.forEach((listener) -> {
            listener.projectFocusNotify(focusFrame.getProjectContainer());
        });
    }

    // TODO pass this as interface to frame instead of instance access
    public void addFrame(@Nonnull FrinikaFrame frame) {
        openProjectFrames.add(frame);

        // Hide welcome dialog on creation of project frame
        if (welcomeFrame != null) {
            if (welcomeFrame.isVisible()) {
                welcomeFrame.setVisible(false);
            }
        }
    }

    public void removeFrame(@Nonnull FrinikaFrame frame) {
        openProjectFrames.remove(frame);
    }

    public void closeFrame(@Nonnull FrinikaFrame frame) {
        if (canClose(frame)) {
            frame.dispose();

            // Reopen welcome dialog on close of last project frame
            if (openProjectFrames.isEmpty()) {
                welcomeFrame = getWelcomeFrameInstance();
                showWelcomePanel();
            }
        }
    }

    public void quit(@Nonnull FrinikaFrame frame) {
        boolean hasChanges = false;
        for (FrinikaFrame openFrame : openProjectFrames) {
            if (openFrame.hasChanges()) {
                hasChanges = true;
                break;
            }
        }

        String quitMessage;
        if (hasChanges) {
            quitMessage = CurrentLocale.getMessage("quit.doYouReallyWantTo.unsaved");
        } else {
            quitMessage = CurrentLocale.getMessage("quit.doYouReallyWantTo");
        }

        boolean quitApproved = (JOptionPane.showOptionDialog(frame, quitMessage,
                CurrentLocale.getMessage("quit.really"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null) == 0);

        if (quitApproved) {
            // Todo changed projects with checkboxes
            // Close frame one by one
            while (!openProjectFrames.isEmpty()) {
                FrinikaFrame openFrame = openProjectFrames.get(0);
                if (canClose(openFrame)) {
                    openFrame.dispose();
                }
            }

            mainExit();
        }
    }

    private void mainExit() {
        /**
         * VERY IMPORTANT! Make sure all midi-in devices are closed for
         * system.exit!
         */
        MidiInDeviceManager.close();
        FrinikaConfig.storeAndQuit();

        System.exit(0);
    }

    private boolean canClose(@Nonnull FrinikaFrame frame) {
        if (!frame.hasChanges()) {
            return true;
        }

        return JOptionPane.showOptionDialog(frame,
                CurrentLocale.getMessage("close.unsavedChanges"),
                CurrentLocale.getMessage("close.unsavedChanges.dialogTitle"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null) == 0;
    }

    private void showWelcomePanel() {
        reloadRecentProjects();
        welcomeFrame.setEnabled(true);
        welcomeFrame.setVisible(true);
        welcomeFrame.repaint();
    }

    private boolean audioServicesScanCapable() {
        String version = System.getProperty("java.version");
        int subVersionPos = version.indexOf('.');

        int mainVersion;
        if (subVersionPos > 0) {
            mainVersion = Integer.valueOf(version.substring(0, subVersionPos));
        } else {
            mainVersion = Integer.valueOf(version);
        }

        return mainVersion < 2;
    }

    private static class FrinikaExitHandler extends Thread {

        @Override
        public void run() {
            MidiInDeviceManager.close();
            FrinikaAudioSystem.close();
            SwingUtilities.invokeLater(() -> {
                System.out.println(" Closing ALL midi devices ");
                FrinikaProjectContainer.closeAllMidiOutDevices();
            });
        }
    }
}
