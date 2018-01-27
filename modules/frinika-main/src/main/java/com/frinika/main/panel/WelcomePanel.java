/*
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
package com.frinika.main.panel;

import com.frinika.gui.util.SupportedLaf;
import com.frinika.gui.util.WindowUtils;
import com.frinika.main.model.ProjectFileRecord;
import com.frinika.main.model.ProjectFileRecordCellRenderer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;

/**
 * Welcome panel for application start.
 *
 * @author hajdam
 */
public class WelcomePanel extends javax.swing.JPanel {

    private ActionListener actionListener = null;
    private final DefaultListModel<ProjectFileRecord> sampleListModel = new DefaultListModel<>();
    private final DefaultListModel<ProjectFileRecord> recentListModel = new DefaultListModel<>();

    public WelcomePanel() {
        initComponents();
        init();
    }

    private void init() {
        mainTitleTextPane.setOpaque(false);
        AboutPanel.initializeTextPane(mainTitleTextPane, AboutPanel.MAIN_TITLE);
        recentList.setModel(recentListModel);
        recentList.setCellRenderer(new ProjectFileRecordCellRenderer());
        recentList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    Object source = event.getSource();
                    if (source instanceof JList<?>) {
                        int index = ((JList<?>) source).getSelectedIndex();
                        if (index >= 0) {
                            ProjectFileRecord projectFileRecord = recentListModel.get(index);
                            actionListener.openRecentProject(projectFileRecord);
                        }
                    }
                }
            }
        });
        recentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Object source = event.getSource();
                    if (source instanceof JList<?>) {
                        int index = ((JList<?>) source).locationToIndex(event.getPoint());
                        if (index >= 0) {
                            ProjectFileRecord projectFileRecord = recentListModel.get(index);
                            actionListener.openRecentProject(projectFileRecord);
                        }
                    }
                }
            }
        });

        sampleList.setModel(sampleListModel);
        sampleList.setCellRenderer(new ProjectFileRecordCellRenderer());

        sampleList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    Object source = event.getSource();
                    if (source instanceof JList<?>) {
                        int index = ((JList<?>) source).getSelectedIndex();
                        if (index >= 0) {
                            ProjectFileRecord projectFileRecord = sampleListModel.get(index);
                            actionListener.openSampleProject(projectFileRecord);
                        }
                    }
                }
            }
        });
        sampleList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Object source = event.getSource();
                    if (source instanceof JList<?>) {
                        int index = ((JList<?>) source).locationToIndex(event.getPoint());
                        if (index >= 0) {
                            ProjectFileRecord projectFileRecord = sampleListModel.get(index);
                            actionListener.openSampleProject(projectFileRecord);
                        }
                    }
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lafButtonGroup = new javax.swing.ButtonGroup();
        projectsTabbedPane = new javax.swing.JTabbedPane();
        recentProjectsPanel = new javax.swing.JPanel();
        recentListScrollPane = new javax.swing.JScrollPane();
        recentList = new javax.swing.JList<>();
        sampleProjectsPanel = new javax.swing.JPanel();
        samplListScrollPane = new javax.swing.JScrollPane();
        sampleList = new javax.swing.JList<>();
        newProjectButton = new javax.swing.JButton();
        openProjectButton = new javax.swing.JButton();
        controlPanel = new javax.swing.JPanel();
        exitButton = new javax.swing.JButton();
        optionsPanel = new javax.swing.JPanel();
        configureAudioButton = new javax.swing.JButton();
        lafPanel = new javax.swing.JPanel();
        defaultLafToggleButton = new javax.swing.JToggleButton();
        darculaLafToggleButton = new javax.swing.JToggleButton();
        animatedLogoPanel = new com.frinika.main.panel.AnimatedLogoPanel();
        mainTitleTextPane = new javax.swing.JTextPane();

        recentProjectsPanel.setLayout(new java.awt.BorderLayout());

        recentListScrollPane.setViewportView(recentList);

        recentProjectsPanel.add(recentListScrollPane, java.awt.BorderLayout.CENTER);

        projectsTabbedPane.addTab("Recent Projects", recentProjectsPanel);

        sampleProjectsPanel.setLayout(new java.awt.BorderLayout());

        samplListScrollPane.setViewportView(sampleList);

        sampleProjectsPanel.add(samplListScrollPane, java.awt.BorderLayout.CENTER);

        projectsTabbedPane.addTab("Sample Projects", sampleProjectsPanel);

        newProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/new.gif"))); // NOI18N
        newProjectButton.setText("Create new project");
        newProjectButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        newProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectButtonActionPerformed(evt);
            }
        });

        openProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/open.gif"))); // NOI18N
        openProjectButton.setText("Open project");
        openProjectButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        openProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProjectButtonActionPerformed(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

        configureAudioButton.setText("Configure audio...");
        configureAudioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureAudioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configureAudioButton)
                .addContainerGap(41, Short.MAX_VALUE))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(configureAudioButton, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        lafPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Look and Feel"));

        lafButtonGroup.add(defaultLafToggleButton);
        defaultLafToggleButton.setSelected(true);
        defaultLafToggleButton.setText("Default");
        defaultLafToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultLafToggleButtonActionPerformed(evt);
            }
        });

        lafButtonGroup.add(darculaLafToggleButton);
        darculaLafToggleButton.setText("Darcula");
        darculaLafToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                darculaLafToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout lafPanelLayout = new javax.swing.GroupLayout(lafPanel);
        lafPanel.setLayout(lafPanelLayout);
        lafPanelLayout.setHorizontalGroup(
            lafPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lafPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(defaultLafToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(darculaLafToggleButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lafPanelLayout.setVerticalGroup(
            lafPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lafPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(defaultLafToggleButton)
                .addComponent(darculaLafToggleButton))
        );

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(exitButton)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
                .addComponent(lafPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(optionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lafPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exitButton)
                .addContainerGap())
        );

        mainTitleTextPane.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(projectsTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(140, 140, 140)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(openProjectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(newProjectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mainTitleTextPane, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(animatedLogoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 6, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(animatedLogoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainTitleTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newProjectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openProjectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(projectsTabbedPane))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureAudioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureAudioButtonActionPerformed
        actionListener.configureAudio();
    }//GEN-LAST:event_configureAudioButtonActionPerformed

    private void defaultLafToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultLafToggleButtonActionPerformed
        switchLookAndFeel(SupportedLaf.DEFAULT);
        saveDefaultTheme();
    }//GEN-LAST:event_defaultLafToggleButtonActionPerformed

    private void darculaLafToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_darculaLafToggleButtonActionPerformed
        switchLookAndFeel(SupportedLaf.DARCULA);
        saveDefaultTheme();
    }//GEN-LAST:event_darculaLafToggleButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        actionListener.closeDialog();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void newProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectButtonActionPerformed
        actionListener.newProject();
    }//GEN-LAST:event_newProjectButtonActionPerformed

    private void openProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openProjectButtonActionPerformed
        actionListener.openProject();
    }//GEN-LAST:event_openProjectButtonActionPerformed

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(@Nonnull String args[]) {
        WindowUtils.invokeDialog(new WelcomePanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.frinika.main.panel.AnimatedLogoPanel animatedLogoPanel;
    private javax.swing.JButton configureAudioButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JToggleButton darculaLafToggleButton;
    private javax.swing.JToggleButton defaultLafToggleButton;
    private javax.swing.JButton exitButton;
    private javax.swing.ButtonGroup lafButtonGroup;
    private javax.swing.JPanel lafPanel;
    private javax.swing.JTextPane mainTitleTextPane;
    private javax.swing.JButton newProjectButton;
    private javax.swing.JButton openProjectButton;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JTabbedPane projectsTabbedPane;
    private javax.swing.JList<ProjectFileRecord> recentList;
    private javax.swing.JScrollPane recentListScrollPane;
    private javax.swing.JPanel recentProjectsPanel;
    private javax.swing.JScrollPane samplListScrollPane;
    private javax.swing.JList<ProjectFileRecord> sampleList;
    private javax.swing.JPanel sampleProjectsPanel;
    // End of variables declaration//GEN-END:variables

    private void switchLookAndFeel(@Nonnull SupportedLaf selectedLaf) {
        WindowUtils.switchLookAndFeel(selectedLaf);
        animatedLogoPanel.switchLookAndFeel();
        SwingUtilities.updateComponentTreeUI(this);

        HTMLDocument doc = (HTMLDocument) mainTitleTextPane.getDocument();
        if (!WindowUtils.isDarkMode()) {
            doc.getStyleSheet().addRule("body { color: #000000; }");
        } else {
            doc.getStyleSheet().addRule("body { color: #C0C0C0; }");
        }

        invalidate();
    }

    public void setActionListener(@Nonnull ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    private void saveDefaultTheme() {
        actionListener.saveDefaultTheme(defaultLafToggleButton.isSelected() ? null : SupportedLaf.DARCULA.name());
    }

    public static interface ActionListener {

        void newProject();

        void openProject();

        void configureAudio();

        void closeDialog();

        void openRecentProject(@Nonnull ProjectFileRecord projectFileRecord);

        void openSampleProject(@Nonnull ProjectFileRecord projectFileRecord);

        void saveDefaultTheme(@Nullable String theme);
    }

    public void setRecentProjects(@Nonnull List<ProjectFileRecord> projectRecord) {
        projectRecord.forEach((projectFileRecord) -> {
            recentListModel.addElement(projectFileRecord);
        });
    }

    public void setExampleProjects(@Nonnull List<ProjectFileRecord> projectRecord) {
        projectRecord.forEach((projectFileRecord) -> {
            sampleListModel.addElement(projectFileRecord);
        });
    }

    public void setInitialTheme(@Nullable String theme) {
        SupportedLaf themeLaf = theme == null ? SupportedLaf.DEFAULT : SupportedLaf.DARCULA;
        darculaLafToggleButton.setSelected(themeLaf == SupportedLaf.DARCULA);
    }
}
