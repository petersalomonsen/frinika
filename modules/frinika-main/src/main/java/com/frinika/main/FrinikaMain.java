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

import com.frinika.base.FrinikaAudioSystem;
import com.frinika.global.FrinikaConfig;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.global.property.RecentProjectRecord;
import com.frinika.gui.util.WindowUtils;
import com.frinika.main.action.CreateProjectAction;
import com.frinika.main.action.OpenProjectAction;
import com.frinika.main.model.ExampleProjectFile;
import com.frinika.main.model.ProjectFileRecord;
import com.frinika.main.panel.WelcomePanel;
import com.frinika.project.FrinikaProjectContainer;
import com.frinika.settings.SetupDialog;
import com.frinika.tootX.midi.MidiInDeviceManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JFrame;

/**
 * Main class for Frinika handling multiple frames and welcome window.
 *
 * @author hajdam
 */
public class FrinikaMain {

    private static final String DOWNLOAD_PATH_PREFIX = "http://sourceforge.net/projects/frinika/files/frinika-example-projects/Frinka-example-projects/";
    private static final String DOWNLOAD_PATH_POSTFIX = "/download";

    private final FrinikaExitHandler exitHook = new FrinikaExitHandler();

    public FrinikaMain() {
    }

    public void startFrinika(@Nullable String argProjectFile) {
        Runtime.getRuntime().addShutdownHook(exitHook);

        FrinikaFrame.addProjectFocusListener((FrinikaProjectContainer project) -> {
            FrinikaAudioSystem.installClient(project.getAudioClient());
        });

        FrinikaAudioSystem.getAudioServer().start();

//        try {
        JFrame welcomeFrame = new JFrame();
        welcomeFrame.setTitle("Welcome to Frinika");
        welcomeFrame.setIconImage(new javax.swing.ImageIcon(FrinikaMain.class.getResource("/icons/frinika.png")).getImage());
        welcomeFrame.setResizable(false);

        if (argProjectFile != null) {
            OpenProjectAction openProjectAction = new OpenProjectAction();
            openProjectAction.setSelectedFile(new File(argProjectFile));
            openProjectAction.actionPerformed(null);
            welcomeFrame.setVisible(false);
        } else {
            WelcomePanel welcomePanel = new WelcomePanel();
            setupWelcomePanel(welcomeFrame, welcomePanel);
            WindowUtils.initWindowByComponent(welcomeFrame, welcomePanel);
            WindowUtils.setWindowCenterPosition(welcomeFrame);
        }

        welcomeFrame.setVisible(true);

//
//        exitHook = new FrinikaExitHandler();
//        Runtime.getRuntime().addShutdownHook(exitHook);
//
//        FrinikaFrame.addProjectFocusListener(new ProjectFocusListener() {
//            @Override
//            public void projectFocusNotify(FrinikaProjectContainer project) {
//                FrinikaAudioSystem.installClient(project.getAudioClient());
//            }
//        });
//
//        SplashDialog.closeSplash();
//
//        FrinikaAudioSystem.getAudioServer().start();
    }

    @Nonnull
    private static File getSampleFile(@Nonnull String projectFileName) {
        File exampleFilesPath = FrinikaConfig.getExampleFilesPath();
        exampleFilesPath.mkdirs();
        return new File(exampleFilesPath, projectFileName);
    }

    private static void setupWelcomePanel(@Nonnull final JFrame welcomeFrame, @Nonnull WelcomePanel welcomePanel) {
        welcomePanel.setActionListener(new WelcomePanel.ActionListener() {
            @Override
            public void newProject() {
                welcomeFrame.setVisible(false);
                new CreateProjectAction().actionPerformed(null);
                welcomeFrame.setVisible(false);
            }

            @Override
            public void openProject() {
                String lastFile = FrinikaGlobalProperties.LAST_PROJECT_FILENAME.getValue();
                OpenProjectAction openProjectAction = new OpenProjectAction();
                if (lastFile != null) {
                    openProjectAction.setSelectedFile(new File(lastFile));
                }
                openProjectAction.actionPerformed(null);
                welcomeFrame.setVisible(false);
            }

            @Override
            public void configureAudio() {
                SetupDialog.showSettingsModal();
            }

            @Override
            public void closeDialog() {
                welcomeFrame.setVisible(false);

                System.exit(0);
            }

            @Override
            public void openRecentProject(@Nonnull ProjectFileRecord projectFileRecord) {
                try {
                    String projectName = projectFileRecord.getProjectName();
                    String filePath = projectFileRecord.getFilePath();
                    FrinikaConfig.setLastProject(filePath, projectName);
                    File file = new File(filePath);
                    FrinikaFrame projectFrame = new FrinikaFrame();
                    ProgressOperation.openProjectFile(projectFrame, file);
                    welcomeFrame.setVisible(false);
                } catch (Exception ex) {
                    Logger.getLogger(FrinikaMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void openExampleProject(@Nonnull ProjectFileRecord projectFileRecord) {
                try {
                    FrinikaFrame projectFrame = new FrinikaFrame();

                    final File projectFile = getSampleFile(projectFileRecord.getFilePath());
                    if (!projectFile.exists()) {
                        String downloadUrl = DOWNLOAD_PATH_PREFIX + projectFileRecord.getFilePath() + DOWNLOAD_PATH_POSTFIX;
                        ProgressOperation.downloadSampleFile(projectFrame, projectFile, downloadUrl);
                    }

                    if (projectFile.exists()) {
                        ProgressOperation.openProjectFile(projectFrame, projectFile);
                        welcomeFrame.setVisible(false);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FrinikaMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void saveDefaultTheme(@Nullable String theme) {
                FrinikaGlobalProperties.THEME.setValue(theme);
            }
        });

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

        List<ProjectFileRecord> sampleProjects = new ArrayList<>();
        for (ExampleProjectFile exampleFile : ExampleProjectFile.values()) {
            sampleProjects.add(new ProjectFileRecord(exampleFile.getName(), exampleFile.getFileName()));
        }
        welcomePanel.setExampleProjects(sampleProjects);

        String theme = FrinikaGlobalProperties.THEME.getValue();
        welcomePanel.setInitialTheme(theme);
    }

    private static class FrinikaExitHandler extends Thread {

        @Override
        public void run() {
            MidiInDeviceManager.close();
            FrinikaAudioSystem.close();
//			SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//					System.out.println(" Closing ALL midi devices ");
//					ProjectContainer.closeAllMidiOutDevices();
//				}
//			});
        }
    }

}
